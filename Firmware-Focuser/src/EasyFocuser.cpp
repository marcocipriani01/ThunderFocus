#include "EasyFocuser.h"
#include "OpenFocuserUUID.h"

EasyFocuser::EasyFocuser(Focuser *f) {
	focuser = f;
	state = FS_ERROR;
	lastSendTime = 0;
}

void EasyFocuser::begin() {
	Serial.println("syncme");
	Serial.println("LSetup");
}

void EasyFocuser::flagReady() {
	Serial.println("R");
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

	if (Serial.available() && Serial.find('G')) {
		Serial.print("LCmd: ");
		String type = Serial.readStringUntil('N');
		if (type.equals("R")) {
			Serial.print("Move = ");
			// Cmd: GRN-1234# -> move stepper by -1234
			long n = Serial.parseInt();
			Serial.println(n);
			focuser->move(n);

		} else if (type.equals("A")) {
			Serial.print("GoTo = ");
			// Cmd: GAN-1234# -> move stepper to -1234
			long n = Serial.parseInt();
			Serial.println(n);
			focuser->moveToTargetPos(n);

		} else if (type.equals("S")) {
			Serial.println("LBrake");
			// Cmd: GSN -> stop stepper
			focuser->brake();

		} else if (type.equals("Z")) {
			Serial.println("LSetZero");
			// Cmd: GSN -> stop stepper
			focuser->setCurrentPos(0);

		} else if (type.equals("P")) {
			Serial.print("PowerSaver = ");
			boolean b = (Serial.parseInt() > 0);
			Serial.println(b);
			focuser->setHoldControlEnabled(b);

		} else if (type.equals("V")) {
			Serial.print("Speed = ");
			long n = Serial.parseInt();
			Serial.println(n);
			focuser->setSpeed(n);

		} else if (type.equals("B")) {
			Serial.print("Backlash = ");
			long n = Serial.parseInt();
			Serial.println(n);
			focuser->setBacklash(n);

		} else if (type.equals("U")) {
			Serial.print("Product UUID = ");
			Serial.println(OPENFOCUSER_UUID);

		} else {
			Serial.println("unknown");
		}
	}
}
