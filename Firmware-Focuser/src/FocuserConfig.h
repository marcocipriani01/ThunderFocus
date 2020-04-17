#ifndef FOCUSER_CONFIG_H
#define FOCUSER_CONFIG_H

#define ENOUGH_MICROS 100

// Status LED pin
#define LED1 13
//#define LED2 17
#define BLINK_PERIOD 300

// ----- Stepper driver configuration -----
// Microstepping pin mapping: none (0), DRV8825 (1), A4988 (2), ULN2003 (3)
#define STEPPER_TYPE 3

#if STEPPER_TYPE == 3
// Driver pins
#define DRIVER_IN1 11
#define DRIVER_IN2 10
#define DRIVER_IN3 9
#define DRIVER_IN4 8
// Speeds & steps
// Step unit. Used to scale up all the movements or reverse all directions.
#define SINGLE_STEP 1
#define STEPS_REV 64 // Actual 4076
#define MOTOR_ACCEL 20
#define MOTOR_RPM_MAX 130
#define MOTOR_RPM_MIN 40
// The time (in milliseconds) to wait before turning off the motor
// if no movements are being done and the hold control is enabled
#define DRIVER_POWER_TIMEOUT 2000
#define DEFAULT_ENABLE_HOLD_CONTROL true
#else
// Driver pins
#define DRIVER_STEP 3
#define DRIVER_DIR 2
// Speeds & steps
// Step unit. Used to scale up all the movements or reverse all directions.
#define SINGLE_STEP 1
#define STEPS_REV 200
#define MOTOR_ACCEL 10
#define MOTOR_RPM_MAX 700
#define MOTOR_RPM_MIN 150
// Power control
//#define DRIVER_EN 7
// The time (in milliseconds) to wait before turning off the motor
// if no movements are being done and the hold control is enabled
#define DRIVER_POWER_TIMEOUT 30000
#define DEFAULT_ENABLE_HOLD_CONTROL false
#endif

#endif
