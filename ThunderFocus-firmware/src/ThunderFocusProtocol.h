#ifndef THUNDERFOCUS_H
#define THUNDERFOCUS_H

#include <Arduino.h>
#include "AccelStepper.h"
#include "config.h"
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

#define THUNDERFOCUS_SEND_DELAY 150
#define THUNDERFOCUS_UUID "a537d6e0-c155-405a-9234-7a6ef62913a9"

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

FocuserState thunderFocusManage(AccelStepper *stepper);
boolean thunderFocusSerialEvent(AccelStepper *stepper);

#if ENABLE_DEVMAN == true
void thunderFocusUpdPins();
#endif

#if TIME_CONTROL == true
inline void thunderFocusUpdSunPos();
#endif

#endif
