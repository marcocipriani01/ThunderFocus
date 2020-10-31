#ifndef SETTINGS_H
#define SETTINGS_H

#include <Arduino.h>
#include <EEPROM.h>
#define EEPROM_MARKER 'B'

struct Settings {
	uint8_t marker;
	uint32_t currentPosition;
	uint8_t speed;
	uint8_t holdControlEnabled;
	int32_t backlash;
};

void loadSettings();
void saveSettings();

#endif
