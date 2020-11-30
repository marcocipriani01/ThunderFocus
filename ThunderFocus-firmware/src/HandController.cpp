#include "HandController.h"

HandController::HandController(Focuser *focuser) {
	f = focuser;
	lastButtonTime = 0;
}

void HandController::begin() {
	pinMode(FOK1_HC_LEFT, INPUT_PULLUP);
	pinMode(FOK1_HC_RIGHT, INPUT_PULLUP);
	f->setHCSpeedInterval(FOK1_HC_PPS_MIN, FOK1_HC_PPS_MAX);
	f->setHCMaxStepsPerPush(FOK1_HC_MAX_STEPS);
}

void HandController::manage() {
	unsigned long currentTime = millis();
	unsigned int val = analogRead(FOK1_HC_KNOB);
	if ((currentTime - lastButtonTime) >=
	    ((unsigned long) map(val, 0, 1023, FOK1_HC_MIN_SPEED_DELAY, FOK1_HC_MAX_SPEED_DELAY))) {
		lastButtonTime = currentTime;
		if (digitalRead(FOK1_HC_RIGHT) == LOW && digitalRead(FOK1_HC_LEFT) == HIGH) {
			f->hCMove(val, false);

		} else if (digitalRead(FOK1_HC_LEFT) == LOW && digitalRead(FOK1_HC_RIGHT) == HIGH) {
			f->hCMove(val, true);

		} else if (digitalRead(FOK1_HC_LEFT) == LOW && digitalRead(FOK1_HC_RIGHT) == LOW) {
			f->brake();
		}
	}
}
