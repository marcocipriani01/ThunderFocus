#ifndef CONFIG_H
#define CONFIG_H

// General config
// 1 to flag this is a OpenFocuser's focuser module, 2 to flag this is a device controller, or 2 for both.
#define BOARD_TYPE 1
#define SERIAL_TIMEOUT 100

// 1 = MoonLite full protocol, 2 = EasyFocuser light protocol
#define PROTOCOL 2
#if PROTOCOL == 1
#define SERIAL_SPEED 9600
// Focuser motor
#define ENABLE_FOCUSER true
// Device manager
#define ENABLE_DEVMAN false
// Hand controller
#define ENABLE_HC true
// Temperature compensation support
#define ENABLE_TEMP_COMP false
#elif PROTOCOL == 2
#define SERIAL_SPEED 115200
// Configuration for the EasyFocuser protocol. Cannot be changed!
#define ENABLE_FOCUSER true
#define ENABLE_DEVMAN false
#define ENABLE_HC false
#define ENABLE_TEMP_COMP false
#endif

// Settings support for recovering the last active session
// Requires EEPROM, not available on ARM microprocessors.
#if ENABLE_FOCUSER == true
#if defined(__AVR__)
#define SETTINGS_SUPPORT true
#endif
#endif

#endif
