// ----- General config -----
// Status LED pin
#define LED 13
#define SERIAL_SPEED 9600
// Standard (0), Plus prototype (1), Plus (2)
#define EDITION 2

#if EDITION == 0 // Standard


// ----- Stepper driver configuration -----
// Motor driver type: BasicStepperDriver (0), DRV8825 (1), A4988 (2) or DRV8834 (3)
#define STEPPER_TYPE 1
#define DRIVER_DIR 14
#define DRIVER_STEP 15
#if STEPPER_TYPE != 0
#define MODE0 18
#define MODE1 17
#if STEPPER_TYPE != 2
#define MODE2 16
#endif
#endif
// Steps per revolution of the motor
#define STEPS_REV 200
// Inverts the direction of the motor
#define REVERSE_DIR false
// RPM. Decreasing this value should decrease telescope shaking.
#define MOTOR_RPM 120
// Acceleration
#define MOTOR_ACCEL 1300

// ----- Hand controller -----
#define ENABLE_HC true
#define HC_SPEED_POT A7
#define BUTTON_UP 2
#define BUTTON_DOWN 3

// ----- Customizable pins -----
#define ENABLE_PIN_CONTROL false
// ----- Polar finder illuminator -----
#define ENABLE_PFI false


#elif EDITION == 1 // Plus prototype


// ----- Stepper driver configuration -----
// Motor driver type: BasicStepperDriver (0), DRV8825 (1), A4988 (2) or DRV8834 (3)
#define STEPPER_TYPE 1
#define DRIVER_DIR 2
#define DRIVER_STEP 3
#if STEPPER_TYPE != 0
#define MODE0 7
#define MODE1 8
#if STEPPER_TYPE != 2
#define MODE2 11
#endif
#endif
// Steps per revolution of the motor
#define STEPS_REV 200
// Inverts the direction of the motor
#define REVERSE_DIR false
// RPM. Decreasing this value should decrease telescope shaking.
#define MOTOR_RPM 120
// Acceleration
#define MOTOR_ACCEL 1300

// ----- Customizable pins -----
// List of pins that can be modified by the serial port
#define ENABLE_PIN_CONTROL true
#define CUSTOMIZABLE_PINS {5, 6, 9}
// ----- Polar finder illuminator -----
#define ENABLE_PFI true
#define PFI_POT A0
#define PFI_LED 10

// ----- Hand controller -----
#define ENABLE_HC false


#elif EDITION == 2 // Plus


// ----- Stepper driver configuration -----
// Motor driver type: BasicStepperDriver (0), DRV8825 (1), A4988 (2) or DRV8834 (3)
#define STEPPER_TYPE 1
#define DRIVER_DIR 14
#define DRIVER_STEP 15
#if STEPPER_TYPE != 0
#define MODE0 18
#define MODE1 17
#if STEPPER_TYPE != 2
#define MODE2 16
#endif
#endif
// Steps per revolution of the motor
#define STEPS_REV 200
// Inverts the direction of the motor
#define REVERSE_DIR false
// RPM. Decreasing this value should decrease telescope shaking.
#define MOTOR_RPM 120
// Acceleration
#define MOTOR_ACCEL 1300

// ----- Customizable pins -----
// List of pins that can be modified by the serial port
#define ENABLE_PIN_CONTROL true
#define CUSTOMIZABLE_PINS {3, 5, 6}
// ----- Polar finder illuminator -----
#define ENABLE_PFI true
#define PFI_POT A7
#define PFI_LED 11

// ----- Hand controller -----
#define ENABLE_HC false
#endif
