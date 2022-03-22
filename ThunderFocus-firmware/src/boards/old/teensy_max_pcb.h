#include "../definions.h"

// ===================================================================================
// ============================== GENERAL CONFIGURATION ==============================
// ===================================================================================
#define STATUS_LED 9
#define STATUS_LED_MANAGED true
#define STATUS_LED_BLINK_PERIOD 300
#define ENABLE_DEVMAN true

// ===================================================================================
// ============================== FOCUSER CONFIGURATION ==============================
// ===================================================================================
#define FOK1_STEPPER DRIVER_POLOLU

#if FOK1_STEPPER == DRIVER_POLOLU
#define FOK1_DIR 14
#define FOK1_STEP 15
#define FOK1_EN 19
#define FOK1_uSTEPS 32
#define FOK1_MODE0 18
#define FOK1_MODE1 17
#define FOK1_MODE2 16
#define FOK1_DIR_INVERT false
#define FOK1_ACCEL 50 * FOK1_uSTEPS
#define FOK1_PPS_MIN 2 * FOK1_uSTEPS
#define FOK1_PPS_MAX 400 * FOK1_uSTEPS
#define FOK1_HOLD_CONTROL true
#define FOK1_POWER_TIMEOUT 30000
#elif FOK1_STEPPER == DRIVER_ULN2003
#define FOK1_IN1 21
#define FOK1_IN2 22
#define FOK1_IN3 23
#define FOK1_IN4 20
#define FOK1_ACCEL 80
#define FOK1_PPS_MIN 40
#define FOK1_PPS_MAX 700
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
#define MANAGED_PINS_COUNT 5
#define MANAGED_PINS {{0, true, 0, false},  \
					{1, true, 0, false},    \
					{2, false, 0, false},	\
					{9, false, 0, false},	\
					{10, false, 0, false}}
#define AUTOMATIC_DEVMAN_TIMER 30000
#define AUTOMATIC_DEVMAN_THRESHOLD 60.0
#define AUTOMATIC_DEVMAN_OFFSET_FACTOR 0.4

// ---------- Ambient sensors ----------
#define TEMP_HUM_SENSOR true
#define SENSORS_DELAY 10000
#define SENSORS_DATAPOINTS 6

// ---------- Time ----------
#define TIME_CONTROL true

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
#define SETTINGS_SAVE_DELAY 5000
#else
#define SETTINGS_SUPPORT false
#endif