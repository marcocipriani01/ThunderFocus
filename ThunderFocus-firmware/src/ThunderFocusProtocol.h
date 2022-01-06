#ifndef THUNDERFOCUS_H
#define THUNDERFOCUS_H

#include "config.h"
#include <Arduino.h>
#include "AccelStepper.h"
#if ENABLE_DEVMAN == true
#include "DevManager.h"
#endif
#if TIME_CONTROL == true
#include <TimeLib.h>
#include "SunKeeper.h"
#endif
#if TEMP_HUM_SENSOR == true
#include "AmbientManager.h"
#endif

enum FocuserState {
	MOVING = (int)'M',
	HOLD = (int)'H',
	ARRIVED = (int)'A',
	POWER_SAVE = (int)'P'
};

extern FocuserState lastFocuserState;
extern unsigned long lastThunderFocusSend;
#if TEMP_HUM_SENSOR == true
extern unsigned long lastThunderFocusAmbientSend;
#endif
#if TIME_CONTROL == true
extern unsigned long lastThunderFocusSunPosSend;
#endif

int speedToPercentage(double speed);
double percentageToSpeed(int percentage);

FocuserState thunderFocusManage(AccelStepper *stepper);
boolean thunderFocusSerialEvent(AccelStepper *stepper);

void thunderFocusLog(const String &msg);

#if ENABLE_DEVMAN == true
void thunderFocusUpdPins();
#endif

#if TIME_CONTROL == true
void thunderFocusUpdSunPos();
#endif
#endif
