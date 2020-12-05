#ifndef THUNDERFOCUS_H
#define THUNDERFOCUS_H

#include <Arduino.h>
#include "Focuser.h"
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

extern FocuserState lastFok1State;
extern unsigned long lastThunderFocusSerialSend;
#if TEMP_HUM_SENSOR == true
extern unsigned long lastThunderFocusAmbientSend;
#endif
#if TIME_CONTROL == true
extern unsigned long lastThunderFocusSunPosSend;
#endif

FocuserState thunderFocusManage(Focuser *focuser);
boolean thunderFocusSerialEvent(Focuser *focuser);
#if ENABLE_DEVMAN == true
void thunderFocusUpdPins();
#endif
#if TIME_CONTROL == true
inline void thunderFocusUpdSunPos();
#endif

#endif