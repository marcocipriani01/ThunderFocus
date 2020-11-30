#ifndef MAIN_H
#define MAIN_H

#include <Arduino.h>
#include "config.h"

#if PROTOCOL == PROTOCOL_MOONLITE
#include "Moonlite.h"
#elif PROTOCOL == PROTOCOL_THUNDERFOCUS
#include "ThunderFocusProtocol.h"
#endif

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

void handleSerial();
inline void flagSettings();

#endif
