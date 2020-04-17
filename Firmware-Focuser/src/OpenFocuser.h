#ifndef OPENFOCUSER_H
#define OPENFOCUSER_H

#include <Arduino.h>
// Configuration
#include "Config.h"

#if PROTOCOL == 1
// MoonLite full protocol
#include "Moonlite.h"
// Focuser
#if ENABLE_FOCUSER == true
#include "Focuser.h"
#endif
// Temperature compensation
#if ENABLE_TEMP_COMP == true
#include "TemperatureCompensation.h"
#include "TempSensorConfig.h"
#endif
// Hand controller
#if ENABLE_HC == true
#include "HandController.h"
#endif
#if ENABLE_DEVMAN == true
#include "DevManager.h"
#endif
#elif PROTOCOL == 2
// EasyFocuser light protocol
#include "EasyFocuser.h"
#include "Focuser.h"
#endif

// EEPROM library for settings storage
#ifdef SETTINGS_SUPPORT
#if SETTINGS_SUPPORT == true
#include "Settings.h"
#endif
#endif

void handleSerial();
void flagSettings();

#endif
