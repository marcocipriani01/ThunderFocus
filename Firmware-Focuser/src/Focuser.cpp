#include "Focuser.h"

Focuser::Focuser() :
#if STEPPER_TYPE == 3
	stepper(DRIVER_IN1, DRIVER_IN2, DRIVER_IN3, DRIVER_IN4)
#else
	stepper(1, DRIVER_STEP, DRIVER_DIR)
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
                    uint8_t initSpeed,
                    long backlash) {
	pinMode(LED1, OUTPUT);
#ifdef LED2
	pinMode(LED2, OUTPUT);
#endif
#ifdef DRIVER_EN
	stepper.setEnablePin(DRIVER_EN);
	stepper.setPinsInverted(false, false, true);
#endif
#ifdef MODE0
	digitalWrite(MODE0, HIGH);
#endif
#ifdef MODE1
	digitalWrite(MODE1, HIGH);
#endif
#ifdef MODE2
	digitalWrite(MODE2, HIGH);
#endif
	stepper.setBacklash(backlash);
	speed = initSpeed;
	applySpeed();
	holdControlEnabled = initHoldControlEnabled;
	turnOff();
}

void Focuser::begin() {
	begin(DEFAULT_ENABLE_HOLD_CONTROL, 80, 0);
}

void Focuser::moveToTargetPos(long newPos) {
	pinMode(LED1, HIGH);
	isHcMoving = false;
	turnOn();
	applySpeed();
	stepper.moveTo(newPos);
}

void Focuser::move(long newPos) {
	pinMode(LED1, HIGH);
	isHcMoving = false;
	turnOn();
	applySpeed();
	stepper.move(newPos);
}

long Focuser::getTargetPos() {
	return stepper.targetPosition();
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
		stepper.setCurrentPosition(newPos);
	}
}

unsigned long Focuser::getCurrentPos() {
	return stepper.currentPosition();
}

FocuserState Focuser::run() {
	if (isMoving) {
		if (stepper.run()) {
			return FS_MOVING;

		} else {
			isMoving = false;
			applySpeed();
			lastMovementTime = millis();
#ifdef LED2
			digitalWrite(LED2, LOW);
#endif
			return FS_JUST_ARRIVED;
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

void Focuser::setSpeed(uint8_t speedNew) {
	speed = speedNew;
	if (!isMoving) {
		applySpeed();
	}
}

uint8_t Focuser::getSpeed() {
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
			stepper.move(-steps);
#ifdef LED2
			digitalWrite(LED1, LOW);
			digitalWrite(LED2, HIGH);
#else
			digitalWrite(LED1, HIGH);
#endif

		} else {
			stepper.move(steps);
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
	stepper.setMaxSpeed(map(speed, 0, 100, rpmToSpeed(MOTOR_RPM_MAX), rpmToSpeed(MOTOR_RPM_MIN)));
}

int Focuser::rpmToSpeed(int rpm) {
	return rpm * 60.0 / STEPS_REV;
}

void Focuser::turnOn() {
	stepper.enableOutputs();
	powerOn = true;
	isMoving = true;
}

void Focuser::turnOff() {
	stepper.disableOutputs();
	powerOn = false;
	isMoving = false;
	digitalWrite(LED1, LOW);
#ifdef LED2
	digitalWrite(LED2, LOW);
#endif
}
