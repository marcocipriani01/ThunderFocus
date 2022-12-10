#include "../definitions.h"
// ===================================================================================
// ============================== GENERAL CONFIGURATION ==============================
// ===================================================================================
#define STATUS_LED LED_BUILTIN

// ===================================================================================
// ============================== FOCUSER CONFIGURATION ==============================
// ===================================================================================
#define FOCUSER_DRIVER BIPOLAR
#define FOCUSER_DIR 15
#define FOCUSER_STEP 16
#define FOCUSER_EN 23
#define FOCUSER_MODE0 22
#define FOCUSER_MODE1 21
#define FOCUSER_MODE2 20
#define FOCUSER_ACCEL 1600.0
#define FOCUSER_PPS_MIN 100.0
#define FOCUSER_PPS_MAX 60000
#define FOCUSER_POWER_TIMEOUT 30000

// ===================================================================================
// ============================== ROTATOR CONFIGURATION ==============================
// ===================================================================================
// TODO: implement the rotator driver
#define ROTATOR_DRIVER BIPOLAR
#define ROTATOR_DIR 0
#define ROTATOR_STEP 11
#define ROTATOR_EN 12
//#define ROTATOR_MODE0 22
//#define ROTATOR_MODE1 21
//#define ROTATOR_MODE2 20
#define ROTATOR_ACCEL 1600.0
#define ROTATOR_PPS_MIN 100.0
#define ROTATOR_PPS_MAX 60000
#define ROTATOR_POWER_TIMEOUT 30000

// ===================================================================================
// ============================== DEVICE MANAGER CONFIG ==============================
// ===================================================================================
#define ENABLE_DEVMAN true
#if ENABLE_DEVMAN == true
#define MANAGED_PINS_COUNT 8
#define MANAGED_PINS {{ 1,  0,  false,   false,  false}, \
                    {   2,  0,  false,   false,  false}, \
                    {   3,  0,  true,    false,  false}, \
                    {   4,  0,  true,    false,  false}, \
                    {   5,  0,  false,   false,  false}, \
                    {   6,  0,  true,    false,  false}, \
                    {   7,  0,  false,   false,  false}, \
                    {  17,  0,  false,   false,  false}}
#define STATUS_LED_MANAGED false

// ---------- Ambient sensors ----------
#define TEMP_HUM_SENSOR HTU21D

// ---------- Time ----------
#define RTC_SUPPORT TEENSY_RTC
#endif

// ===================================================================================
// ================================ FLAT PANEL CONFIG ================================
// ===================================================================================
#define FLAT_PANEL true
#if FLAT_PANEL == true
#define EL_PANEL_PIN 10
#define EL_PANEL_LOG_SCALE true
#define EL_PANEL_ON_BOOT false
#define SERVO_MOTOR SERVO_RDS3225
#if defined(SERVO_MOTOR) && SERVO_MOTOR != OFF
#define SERVO_PIN 14
#define SERVO_POWER_PIN 17 // TODO: implement this feature
#endif
#endif
