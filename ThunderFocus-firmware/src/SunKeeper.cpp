#include "SunKeeper.h"

float sunKeepLat = 0;
float sunKeepLong = 0;

void initTime() {
    setSyncProvider(getTeensyTime);
    SolarPosition::setTimeProvider(getTeensyTime);
}

void setWorldCoord(float lat, float lng) {
    sunKeepLat = lat;
    sunKeepLong = lng;
}

float getWorldLat() {
    return sunKeepLat;
}

float getWorldLong() {
    return sunKeepLong;
}

float getSolarElevation() {
	return (calculateSolarPosition(getTeensyTime(), sunKeepLat, sunKeepLong).elevation) * RAD_TO_DEG;
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