#include "Focuser.h"

Focuser::Focuser() :
#if STEPPER_TYPE == 3
	stepper(AccelStepper::FULL4WIRE, DRIVER_IN1, DRIVER_IN3, DRIVER_IN2, DRIVER_IN4)
#else
	stepper(AccelStepper::DRIVER, DRIVER_STEP, DRIVER_DIR)
#endif
{
	stepper.setAcceleration(MOTOR_ACCEL);
	ledState = false;
	blinkStartTime = millis();
	lastMovementTime = blinkStartTime;
	powerOn = false;
	isMoving = false;
	isHcMoving = true;
}

void Focuser::begin(boolean initHoldControlEnabled,
                    uint8_t initSpeed,
                    long backlash,
					boolean reverseDir) {
	pinMode(FOK_LED1, OUTPUT);
#ifdef FOK_LED2
	pinMode(FOK_LED2, OUTPUT);
#endif
#ifdef DRIVER_EN
	stepper.setEnablePin(DRIVER_EN);
	stepper.setPinsInverted(reverseDir, false, true);
#else
	stepper.setPinsInverted(reverseDir, false, false);
#endif
#ifdef MODE0
	pinMode(MODE0, OUTPUT);
	digitalWrite(MODE0, HIGH);
#endif
#ifdef MODE1
	pinMode(MODE1, OUTPUT);
	digitalWrite(MODE1, HIGH);
#endif
#ifdef MODE2
	pinMode(MODE2, OUTPUT);
	digitalWrite(MODE2, HIGH);
#endif
	stepper.setBacklash(backlash);
	speed = initSpeed;
	applySpeed();
	holdControlEnabled = initHoldControlEnabled;
	turnOff();
}

void Focuser::begin() {
	begin(DEFAULT_ENABLE_HOLD_CONTROL, 80, 0, DEFAULT_DIRECTION_INVERT);
}

void Focuser::moveToTargetPos(long newPos) {
	pinMode(FOK_LED1, HIGH);
	isHcMoving = false;
	turnOn();
	applySpeed();
	stepper.moveTo(newPos);
}

void Focuser::move(long newPos) {
	pinMode(FOK_LED1, HIGH);
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
		digitalWrite(FOK_LED1, HIGH);
#ifdef FOK_LED2
		digitalWrite(FOK_LED2, HIGH);
#endif
	}
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
			return FS_MOVING;
		} else {
			isMoving = false;
			applySpeed();
			lastMovementTime = time;
#ifdef FOK_LED2
			digitalWrite(FOK_LED2, LOW);
#endif
			return FS_JUST_ARRIVED;
		}
	}
	if (powerOn) {
		// Turn power off if active time period has passed
		if (holdControlEnabled && ((time - lastMovementTime) >= DRIVER_POWER_TIMEOUT)) {
			turnOff();
		}
		return FS_HOLD_MOTOR;
	}
	if (time - blinkStartTime >= FOK_LED_BLINK_PERIOD) {
		blinkStartTime = time;
		ledState = !ledState;
		digitalWrite(FOK_LED1, ledState);
	}
	return FS_POWERSAVE;
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
#ifdef DRIVER_EN
	stepper.setPinsInverted(b, false, true);
#else
	stepper.setPinsInverted(b, false, false);
#endif
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
#ifdef FOK_LED2
			digitalWrite(FOK_LED1, LOW);
			digitalWrite(FOK_LED2, HIGH);
#else
			digitalWrite(FOK_LED1, HIGH);
#endif

		} else {
			stepper.move(steps);
			digitalWrite(FOK_LED1, HIGH);
#ifdef FOK_LED2
			digitalWrite(FOK_LED2, LOW);
#endif
		}
	}
}

void Focuser::applySpeed() {
	stepper.setMaxSpeed(map(speed, 0, 100, MOTOR_PPS_MIN, MOTOR_PPS_MAX));
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
	digitalWrite(FOK_LED1, LOW);
#ifdef FOK_LED2
	digitalWrite(FOK_LED2, LOW);
#endif
}
