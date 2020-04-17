// AccelStepper.cpp
//
// Copyright (C) 2009-2013 Mike McCauley
// $Id: AccelStepper.cpp,v 1.23 2016/08/09 00:39:10 mikem Exp $

#include "AccelStepper.h"

AccelStepper::AccelStepper(uint8_t stepPin, uint8_t dirPin, uint8_t enablePin) {
	_interface = DRIVER;
	_enablePin = enablePin;
	_stepPin_in1 = stepPin;
	_dirPin_in2 = dirPin;
	_m0_in3 = 0xff;
	_m1_in4 = 0xff;
	_m2 = 0xff;
	initVals();
}

AccelStepper::AccelStepper(void (*forward)(), void (*backward)()) {
	_interface = FUNCTION;
	_forward = forward;
	_backward = backward;
	_stepPin_in1 = 0;
	_dirPin_in2 = 0;
	_enablePin = 0xff;
	_m0_in3 = 0xff;
	_m1_in4 = 0xff;
	_m2 = 0xff;
	initVals();
}

AccelStepper::AccelStepper(uint8_t in1, uint8_t in2, uint8_t in3, uint8_t in4) {
	_interface = DRIVER_ULN2003;
	_stepPin_in1 = in1;
	_dirPin_in2 = in3;
	_m0_in3 = in2;
	_m1_in4 = in4;
	_m2 = 0xff;
	_enablePin = 0xff;
	initVals();
}

void AccelStepper::initVals() {
	_currentPos = 0;
	_targetPos = 0;
	_speed = 0.0;
	_maxSpeed = 1.0;
	_sqrt_twoa = 1.0;
	_stepInterval = 0;
	_minPulseWidth = 1;
	_lastStepTime = 0;
	_acceleration = 1.0;
	_n = 0;
	_c0 = 0.0;
	_cn = 0.0;
	_cmin = 1.0;
	_direction = DIRECTION_CCW;
	_stepPinInverted = false;
	_dirPinInverted = false;
	_microStepFraction = 1;
}

void AccelStepper::begin() {
	if (!_interface) {
		return;
	}
	pinMode(_stepPin_in1, OUTPUT);
	pinMode(_dirPin_in2, OUTPUT);
	if (_interface == DRIVER_ULN2003) {
		pinMode(_m0_in3, OUTPUT);
		pinMode(_m1_in4, OUTPUT);

	} else if (_enablePin != 0xff) {
		enableOutputs();
	}
}

void AccelStepper::setMicroStepping(uint8_t fraction, MicroSteppingDriver driver) {
	switch (_interface) {
	case FUNCTION: {
		return;
	}
	break;

	case DRIVER_ULN2003: {
		if (fraction == 1 || fraction == 2) {
			_microStepFraction = fraction;
		}
	}

	case DRIVER: {
		if ((_m0_in3 == 0xff) || (_m1_in4 == 0xff) || (_m2 == 0xff)) {
			return;
		}
		switch (driver) {
		case NONE: {
			_microStepFraction = fraction;
			return;
		}
		break;

		case DRV8825: {
			switch (fraction) {
			case 1: {
				digitalWrite(_m0_in3, LOW);
				digitalWrite(_m1_in4, LOW);
				digitalWrite(_m2, LOW);
			}
			break;

			case 2: {
				digitalWrite(_m0_in3, HIGH);
				digitalWrite(_m1_in4, LOW);
				digitalWrite(_m2, LOW);
			}
			break;

			case 4: {
				digitalWrite(_m0_in3, LOW);
				digitalWrite(_m1_in4, HIGH);
				digitalWrite(_m2, LOW);
			}
			break;

			case 8: {
				digitalWrite(_m0_in3, HIGH);
				digitalWrite(_m1_in4, HIGH);
				digitalWrite(_m2, LOW);
			}
			break;

			case 16: {
				digitalWrite(_m0_in3, LOW);
				digitalWrite(_m1_in4, LOW);
				digitalWrite(_m2, HIGH);
			}
			break;

			case 32: {
				digitalWrite(_m0_in3, HIGH);
				digitalWrite(_m1_in4, HIGH);
				digitalWrite(_m2, HIGH);
			}
			break;

			default: {
				return;
			}
			}
			_microStepFraction = fraction;
		}
		break;

		case A4988: {
			switch (fraction) {
			case 1: {
				digitalWrite(_m0_in3, LOW);
				digitalWrite(_m1_in4, LOW);
				digitalWrite(_m2, LOW);
			}
			break;

			case 2: {
				digitalWrite(_m0_in3, HIGH);
				digitalWrite(_m1_in4, LOW);
				digitalWrite(_m2, LOW);
			}
			break;

			case 4: {
				digitalWrite(_m0_in3, LOW);
				digitalWrite(_m1_in4, HIGH);
				digitalWrite(_m2, LOW);
			}
			break;

			case 8: {
				digitalWrite(_m0_in3, HIGH);
				digitalWrite(_m1_in4, HIGH);
				digitalWrite(_m2, LOW);
			}
			break;

			case 16: {
				digitalWrite(_m0_in3, HIGH);
				digitalWrite(_m1_in4, HIGH);
				digitalWrite(_m2, HIGH);
			}
			break;

			default: {
				return;
			}
			}
			_microStepFraction = fraction;
		}
		break;
		}
	}
	break;
	}
}

void AccelStepper::setMicroSteppingPins(uint8_t m0, uint8_t m1, uint8_t m2) {
	if (_interface != DRIVER) {
		return;
	}
	_m0_in3 = m0;
	pinMode(_m0_in3, OUTPUT);
	_m1_in4 = m1;
	pinMode(_m1_in4, OUTPUT);
	_m2 = m2;
	pinMode(_m2, OUTPUT);
}

void AccelStepper::moveTo(long absolute) {
	if (_targetPos != absolute) {
		_targetPos = absolute;
		computeNewSpeed();
	}
}

void AccelStepper::move(long relative) {
	moveTo(_currentPos + relative);
}

// Implements steps according to the current step interval
// You must call this at least once per step
// returns true if a step occurred
boolean AccelStepper::runSpeed() {
	// Dont do anything unless we actually have a step interval
	if (!_stepInterval) {
		return false;
	}
	unsigned long time = micros();
	if (time - _lastStepTime >= _stepInterval / _microStepFraction) {
		_currentPos += ((_direction == DIRECTION_CW) ? 1 : (-1));
		step(_currentPos);
		_lastStepTime = time; // Caution: does not account for costs in step()
		return true;
	}
	return false;
}

long AccelStepper::distanceToGo() {
	return _targetPos - _currentPos;
}

long AccelStepper::targetPosition() {
	return _targetPos;
}

long AccelStepper::currentPosition() {
	return _currentPos;
}

// Useful during initialisations or after initial positioning
// Sets speed to 0
void AccelStepper::setCurrentPosition(long position) {
	_targetPos = _currentPos = position;
	_n = 0;
	_stepInterval = 0;
	_speed = 0.0;
}

void AccelStepper::computeNewSpeed() {
	long distanceTo = distanceToGo();   // +ve is clockwise from curent location
	long stepsToStop = (long)((_speed * _speed) / (2.0 * _acceleration));   // Equation 16

	if (distanceTo == 0 && stepsToStop <= 1) {
		// We are at the target and its time to stop
		_stepInterval = 0;
		_speed = 0.0;
		_n = 0;
		return;
	}

	if (distanceTo > 0) {
		// We are anticlockwise from the target
		// Need to go clockwise from here, maybe decelerate now
		if (_n > 0) {
			// Currently accelerating, need to decel now? Or maybe going the wrong way?
			if ((stepsToStop >= distanceTo) || _direction == DIRECTION_CCW) {
				_n = -stepsToStop; // Start deceleration
			}

		} else if (_n < 0) {
			// Currently decelerating, need to accel again?
			if ((stepsToStop < distanceTo) && _direction == DIRECTION_CW) {
				_n = -_n; // Start accceleration
			}
		}

	} else if (distanceTo < 0) {
		// We are clockwise from the target
		// Need to go anticlockwise from here, maybe decelerate
		if (_n > 0) {
			// Currently accelerating, need to decel now? Or maybe going the wrong way?
			if ((stepsToStop >= -distanceTo) || _direction == DIRECTION_CW) {
				_n = -stepsToStop; // Start deceleration
			}

		} else if (_n < 0) {
			// Currently decelerating, need to accel again?
			if ((stepsToStop < -distanceTo) && _direction == DIRECTION_CCW)
				_n = -_n; // Start accceleration
		}
	}

	// Need to accelerate or decelerate
	if (_n == 0) {
		// First step from stopped
		_cn = _c0;
		_direction = (distanceTo > 0) ? DIRECTION_CW : DIRECTION_CCW;

	} else {
		// Subsequent step. Works for accel (n is +_ve) and decel (n is -ve).
		_cn = _cn - ((2.0 * _cn) / ((4.0 * _n) + 1)); // Equation 13
		_cn = max(_cn, _cmin);
	}
	_n++;
	_stepInterval = _cn;
	_speed = 1000000.0 / _cn;
	if (_direction == DIRECTION_CCW) {
		_speed = -_speed;
	}
}

// Run the motor to implement speed and acceleration in order to proceed to the target position
// You must call this at least once per step, preferably in your main loop
// If the motor is in the desired position, the cost is very small
// returns true if the motor is still running to the target position.
boolean AccelStepper::run() {
	if (runSpeed()) {
		computeNewSpeed();
	}
	return _speed != 0.0 || distanceToGo() != 0;
}

void AccelStepper::setMaxSpeed(float speed) {
	if (speed < 0.0) {
		speed = -speed;
	}
	if (_maxSpeed != speed) {
		_maxSpeed = speed;
		_cmin = 1000000.0 / speed;
		// Recompute _n from current speed and adjust speed if accelerating or cruising
		if (_n > 0) {
			_n = (long)((_speed * _speed) / (2.0 * _acceleration)); // Equation 16
			computeNewSpeed();
		}
	}
}

float AccelStepper::maxSpeed() {
	return _maxSpeed;
}

void AccelStepper::setAcceleration(float acceleration) {
	if (acceleration == 0.0) {
		return;
	}
	if (acceleration < 0.0) {
		acceleration = -acceleration;
	}
	if (_acceleration != acceleration) {
		// Recompute _n per Equation 17
		_n = _n * (_acceleration / acceleration);
		// New c0 per Equation 7, with correction per Equation 15
		_c0 = 0.676 * sqrt(2.0 / acceleration) * 1000000.0; // Equation 15
		_acceleration = acceleration;
		computeNewSpeed();
	}
}

void AccelStepper::setSpeed(float speed) {
	if (speed == _speed) {
		return;
	}
	speed = constrain(speed, -_maxSpeed, _maxSpeed);
	if (speed == 0.0) {
		_stepInterval = 0;

	} else {
		_stepInterval = fabs(1000000.0 / speed);
		_direction = (speed > 0.0) ? DIRECTION_CW : DIRECTION_CCW;
	}
	_speed = speed;
}

float AccelStepper::speed() {
	return _speed;
}

// Subclasses can override
void AccelStepper::step(long step) {
	switch (_interface) {
	case FUNCTION: {
		if (_speed > 0) {
			_forward();

		} else {
			_backward();
		}
	}
	break;

	case DRIVER: {
		setDirPin(_direction);
		setStepPin(HIGH);
		// Delay the minimum allowed pulse width
		delayMicroseconds(_minPulseWidth);
		setStepPin(LOW);
	}
	break;

	case DRIVER_ULN2003: {
		if (_microStepFraction == 1) {
			switch (step & 0x3) {
			case 0: // 1010
				set4Pins(1, 0, 1, 0);
				break;

			case 1: // 0110
				set4Pins(0, 1, 1, 0);
				break;

			case 2: //0101
				set4Pins(0, 1, 0, 1);
				break;

			case 3: //1001
				set4Pins(1, 0, 0, 1);
				break;
			}

		} else if (_microStepFraction == 2) {
			switch (step & 0x7) {
			case 0: // 1000
				set4Pins(1, 0, 0, 0);
				break;

			case 1: // 1010
				set4Pins(1, 0, 1, 0);
				break;

			case 2: // 0010
				set4Pins(0, 0, 1, 0);
				break;

			case 3: // 0110
				set4Pins(0, 1, 1, 0);
				break;

			case 4: // 0100
				set4Pins(0, 1, 0, 0);
				break;

			case 5: //0101
				set4Pins(0, 1, 0, 1);
				break;

			case 6: // 0001
				set4Pins(0, 0, 0, 1);
				break;

			case 7: //1001
				set4Pins(1, 0, 0, 1);
				break;
			}
		}
	}
	break;
	}
}

void AccelStepper::setDirPin(boolean val) {
	setStepPin(LOW);
	digitalWrite(_dirPin_in2, _dirPinInverted ? (!val) : val);
}

void AccelStepper::setStepPin(boolean val) {
	digitalWrite(_stepPin_in1, _stepPinInverted ? (!val) : val);
}

void AccelStepper::set4Pins(boolean one, boolean two, boolean three, boolean four) {
	digitalWrite(_stepPin_in1, one);
	digitalWrite(_dirPin_in2, two);
	digitalWrite(_m0_in3, three);
	digitalWrite(_m1_in4, four);
}

// Prevents power consumption on the outputs
void AccelStepper::disableOutputs() {
	switch (_interface) {
		case FUNCTION: {
			return;
		}

		case DRIVER: {
			setDirPin(LOW);
		}
		break;

		case DRIVER_ULN2003: {
			set4Pins(LOW, LOW, LOW, LOW);
		}
		break;
	}
	if (_enablePin != 0xff) {
		pinMode(_enablePin, OUTPUT);
		digitalWrite(_enablePin, LOW ^ _enableInverted);
	}
}

void AccelStepper::enableOutputs() {
	if ((_interface != FUNCTION) && (_enablePin != 0xff)) {
		pinMode(_enablePin, OUTPUT);
		digitalWrite(_enablePin, HIGH ^ _enableInverted);
	}
}

void AccelStepper::setMinPulseWidth(unsigned int minWidth) {
	_minPulseWidth = minWidth;
}

void AccelStepper::setEnablePin(uint8_t enablePin) {
	_enablePin = enablePin;
	// This happens after construction, so init pin now.
	if (_enablePin != 0xff) {
		pinMode(_enablePin, OUTPUT);
		digitalWrite(_enablePin, HIGH ^ _enableInverted);
	}
}

void AccelStepper::setPinsInverted(boolean dirPinInverted, boolean stepPinInverted, boolean enableInverted) {
	if (_interface == DRIVER) {
		_stepPinInverted = stepPinInverted;
		_dirPinInverted = dirPinInverted;
		_enableInverted = enableInverted;
	}
}

// Blocks until the target position is reached and stopped
void AccelStepper::runToPosition() {
	while (run()) {
		;
	}
}

boolean AccelStepper::runSpeedToPosition() {
	if (_targetPos == _currentPos) {
		return false;
	}
	if (_targetPos >_currentPos) {
		_direction = DIRECTION_CW;

	} else {
		_direction = DIRECTION_CCW;
	}
	return runSpeed();
}

// Blocks until the new target position is reached
void AccelStepper::runToNewPosition(long position) {
	moveTo(position);
	runToPosition();
}

void AccelStepper::stop() {
	if (_speed != 0.0) {
		long stepsToStop = (long)((_speed * _speed) / (2.0 * _acceleration)) + 1; // Equation 16 (+integer rounding)
		if (_speed > 0) {
			move(stepsToStop);

		} else {
			move(-stepsToStop);
		}
	}
}

bool AccelStepper::isRunning() {
	return !(_speed == 0.0 && _targetPos == _currentPos);
}
