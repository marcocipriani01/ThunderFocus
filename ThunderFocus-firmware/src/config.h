#ifndef CONFIG_H
#define CONFIG_H

#define DEBUG_EN false

//#include "boards/thunderfocus_pcb_v1.h"
//#include "boards/thunderfocus_pcb_v2.h"
//#include "boards/arduino_nano_prototype.h"
//#include "boards/my_guidescope.h"
//#include "boards/nik_solar_scope_fok.h"


// ===================================================================================
// ============================== FOCUSER VALIDATION =================================
// ===================================================================================
#ifndef FOCUSER_DRIVER
#define FOCUSER_DRIVER OFF
#endif
#if FOCUSER_DRIVER != OFF
#ifndef FOCUSER_ACCEL
#error "FOCUSER_ACCEL must be defined"
#endif
#ifndef FOCUSER_PPS_MIN
#error "FOCUSER_PPS_MIN must be defined"
#endif
#ifndef FOCUSER_PPS_MAX
#error "FOCUSER_PPS_MAX must be defined"
#endif
#if defined(FOCUSER_EN) && (!defined(FOCUSER_POWER_TIMEOUT))
#error "FOCUSER_POWER_TIMEOUT must be defined if FOCUSER_EN is defined"
#endif
#if FOCUSER_DRIVER == BIPOLAR
#ifndef FOCUSER_DIR
#error "FOCUSER_DIR must be defined"
#endif
#ifndef FOCUSER_STEP
#error "FOCUSER_STEP must be defined"
#endif
#elif FOCUSER_DRIVER == UNIPOLAR
#ifndef FOCUSER_IN1
#error "FOCUSER_IN1 must be defined"
#endif
#ifndef FOCUSER_IN2
#error "FOCUSER_IN2 must be defined"
#endif
#ifndef FOCUSER_IN3
#error "FOCUSER_IN3 must be defined"
#endif
#ifndef FOCUSER_IN4
#error "FOCUSER_IN4 must be defined"
#endif
#else
#error "FOCUSER_DRIVER must be defined as either BIPOLAR or UNIPOLAR"
#endif
#endif

// ===================================================================================
// =========================== DEVICE MANAGER VALIDATION =============================
// ===================================================================================
#ifndef ENABLE_DEVMAN
#define ENABLE_DEVMAN false
#endif
#if ENABLE_DEVMAN == true
#if MANAGED_PINS_COUNT <= 0
#error "MANAGED_PINS_COUNT must be greater than 0"
#endif
#if defined(STATUS_LED) && (!defined(STATUS_LED_MANAGED))
#error "STATUS_LED_MANAGED must be defined if STATUS_LED is defined"
#endif
#ifndef TEMP_HUM_SENSOR
#define TEMP_HUM_SENSOR OFF
#endif
#ifndef RTC_SUPPORT
#define RTC_SUPPORT OFF
#endif
#if (RTC_SUPPORT != OFF) || (TEMP_HUM_SENSOR != OFF)
#define DEVMAN_HAS_AUTO_MODES true
#else
#define DEVMAN_HAS_AUTO_MODES false
#endif
#if RTC_SUPPORT == TEENSY_RTC
#ifndef CORE_TEENSY
#error "TEENSY_RTC can't be used without a Teensy board!"
#endif
#if !(defined(__MK20DX128__) || defined(__MK20DX256__) || defined(__MK64FX512__) || defined(__MK66FX1M0__) || defined(__IMXRT1052__) || defined(__IMXRT1062__))
#error "The selected Teensy board doesn't have a built-in RTC!"
#endif
#endif
#else
#define ENABLE_PFI false
#define RTC_SUPPORT OFF
#define TEMP_HUM_SENSOR OFF
#define STATUS_LED_MANAGED false
#endif

// ===================================================================================
// ============================== FLAT PANEL VALIDATION ==============================
// ===================================================================================
#ifndef FLAT_PANEL
#define FLAT_PANEL false
#endif
#if FLAT_PANEL == true
#ifndef EL_PANEL_PIN
#error "EL_PANEL_PIN must be defined"
#endif
#ifndef LOG_SCALE_ENABLE
#define LOG_SCALE_ENABLE false
#endif
#ifndef EL_PANEL_ON_BOOT
#define EL_PANEL_ON_BOOT false
#endif
#ifndef SERVO_MOTOR
#define SERVO_MOTOR OFF
#endif
#if (SERVO_MOTOR != OFF) && (!defined(SERVO_PIN))
#error "SERVO_PIN must be defined if SERVO_MOTOR is defined"
#endif
#endif

// ===================================================================================
// ============================== EEPROM CONFIGURATION ===============================
// ===================================================================================
#if defined(__AVR__) || defined(CORE_TEENSY) || defined(ESP32)
#define SETTINGS_SUPPORT true
#define EEPROM_MARKER (__TIME__[4] + __TIME__[6] + __TIME__[7] - (3 * '0'))
#define EEPROM_START 0
#else
#define SETTINGS_SUPPORT false
#endif

// ===================================================================================
// =============================== GENERAL VALIDATION ================================
// ===================================================================================
#if (FOCUSER_DRIVER == OFF) && (ENABLE_DEVMAN == false) && (FLAT_PANEL == false)
#error "At least one of FOCUSER_DRIVER, ENABLE_DEVMAN, or FLAT_PANEL must be defined"
#endif

#endif