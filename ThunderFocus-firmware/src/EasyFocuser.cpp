#include "EasyFocuser.h"

EasyFocuser::EasyFocuser(Focuser *f) {
	focuser = f;
	state = FocuserState::FS_POWERSAVE;
	lastSendTime = 0;
}

void EasyFocuser::flagState(FocuserState s) {
	if (s != state) {
		Serial.println((char) s);
		state = s;
	}
}

void EasyFocuser::manage() {
	unsigned long currentTime = millis();
	if (currentTime - lastSendTime >= EASYFOC_SERIAL_DELAY) {
		Serial.print("S");
		Serial.println(focuser->getCurrentPos());
		lastSendTime = currentTime;
	}


	while (Serial.available()) {
		if (Serial.read() == '$') {
			Serial.println("LFoundCmd");
			char cmd = Serial.read();
			switch (cmd) {
				case 'U': {
					Serial.print("U");
					Serial.println(EASYFOC_UUID);
				}

				case 'Q': {
					Serial.print("Q");
					Serial.print(ENABLE_FOCUSER);
					Serial.print(",");
					Serial.print(ENABLE_DEVMAN);
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
#if ENABLE_DEVMAN == true
					Serial.print(",");
					for (uint8_t i = 0; i < getPwmPinCount(); i++) {
						Serial.print("(");
						Serial.print(getPwmPin(i));
						Serial.print("@");
						Serial.print(getPwmPinValue(i));
						Serial.print(")");
					}
					Serial.print(",");
					for (uint8_t i = 0; i < getDioPinCount(); i++) {
						Serial.print("(");
						Serial.print(getDioPin(i));
						Serial.print("@");
						Serial.print(getDioPinValue(i));
						Serial.print(")");
					}
#endif
					Serial.println();
					break;
				}

#if ENABLE_FOCUSER == true
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

				case 'Z': {
					Serial.println(F("LSetZero"));
					focuser->setCurrentPos(0);
					break;
				}

				case 'P': {
					int n = Serial.parseInt();
					Serial.print(F("LSetPos="));
					Serial.println(n);
					focuser->setCurrentPos(n);
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

				case 'C': {
					boolean b = (Serial.parseInt() > 0);
					Serial.print(F("LDirReverse="));
					Serial.println(b);
					focuser->setDirReverse(b);
					break;
				}
#endif
#if ENABLE_DEVMAN == true
				case 'd': {
					int pin = Serial.parseInt();
					int value = Serial.parseInt();
					Serial.print(F("LSetPin="));
					Serial.print(pin);
					Serial.print(F("@"));
					Serial.println(value);
					updatePin(pin, value);
					break;
				}
#endif
			}
		}
	}
}
