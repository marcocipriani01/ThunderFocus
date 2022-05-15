#if defined(__arm__) && defined(__MKL26Z64__)

#include "../ServoHack.h"

#define LPTMR_CONFIG LPTMR_CSR_TIE | LPTMR_CSR_TFC | LPTMR_CSR_TEN
#define usToTicks(us) ((us)*8)
#define ticksToUs(ticks) ((ticks) / 8)

#if SERVOS_PER_TIMER <= 16
static uint16_t servo_active_mask = 0;
static uint16_t servo_allocated_mask = 0;
#else
static uint32_t servo_active_mask = 0;
static uint32_t servo_allocated_mask = 0;
#endif
static uint8_t servo_pin[MAX_SERVOS];
static uint16_t servo_ticks[MAX_SERVOS];

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
        if (!(SIM_SCGC5 & SIM_SCGC5_LPTIMER)) {
            SIM_SCGC5 |= SIM_SCGC5_LPTIMER;  // TODO: use BME
            OSC0_CR |= OSC_ERCLKEN;
            LPTMR0_CSR = 0;
            LPTMR0_PSR = LPTMR_PSR_PRESCALE(0) | LPTMR_PSR_PCS(3);  // 8 MHz
            LPTMR0_CMR = 1;
            LPTMR0_CSR = LPTMR_CONFIG;
            NVIC_SET_PRIORITY(IRQ_LPTMR, 32);
        }
        NVIC_ENABLE_IRQ(IRQ_LPTMR);
    }
    return servoIndex;
}

void ServoHack::detach() {
    if (servoIndex >= MAX_SERVOS) return;
    servo_active_mask &= ~(1 << servoIndex);
    servo_allocated_mask &= ~(1 << servoIndex);
    if (servo_active_mask == 0) {
        NVIC_DISABLE_IRQ(IRQ_LPTMR);
    }
}

void ServoHack::write(int value) {
    if (servoIndex >= MAX_SERVOS) return;
    value = usToTicks(value);
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

void lptmr_isr(void) {
    static int8_t channel = 0, channel_high = MAX_SERVOS;
    static uint32_t tick_accum = 0;
    uint32_t ticks;
    int32_t wait_ticks;

    // first, if any channel was left high from the previous
    // run, now is the time to shut it off
    if (servo_active_mask & (1 << channel_high)) {
        digitalWrite(servo_pin[channel_high], LOW);
        channel_high = MAX_SERVOS;
    }
    // search for the next channel to turn on
    while (channel < MAX_SERVOS) {
        if (servo_active_mask & (1 << channel)) {
            digitalWrite(servo_pin[channel], HIGH);
            channel_high = channel;
            ticks = servo_ticks[channel];
            tick_accum += ticks;
            LPTMR0_CMR += ticks;
            LPTMR0_CSR = LPTMR_CONFIG | LPTMR_CSR_TCF;
            channel++;
            return;
        }
        channel++;
    }
    // when all channels have output, wait for the
    // minimum refresh interval
    wait_ticks = usToTicks(REFRESH_INTERVAL) - tick_accum;
    if (wait_ticks < usToTicks(100))
        wait_ticks = usToTicks(100);
    else if (wait_ticks > 60000)
        wait_ticks = 60000;
    tick_accum += wait_ticks;
    LPTMR0_CMR += wait_ticks;
    LPTMR0_CSR = LPTMR_CONFIG | LPTMR_CSR_TCF;
    // if this wait is enough to satisfy the refresh
    // interval, next time begin again at channel zero
    if (tick_accum >= usToTicks(REFRESH_INTERVAL)) {
        tick_accum = 0;
        channel = 0;
    }
}

#endif