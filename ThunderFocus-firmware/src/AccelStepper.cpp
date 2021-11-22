#include "AccelStepper.h"

AccelStepper::AccelStepper(uint8_t stepPin, uint8_t dirPin) {
    _currentPos = 0;
    _targetPos = 0;
    _speed = 0.0;
    _maxSpeed = 1.0;
    _acceleration = 1.0;
    _stepInterval = 0;
    _lastStepTime = 0;
    _stepPin = stepPin;
    pinMode(_stepPin, OUTPUT);
    _dirPin = dirPin;
    pinMode(_dirPin, OUTPUT);
#if ACCELSTEPPER_BACKLASH_SUPPORT == true
    _direction = DIRECTION_NONE;
    _currentBacklash = 0;
    _targetBacklash = 0;
#else
    _direction = DIRECTION_CW;
#endif
#if ACCELSTEPPER_INVERT_DIR_SUPPORT == true
    _invertDir = false;
#endif
    _n = 0;
    _cn = 0.0;
    _cmin = 1.0;
    _c0 = 0.676 * sqrt(2.0) * 1000000.0;
#if ACCELSTEPPER_EN_PIN_SUPPORT == true
    _enablePin = -1;
    _enabled = true;
#if ACCELSTEPPER_AUTO_POWER == true
    _autoPowerTimeout = 0;
#endif
#endif
#if ACCELSTEPPER_STEPS_SCALING == true
    _stepsScaling = 1;
#endif
}

#if ACCELSTEPPER_INVERT_DIR_SUPPORT == true
    boolean AccelStepper::isDirectionInverted() {
        return _invertDir;
    }

    void AccelStepper::setDirectionInverted(boolean inverted) {
        _invertDir = inverted;
    }
#endif

#if ACCELSTEPPER_STEPS_SCALING == true
    void AccelStepper::setStepsScaling(long stepsScaling) {
        if (stepsScaling > 0)
            _stepsScaling = stepsScaling;
    }

    long AccelStepper::getStepsScaling() {
        return _stepsScaling;
    }
#endif

#if ACCELSTEPPER_BACKLASH_SUPPORT == true
void AccelStepper::applyBacklashCompensation(Direction newDir) {
    if (_backlash == 0) {
        _direction = newDir;
        return;
    }
    if ((_direction != DIRECTION_NONE) && (newDir != _direction)) {
        if (newDir == DIRECTION_CW) {
            _targetBacklash += _backlash;
        } else if (newDir == DIRECTION_CCW) {
            _targetBacklash -= _backlash;
        }
    }
    _direction = newDir;
}
#endif

#if ACCELSTEPPER_EN_PIN_SUPPORT == true
void AccelStepper::setEnablePin(uint8_t enPin, boolean enabled) {
    _enablePin = enPin;
    if (_enablePin != -1) {
        pinMode(_enablePin, OUTPUT);
        if (distanceToGo() == 0) {
            _enabled = enabled;
            digitalWrite(_enablePin, !_enabled);
        }
    }
}

void AccelStepper::setEnabled(boolean enabled) {
    if (distanceToGo() != 0) return;
    _enabled = enabled;
    if (_enablePin != -1) digitalWrite(_enablePin, !_enabled);
}

boolean AccelStepper::isEnabled() {
    return _enabled;
}

#if ACCELSTEPPER_AUTO_POWER == true
void AccelStepper::setAutoPowerTimeout(unsigned long timeout) { _autoPowerTimeout = timeout; }

unsigned long AccelStepper::getAutoPowerTimeout() { return _autoPowerTimeout; }
#endif
#endif

void AccelStepper::moveTo(long absolute) {
#if ACCELSTEPPER_EN_PIN_SUPPORT == true
    if (!_enabled) return;
#endif
#if ACCELSTEPPER_STEPS_SCALING == true
    absolute *= _stepsScaling;
#endif
    if (_targetPos != absolute) {
        _targetPos = absolute;
        computeNewSpeed();
    }
}

void AccelStepper::move(long relative) {
#if ACCELSTEPPER_EN_PIN_SUPPORT == true
    if (!_enabled) return;
#endif
#if ACCELSTEPPER_STEPS_SCALING == true
    relative *= _stepsScaling;
#endif
    moveTo(_currentPos + relative);
}

boolean AccelStepper::runSpeed() {
#if ACCELSTEPPER_EN_PIN_SUPPORT == true && ACCELSTEPPER_AUTO_POWER == false
    if (!_enabled) return;
#endif
#if ACCELSTEPPER_AUTO_POWER == true
    unsigned long time = micros();
#endif
#if ACCELSTEPPER_BACKLASH_SUPPORT == true
    if ((!_stepInterval) || (_direction == DIRECTION_NONE)) {
#else
    if (!_stepInterval) {
#endif
#if ACCELSTEPPER_AUTO_POWER == true
        if (_enabled && (_autoPowerTimeout != 0) && (((unsigned long)(time - _lastStepTime)) >= (_autoPowerTimeout * 1000L))) {
            _enabled = false;
            if (_enablePin != -1) digitalWrite(_enablePin, true);
        }
#endif
        return false;
    }
#if ACCELSTEPPER_AUTO_POWER == true
    else if (!_enabled) {
        _enabled = true;
        if (_enablePin != -1) digitalWrite(_enablePin, false);
    }
#endif
#if ACCELSTEPPER_AUTO_POWER == false
    unsigned long time = micros();
#endif
    if (((unsigned long)(time - _lastStepTime)) >= _stepInterval) {
        boolean dir = (_direction == DIRECTION_CCW);
#if ACCELSTEPPER_INVERT_DIR_SUPPORT == true
        if (_invertDir) dir = !dir;
#endif
#if ACCELSTEPPER_BACKLASH_SUPPORT == true
        if ((_targetBacklash - _currentBacklash) == 0)
            _currentPos += (dir ? (-1) : 1);
        else
            _currentBacklash += (dir ? (-1) : 1);
#else
        _currentPos += (dir ? (-1) : 1);
#endif
        digitalWrite(_dirPin, dir);
        digitalWrite(_stepPin, HIGH);
#if ACCELSTEPPER_MIN_PULSE_WIDTH > 0
        delayMicroseconds(ACCELSTEPPER_MIN_PULSE_WIDTH);
#endif
        digitalWrite(_stepPin, LOW);
        _lastStepTime = time;
        return true;
    }
    return false;
}

long AccelStepper::distanceToGo() {
#if ACCELSTEPPER_BACKLASH_SUPPORT == true
#if ACCELSTEPPER_STEPS_SCALING == true
    double steps = (_targetPos - _currentPos + _targetBacklash - _currentBacklash) / ((double) _stepsScaling);
    return (steps > 0) ? ceil(steps) : floor(steps);
#else
    return _targetPos - _currentPos + _targetBacklash - _currentBacklash;
#endif
#else
#if ACCELSTEPPER_STEPS_SCALING == true
    double steps = (_targetPos - _currentPos) / ((double) _stepsScaling);
    return (steps > 0) ? ceil(steps) : floor(steps);
#else
    return _targetPos - _currentPos;
#endif
#endif
}

long AccelStepper::getTarget() {
#if ACCELSTEPPER_STEPS_SCALING == true
    return _targetPos / _stepsScaling;
#else
    return _targetPos;
#endif
}

long AccelStepper::getPosition() {
#if ACCELSTEPPER_STEPS_SCALING == true
    return _currentPos / _stepsScaling;
#else
    return _currentPos;
#endif
}

void AccelStepper::setPosition(long position) {
    if (distanceToGo() != 0) return;
#if ACCELSTEPPER_STEPS_SCALING == true
    position *= _stepsScaling;
#endif
    _targetPos = _currentPos = position;
    _targetBacklash = 0;
    _currentBacklash = 0;
    _stepInterval = 0;
    _speed = 0.0;
    _n = 0;
}

#if ACCELSTEPPER_BACKLASH_SUPPORT == true
void AccelStepper::setBacklash(long backlash) {
#if ACCELSTEPPER_STEPS_SCALING == true
    backlash *= _stepsScaling;
#endif
    _backlash = backlash;
    _targetBacklash = 0;
    _currentBacklash = 0;
}

long AccelStepper::getBacklash() {
#if ACCELSTEPPER_STEPS_SCALING == true
    return _backlash / _stepsScaling;
#else
    return _backlash;
#endif
}
#endif

void AccelStepper::computeNewSpeed() {
    long distanceTo = distanceToGo();
    long stepsToStop = (long)((_speed * _speed) / (2.0 * _acceleration));
    if (distanceTo == 0 && stepsToStop <= 1) {
        _stepInterval = 0;
        _speed = 0.0;
        _n = 0;
        return;
    }

    if (distanceTo > 0) {
        if (_n > 0) {
            // Currently accelerating, need to decel now? Or maybe going the wrong way?
            if ((stepsToStop >= distanceTo) || _direction == DIRECTION_CCW) _n = -stepsToStop;  // Start deceleration
        } else if (_n < 0) {
            // Currently decelerating, need to accel again?
            if ((stepsToStop < distanceTo) && _direction == DIRECTION_CW) _n = -_n;  // Start accceleration
        }
    } else if (distanceTo < 0) {
        // We are clockwise from the target
        // Need to go anticlockwise from here, maybe decelerate
        if (_n > 0) {
            // Currently accelerating, need to decel now? Or maybe going the wrong way?
            if ((stepsToStop >= -distanceTo) || _direction == DIRECTION_CW) _n = -stepsToStop;  // Start deceleration
        } else if (_n < 0) {
            // Currently decelerating, need to accel again?
            if ((stepsToStop < -distanceTo) && _direction == DIRECTION_CCW) _n = -_n;  // Start accceleration
        }
    }

    // Need to accelerate or decelerate
    if (_n == 0) {
        // First step from stopped
        _cn = _c0;
#if ACCELSTEPPER_BACKLASH_SUPPORT == true
        applyBacklashCompensation((distanceTo > 0) ? DIRECTION_CW : DIRECTION_CCW);
#else
        _direction = (distanceTo > 0) ? DIRECTION_CW : DIRECTION_CCW;
#endif
    } else {
        // Subsequent step. Works for accel (n is +_ve) and decel (n is -ve).
        _cn = _cn - ((2.0 * _cn) / ((4.0 * _n) + 1));  // Equation 13
        _cn = max(_cn, _cmin);
    }
    _n++;
    _stepInterval = _cn;
    _speed = 1000000.0 / _cn;
    if (_direction == DIRECTION_CCW) _speed = -_speed;
}

boolean AccelStepper::run() {
    if (runSpeed()) computeNewSpeed();
    return (_speed != 0.0) || (distanceToGo() != 0);
}

void AccelStepper::setMaxSpeed(double speed) {
    if (speed < 0.0) speed = -speed;
#if ACCELSTEPPER_STEPS_SCALING == true
    speed *= _stepsScaling;
#endif
    if (_maxSpeed != speed) {
        _maxSpeed = speed;
        _cmin = 1000000.0 / speed;
        if (_n > 0) {
            _n = (long)((_speed * _speed) / (2.0 * _acceleration));
            computeNewSpeed();
        }
    }
}

double AccelStepper::getMaxSpeed() {
#if ACCELSTEPPER_STEPS_SCALING == true
    return _maxSpeed / _stepsScaling;
#else
    return _maxSpeed;
#endif
}

void AccelStepper::setAcceleration(double acceleration) {
    if (acceleration == 0.0) return;
    if (acceleration < 0.0) acceleration = -acceleration;
#if ACCELSTEPPER_STEPS_SCALING == true
    acceleration *= _stepsScaling;
#endif
    if (_acceleration != acceleration) {
        _n = _n * (_acceleration / acceleration);
        _c0 = 0.676 * sqrt(2.0 / acceleration) * 1000000.0;
        _acceleration = acceleration;
        computeNewSpeed();
    }
}

void AccelStepper::setSpeed(double speed) {
    if (speed == _speed) return;
#if ACCELSTEPPER_STEPS_SCALING == true
    speed *= _stepsScaling;
#endif
    speed = constrain(speed, -_maxSpeed, _maxSpeed);
    if (speed == 0.0)
        _stepInterval = 0;
    else {
        _stepInterval = fabs(1000000.0 / speed);
        _direction = (speed > 0.0) ? DIRECTION_CW : DIRECTION_CCW;
    }
    _speed = speed;
}

double AccelStepper::getSpeed() {
#if ACCELSTEPPER_STEPS_SCALING == true
    return _speed / _stepsScaling;
#else
    return _speed;
#endif
}

void AccelStepper::runToPosition() {
    while (run())
        ;
}

boolean AccelStepper::runSpeedToPosition() {
    if (_targetPos == _currentPos) return false;
    if (_targetPos > _currentPos)
        _direction = DIRECTION_CW;
    else
        _direction = DIRECTION_CCW;
    return runSpeed();
}

void AccelStepper::runToNewPosition(long position) {
    moveTo(position);
    runToPosition();
}

void AccelStepper::stop() {
    if (_speed != 0.0) {
        long stepsToStop = (long)((_speed * _speed) / (2.0 * _acceleration)) + 1;  // Equation 16 (+integer rounding)
        move((_speed > 0) ? stepsToStop : (-stepsToStop));
    }
}
