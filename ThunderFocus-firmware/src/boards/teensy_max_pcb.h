#include "../definions.h"

// ===================================================================================
// ============================== GENERAL CONFIGURATION ==============================
// ===================================================================================
#define SERIAL_TIMEOUT 100
#define STATUS_LED 9
#define STATUS_LED_BLINK_PERIOD 300
#define PROTOCOL PROTOCOL_THUNDERFOCUS

#if PROTOCOL == PROTOCOL_MOONLITE
#define SERIAL_SPEED 9600
#define ENABLE_DEVMAN false
#elif PROTOCOL == PROTOCOL_THUNDERFOCUS
#define SERIAL_SPEED 115200
#define THUNDERFOCUS_SEND_DELAY 150
#define ENABLE_DEVMAN true
#endif

// ===================================================================================
// ============================== FOCUSER CONFIGURATION ==============================
// ===================================================================================
#define FOK1_STEPPER DRIVER_ULN2003

#if FOK1_STEPPER == DRIVER_POLOLU
#define FOK1_DIR 2
#define FOK1_STEP 3
//#define FOK1_EN 7
#define FOK1_uSTEPS 32
//#define FOK1_MODE0 6
//#define FOK1_MODE1 5
#define FOK1_MODE2 4
#define FOK1_DIR_INVERT true
#define FOK1_ACCEL 50 * FOK1_uSTEPS
#define FOK1_PPS_MIN 2 * FOK1_uSTEPS
#define FOK1_PPS_MAX 400 * FOK1_uSTEPS
#define FOK1_HOLD_CONTROL false
#define FOK1_POWER_TIMEOUT 30000
#elif FOK1_STEPPER == DRIVER_ULN2003
#define FOK1_IN1 23
#define FOK1_IN2 21
#define FOK1_IN3 22
#define FOK1_IN4 20
#define FOK1_ACCEL 80
#define FOK1_PPS_MIN 40
#define FOK1_PPS_MAX 100
#define FOK1_HOLD_CONTROL true
#define FOK1_POWER_TIMEOUT 2000
#define FOK1_DIR_INVERT false
#define FOK1_uSTEPS 1
#endif

// ---------- Focuser 1 hand controller ----------
#define FOK1_ENABLE_HC false
#define FOK1_HC_KNOB A0
#define FOK1_HC_LEFT 9
#define FOK1_HC_RIGHT 10
#define FOK1_HC_MIN_SPEED_DELAY 80
#define FOK1_HC_MAX_SPEED_DELAY 20
#define FOK1_HC_MAX_STEPS 60 * FOK1_uSTEPS
#define FOK1_HC_PPS_MAX 500 * FOK1_uSTEPS
#define FOK1_HC_PPS_MIN 2 * FOK1_uSTEPS

// ===================================================================================
// ============================== DEVICE MANAGER CONFIG ==============================
// ===================================================================================
#if ENABLE_DEVMAN == true
#define MANAGED_PINS_COUNT 3
#define MANAGED_PINS {{0, true, 0, false},  \
					{1, true, 0, false},    \
					{2, true, 0, false}}
#define AUTOMATIC_DEVMAN_TIMER 30000
#define AUTOMATIC_DEVMAN_THRESHOLD 20

// ---------- Ambient sensors ----------
#define TEMP_HUM_SENSOR false
#define DHT22_PIN 99
#define SENSORS_DELAY 10000
#define SENSORS_DATAPOINTS 12

// ---------- Time ----------
#if defined(CORE_TEENSY)
#define TIME_CONTROL false
#else
#define TIME_CONTROL false
#endif
#define SUN_CHECK_DELAY 60000

// ---------- Polar finder illuminator ----------
#define ENABLE_PFI false
#if ENABLE_PFI == true
#define PFI_KNOB A7
#define PFI_LED 5
#define PFI_THRESHOLD 20
#endif
#else
#define ENABLE_PFI false
#define TEMP_HUM_SENSOR false
#define TIME_CONTROL false
#endif

// ===================================================================================
// ============================== EEPROM CONFIGURATION ===============================
// ===================================================================================
#if defined(__AVR__) || defined(CORE_TEENSY)
#define SETTINGS_SUPPORT true
#define EEPROM_VERSION 1
#else
#define SETTINGS_SUPPORT false
#endif