#ifndef AMBIENT_H
#define AMBIENT_H

#include <Arduino.h>
#include "../config.h"

#if TEMP_HUM_SENSOR != DISABLED
#include <math.h>
#if TEMP_HUM_SENSOR == BME280
#include <Adafruit_BME280.h>
#include <Adafruit_Sensor.h>
#elif TEMP_HUM_SENSOR == HTU21D
#include <Adafruit_HTU21DF.h>
#else
#error "Unsupported temperature and humidity sensor!"
#endif

#define SENSORS_UPDATE_INTERVAL 10000L
#define TEMP_ABSOLUTE_ZERO -273.0
#define SENSORS_DATAPOINTS 6
#define HUMIDITY_INVALID -1

namespace AmbientManger {
#if TEMP_HUM_SENSOR == BME280
extern Adafruit_BME280 bme;
#elif TEMP_HUM_SENSOR == HTU21D
extern Adafruit_HTU21DF htu;
#endif

extern unsigned long lastSensorsCheck;
extern double temperature;
extern double temperatureSum;
extern double humidity;
extern double humiditySum;
extern int integrationCount;
extern double dewPoint;

void begin();
void run();
double getHumidity();
double getTemperature();
double getDewPoint();
}  // namespace AmbientManger

#endif
#endif