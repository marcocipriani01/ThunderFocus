#ifndef CONFIG_H
#define CONFIG_H

// General config
// 1 to flag this is a OpenFocuser's focuser module, 2 to flag this is a device controller, or 3 for both.
#define BOARD_TYPE 1
#define SERIAL_TIMEOUT 100

#define ENABLE_FOCUSER true

// 1 = MoonLite full protocol, 2 = EasyFocuser protocol
#define PROTOCOL 2

#if PROTOCOL == 1
#define SERIAL_SPEED 9600
#define ENABLE_DEVMAN false
#define ENABLE_HC false
#elif PROTOCOL == 2
#define SERIAL_SPEED 115200
#define ENABLE_DEVMAN true
#define ENABLE_HC false
#endif

// Settings support for recovering the last active session
// Requires EEPROM, not available on ARM microprocessors.
#if defined(__AVR__) || defined(CORE_TEENSY)
#define SETTINGS_SUPPORT true
#else
#define SETTINGS_SUPPORT false
#endif

#endif
