#ifndef THUNDERFOCUS_H
#define THUNDERFOCUS_H

#include <Arduino.h>
#include "Focuser.h"
#include "config.h"
#if ENABLE_DEVMAN == true
#include "DevManager.h"
#endif
#if TIME_CONTROL == true
#include "SunKeeper.h"
#endif

extern FocuserState lastFok1State;
extern unsigned long lastThunderFocusSerialSend;

FocuserState thunderFocusManage(Focuser *focuser);
void thunderFocusSerialEvent(Focuser *focuser);
#if ENABLE_DEVMAN == true
void thunderFocusUpdPins();
#endif

#endif