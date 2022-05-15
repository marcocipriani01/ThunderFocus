#include "SunUtil.h"

#if RTC_SUPPORT != DISABLED
using namespace SunUtil;

double sunElevation = 0.0;
unsigned long lastUpdateTime = 0L;

void SunUtil::begin() {
    setSyncProvider(getTime);
    sunElevation = (calculateSolarPosition(SunUtil::getTime(), Settings::settings.latitude * DEG_TO_RAD, Settings::settings.longitude * DEG_TO_RAD).elevation) * RAD_TO_DEG;
    lastUpdateTime = millis();
}

double SunUtil::getSunElevation() {
    unsigned long t = millis();
    if ((t - lastUpdateTime) >= SUN_ELEVATION_UPDATE_TIME) {
        sunElevation = (calculateSolarPosition(SunUtil::getTime(), Settings::settings.latitude * DEG_TO_RAD, Settings::settings.longitude * DEG_TO_RAD).elevation) * RAD_TO_DEG;
        lastUpdateTime = t;
    }
    return sunElevation;
}

void SunUtil::setTime(unsigned long currentTime) {
    if (currentTime > MIN_VALID_UNIX_TIME) {
        setTime(currentTime);
#if RTC_SUPPORT == TEENSY_RTC
        Teensy3Clock.set(currentTime);
#endif
    }
}

time_t SunUtil::getTime() {
#if RTC_SUPPORT == TEENSY_RTC
    return Teensy3Clock.get();
#else
    return now();
#endif
}
#endif