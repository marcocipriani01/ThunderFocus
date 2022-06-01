#include "../definitions.h"
// ===================================================================================
// ============================== GENERAL CONFIGURATION ==============================
// ===================================================================================
#define STATUS_LED 9

// ===================================================================================
// ============================== FOCUSER CONFIGURATION ==============================
// ===================================================================================
#define FOCUSER_DRIVER BIPOLAR
#define FOCUSER_DIR 14
#define FOCUSER_STEP 15
#define FOCUSER_EN 19
#define FOCUSER_MODE0 18
//#define FOCUSER_MODE1 17
//#define FOCUSER_MODE2 16
#define FOCUSER_ACCEL 1600.0
#define FOCUSER_PPS_MIN 100.0
#define FOCUSER_PPS_MAX 60000
#define FOCUSER_POWER_TIMEOUT 30000

// Unipolar stepper
//#define FOCUSER_IN1 21
//#define FOCUSER_IN2 22
//#define FOCUSER_IN3 23
//#define FOCUSER_IN4 20
//#define FOCUSER_ACCEL 80
//#define FOCUSER_PPS_MIN 40
//#define FOCUSER_PPS_MAX 700

// ===================================================================================
// ============================== DEVICE MANAGER CONFIG ==============================
// ===================================================================================
#define ENABLE_DEVMAN true
#if ENABLE_DEVMAN == true
#define MANAGED_PINS_COUNT 6
#define MANAGED_PINS {{ 0,  0,  true,   true},  \
                    {   1,  0,  true,   true},  \
                    {   2,  0,  true,  false},  \
                    {   3,  0,  false,  false}, \
                    {   9,  0,  false,  false}, \
                    {   10, 0,  false,  false}}
#define STATUS_LED_MANAGED true

// ---------- Ambient sensors ----------
#define TEMP_HUM_SENSOR BME280
#define I2C_SDA_PIN 17
#define I2C_SCL_PIN 16

// ---------- Time ----------
#define RTC_SUPPORT TEENSY_RTC
#endif
