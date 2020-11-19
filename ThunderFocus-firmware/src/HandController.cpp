#include "HandController.h"

HandController::HandController(Focuser *focuser) {
	f = focuser;
	lastButtonTime = 0;
}

void HandController::begin() {
	pinMode(BUTTON_UP, INPUT_PULLUP);
	pinMode(BUTTON_DOWN, INPUT_PULLUP);
	f->setHCSpeedInterval(MOTOR_HC_PPS_MIN, MOTOR_HC_PPS_MAX);
	f->setHCMaxStepsPerPush(MAX_STEPS_PUSH);
}

void HandController::manage() {
	unsigned long currentTime = millis();
	unsigned int val = analogRead(HC_SPEED_POT);
	if ((currentTime - lastButtonTime) >=
	    ((unsigned long) map(val, 0, 1023, BUTTONS_CHECK_DELAY_MINSPEED, BUTTONS_CHECK_DELAY_MAXSPEED))) {
		lastButtonTime = currentTime;
		if (digitalRead(BUTTON_UP) == LOW && digitalRead(BUTTON_DOWN) == HIGH) {
			f->hCMove(val, false);

		} else if (digitalRead(BUTTON_DOWN) == LOW && digitalRead(BUTTON_UP) == HIGH) {
			f->hCMove(val, true);

		} else if (digitalRead(BUTTON_DOWN) == LOW && digitalRead(BUTTON_UP) == LOW) {
			f->brake();
		}
	}
}
