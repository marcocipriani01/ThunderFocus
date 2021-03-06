#ifndef AMBIENT_H
#define AMBIENT_H

#include <Arduino.h>
#include "config.h"
#include <DHT.h>
#include <math.h>
#define TEMP_ABSOLUTE_ZERO -273
#define HUMIDITY_INVALID -1

extern DHT dhtSensor;
extern unsigned long lastSensorsCheck;
extern double temperature;
extern double integrationTemperature;
extern double humidity;
extern double integrationHumidity;
extern int sensIntegrationCount;
extern double dewPoint;

void ambientInit();
void ambientManage();
double getHumidity();
double getTemperature();
double getDewPoint();

#endif