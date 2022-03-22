// ===================================================================================
// ============================== GENERAL CONFIGURATION ==============================
// ===================================================================================
#define STATUS_LED 13
#define STATUS_LED_MANAGED false
#define STATUS_LED_BLINK_PERIOD 300
#define ENABLE_DEVMAN true

// ===================================================================================
// ============================== FOCUSER CONFIGURATION ==============================
// ===================================================================================
// 0 = DRV8825, A4988, etc.
// 1 = ULN2003
#define FOCUSER_DRIVER 0

#if FOCUSER_DRIVER == 0
#define FOCUSER_DIR 2
#define FOCUSER_STEP 3
#define FOCUSER_EN 7
#define FOCUSER_STPES_SCALING 4
//#define FOCUSER_MODE0 6
//#define FOCUSER_MODE1 5
#define FOCUSER_MODE2 4
#define FOCUSER_ACCEL 1000.0
#define FOCUSER_PPS_MIN 100.0
#define FOCUSER_PPS_MAX 15000.0
#define FOCUSER_POWER_TIMEOUT 60000
#else
#define FOCUSER_IN1 0
#define FOCUSER_IN2 0
#define FOCUSER_IN3 0
#define FOCUSER_IN4 0
#define FOCUSER_ACCEL 1000.0
#define FOCUSER_PPS_MIN 100.0
#define FOCUSER_PPS_MAX 15000.0
#define FOCUSER_POWER_TIMEOUT 60000
#endif

// ===================================================================================
// ============================== DEVICE MANAGER CONFIG ==============================
// ===================================================================================
#if ENABLE_DEVMAN == true
#define MANAGED_PINS_COUNT 4
#define MANAGED_PINS {{6, true, 0, false},	\
					{9, true, 0, false},    \
					{10, true, 0, false}, 	\
					{12, false, 0, false}}
#define AUTOMATIC_DEVMAN_TIMER 30000
#define AUTOMATIC_DEVMAN_THRESHOLD 60.0
#define AUTOMATIC_DEVMAN_OFFSET_FACTOR 0.4

// ---------- Ambient sensors ----------
#define TEMP_HUM_SENSOR true
#define SENSORS_DELAY 10000
#define SENSORS_DATAPOINTS 6

// ---------- Time ----------
#define TIME_CONTROL false

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
#define EEPROM_MARKER 'A'
#define EEPROM_PADDING 20
#define SETTINGS_SAVE_DELAY 5000
#else
#define SETTINGS_SUPPORT false
#endif
