#ifndef MAIN_H
#define MAIN_H

#include <Arduino.h>
#include "ThunderFocusProtocol.h"
#include "config.h"

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
#endif
