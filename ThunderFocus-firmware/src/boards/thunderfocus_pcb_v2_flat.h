#include "../definitions.h"
// ===================================================================================
// ============================== GENERAL CONFIGURATION ==============================
// ===================================================================================
#define STATUS_LED 9

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
                    {   9,  0,  false,   false,  false}}
#define STATUS_LED_MANAGED true

// ---------- Ambient sensors ----------
#define TEMP_HUM_SENSOR HTU21D

// ---------- Time ----------
#define RTC_SUPPORT SERIAL_TIME
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
#define SERVO_PIN 17
#endif

#endif