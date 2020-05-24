#include "Focuser.h"

Focuser::Focuser() :
#if STEPPER_TYPE == 3
	stepper(DRIVER_IN1, DRIVER_IN2, DRIVER_IN3, DRIVER_IN4)
#else
	stepper(DRIVER_STEP, DRIVER_DIR)
#endif
{
	stepper.setAcceleration(MOTOR_ACCEL);
	ledState = false;
	blinkStartTime = millis();
	lastMovementTime = blinkStartTime;
	powerOn = false;
	isMoving = false;
	justStopped = false;
	isHcMoving = true;
}

void Focuser::begin(boolean initHoldControlEnabled,
                    boolean initMicrostepEnabled,
                    uint8_t initSpeed,
                    long backlash) {
	pinMode(LED1, OUTPUT);
#ifdef LED2
	pinMode(LED2, OUTPUT);
#endif
	stepper.begin();
#ifdef DRIVER_EN
	pinMode(DRIVER_EN, OUTPUT);
#endif
#if STEPPER_TYPE != 3
#if STEPPER_TYPE != 0
	stepper.setMicroSteppingPins(MODE0, MODE1, MODE2);
#endif
#endif
	stepper.setBacklash(backlash);
	microstepEnabled = initMicrostepEnabled;
	applyMicrostepGear();
	speed = valToSpeed(initSpeed);
	applySpeed();
	holdControlEnabled = initHoldControlEnabled;
	turnOff();
}

void Focuser::begin() {
	begin(DEFAULT_ENABLE_HOLD_CONTROL, false, 2, 0);
}

void Focuser::moveToTargetPos(long newPos) {
	pinMode(LED1, HIGH);
	isHcMoving = false;
	turnOn();
	applySpeed();
	stepper.moveTo(stepsToSteps(newPos));
}

void Focuser::move(long newPos) {
	pinMode(LED1, HIGH);
	isHcMoving = false;
	turnOn();
	applySpeed();
	stepper.move(stepsToSteps(newPos));
}

long Focuser::getTargetPos() {
	return stepsToStepsRev(stepper.targetPosition());
}

void Focuser::brake() {
	if (isMoving) {
		stepper.stop();
		digitalWrite(LED1, HIGH);
#ifdef LED2
		digitalWrite(LED2, HIGH);
#endif
	}
}

void Focuser::setCurrentPos(long newPos) {
	if (!isMoving) {
		stepper.setCurrentPosition(stepsToSteps(newPos));
	}
}

unsigned long Focuser::getCurrentPos() {
	return stepsToStepsRev(stepper.currentPosition());
}

FocuserState Focuser::run() {
	if (isMoving) {
		AccelStepper::CurrentState state = stepper.run();
		switch (state) {
			case AccelStepper::WAITING_STEP:
			case AccelStepper::MOVING: {
				return FS_MOVING;
			}

			case AccelStepper::WAITING_STEP_BACKLASH:
			case AccelStepper::BACKLASHING: {
				return FS_BACKLASHING;
			}

			case AccelStepper::IDLE: {
				isMoving = false;
				applySpeed();
				lastMovementTime = millis();
#ifdef LED2
				digitalWrite(LED2, LOW);
#endif
				return FS_JUST_ARRIVED;
			}

			default:
			case AccelStepper::NO_DIRECTION: {
				return FS_ERROR;
			}
		}
	}
	if (powerOn) {
		// Turn power off if active time period has passed
		if (holdControlEnabled && ((millis() - lastMovementTime) >= DRIVER_POWER_TIMEOUT)) {
			turnOff();
		}
		return FS_HOLD_MOTOR;
	}
	unsigned long currentMillis = millis();
	if (currentMillis - blinkStartTime >= BLINK_PERIOD) {
		blinkStartTime = currentMillis;
		ledState = !ledState;
		digitalWrite(LED1, ledState);
	}
	return FS_POWERSAVE;
}

boolean Focuser::hasToRun() {
	return stepper.distanceToGo() != 0;
}

void Focuser::setHoldControlEnabled(boolean holdControlEnabledNew) {
	holdControlEnabled = holdControlEnabledNew;
}

boolean Focuser::isHoldControlEnabled() {
	return holdControlEnabled;
}

boolean Focuser::isPowerOn() {
	return powerOn;
}

void Focuser::setMicrostepEnabled(boolean microstepEnabledNew) {
	if (!isMoving) {
		microstepEnabled = microstepEnabledNew;
		applyMicrostepGear();
	}
}

boolean Focuser::isMicrostepEnabled() {
	return microstepEnabled;
}

void Focuser::setSpeed(Speed speedNew) {
	speed = speedNew;
	if (!isMoving) {
		applySpeed();
	}
}

void Focuser::setSpeed(uint8_t speedNew) {
	setSpeed(valToSpeed(speedNew));
}

Speed Focuser::getSpeed() {
	return speed;
}

void Focuser::setBacklash(long backlash) {
	stepper.setBacklash(backlash);
}

long Focuser::getBacklash() {
	return stepper.getBacklash();
}

void Focuser::setHCSpeedInterval(unsigned int rpmMinNew, unsigned int rpmMaxNew) {
	hCRpmMin = rpmMinNew;
	hCRpmMax = rpmMaxNew;
}

void Focuser::setHCMaxStepsPerPush(unsigned long maxStepsPerPushNew) {
	maxStepsPerPush = maxStepsPerPushNew;
}

void Focuser::hCMove(unsigned int analogValue, boolean reverse) {
	if (isHcMoving || !isMoving) {
		stepper.setMaxSpeed(map(analogValue, 0, 1023, hCRpmMin, hCRpmMax));
		long steps = map(analogValue, 0, 1023, 1, maxStepsPerPush);
		turnOn();
		isHcMoving = true;
		if (reverse) {
			stepper.move(stepsToSteps(-steps));
#ifdef LED2
			digitalWrite(LED1, LOW);
			digitalWrite(LED2, HIGH);
#else
			digitalWrite(LED1, HIGH);
#endif

		} else {
			stepper.move(stepsToSteps(steps));
			digitalWrite(LED1, HIGH);
#ifdef LED2
			digitalWrite(LED2, LOW);
#endif
		}
	}
}

boolean Focuser::hasJustStopped() {
	if (justStopped) {
		justStopped = false;
		return true;
	}
	return false;
}

void Focuser::applySpeed() {
	stepper.setMaxSpeed(map(speed, 2, 20, rpmToSpeed(MOTOR_RPM_MAX), rpmToSpeed(MOTOR_RPM_MIN)));
}

int Focuser::rpmToSpeed(int rpm) {
	return rpm * 60 / STEPS_REV;
}

void Focuser::turnOn() {
#ifdef DRIVER_EN
	digitalWrite(DRIVER_EN, LOW);
#endif
	stepper.enableOutputs();
	powerOn = true;
	isMoving = true;
}

void Focuser::turnOff() {
#ifdef DRIVER_EN
	digitalWrite(DRIVER_EN, HIGH);
#endif
	stepper.disableOutputs();
	powerOn = false;
	isMoving = false;
	digitalWrite(LED1, LOW);
#ifdef LED2
	digitalWrite(LED2, LOW);
#endif
}

uint8_t Focuser::getMicrostepGear() {
	return microstepEnabled ? HALF_STEP : FULL_STEP;
}

void Focuser::applyMicrostepGear() {
#if STEPPER_TYPE == 1
	if (microstepEnabled) {
		stepper.setMicroStepping(HALF_STEP, AccelStepper::DRV8825);

	} else {
		stepper.setMicroStepping(FULL_STEP, AccelStepper::DRV8825);
	}
#elif STEPPER_TYPE == 2
	if (microstepEnabled) {
		stepper.setMicroStepping(HALF_STEP, AccelStepper::A4988);

	} else {
		stepper.setMicroStepping(FULL_STEP, AccelStepper::A4988);
	}
#else
	if (microstepEnabled) {
		stepper.setMicroStepping(HALF_STEP);

	} else {
		stepper.setMicroStepping(FULL_STEP);
	}
#endif
}

long Focuser::stepsToSteps(long steps) {
	return steps * SINGLE_STEP * getMicrostepGear();
}

long Focuser::stepsToStepsRev(long steps) {
	return (steps / getMicrostepGear()) / SINGLE_STEP;
}

Speed Focuser::valToSpeed(uint8_t val) {
	switch (val) {
	case 2: {
		return FOCSPEED_FASTEST;
	}

	case 4: {
		return FOCSPEED_FAST;
	}

	case 8: {
		return FOCSPEED_MID;
	}

	case 10: {
		return FOCSPEED_SLOW;
	}

	case 20: {
		return FOCSPEED_SLOWEST;
	}
	}
	return FOCSPEED_FASTEST;
}
