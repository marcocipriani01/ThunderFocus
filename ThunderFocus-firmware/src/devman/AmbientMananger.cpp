#include "AmbientManager.h"
#if TEMP_HUM_SENSOR != DISABLED

namespace AmbientManger {
#if TEMP_HUM_SENSOR == BME280
Adafruit_BME280 bme;
#elif TEMP_HUM_SENSOR == HTU21D
Adafruit_HTU21DF htu;
#endif

unsigned long lastSensorsCheck = 0;
double temperature = TEMP_ABSOLUTE_ZERO;
double temperatureSum = 0.0;
double humidity = HUMIDITY_INVALID;
double humiditySum = 0.0;
int integrationCount = 0;
double dewPoint = TEMP_ABSOLUTE_ZERO;

void begin() {
#if TEMP_HUM_SENSOR == BME280
    if (!bme.begin(BME280_ADDRESS_ALTERNATE)) {
        while (true) {
            Serial.println(F(">BME280 sensor not found."));
            delay(1000);
        }
    }
#elif TEMP_HUM_SENSOR == HTU21D
    if (!htu.begin()) {
        while (true) {
            Serial.println(F(">HTU21D sensor not found."));
            delay(1000);
        }
    }
#endif
}

void run() {
    unsigned long t = millis();
    if (t - lastSensorsCheck >= SENSORS_UPDATE_INTERVAL) {
#if TEMP_HUM_SENSOR == BME280
        float temp = bme.readTemperature();
        float hum = bme.readHumidity();
#elif TEMP_HUM_SENSOR == HTU21D
        float temp = htu.readTemperature();
        float hum = htu.readHumidity();
#endif
        if ((!isnan(hum)) && (!isnan(temp))) {
            humiditySum += hum;
            temperatureSum += temp;
            integrationCount++;
            if (integrationCount == SENSORS_DATAPOINTS) {
                temperature = temperatureSum / ((double)integrationCount);
                humidity = humiditySum / ((double)integrationCount);
                // http://irtfweb.ifa.hawaii.edu/~tcs3/tcs3/Misc/Dewpoint_Calculation_Humidity_Sensor_E.pdf
                double H = (log10(humidity) - 2) / 0.4343 + (17.62 * temperature) / (243.12 + temperature);
                dewPoint = 243.12 * H / (17.62 - H);
                integrationCount = 0;
                humiditySum = 0.0;
                temperatureSum = 0.0;
            }
        }
        lastSensorsCheck = t;
    }
}

double getHumidity() { return humidity; }

double getTemperature() { return temperature; }

double getDewPoint() { return dewPoint; }
}  // namespace AmbientManger
#endif