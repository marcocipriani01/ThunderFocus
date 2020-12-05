#ifndef SUNKEEPER_H
#define SUNKEEPER_H

#include <Arduino.h>
#include "config.h"
#if TIME_CONTROL == true && !defined(CORE_TEENSY)
#error SunKeeper: time control enabled and CORE_TEENSY not defined
#endif
#include <TimeLib.h>
#include <SolarPosition.h>
#define MIN_VALID_UNIX_TIME 1577836800

extern double sunKeepLat;
extern double sunKeepLong;

void initTime();
void setWorldCoord(double lat, double lng);
double getWorldLat();
double getWorldLong();
double getSolarElevation();
void setTeensyTime(unsigned long currentTime);
time_t getTeensyTime();

#endif