#ifndef TEMP_COMP_H
#define TEMP_COMP_H

#include <Arduino.h>

#define DEF_TEMP_INTEGRATION_NUMBER 100
#define DEF_TEMP_TIMEOUT 50
#define ABSOLUTE_ZERO -273.15

class TemperatureCompensation {
public:
  // ----- Constructor -----
	TemperatureCompensation(float (*readTemperature)());

	// ----- Temperature management -----
	boolean manage();

	// Setters:
	void setIntegrationCount(unsigned long integrationLoop);
	/**
	   Set the offset of the temperature in °C.
	 */
	void setTempetatureOffset(float offset);
  void setTempCheckTimeout(unsigned long timeout);

	// ----- Getters -----
	/**
	   Gets the temperature in °C.
	 */
	float getTemperature();
	unsigned long getIntegrationCount();
	float getTempetatureOffset();
  unsigned long getTempCheckTimeout();

  // ----- Compensation -----
  long getCompensatedMotorSteps();
  void setCompensationCoefficient(int8_t coefficient);
  int8_t getCompensationCoefficient();
  bool isCompensationEnabled();
  void enableCompensation();
  void disableCompensation();

private:
  // ----- Temperature -----
	float (*readTemperatureFunction)();
  unsigned long tempCheckTimeout;
	// Temperature is stored in °C
	float temperature;
	float integratedTemperature;
	unsigned long integrationCount;
	unsigned long integrationCountNumber;
	float temperatureOffsetValue;
	void integrateTemperature();
  unsigned long tempTimestamp;

  // ----- Compensation -----
  boolean compensationEnabled;
  boolean compensationInit;
  float lastCompensatedTemperature;
  int8_t compensationCoefficient;
};

#endif
