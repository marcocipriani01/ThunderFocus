#ifndef DEVMAN_H
#define DEVMAN_H

#include <Arduino.h>

#include "../config.h"

#if ENABLE_DEVMAN == true
#include "DevManagerDefinitions.h"
#if SETTINGS_SUPPORT == false
#error "SETTINGS_SUPPORT must be true to use the device manager."
#endif
#include "settings/Settings.h"
#if TEMP_HUM_SENSOR != OFF
#include "AmbientManager.h"
#endif
#if RTC_SUPPORT != OFF
#include <math.h>
#include "SunUtil.h"
#endif

#define DEVMAN_UPDATE_INTERVAL 30000L
#define DEVMAN_PWM_THRESHOLD 60.0
#define DEVMAN_OFFSET_FACTOR 0.4

namespace DevManager {
extern Pin pins[];
#if DEVMAN_HAS_AUTO_MODES
extern AutoMode autoMode;
extern unsigned long lastUpdateTime;
#endif

void begin();
boolean run();
void updateSettings();
Pin getPin(uint8_t index);
void updatePin(uint8_t pin, uint8_t value);

#if DEVMAN_HAS_AUTO_MODES
AutoMode getAutoMode();
boolean updateAutoMode();
boolean setAutoMode(AutoMode am);
boolean processAutoMode(boolean force);
int pwmMap(double in, double min, double max);
boolean forEachAutoPin(int pwm, boolean digital);
boolean setPinAutoModeEn(uint8_t pin, boolean enabled);
boolean setPinPwmEn(uint8_t pin, boolean pwmEn);
#endif
#if TEMP_HUM_SENSOR != OFF
boolean processDewPoint(double triggerDiff);
boolean processHumidity(double triggerHum);
#endif
}  // namespace DevManager

#endif
#endif