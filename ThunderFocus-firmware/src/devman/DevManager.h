#ifndef DEVMAN_H
#define DEVMAN_H

#include <Arduino.h>
#include "../config.h"

#if ENABLE_DEVMAN == true
#include "DevManagerDefinitions.h"
#if TEMP_HUM_SENSOR != DISABLED
#include "AmbientManager.h"
#endif
#if RTC_SUPPORT != DISABLED
#include "SunUtil.h"
#endif

#define DEVMAN_UPDATE_INTERVAL 30000L
#define DEVMAN_PWM_THRESHOLD 60.0
#define DEVMAN_OFFSET_FACTOR 0.4

namespace DevManager {
extern Pin pins[];
#if (RTC_SUPPORT != DISABLED) || (TEMP_HUM_SENSOR != DISABLED)
extern AutoMode autoMode;
#endif
extern unsigned long lastUpdateTime;

void begin();
boolean run();
boolean processAutoMode(boolean force);
Pin getPin(uint8_t index);
void updatePin(uint8_t pin, uint8_t value);
boolean setPinAutoModeEn(uint8_t pin, boolean enabled);
AutoMode getAutoMode();
boolean setAutoMode(AutoMode am);
boolean updateAutoMode();
void updateSettings();

int pwmMap(double in, double min, double max);
boolean forEachAutoPin(int pwm, boolean digital);

#if TEMP_HUM_SENSOR != DISABLED
boolean processDewPoint(double triggerDiff);
boolean processHumidity(double triggerHum);
#endif
}  // namespace DevManager

#endif
#endif