#ifndef FOCUSER_H
#define FOCUSER_H

#include <Arduino.h>
#include "../config.h"

#if FOCUSER_DRIVER != DISABLED
#if SETTINGS_SUPPORT == true
#include "../settings/Settings.h"
#endif
#include "AccelStepper.h"

namespace Focuser {
extern AccelStepper stepper;

void begin();
void updateSettings();
}

#endif
#endif