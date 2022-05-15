#ifndef CONFIG_H
#define CONFIG_H

#include "boards/arduino_nano_prototype.h"
//#include "boards/guidescope.h"


// ===================================================================================
// =============================== GENERAL VALIDATION ================================
// ===================================================================================
#if defined(STATUS_LED) && (!defined(STATUS_LED_BLINK_PERIOD))
#error "STATUS_LED_BLINK_PERIOD must be defined if STATUS_LED is defined"
#endif

// ===================================================================================
// ============================== FOCUSER VALIDATION =================================
// ===================================================================================
#ifndef FOCUSER_DRIVER
#define FOCUSER_DRIVER DISABLED
#endif
#if FOCUSER_DRIVER != DISABLED
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
#if defined(STATUS_LED) && (!defined(STATUS_LED_MANAGED))
#error "STATUS_LED_MANAGED must be defined if STATUS_LED is defined"
#endif
#ifndef TEMP_HUM_SENSOR
#define TEMP_HUM_SENSOR DISABLED
#endif
#ifndef RTC_SUPPORT
#define RTC_SUPPORT DISABLED
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
#define RTC_SUPPORT DISABLED
#define TEMP_HUM_SENSOR DISABLED
#define STATUS_LED_MANAGED false
#endif

// ===================================================================================
// ============================== FLAT PANEL VALIDATION ==============================
// ===================================================================================
#if defined(FLAT_PANEL) && (FLAT_PANEL == true)
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
#define SERVO_MOTOR DISABLED
#endif
#if (SERVO_MOTOR != DISABLED) && (!defined(SERVO_PIN))
#error "SERVO_MOTOR and SERVO_PIN must be defined if FLAT_PANEL is true"
#endif
#endif

// ===================================================================================
// ============================== EEPROM CONFIGURATION ===============================
// ===================================================================================
#if defined(__AVR__) || defined(CORE_TEENSY)
#define SETTINGS_SUPPORT true
#define EEPROM_MARKER 1
//#define EEPROM_PADDING 20
#else
#define SETTINGS_SUPPORT false
#endif

#endif