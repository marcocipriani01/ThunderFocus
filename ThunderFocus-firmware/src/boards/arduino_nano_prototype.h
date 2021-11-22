// ===================================================================================
// ============================== GENERAL CONFIGURATION ==============================
// ===================================================================================
#define STATUS_LED 7
#define STATUS_LED_MANAGED false
#define STATUS_LED_BLINK_PERIOD 300
#define ENABLE_DEVMAN true

// ===================================================================================
// ============================== FOCUSER CONFIGURATION ==============================
// ===================================================================================
#define FOCUSER_DIR 2
#define FOCUSER_STEP 3
//#define FOCUSER_EN 7
#define FOCUSER_STPES_SCALING 4
//#define FOCUSER_MODE0 6
//#define FOCUSER_MODE1 5
#define FOCUSER_MODE2 4
#define FOCUSER_ACCEL 600.0
#define FOCUSER_PPS_MIN 100.0
#define FOCUSER_PPS_MAX 9000.0
#define FOCUSER_POWER_TIMEOUT 60000

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
#define DHT22_PIN 14
#define SENSORS_DELAY 10000
#define SENSORS_DATAPOINTS 6

// ---------- Time ----------
#define TIME_CONTROL false

// ---------- Polar finder illuminator ----------
#define ENABLE_PFI true
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
#define EEPROM_MARKER 1
#define EEPROM_PADDING 20
#define SETTINGS_SAVE_DELAY 5000
#else
#define SETTINGS_SUPPORT false
#endif
