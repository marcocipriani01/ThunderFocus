#include "TemperatureCompensation.h"

// ----- Constructor -----
TemperatureCompensation::TemperatureCompensation(float (*readTemperature)()) {
	this->readTemperatureFunction = readTemperature;
	this->integrationCount = DEF_TEMP_INTEGRATION_NUMBER;
	this->integrationCountNumber = 1;
	this->temperature = this->readTemperatureFunction();
	this->integratedTemperature = this->temperature;
	this->temperatureOffsetValue = 0;
	this->tempTimestamp = millis();
	this->lastCompensatedTemperature = this->temperature;
	this->compensationEnabled = false;
	this->compensationInit = false;
	this->tempCheckTimeout = DEF_TEMP_TIMEOUT;
}

// ----- Setters -----

void TemperatureCompensation::setIntegrationCount(unsigned long integrationLoop) {
	this->integrationCount = integrationLoop;
}

void TemperatureCompensation::setTempetatureOffset(float compensationValue) {
	this->temperatureOffsetValue = compensationValue;
}

void TemperatureCompensation::setTempCheckTimeout(unsigned long timeout) {
	this->tempCheckTimeout = timeout;
}

// ----- Getters -----

float TemperatureCompensation::getTemperature() {
	return this->temperature;
}

unsigned long TemperatureCompensation::getIntegrationCount() {
	return this->integrationCount;
}

float TemperatureCompensation::getTempetatureOffset() {
	return this->temperatureOffsetValue;
}

unsigned long TemperatureCompensation::getTempCheckTimeout() {
	return this->tempCheckTimeout;
}

// ----- Temperature management -----
void TemperatureCompensation::integrateTemperature() {
	float readT = this->readTemperatureFunction();
	if (readT == ABSOLUTE_ZERO) {
		return;
	}
	this->integratedTemperature += readT;
	this->integrationCountNumber++;
	if (this->integrationCountNumber >= this->integrationCount) {
		this->integratedTemperature = this->integratedTemperature / this->integrationCount;
		this->integrationCountNumber = 1;
		temperature = this->integratedTemperature + this->temperatureOffsetValue;
		if (!this->compensationInit) {
			this->compensationInit = true;
		}
	}
}

boolean TemperatureCompensation::manage() {
	if ((millis() - tempTimestamp) >= tempCheckTimeout) {
		this->integrateTemperature();
		return (this->compensationInit) && (this->compensationEnabled);
	}
	return false;
}

// ----- Temperature compensation -----

long TemperatureCompensation::getCompensatedMotorSteps() {
	long correction = 0;

	if ((this->compensationInit) && (this->compensationEnabled)) {
		correction = (long)((this->lastCompensatedTemperature - this->temperature)
		                    * ((float) this->compensationCoefficient));
		if (correction != 0) {
			this->lastCompensatedTemperature = this->temperature;
		}
	}
	return correction;
}

void TemperatureCompensation::setCompensationCoefficient(int8_t coefficient) {
  this->compensationCoefficient = coefficient;
}

int8_t TemperatureCompensation::getCompensationCoefficient() {
	return this->compensationCoefficient;
}

bool TemperatureCompensation::isCompensationEnabled() {
	return this->compensationEnabled;
}

void TemperatureCompensation::enableCompensation() {
	this->compensationEnabled = true;
}

void TemperatureCompensation::disableCompensation() {
	this->compensationEnabled = false;
}
