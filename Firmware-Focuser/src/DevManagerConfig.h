#ifndef DEVMAN_CONFIG_H
#define DEVMAN_CONFIG_H

// ----- General config -----
// Status LED pin
#define DEVMAN_LED 9

// Customizable pins
int customPins[] = {3, 5, 6, 7};
#define CUSTOM_PINS_COUNT 4

// Polar finder illuminator
#define ENABLE_PFI true
#if ENABLE_PFI == true
#define PFI_POT A7
#define PFI_LED 10
#define PFI_THRESHOLD 20
#endif

#endif
