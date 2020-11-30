#ifndef AMBIENT_H
#define AMBIENT_H

#include <Arduino.h>
#include "config.h"
#if TEMP_HUM_SENSOR == false
#error Ambient manager without temperature and humidity sensors! 
#endif
#include <DHT.h>
#include <math.h>
#define TEMP_ABSOLUTE_ZERO -273.15
#define HUMIDITY_INVALID -1

extern DHT dhtSensor;
extern unsigned long lastSensorsCheck;
extern double temperature;
extern double integrationTemperature;
extern double humidity;
extern double integrationHumidity;
extern int sensIntegrationCount;
extern double dewPoint;

void ambientManage();
double getHumidity();
double getTemperature();
double getDewPoint();

#endif