#include "ThunderFocusProtocol.h"

FocuserState lastFok1State = FocuserState::FOCUSER_POWERSAVE;
unsigned long lastThunderFocusSerialSend = 0;
#if TEMP_HUM_SENSOR == true
unsigned long lastThunderFocusAmbientSend = 0;
#endif
#if TIME_CONTROL == true
unsigned long lastThunderFocusSunPosSend = 0;
#endif

FocuserState thunderFocusManage(Focuser *focuser) {
	FocuserState currentState = focuser->run();
	if (currentState != lastFok1State) {
		Serial.println((char) currentState);
		lastFok1State = currentState;
	}
	unsigned long currentTime = millis();
	if (currentTime - lastThunderFocusSerialSend >= THUNDERFOCUS_SEND_DELAY) {
		Serial.print("S");
		Serial.println(focuser->getCurrentPos());
		lastThunderFocusSerialSend = currentTime;
	}
#if ENABLE_DEVMAN == true
	if (devManage()) {
		thunderFocusUpdPins();
	}
#endif
#if TEMP_HUM_SENSOR == true
	ambientManage();
	if (currentTime - lastThunderFocusAmbientSend >= SENSORS_DELAY) {
		Serial.print("J");
		Serial.print(getTemperature(), 1);
		Serial.print(",");
		Serial.print(getHumidity(), 1);
		Serial.print(",");
		Serial.println(getDewPoint(), 1);
		lastThunderFocusAmbientSend = currentTime;
	}
#endif
#if TIME_CONTROL == true
	if (currentTime - lastThunderFocusSunPosSend >= AUTOMATIC_DEVMAN_TIMER) {
		thunderFocusUpdSunPos();
		lastThunderFocusSunPosSend = currentTime;
	}
#endif
	return currentState;
}

boolean thunderFocusSerialEvent(Focuser *focuser) {
	while (Serial.available()) {
		if (Serial.read() == '$') {
			Serial.println("LFoundCmd");
			switch (Serial.read()) {
			case 'C': {
				Serial.print("C");
				Serial.print(FIRMWARE_VERSION);
				Serial.print(",");
				Serial.print(focuser->getCurrentPos());
				Serial.print(",");
				Serial.print(focuser->getSpeed());
				Serial.print(",");
				Serial.print(focuser->isHoldControlEnabled());
				Serial.print(",");
				Serial.print(focuser->getBacklash());
				Serial.print(",");
				Serial.print(focuser->getDirReverse());
				Serial.print(",");
				Serial.print(ENABLE_DEVMAN);
#if ENABLE_DEVMAN == true
				Serial.print(",");
				Serial.print(getDevManAutoMode());
				Serial.print(",");
				for (byte i = 0; i < getManagedPinsCount(); i++) {
					Pin pin = getManagedPin(i);
					Serial.print("(");
					Serial.print(pin.number);
					Serial.print("%");
					Serial.print(pin.value);
					Serial.print("%");
					Serial.print(pin.isPwm);
					Serial.print("%");
					Serial.print(pin.autoModeEn);
					Serial.print(")");
				}
				Serial.print(",");
				Serial.print(TEMP_HUM_SENSOR);
				Serial.print(",");
				Serial.print(TIME_CONTROL);
#if TIME_CONTROL == true
				Serial.print(",");
				Serial.print(getWorldLat(), 3);
				Serial.print(",");
				Serial.print(getWorldLong(), 3);
#endif
#endif
				Serial.println();
#if TIME_CONTROL == true
				Serial.print("LStoredTime=");
				Serial.print(hour());
				Serial.print(":");
				Serial.print(minute());
				Serial.print(":");
				Serial.print(second());
				Serial.print(" ");
				Serial.print(day());
				Serial.print("/");
				Serial.print(month());
				Serial.print("/");
				Serial.print(year());
				Serial.println(" UTC");
				thunderFocusUpdSunPos();
#endif
				break;
			}

			case 'R': {
				long n = Serial.parseInt();
				Serial.print("LMove=");
				Serial.println(n);
				focuser->move(n);
				break;
			}

			case 'A': {
				long n = Serial.parseInt();
				Serial.print("LGoTo=");
				Serial.println(n);
				focuser->moveToTargetPos(n);
				break;
			}

			case 'S': {
				Serial.println(F("LStop"));
				focuser->brake();
				break;
			}

			case 'P': {
				int n = Serial.parseInt();
				Serial.print(F("LSetPos="));
				Serial.println(n);
				focuser->setCurrentPos(n);
				return true;
			}

			case 'W': {
				Serial.println(F("LSetZero"));
				focuser->setCurrentPos(0);
				return true;
			}

			case 'H': {
				boolean b = (Serial.parseInt() > 0);
				Serial.print(F("LHoldControl="));
				Serial.println(b);
				focuser->setHoldControlEnabled(b);
				return true;
			}

			case 'V': {
				long n = Serial.parseInt();
				Serial.print(F("LSpeed="));
				Serial.println(n);
				focuser->setSpeed(n);
				return true;
			}

			case 'B': {
				long n = Serial.parseInt();
				Serial.print(F("LBacklash="));
				Serial.println(n);
				focuser->setBacklash(n);
				return true;
			}

			case 'D': {
				boolean b = (Serial.parseInt() > 0);
				Serial.print(F("LDirReverse="));
				Serial.println(b);
				focuser->setDirReverse(b);
				return true;
			}

#if ENABLE_DEVMAN == true
			case 'X': {
				byte pin = Serial.parseInt();
				byte value = Serial.parseInt();
				Serial.print(F("LSetPin="));
				Serial.print(pin);
				Serial.print(F("@"));
				Serial.println(value);
				updatePin(pin, value);
				return true;
			}

			case 'K': {
				DevManAutoModes am = (DevManAutoModes) Serial.parseInt();
				Serial.print(F("LSetAutoMode="));
				Serial.println((int) am);
				if (setDevManAutoMode(am)) {
					thunderFocusUpdPins();
				}
				return true;
			}

			case 'Y': {
				byte pin = Serial.parseInt();
				boolean amEn = (Serial.parseInt() == 1);
				Serial.print(F("LSetPinAuto="));
				Serial.print(pin);
				Serial.print(F("@"));
				Serial.println(amEn);
				if (setPinAutoMode(pin, amEn)) {
					thunderFocusUpdPins();
				}
				return true;
			}
#endif

#if TIME_CONTROL == true
			case 'T': {
				unsigned long t = Serial.parseInt();
				if (t != 0) {
					setTeensyTime(t);
					Serial.print(F("LSetTime="));
					Serial.println(t);
				}
				long lat = Serial.parseInt();
				long lng = Serial.parseInt();
				if (lat != 0 && lng != 0) {
					setWorldCoord(((double) lat) / 1000.0, ((double) lng) / 1000.0);
					Serial.print(F("LSetWorldCoord="));
					Serial.print(lat);
					Serial.print(",");
					Serial.println(lng);
					thunderFocusUpdSunPos();
					return true;
				}
				break;
			}
#endif
			}
		}
	}
	return false;
}

#if ENABLE_DEVMAN == true
void thunderFocusUpdPins() {
	Serial.print("Y");
	Serial.print(getDevManAutoMode());
	Serial.print(",");
	for (byte i = 0; i < getManagedPinsCount(); i++) {
		Pin pin = getManagedPin(i);
		if (pin.autoModeEn) {
			Serial.print("(");
			Serial.print(pin.number);
			Serial.print("%");
			Serial.print(pin.value);
			Serial.print(")");
		}
	}
	Serial.println();
}
#endif

#if TIME_CONTROL == true
inline void thunderFocusUpdSunPos() {
	Serial.print("T");
	Serial.println(getCalculatedSunElev(), 2);
}
#endif