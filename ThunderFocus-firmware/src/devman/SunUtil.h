#ifndef SUN_UTIL_H
#define SUN_UTIL_H

#include <Arduino.h>

#include "../config.h"

#if RTC_SUPPORT != DISABLED
#if SETTINGS_SUPPORT == false
#error "The sun elevation feature requires an EEPROM!"
#endif
#include <SolarPosition.h>
#include <TimeLib.h>

#include "../settings/Settings.h"

#define MIN_VALID_UNIX_TIME 1652640903L
#define SUN_ELEVATION_UPDATE_TIME 30000L

namespace SunUtil {
extern double sunElevation;
extern unsigned long lastUpdateTime;

void begin();
double getSunElevation();
void setTime(unsigned long currentTime);
time_t getTime();
}  // namespace SunUtil

#endif
#endif