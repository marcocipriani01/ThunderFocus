#ifndef DEVMAN_H
#define DEVMAN_H

#include <Arduino.h>
#include "config.h"
#if TEMP_HUM_SENSOR == true
#include "AmbientManager.h"
#endif
#if TIME_CONTROL == true
#include "SunKeeper.h"
#endif

enum DevManAutoModes {
	NIGHT_ASTRONOMICAL,
	NIGHT_CIVIL,
	DAYTIME,
	DEW_POINT_DIFF1,
	DEW_POINT_DIFF2,
	DEW_POINT_DIFF3,
	DEW_POINT_DIFF5,
	DEW_POINT_DIFF7,
	HUMIDITY_90,
	HUMIDITY_80,
	HUMIDITY_70,
	TEMP_FREEZE,
	UNAVAILABLE
};

struct Pin {
	byte number;
	boolean isPwm;
	byte value;
	boolean autoModeEn;
};

extern Pin pins[];
extern DevManAutoModes devManAutoMode;
extern unsigned long lastDevManTime;
#if TIME_CONTROL == true
extern double calculatedSunElev;
#endif

void beginDevMan();
boolean devManage();
boolean processAutoMode(boolean force);
Pin getManagedPin(byte index);
byte getManagedPinsCount();
void updatePin(byte pin, byte value);
boolean setPinAutoMode(byte pin, boolean autoModeEn);
DevManAutoModes getDevManAutoMode();
boolean setDevManAutoMode(DevManAutoModes autoMode);

byte pwmMap(double in, double min, double max);
boolean forEachAutoPin(byte pwm, boolean digital);

#if TIME_CONTROL == true
double getCalculatedSunElev();
#endif
#if TEMP_HUM_SENSOR == true
boolean dewPointDevManage(double triggerDiff);
boolean humidityDevManage(double triggerHum);
#endif

#endif
