#include "SunUtil.h"
#if RTC_SUPPORT != OFF

namespace SunUtil {

double sunElevation = NAN;
unsigned long lastUpdateTime = 0L;

void begin() {
    setSyncProvider(requestSync);
    sunElevation = (calculateSolarPosition(now(), Settings::settings.latitude * DEG_TO_RAD, Settings::settings.longitude * DEG_TO_RAD).elevation) * RAD_TO_DEG;
    lastUpdateTime = millis();
}

double getSunElevation() {
    unsigned long t = millis();
    if ((timeStatus() != timeNotSet) && ((t - lastUpdateTime) >= SUN_ELEVATION_UPDATE_TIME)) {
        sunElevation = (calculateSolarPosition(now(), Settings::settings.latitude * DEG_TO_RAD, Settings::settings.longitude * DEG_TO_RAD).elevation) * RAD_TO_DEG;
        lastUpdateTime = t;
    }
    return sunElevation;
}

void setRTCTime(unsigned long currentTime) {
    if (currentTime > MIN_VALID_UNIX_TIME) {
#if RTC_SUPPORT == TEENSY_RTC
        Teensy3Clock.set(currentTime);
#endif
        setTime(currentTime);
    }
#if DEBUG_EN
    else {
        Serial.print(F(">Invalid time: "));
        Serial.println(currentTime);
    }
#endif
}

time_t requestSync() {
#if RTC_SUPPORT == TEENSY_RTC
    return Teensy3Clock.get();
#else
#if DEBUG_EN
    Serial.println(F(">Time sync required"));
#endif
    Serial.println(F("W"));
    return 0;
#endif
}
}
#endif