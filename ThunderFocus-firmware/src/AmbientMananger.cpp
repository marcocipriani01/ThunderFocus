#include "AmbientManager.h"

Adafruit_BME280 bme;
unsigned long lastSensorsCheck = 0;
double temperature = TEMP_ABSOLUTE_ZERO;
double integrationTemperature = 0.0;
double humidity = HUMIDITY_INVALID;
double integrationHumidity = 0.0;
int sensIntegrationCount = 0;
double dewPoint = TEMP_ABSOLUTE_ZERO;

void ambientInit() {
#ifdef I2C_SDA_PIN
	Wire.setSDA(I2C_SDA_PIN);
#endif
#ifdef I2C_SCL_PIN
	Wire.setSCL(I2C_SCL_PIN);
#endif
	if (!bme.begin(0x76)) {
		while (true) {
			Serial.println("BME280 not found.");
			delay(1000);
		}
	}
}

void ambientManage() {
    unsigned long currentTime = millis();
	if (currentTime - lastSensorsCheck >= SENSORS_DELAY) {
		float lHum = bme.readHumidity();
		float lTemp = bme.readTemperature();
		if ((!isnan(lHum)) && (!isnan(lTemp))) {
			integrationHumidity += lHum;
			integrationTemperature += lTemp;
			sensIntegrationCount++;
			if (sensIntegrationCount == SENSORS_DATAPOINTS) {
				temperature = integrationTemperature / ((double) sensIntegrationCount);
				humidity = integrationHumidity / ((double) sensIntegrationCount);
				// http://irtfweb.ifa.hawaii.edu/~tcs3/tcs3/Misc/Dewpoint_Calculation_Humidity_Sensor_E.pdf
				double H = (log10(humidity) - 2) / 0.4343 + (17.62 * temperature) / (243.12 + temperature);
				dewPoint = 243.12 * H / (17.62 - H);
				sensIntegrationCount = 0;
				integrationHumidity = 0;
				integrationTemperature = 0;
			}
		}
        lastSensorsCheck = currentTime;
	}
}

double getHumidity() {
    return humidity;
}

double getTemperature() {
    return temperature;
}

double getDewPoint() {
    return dewPoint;
}