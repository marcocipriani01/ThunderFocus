#ifndef THUNDERFOCUS_H
#define THUNDERFOCUS_H

#include <Arduino.h>

#include "config.h"
#include "version.h"

#if FOCUSER_DRIVER != DISABLED
#include "focuser/Focuser.h"
#include "focuser/AccelStepper.h"
#endif

#if ENABLE_DEVMAN == true
#include "devman/DevManager.h"
#if RTC_SUPPORT != DISABLED
#include <TimeLib.h>
#include <math.h>
#include "devman/SunUtil.h"
#endif
#if TEMP_HUM_SENSOR != DISABLED
#include "devman/AmbientManager.h"
#endif
#endif

#if FLAT_PANEL == true
#include "flat/FlatPanel.h"
#endif

#define CMD_RECEIVE_WAIT 1
#define SERIAL_TIMEOUT 100
#define SERIAL_SPEED 115200
#define THUNDERFOCUS_SEND_DELAY 150

namespace ThunderFocus {

#if FOCUSER_DRIVER != DISABLED
enum FocuserState { MOVING = (int)'M', HOLD = (int)'H', ARRIVED = (int)'A', POWER_SAVE = (int)'P' };

extern FocuserState lastFocuserState;
extern unsigned long focuserSyncTime;
#endif

#if TEMP_HUM_SENSOR != DISABLED
extern unsigned long sensorsSyncTime;
#endif
#if RTC_SUPPORT != DISABLED
extern unsigned long sunSyncTime;
#endif

#if FLAT_PANEL != false
extern unsigned long flatPanelSyncTime;
#endif

void setup();
void run();
void serialEvent();

#if ENABLE_DEVMAN == true
void updatePins();
#if RTC_SUPPORT != DISABLED
void updateSunPosition();
#endif
#endif

inline int speedToPercentage(double speed);
inline double percentageToSpeed(int percentage);
}  // namespace ThunderFocus

#endif