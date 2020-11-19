#ifndef SETTINGS_H
#define SETTINGS_H

#include <Arduino.h>
#include <EEPROM.h>
#define EEPROM_MARKER 'A'

struct Settings {
	uint8_t marker;
#if ENABLE_FOCUSER == true
	uint32_t currentPosition;
	uint8_t speed;
	uint8_t holdControlEnabled;
	int32_t backlash;
	boolean reverseDir;
#endif
#if ENABLE_DEVMAN == true
	uint8_t devsPwmStates[DEVMAN_PWM_COUNT];
	boolean devsDioStates[DEVMAN_DIO_COUNT];
#endif
};

void loadSettings();
void saveSettings();

#endif
