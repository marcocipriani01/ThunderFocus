#include "ThunderFocusProtocol.h"

FocuserState lastFok1State = FocuserState::FOCUSER_POWERSAVE;
unsigned long lastThunderFocusSerialSend = 0;

FocuserState thunderFocusManage(Focuser *focuser) {
	FocuserState currentState = focuser->run();
	if (currentState != lastFok1State) {
		Serial.println((char) currentState);
		lastFok1State = currentState;
	}
	unsigned long currentTime = millis();
	if (currentTime - lastThunderFocusSerialSend >= THUNDERFOCUS_SEND_DELAY) {
		Serial.print("P");
		Serial.println(focuser->getCurrentPos());
		lastThunderFocusSerialSend = currentTime;
	}
#if ENABLE_DEVMAN == true
	if (devManage()) {
		thunderFocusUpdPins();
	}
#endif
	return currentState;
}

void thunderFocusSerialEvent(Focuser *focuser) {
	while (Serial.available()) {
		if (Serial.read() == '$') {
			Serial.println("LFoundCmd");
			switch (Serial.read()) {
			case 'C': {
				Serial.print("C");
				Serial.print(FIRMWARE_VERSION);
				Serial.print(",");
				Serial.print(1);
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
				Serial.print(TIME_CONTROL);
				Serial.print(",");
				Serial.print(TEMP_HUM_SENSOR);
				Serial.print(",");
				for (byte i = 0; i < getManagedPinsCount(); i++) {
					Pin pin = getManagedPin(i);
					Serial.print("(");
					Serial.print(pin.number);
					Serial.print("%");
					Serial.print(pin.isPwm);
					Serial.print("%");
					Serial.print(pin.value);
					Serial.print("%");
					Serial.print((int) pin.autoModeEn);
					Serial.print(")");
				}
#endif
				Serial.println();
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
				break;
			}

			case 'W': {
				Serial.println(F("LSetZero"));
				focuser->setCurrentPos(0);
				break;
			}

			case 'H': {
				boolean b = (Serial.parseInt() > 0);
				Serial.print(F("LHoldControl="));
				Serial.println(b);
				focuser->setHoldControlEnabled(b);
				break;
			}

			case 'V': {
				long n = Serial.parseInt();
				Serial.print(F("LSpeed="));
				Serial.println(n);
				focuser->setSpeed(n);
				break;
			}

			case 'B': {
				long n = Serial.parseInt();
				Serial.print(F("LBacklash="));
				Serial.println(n);
				focuser->setBacklash(n);
				break;
			}

			case 'D': {
				boolean b = (Serial.parseInt() > 0);
				Serial.print(F("LDirReverse="));
				Serial.println(b);
				focuser->setDirReverse(b);
				break;
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
				break;
			}

			case 'K': {
				DevManAutoModes am = (DevManAutoModes) Serial.parseInt();
				Serial.print(F("LSetAutoMode="));
				Serial.println((int) am);
				setDevManAutoMode(am);
				break;
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
			}
#endif

#if TIME_CONTROL == true
			case 'T': {
				setTeensyTime(Serial.parseInt());
				double lat = Serial.parseInt();
				double lng = Serial.parseInt();
				setWorldCoord(lat / 1000.0, lng / 1000.0);
				break;
			}
#endif
			}
		}
	}
}

#if ENABLE_DEVMAN == true
void thunderFocusUpdPins() {
	Serial.print("Y");
	Serial.print(getDevManAutoMode());
	Serial.print(",");
	for (byte i = 0; i < getManagedPinsCount(); i++) {
		Pin pin = getManagedPin(i);
		Serial.print("(");
		Serial.print(pin.number);
		Serial.print("%");
		Serial.print(pin.value);
		Serial.print(")");
	}
	Serial.println();
}
#endif