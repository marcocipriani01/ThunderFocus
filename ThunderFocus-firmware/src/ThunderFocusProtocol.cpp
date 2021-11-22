#include "ThunderFocusProtocol.h"

FocuserState lastFocuserState = FocuserState::POWER_SAVE;
unsigned long lastThunderFocusSend = 0;
#if TEMP_HUM_SENSOR == true
unsigned long lastThunderFocusAmbientSend = 0;
#endif
#if TIME_CONTROL == true
unsigned long lastThunderFocusSunPosSend = 0;
#endif

FocuserState thunderFocusManage(AccelStepper *stepper) {
	FocuserState currentState;
	if (stepper->run())
		currentState = FocuserState::MOVING;
	else if (lastFocuserState == FocuserState::MOVING)
		currentState = FocuserState::ARRIVED;
	else if (stepper->isEnabled())
		currentState = FocuserState::HOLD;
	else
		currentState = FocuserState::POWER_SAVE;

	if (currentState != lastFocuserState) {
		Serial.println((char) currentState);
		lastFocuserState = currentState;
	}
	unsigned long currentTime = millis();
	if (currentTime - lastThunderFocusSend >= THUNDERFOCUS_SEND_DELAY) {
		Serial.print("S");
		Serial.println(stepper->getPosition());
		lastThunderFocusSend = currentTime;
	}

#if ENABLE_DEVMAN == true
	if (devManage())
		thunderFocusUpdPins();
#endif
#if TEMP_HUM_SENSOR == true
	ambientManage();
	if (currentTime - lastThunderFocusAmbientSend >= AUTOMATIC_DEVMAN_TIMER) {
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

boolean thunderFocusSerialEvent(AccelStepper *stepper) {
	while (Serial.available()) {
		if (Serial.read() == '$') {
			Serial.println("LFoundCmd");
			switch (Serial.read()) {
			case 'C': {
				Serial.print("C");
				Serial.print(FIRMWARE_VERSION);
				Serial.print(",");
				Serial.print(stepper->getPosition());
				Serial.print(",");
				Serial.print(map(stepper->getMaxSpeed(), FOCUSER_PPS_MIN, FOCUSER_PPS_MAX, 0, 100));
				Serial.print(",");
				Serial.print(stepper->getAutoPowerTimeout() != 0);
				Serial.print(",");
				Serial.print(stepper->getBacklash());
				Serial.print(",");
				Serial.print(stepper->isDirectionInverted());
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
					Serial.print(pin.autoMode);
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
				stepper->move(n);
				break;
			}

			case 'A': {
				long n = Serial.parseInt();
				Serial.print("LGoTo=");
				Serial.println(n);
				stepper->moveTo(n);
				break;
			}

			case 'S': {
				Serial.println(F("LStop"));
				stepper->stop();
				break;
			}

			case 'P': {
				long n = Serial.parseInt();
				Serial.print(F("LSetPos="));
				Serial.println(n);
				stepper->setPosition(n);
				return true;
			}

			case 'W': {
				Serial.println(F("LSetZero"));
				stepper->setPosition(0);
				return true;
			}

			case 'H': {
				boolean b = (Serial.parseInt() > 0);
				Serial.print(F("LHoldControl="));
				Serial.println(b);
				stepper->setAutoPowerTimeout(b ? FOCUSER_POWER_TIMEOUT : 0);
				return true;
			}

			case 'V': {
				long n = Serial.parseInt();
				Serial.print(F("LSpeed="));
				Serial.println(n);
				stepper->setMaxSpeed(map(n, 0, 100, FOCUSER_PPS_MIN, FOCUSER_PPS_MAX));
				return true;
			}

			case 'B': {
				long n = Serial.parseInt();
				Serial.print(F("LBacklash="));
				Serial.println(n);
				stepper->setBacklash(n);
				return true;
			}

			case 'D': {
				boolean b = (Serial.parseInt() > 0);
				Serial.print(F("LDirReverse="));
				Serial.println(b);
				stepper->setDirectionInverted(b);
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
					setSunKeeperTime(t);
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
		if (pin.autoMode) {
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
