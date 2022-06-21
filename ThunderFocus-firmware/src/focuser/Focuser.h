#ifndef FOCUSER_H
#define FOCUSER_H

#include <Arduino.h>
#include "../config.h"

#if FOCUSER_DRIVER != OFF
#if SETTINGS_SUPPORT == true
#include "../settings/Settings.h"
#endif
#include "AccelStepper.h"

namespace Focuser {
extern AccelStepper stepper;

void begin();

#if SETTINGS_SUPPORT == true
void updateSettings();
#endif
}

#endif
#endif