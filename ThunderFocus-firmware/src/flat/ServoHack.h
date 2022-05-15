#ifndef SERVO_HACK_H
#define SERVO_HACK_H

#include <Arduino.h>
#include <inttypes.h>

#if defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
#define _useTimer5
#define _useTimer1
#define _useTimer3
#define _useTimer4
typedef enum { _timer5, _timer1, _timer3, _timer4, _Nbr_16timers } timer16_Sequence_t;
#elif defined(__AVR_AT90USB646__) || defined(__AVR_AT90USB1286__)
#define _useTimer3
#define _useTimer1
typedef enum { _timer3, _timer1, _Nbr_16timers } timer16_Sequence_t;
#elif defined(__AVR_ATmega128__) || defined(__AVR_ATmega1281__) || defined(__AVR_ATmega2561__)
#define _useTimer3
#define _useTimer1
typedef enum { _timer3, _timer1, _Nbr_16timers } timer16_Sequence_t;
#else  // everything else
#define _useTimer1
typedef enum { _timer1, _Nbr_16timers } timer16_Sequence_t;
#endif

#define DEFAULT_PULSE_WIDTH 1500
#define REFRESH_INTERVAL 20000  // minumim time to refresh servos in microseconds

#define SERVOS_PER_TIMER 12  // the maximum number of servos controlled by one timer
#define MAX_SERVOS (_Nbr_16timers * SERVOS_PER_TIMER)

#define INVALID_SERVO 255  // flag indicating an invalid servo index

typedef struct {
    uint8_t nbr : 6;       // a pin number from 0 to 63
    uint8_t isActive : 1;  // true if this channel is enabled, pin not pulsed if false
} ServoPin_t;

typedef struct {
    ServoPin_t Pin;
    volatile unsigned int ticks;
} servo_t;

class ServoHack {
   public:
    ServoHack();
    uint8_t attach(int pin);
    void detach();
    void write(int value);  // Write pulse width in microseconds
    int read();             // Returns current pulse width in microseconds for this servo
    bool attached();        // return true if this servo is attached, otherwise false

   private:
    uint8_t servoIndex;     // index into the channel data for this servo
};

#endif