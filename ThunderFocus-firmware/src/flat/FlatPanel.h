#ifndef FLAT_PANEL_H
#define FLAT_PANEL_H

#include <Arduino.h>
#include "../config.h"

#if FLAT_PANEL == true
#include "FlatPanelEnums.h"
#if SERVO_MOTOR != DISABLED
#if SETTINGS_SUPPORT == false
#error "SETTINGS_SUPPORT must be true to use the flat panel servo feature."
#endif
#include "../settings/Settings.h"
#include "ServoHack.h"
#include "ServoModels.h"
#if EL_PANEL_LOG_SCALE == true
#include <math.h>
#endif
#endif

#define SERVO_STEP_SIZE 10
#define EL_PANEL_FADE_DELAY 8

namespace FlatPanel {
#if SERVO_MOTOR != DISABLED
extern ServoHack servo;
extern int targetVal;
extern int currentVal;
extern CoverStatus coverStatus;
extern MotorDirection motorDirection;
extern unsigned long lastMoveTime;
#endif

extern boolean lightStatus;
extern uint16_t brightness;
extern uint16_t targetBrightness;
extern uint16_t currentBrightness;
extern unsigned long lastBrightnessAdj;

void begin();
void run();
void setLight(boolean val);
void setBrightness(int val);
#if SERVO_MOTOR != DISABLED
void setShutter(int val);
#endif
};  // namespace FlatPanel

#endif
#endif