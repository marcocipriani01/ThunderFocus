#ifndef SERVO_MODELS_H
#define SERVO_MODELS_H

#include "config.h"

#if defined(FLAT_PANEL) && (FLAT_PANEL == true) && defined(SERVO_MOTOR) && (SERVO_MOTOR != DISABLED)
#if SERVO_MOTOR == SERVO_RDS3225
// Raw servo value for +15°
#define CLOSED_SERVO_15deg 2400
// Raw servo value for -15°
#define CLOSED_SERVO_m15deg 2550
// Default value
#define CLOSED_SERVO_DEFAULT 2510

// Raw servo value for +290°
#define OPEN_SERVO_290deg 400
// Raw servo value for +170°
#define OPEN_SERVO_170deg 1300
// Default value
#define OPEN_SERVO_DEFAULT 550

// Delay for the maximum speed
#define SERVO_DELAY_MIN 1000
// Delay for the minimum speed
#define SERVO_DELAY_MAX 16300
// Default delay
#define SERVO_DELAY_DEFAULT 7000
#else
#error "Unknown servo motor model."
#endif
#endif

#endif