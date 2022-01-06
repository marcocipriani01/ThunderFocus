#ifndef SETTINGS_H
#define SETTINGS_H

#include "config.h"
#include <Arduino.h>
#include <EEPROM.h>
#if ENABLE_DEVMAN == true
#include "DevManager.h"
#endif

struct Settings {
#if EEPROM_PADDING > 0
	byte padding[EEPROM_PADDING];
#endif
	uint8_t marker;
	long position;
	double speed;
	long backlash;
	unsigned long powerTimeout;
	boolean reverse;

#if ENABLE_DEVMAN == true
	Pin powerPins[MANAGED_PINS_COUNT];
	DevManAutoModes powerPinsMode;
#endif

#if TIME_CONTROL == true
	double worldLat;
	double worldLong;
#endif
};

void resetSettings();
void loadSettings();
void saveSettings();

#endif
