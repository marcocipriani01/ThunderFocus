#ifndef OPENFOCUSER_H
#define OPENFOCUSER_H

#include <Arduino.h>
// Configuration
#include "config.h"

#if PROTOCOL == 1
#include "Moonlite.h"
#elif PROTOCOL == 2
#include "EasyFocuser.h"
#endif

#if ENABLE_FOCUSER == true
#include "Focuser.h"
#if ENABLE_HC == true
#include "HandController.h"
#endif
#endif

#if ENABLE_DEVMAN == true
#include "DevManager.h"
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
