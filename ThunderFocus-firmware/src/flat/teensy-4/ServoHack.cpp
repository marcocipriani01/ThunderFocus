#if defined(__arm__) && (defined(__IMXRT1052__) || defined(__IMXRT1062__))

#include "../ServoHack.h"

#define usToTicks(us) ((us)*16)
#define ticksToUs(ticks) ((ticks) / 16)
#define ticksToUs_f(t) ((float)(t)*0.0625f)

static uint32_t servo_active_mask = 0;
static uint32_t servo_allocated_mask = 0;
static uint8_t servo_pin[MAX_SERVOS];
static uint16_t servo_ticks[MAX_SERVOS];

static IntervalTimer timer;
static void isr(void);

ServoHack::ServoHack() {
    uint16_t mask;

    servoIndex = 0;
    for (mask = 1; mask < (1 << MAX_SERVOS); mask <<= 1) {
        if (!(servo_allocated_mask & mask)) {
            servo_allocated_mask |= mask;
            servo_active_mask &= ~mask;
            return;
        }
        servoIndex++;
    }
    servoIndex = INVALID_SERVO;
}

uint8_t ServoHack::attach(int pin) {
    if (servoIndex < MAX_SERVOS) {
        pinMode(pin, OUTPUT);
        servo_pin[servoIndex] = pin;
        servo_ticks[servoIndex] = usToTicks(DEFAULT_PULSE_WIDTH);
        servo_active_mask |= (1 << servoIndex);
        if ((IRQ_NUMBER_t)timer >= NVIC_NUM_INTERRUPTS) timer.begin(isr, 10);
    }
    return servoIndex;
}

void ServoHack::detach() {
    if (servoIndex >= MAX_SERVOS) return;
    servo_active_mask &= ~(1 << servoIndex);
    servo_allocated_mask &= ~(1 << servoIndex);
    if (servo_active_mask == 0) timer.end();
}

void ServoHack::write(int value) {
    value = usToTicks(value);
    if (servoIndex >= MAX_SERVOS) return;
    servo_ticks[servoIndex] = value;
}

int ServoHack::read() {
    if (servoIndex >= MAX_SERVOS) return 0;
    return ticksToUs(servo_ticks[servoIndex]);
}

bool ServoHack::attached() {
    if (servoIndex >= MAX_SERVOS) return 0;
    return servo_active_mask & (1 << servoIndex);
}

static void isr(void) {
    static uint8_t channel = MAX_SERVOS;
    static uint8_t next_low = 255;
    static uint32_t tick_accum = 0;

    // If a pin is still HIGH from a prior run, turn it off
    if (next_low < 255) {
        digitalWrite(next_low, LOW);
    }

    // If we're on an active channel, drive it HIGH
    if (channel < MAX_SERVOS && (servo_active_mask & (1 << channel))) {
        uint8_t pin = servo_pin[channel];
        digitalWrite(pin, HIGH);
        next_low = pin;
    } else {
        next_low = 255;
    }

    // Generate an oscilloscope trigger pulse at beginning
    // if (channel == __builtin_ctz(servo_active_mask)) {
    // digitalWrite(2, HIGH);
    // delayMicroseconds(1);
    // digitalWrite(2, LOW);
    //}

    // Find the next channel and set the timer up
    if (++channel >= MAX_SERVOS) {
        channel = 0;
    }
    do {
        if (servo_active_mask & (1 << channel)) {
            uint32_t ticks = servo_ticks[channel];
            tick_accum += ticks;
            timer.update(ticksToUs_f(ticks));
            return;
        }
        channel++;
    } while (channel < MAX_SERVOS);

    // when all channels have output, wait for the refresh interval
    if (tick_accum < usToTicks(REFRESH_INTERVAL)) {
        timer.update(ticksToUs_f(usToTicks(REFRESH_INTERVAL) - tick_accum));
    } else {
        timer.update(ticksToUs_f(100));
    }
    tick_accum = 0;
}

#endif