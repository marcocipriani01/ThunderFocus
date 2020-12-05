#include "SunKeeper.h"

double sunKeepLat = 0.0;
double sunKeepLong = 0.0;

void initTime() {
    setSyncProvider(getTeensyTime);
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
	return (calculateSolarPosition(getTeensyTime(), sunKeepLat * DEG_TO_RAD, sunKeepLong * DEG_TO_RAD).elevation) * RAD_TO_DEG;
}

void setTeensyTime(unsigned long currentTime) {
    if (currentTime > MIN_VALID_UNIX_TIME) {
        setTime(currentTime);
        Teensy3Clock.set(currentTime);
    }
}

time_t getTeensyTime() {
  return Teensy3Clock.get();
}