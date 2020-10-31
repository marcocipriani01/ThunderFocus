#ifndef DEVMAN_H
#define DEVMAN_H

#include <Arduino.h>
// Configuration
#include "DevManagerConfig.h"

void beginDevMan() {
#ifdef DEVMAN_LED
	pinMode(DEVMAN_LED, OUTPUT);
	digitalWrite(DEVMAN_LED, LOW);
#endif
	// Customizable pins
	for (unsigned int i = 0; i < CUSTOM_PINS_COUNT; i++) {
		pinMode(customPins[i], OUTPUT);
		digitalWrite(customPins[i], 0);
	}
	// Polar finder illuminator
#if ENABLE_PFI == true
	pinMode(PFI_LED, OUTPUT);
#endif
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
	for (uint8_t i = 0; i < CUSTOM_PINS_COUNT; i++) {
		digitalWrite(customPins[i], 0);
	}
#ifdef DEVMAN_LED
	digitalWrite(DEVMAN_LED, LOW);
#endif
}

void updatePin(int pin, int value) {
	for (uint8_t i = 0; i < CUSTOM_PINS_COUNT; i++) {
		if (customPins[i] == pin) {
			analogWrite(pin, constrain(value, 0, 255));
#ifdef DEVMAN_LED
			digitalWrite(DEVMAN_LED, HIGH);
#endif
			break;
		}
	}
}

#endif
