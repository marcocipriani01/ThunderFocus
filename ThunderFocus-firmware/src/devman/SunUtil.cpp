#include "SunUtil.h"
#if RTC_SUPPORT != DISABLED

namespace SunUtil {

double sunElevation = 0.0;
unsigned long lastUpdateTime = 0L;

void begin() {
    setSyncProvider(getRTCTime);
    sunElevation = (calculateSolarPosition(getRTCTime(), Settings::settings.latitude * DEG_TO_RAD, Settings::settings.longitude * DEG_TO_RAD).elevation) * RAD_TO_DEG;
    lastUpdateTime = millis();
}

double getSunElevation() {
    unsigned long t = millis();
    if ((t - lastUpdateTime) >= SUN_ELEVATION_UPDATE_TIME) {
        sunElevation = (calculateSolarPosition(getRTCTime(), Settings::settings.latitude * DEG_TO_RAD, Settings::settings.longitude * DEG_TO_RAD).elevation) * RAD_TO_DEG;
        lastUpdateTime = t;
    }
    return sunElevation;
}

void setRTCTime(unsigned long currentTime) {
    if (currentTime > MIN_VALID_UNIX_TIME) {
        setTime(currentTime);
#if RTC_SUPPORT == TEENSY_RTC
        Teensy3Clock.set(currentTime);
#endif
    }
}

time_t getRTCTime() {
#if RTC_SUPPORT == TEENSY_RTC
    return Teensy3Clock.get();
#else
    return now();
#endif
}
}
#endif