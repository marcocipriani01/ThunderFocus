#include "../definitions.h"
// ===================================================================================
// ============================== GENERAL CONFIGURATION ==============================
// ===================================================================================
#define STATUS_LED 13

// ===================================================================================
// ============================== FOCUSER CONFIGURATION ==============================
// ===================================================================================
#define FOCUSER_DRIVER BIPOLAR
#define FOCUSER_DIR 2
#define FOCUSER_STEP 3
#define FOCUSER_EN 7
#define FOCUSER_STEPS_SCALING 4
//#define FOCUSER_MODE0 6
//#define FOCUSER_MODE1 5
#define FOCUSER_MODE2 4
#define FOCUSER_ACCEL 1000.0
#define FOCUSER_PPS_MIN 100.0
#define FOCUSER_PPS_MAX 15000.0
#define FOCUSER_POWER_TIMEOUT 60000

// ===================================================================================
// ============================== DEVICE MANAGER CONFIG ==============================
// ===================================================================================
#define ENABLE_DEVMAN true
#if ENABLE_DEVMAN == true
#define MANAGED_PINS_COUNT 4
#define MANAGED_PINS {{ 6,  0,  true,   false,  false}, \
                    {   9,  0,  true,   false,  false}, \
                    {   10, 0,  true,   false,  false}, \
                    {   12, 0,  false,  false,  false}}
#define STATUS_LED_MANAGED true

// ---------- Ambient sensors ----------
#define TEMP_HUM_SENSOR BME280

// ---------- Time ----------
#define RTC_SUPPORT SERIAL_TIME

// ---------- Polar finder illuminator ----------
#define ENABLE_PFI false
#if ENABLE_PFI == true
#define PFI_KNOB A7
#define PFI_LED 5
#define PFI_THRESHOLD 20
#endif
#endif
