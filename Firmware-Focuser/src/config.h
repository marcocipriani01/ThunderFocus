// ----- General config -----
// Status LED pin
#define LED 8
#define BLINK_PERIOD 300
#define SERIAL_SPEED 9600
#define BOARD_TYPE 2

// ----- Stepper driver configuration -----
// Motor driver type: BasicStepperDriver (0), DRV8825 (1), A4988 (2) or DRV8834 (3)
#define STEPPER_TYPE 1
//#define DRIVER_EN 7
#define DRIVER_DIR 2
#define DRIVER_STEP 3

#define STEPS_REV 200
#define REVERSE_DIR false
#define MOTOR_RPM 120
#define MOTOR_ACCEL 100

#if STEPPER_TYPE != 0
#define MODE0 6
#define MODE1 5
#if STEPPER_TYPE != 2
#define MODE2 4
#endif
#endif

// ----- Hand controller -----
#define ENABLE_HC false
//#define HC_SPEED_POT
//#define BUTTON_UP
//#define BUTTON_DOWN
