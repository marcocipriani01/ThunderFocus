#include "Focuser.h"

Focuser::Focuser() :
#if FOK1_STEPPER == DRIVER_POLOLU
	stepper(AccelStepper::DRIVER, FOK1_STEP, FOK1_DIR)
#elif FOK1_STEPPER == DRIVER_ULN2003
	stepper(AccelStepper::FULL4WIRE, FOK1_IN1, FOK1_IN2, FOK1_IN3, FOK1_IN4)
#else
#error Unsupported focuser driver
#endif
{
	stepper.setAcceleration(FOK1_ACCEL);
	lastMovementTime = millis();
	powerOn = false;
	isMoving = false;
	isHcMoving = true;
}

void Focuser::begin(boolean initHoldControlEnabled,
                    uint8_t initSpeed,
                    long backlash,
					boolean reverseDir) {
	stepper.setMinPulseWidth(0);
#ifdef FOK1_EN
	stepper.setEnablePin(FOK1_EN, true);
#endif
	stepper.setDirectionInverted(reverseDir);
#ifdef FOK1_MODE0
	pinMode(FOK1_MODE0, OUTPUT);
	digitalWrite(FOK1_MODE0, HIGH);
#endif
#ifdef FOK1_MODE1
	pinMode(FOK1_MODE1, OUTPUT);
	digitalWrite(FOK1_MODE1, HIGH);
#endif
#ifdef FOK1_MODE2
	pinMode(FOK1_MODE2, OUTPUT);
	digitalWrite(FOK1_MODE2, HIGH);
#endif
	stepper.setBacklash(backlash);
	speed = initSpeed;
	applySpeed();
	holdControlEnabled = initHoldControlEnabled;
	turnOff();
}

void Focuser::begin() {
	begin(FOK1_HOLD_CONTROL, 80, 0, FOK1_DIR_INVERT);
}

void Focuser::moveToTargetPos(long newPos) {
	isHcMoving = false;
	turnOn();
	applySpeed();
	stepper.moveTo(newPos);
}

void Focuser::move(long newPos) {
	isHcMoving = false;
	turnOn();
	applySpeed();
	stepper.move(newPos);
}

long Focuser::getTargetPos() {
	return stepper.targetPosition();
}

void Focuser::brake() {
	if (isMoving) stepper.stop();
}

void Focuser::setCurrentPos(long newPos) {
	if (!isMoving) {
		stepper.setCurrentPosition(newPos);
	}
}

long Focuser::getCurrentPos() {
	return stepper.currentPosition();
}

FocuserState Focuser::run() {
	unsigned long time = millis();
	if (isMoving) {
		if (stepper.run()) {
			return FocuserState::FOCUSER_MOVING;
		} else {
			isMoving = false;
			applySpeed();
			lastMovementTime = time;
			return FocuserState::FOCUSER_ARRIVED;
		}
	}
	if (powerOn) {
		// Turn power off if active time period has passed
		if (holdControlEnabled && ((time - lastMovementTime) >= FOK1_POWER_TIMEOUT)) {
			turnOff();
		}
		return FocuserState::FOCUSER_HOLD;
	}
	return FocuserState::FOCUSER_POWERSAVE;
}

boolean Focuser::isRunning() {
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

void Focuser::setDirReverse(boolean b) {
	stepper.setDirectionInverted(b);
}

boolean Focuser::getDirReverse() {
	return stepper.isDirectionInverted();
}

void Focuser::setBacklash(long backlash) {
	stepper.setBacklash(backlash);
}

long Focuser::getBacklash() {
	return stepper.getBacklash();
}

void Focuser::setHCSpeedInterval(unsigned int ppsMin, unsigned int ppsMax) {
	hCMinPPS = ppsMin;
	hCMaxPPS = ppsMax;
}

void Focuser::setHCMaxStepsPerPush(long maxStepsPerPushNew) {
	maxStepsPerPush = maxStepsPerPushNew;
}

void Focuser::hCMove(unsigned int analogValue, boolean reverse) {
	if (isHcMoving || !isMoving) {
		stepper.setMaxSpeed(map(analogValue, 0, 1023, hCMinPPS, hCMaxPPS));
		long steps = map(analogValue, 0, 1023, 1, maxStepsPerPush);
		turnOn();
		isHcMoving = true;
		if (reverse) {
			stepper.move(-steps);
		} else {
			stepper.move(steps);
		}
	}
}

void Focuser::applySpeed() {
	stepper.setMaxSpeed(map(speed, 0, 100, FOK1_PPS_MIN, FOK1_PPS_MAX));
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
}