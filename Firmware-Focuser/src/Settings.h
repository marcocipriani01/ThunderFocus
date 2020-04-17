#ifndef SETTINGS_H
#define SETTINGS_H

#include <Arduino.h>
#include <EEPROM.h>
#define EEPROM_MARKER 'A'

struct Settings {
	uint8_t marker;
	uint16_t currentPosition;
	uint8_t speed;
	uint8_t microstepEnabled;
	uint8_t holdControlEnabled;
};

void loadSettings();
void saveSettings();

#endif
