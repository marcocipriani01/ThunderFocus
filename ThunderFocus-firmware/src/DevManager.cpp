#include "DevManager.h"

byte pwmPins[] = DEVMAN_PWM;
byte pwmPinsValues[DEVMAN_PWM_COUNT];
byte dioPins[] = DEVMAN_DIO;
boolean dioPinsValues[DEVMAN_DIO_COUNT];

void beginDevMan() {
#ifdef DEVMAN_LED
	pinMode(DEVMAN_LED, OUTPUT);
	digitalWrite(DEVMAN_LED, LOW);
#endif
	for (uint8_t i = 0; i < DEVMAN_PWM_COUNT; i++) {
		pinMode(pwmPins[i], OUTPUT);
		analogWrite(pwmPins[i], 0);
		pwmPinsValues[i] = 0;
	}
	for (uint8_t i = 0; i < DEVMAN_DIO_COUNT; i++) {
		pinMode(dioPins[i], OUTPUT);
		digitalWrite(dioPins[i], LOW);
		dioPinsValues[i] = LOW;
	}
	// Polar finder illuminator
#if ENABLE_PFI == true
	pinMode(PFI_LED, OUTPUT);
#endif
}

uint8_t getPwmPin(uint8_t index) {
	return pwmPins[index];
}

uint8_t getPwmPinValue(uint8_t index) {
	return pwmPinsValues[index];
}

uint8_t getPwmPinCount() {
	return DEVMAN_PWM_COUNT;
}

uint8_t getDioPin(uint8_t index) {
	return dioPins[index];
}

boolean getDioPinValue(uint8_t index) {
	return dioPinsValues[index];
}

uint8_t getDioPinCount() {
	return DEVMAN_DIO_COUNT;
}

void managePFI() {
#if ENABLE_PFI == true
	int val = analogRead(PFI_POT);
	if (val > PFI_THRESHOLD) {
		analogWrite(PFI_LED, map(val, PFI_THRESHOLD, 1023, 0, 255));

	} else {
		analogWrite(PFI_LED, 0);
	}
#endif
}

void resetPins() {
	for (uint8_t i = 0; i < DEVMAN_PWM_COUNT; i++) {
		analogWrite(pwmPins[i], 0);
		pwmPinsValues[i] = 0;
	}
	for (uint8_t i = 0; i < DEVMAN_DIO_COUNT; i++) {
		digitalWrite(dioPins[i], LOW);
		dioPinsValues[i] = LOW;
	}
#ifdef DEVMAN_LED
	digitalWrite(DEVMAN_LED, LOW);
#endif
}

void updatePin(byte pin, byte value) {
	for (uint8_t i = 0; i < DEVMAN_PWM_COUNT; i++) {
		if (pwmPins[i] == pin) {
			analogWrite(pin, value);
			pwmPinsValues[i] = value;
#ifdef DEVMAN_LED
			digitalWrite(DEVMAN_LED, HIGH);
#endif
			return;
		}
	}
	for (uint8_t i = 0; i < DEVMAN_DIO_COUNT; i++) {
		if (dioPins[i] == pin) {
			dioPinsValues[i] = (value > 100);
			digitalWrite(pin, dioPinsValues[i]);
#ifdef DEVMAN_LED
			digitalWrite(DEVMAN_LED, HIGH);
#endif
			return;
		}
	}
}
