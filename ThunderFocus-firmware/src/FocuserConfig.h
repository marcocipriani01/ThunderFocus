#ifndef FOCUSER_CONFIG_H
#define FOCUSER_CONFIG_H

// Status LED pin
#define FOK_LED1 7
//#define FOK_LED2 17
#define FOK_LED_BLINK_PERIOD 300

// ----- Stepper driver configuration -----
// Microstepping pin mapping: none (0), DRV8825 (1), A4988 (2), ULN2003 (3)
#define STEPPER_TYPE 1

#if STEPPER_TYPE == 3
// Driver pins
#define DRIVER_IN1 5
#define DRIVER_IN2 4
#define DRIVER_IN3 3
#define DRIVER_IN4 2
// Speeds & steps
#define MOTOR_ACCEL 80
#define MOTOR_PPS_MAX 100
#define MOTOR_PPS_MIN 40
// The time (in milliseconds) to wait before turning off the motor
// if no movements are being done and the hold control is enabled
#define DRIVER_POWER_TIMEOUT 2000
#define DEFAULT_ENABLE_HOLD_CONTROL true
#else
// Driver pins
#define DRIVER_DIR 2
#define DRIVER_STEP 3
//#define DRIVER_EN 7
//#define MODE0 6
//#define MODE1 5
#define MODE2 4
// Speeds & steps
#define DEFAULT_DIRECTION_INVERT true
#define MICROSTEPPING 32
#define MOTOR_ACCEL 50 * MICROSTEPPING
#define MOTOR_PPS_MAX 400 * MICROSTEPPING
#define MOTOR_PPS_MIN 2 * MICROSTEPPING
// The time (in milliseconds) to wait before turning off the motor
// if no movements are being done and the hold control is enabled
#define DRIVER_POWER_TIMEOUT 30000
#define DEFAULT_ENABLE_HOLD_CONTROL false
#endif

#endif
