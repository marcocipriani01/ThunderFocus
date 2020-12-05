#ifndef SETTINGS_H
#define SETTINGS_H

#include <Arduino.h>
#include <EEPROM.h>
#if ENABLE_DEVMAN == true
#include "DevManager.h"
#endif

struct Settings {
	uint8_t marker;
	long fok1Pos;
	uint8_t fok1Speed;
	long fok1Backlash;
	boolean fok1HoldControl;
	boolean fok1Reverse;
#if ENABLE_DEVMAN == true
	Pin devManPins[MANAGED_PINS_COUNT];
	DevManAutoModes devManAutoMode;
#endif
#if TIME_CONTROL == true
	double worldLat;
	double worldLong;
#endif
};

void loadSettings();
void saveSettings();

#endif
