#include "AmbientManager.h"
#if TEMP_HUM_SENSOR != OFF

namespace AmbientManger {
#if TEMP_HUM_SENSOR == BME280
Adafruit_BME280 sensor;
#elif TEMP_HUM_SENSOR == HTU21D
Adafruit_HTU21DF sensor;
#endif

unsigned long lastSensorsCheck = 0;
double temperature = TEMP_ABSOLUTE_ZERO;
double humidity = HUMIDITY_INVALID;
double dewPoint = TEMP_ABSOLUTE_ZERO;

MedianFilter temperatureFilter(SENSORS_DATAPOINTS);
MedianFilter humidityFilter(SENSORS_DATAPOINTS);

void begin() {
#ifdef I2C_SDA_PIN
	Wire.setSDA(I2C_SDA_PIN);
#endif
#ifdef I2C_SCL_PIN
	Wire.setSCL(I2C_SCL_PIN);
#endif
#if TEMP_HUM_SENSOR == BME280
    if (!sensor.begin(BME280_ADDRESS_ALTERNATE)) {
#elif TEMP_HUM_SENSOR == HTU21D
    if (!sensor.begin()) {
#endif
        do {
            Serial.println(F(">Temperature and humidity sensor not found."));
#ifdef STATUS_LED
            digitalWrite(STATUS_LED, HIGH);
            delay(500);
            digitalWrite(STATUS_LED, LOW);
            delay(500);
#else
            delay(1000);
#endif
#if TEMP_HUM_SENSOR == BME280
        } while (!sensor.begin(BME280_ADDRESS_ALTERNATE));
#elif TEMP_HUM_SENSOR == HTU21D
        } while (!sensor.begin());
#endif
    }
}

void run() {
    unsigned long t = millis();
    if ((t - lastSensorsCheck) >= SENSORS_UPDATE_INTERVAL) {
        double tempRaw = temperatureFilter.add((double)sensor.readTemperature()),
            humRaw = humidityFilter.add((double)sensor.readHumidity());
        if (isnan(tempRaw) || isinf(tempRaw) || isnan(humRaw) || isinf(humRaw)) {
            temperature = TEMP_ABSOLUTE_ZERO;
            humidity = HUMIDITY_INVALID;
            dewPoint = TEMP_ABSOLUTE_ZERO;
        } else {
            temperature = tempRaw;
            humidity = humRaw;
            double H = (log10(humidity) - 2.0) / 0.4343 + (17.62 * temperature) / (243.12 + temperature);
            dewPoint = 243.12 * H / (17.62 - H);
        }
        lastSensorsCheck = t;
    }
}

double getHumidity() { return humidity; }

double getTemperature() { return temperature; }

double getDewPoint() { return dewPoint; }
}  // namespace AmbientManger
#endif