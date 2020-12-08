#include "SunKeeper.h"

double sunKeepLat = 0.0;
double sunKeepLong = 0.0;

void initTime() {
    setSyncProvider(getSunKeeperTime);
}

void setWorldCoord(double lat, double lng) {
    sunKeepLat = lat;
    sunKeepLong = lng;
}

double getWorldLat() {
    return sunKeepLat;
}

double getWorldLong() {
    return sunKeepLong;
}

double getSolarElevation() {
	return (calculateSolarPosition(getSunKeeperTime(), sunKeepLat * DEG_TO_RAD, sunKeepLong * DEG_TO_RAD).elevation) * RAD_TO_DEG;
}

void setSunKeeperTime(unsigned long currentTime) {
    if (currentTime > MIN_VALID_UNIX_TIME) {
        setTime(currentTime);
#if defined(CORE_TEENSY)
        Teensy3Clock.set(currentTime);
#endif
    }
}

time_t getSunKeeperTime() {
#if defined(CORE_TEENSY)
    return Teensy3Clock.get();
#else
    return now();
#endif
}