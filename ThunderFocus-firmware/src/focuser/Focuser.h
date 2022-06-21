#ifndef FOCUSER_H
#define FOCUSER_H

#include <Arduino.h>
#include "../config.h"

#if FOCUSER_DRIVER != OFF
#if SETTINGS_SUPPORT == true
#include "../settings/Settings.h"
#endif
#include "AccelStepper.h"

#if HAND_CONTROLLER == true
#define HAND_CONTROLLER_DELAY_MIN 100L
#define HAND_CONTROLLER_DELAY_MAX 20L
#if SETTINGS_SUPPORT == false
#error "HAND_CONTROLLER requires SETTINGS_SUPPORT"
#endif
#endif

namespace Focuser {
extern AccelStepper stepper;

void begin();

#if SETTINGS_SUPPORT == true
void updateSettings();
#endif

#if HAND_CONTROLLER == true
extern unsigned long lastHcUpdate;

void updateHandController();
#endif
}

#endif
#endif