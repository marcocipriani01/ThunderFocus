#ifndef MAIN_H
#define MAIN_H

#include <Arduino.h>
#include "config.h"

#include "ThunderFocusProtocol.h"

#include "Focuser.h"
#if FOK1_ENABLE_HC == true
#include "HandController.h"
#endif

#if ENABLE_DEVMAN == true
#include "DevManager.h"
#endif

#if TIME_CONTROL == true
#include "SunKeeper.h"
#endif

#if SETTINGS_SUPPORT == true
#include "Settings.h"
#endif

inline void flagSettings();

#endif