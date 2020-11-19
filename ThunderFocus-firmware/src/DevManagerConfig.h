#ifndef DEVMAN_CONFIG_H
#define DEVMAN_CONFIG_H

// Status LED pin
#define DEVMAN_LED 8

// Customizable pins
#define DEVMAN_PWM {6, 9, 10}
#define DEVMAN_PWM_COUNT 3
#define DEVMAN_DIO {12}
#define DEVMAN_DIO_COUNT 1

// Polar finder illuminator
#define ENABLE_PFI true
#if ENABLE_PFI == true
#define PFI_POT A7
#define PFI_LED 5
#define PFI_THRESHOLD 20
#endif

#endif
