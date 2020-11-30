#include "DevManager.h"

Pin pins[] = MANAGED_PINS;
DevManAutoModes devManAutoMode = DevManAutoModes::NIGHT_ASTRONOMICAL;
unsigned long lastDevManTime = 0;

void beginDevMan() {
	// Polar finder illuminator
#if ENABLE_PFI == true
	pinMode(PFI_LED, OUTPUT);
#endif

#if TIME_CONTROL == true
	initTime();
#endif

	for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
		pinMode(pins[i].number, OUTPUT);
		pins[i].value = 0;
		if (pins[i].isPwm) {
			analogWrite(pins[i].number, 0);
		} else {
			digitalWrite(pins[i].number, LOW);
		}
	}
}

boolean devManage() {
#if ENABLE_PFI == true
	int val = analogRead(PFI_KNOB);
	if (val > PFI_THRESHOLD) {
		analogWrite(PFI_LED, map(val, PFI_THRESHOLD, 1023, 0, 255));

	} else {
		analogWrite(PFI_LED, 0);
	}
#endif
	return processAutoMode(false);
}

boolean processAutoMode(boolean force) {
	boolean hasChanged = false;
	if (devManAutoMode != DevManAutoModes::UNAVAILABLE) {
		unsigned long currentTime = millis();
		if (force || ((currentTime - lastDevManTime) >= AUTOMATIC_DEVMAN_TIMER)) {
			switch (devManAutoMode) {
#if TIME_CONTROL == true
			case DevManAutoModes::NIGHT_ASTRONOMICAL: {
				hasChanged = sunDevManage(-18.0);
				break;
			}
			case DevManAutoModes::NIGHT_CIVIL: {
				hasChanged = sunDevManage(-6.0);
				break;
			}
			case DevManAutoModes::DAYTIME: {
				double elev = getSolarElevation();
				forEachAutoPin(pwmMap(constrain(elev, 0.0, 3.0), 0.0, 3.0), elev > 0.0);
				break;
			}
#endif

#if TEMP_HUM_SENSOR == true
			case DevManAutoModes::DEW_POINT_DIFF1: {
				hasChanged = dewPointDevManage(1.0);
				break;
			}
			case DevManAutoModes::DEW_POINT_DIFF2: {
				hasChanged = dewPointDevManage(2.0);
				break;
			}
			case DevManAutoModes::DEW_POINT_DIFF3: {
				hasChanged = dewPointDevManage(3.0);
				break;
			}
			case DevManAutoModes::DEW_POINT_DIFF5: {
				hasChanged = dewPointDevManage(5.0);
				break;
			}
			case DevManAutoModes::DEW_POINT_DIFF7:{
				hasChanged = dewPointDevManage(7.0);
				break;
			}

			case DevManAutoModes::HUMIDITY_90: {
				hasChanged = humidityDevManage(90.0);
				break;
			}
			case DevManAutoModes::HUMIDITY_80: {
				hasChanged = humidityDevManage(80.0);
				break;
			}
			case DevManAutoModes::HUMIDITY_70: {
				hasChanged = humidityDevManage(70.0);
				break;
			}

			case DevManAutoModes::TEMP_FREEZE: {
				double temperature = getTemperature();
				if (temperature != TEMP_ABSOLUTE_ZERO) {
					hasChanged = forEachAutoPin(pwmMap(
						constrain(temperature, 2.0, 4.0), 4.0, 2.0), temperature <= 3.0);
				}
				break;
			}
#endif
			default: {
				devManAutoMode = DevManAutoModes::UNAVAILABLE;
				break;
			}
			}
			lastDevManTime = currentTime;
		}
	}
	return hasChanged;
}

Pin getManagedPin(byte index) {
	return pins[index];
}

byte getManagedPinsCount() {
	return MANAGED_PINS_COUNT;
}

void updatePin(byte pin, byte value) {
	for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
		if (pin == pins[i].number){
			pins[i].autoModeEn = false;
			if (pins[i].isPwm) {
				pins[i].value = value;
				analogWrite(pins[i].number, value);
			} else {
				boolean bVal = (value > 100);
				pins[i].value = bVal ? 255 : 0;
				digitalWrite(pins[i].number, bVal);
			}
			return;
		}
	}
}

boolean setPinAutoMode(byte pin, boolean autoModeEn) {
	for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
		if (pin == pins[i].number){
			pins[i].autoModeEn = autoModeEn;
			return processAutoMode(true);
		}
	}
	return false;
}

DevManAutoModes getDevManAutoMode() {
	return devManAutoMode;
}

void setDevManAutoMode(DevManAutoModes autoMode) {
	switch (autoMode) {
#if TIME_CONTROL == true
	case DevManAutoModes::NIGHT_ASTRONOMICAL:
	case DevManAutoModes::NIGHT_CIVIL:
	case DevManAutoModes::DAYTIME: {
		devManAutoMode = autoMode;
		break;
	}
#endif
#if TEMP_HUM_SENSOR == true
	case DevManAutoModes::DEW_POINT_DIFF1:
	case DevManAutoModes::DEW_POINT_DIFF2:
	case DevManAutoModes::DEW_POINT_DIFF3:
	case DevManAutoModes::DEW_POINT_DIFF5:
	case DevManAutoModes::DEW_POINT_DIFF7:
	case DevManAutoModes::HUMIDITY_90:
	case DevManAutoModes::HUMIDITY_80:
	case DevManAutoModes::HUMIDITY_70:
	case DevManAutoModes::TEMP_FREEZE: {
		devManAutoMode = autoMode;
		break;
	}
#endif
	default: {
		devManAutoMode = DevManAutoModes::UNAVAILABLE;
		break;
	}
	}
}

byte pwmMap(double in, double min, double max) {
	return (in - min) * (255.0 - AUTOMATIC_DEVMAN_THRESHOLD) / (max - min) + AUTOMATIC_DEVMAN_THRESHOLD;
}

boolean forEachAutoPin(byte pwm, boolean digital) {
	boolean hasChanged = false;
	for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
		if (pins[i].autoModeEn) {
			if (pins[i].isPwm) {
				if (pwm != pins[i].value) {
					pins[i].value = pwm;
					analogWrite(pins[i].number, pwm);
					hasChanged = true;
				}
			} else {
				byte nVal = digital ? 255 : 0;
				if (nVal != pins[i].value) {
					pins[i].value = nVal;
					digitalWrite(pins[i].number, digital);
					hasChanged = true;
				}
			}
		}
	}
	return hasChanged;
}

#if TIME_CONTROL == true
boolean sunDevManage(double trigger) {
	double elev = getSolarElevation();
	return forEachAutoPin(pwmMap(constrain(elev, trigger - 3.0, trigger + 3.0), trigger + 3.0, trigger - 3.0), elev <= trigger);
}
#endif

#if TEMP_HUM_SENSOR == true
boolean dewPointDevManage(double triggerDiff) {
	double dewPoint = getDewPoint(), temperature = getTemperature();
	if ((dewPoint != TEMP_ABSOLUTE_ZERO) && (temperature != TEMP_ABSOLUTE_ZERO)) {
		double tempDiff = temperature - triggerDiff;
		return forEachAutoPin(pwmMap(constrain(dewPoint, tempDiff, temperature),
			tempDiff, temperature), temperature - dewPoint <= triggerDiff);
	}
	return false;
}

boolean humidityDevManage(double triggerHum) {
	double humidity = getHumidity();
	if (humidity != HUMIDITY_INVALID) {
		return forEachAutoPin(pwmMap(
			constrain(humidity, triggerHum - 5.0, triggerHum + 5.0), triggerHum - 5.0, triggerHum + 5.0),
			humidity >= triggerHum);
	}
	return false;
}
#endif