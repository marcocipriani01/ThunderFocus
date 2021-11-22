#ifndef AccelStepper_h
#define AccelStepper_h

#include <Arduino.h>
#define ACCELSTEPPER_PULSE_WIDTH_MICROS 1
#define ACCELSTEPPER_BACKLASH_SUPPORT true
#define ACCELSTEPPER_EN_PIN_SUPPORT true
#define ACCELSTEPPER_AUTO_POWER true
#define ACCELSTEPPER_STEPS_SCALING true

#if ACCELSTEPPER_AUTO_POWER == true && ACCELSTEPPER_EN_PIN_SUPPORT == false
#error "Can't use auto-power without the enabled pin support."
#endif

class AccelStepper {
   public:
    AccelStepper(uint8_t stepPin, uint8_t dirPin);

    void stop();
    void move(long relative);
    void moveTo(long absolute);

    boolean run();
    boolean runSpeed();
    void runToPosition();
    boolean runSpeedToPosition();
    void runToNewPosition(long position);

    float getMaxSpeed();
    void setMaxSpeed(float speed);
    void setAcceleration(float acceleration);
    void setSpeed(float speed);
    float getSpeed();

    long distanceToGo();
    long getTarget();
    long getPosition();
    void setPosition(long position);

#if ACCELSTEPPER_BACKLASH_SUPPORT == true
    void setBacklash(long backlash);
	long getBacklash();
#endif

    boolean isDirectionInverted();
    void setDirectionInverted(boolean b);

#if ACCELSTEPPER_EN_PIN_SUPPORT == true
    void setEnablePin(uint8_t enPin, boolean enabled);
    void setEnabled(boolean enabled);
    boolean isEnabled();
#if ACCELSTEPPER_AUTO_POWER == true
    void setAutoPowerTimeout(unsigned long timeout);
    unsigned long getAutoPowerTimeout();
#endif
#endif

#if ACCELSTEPPER_STEPS_SCALING == true
    void setStepsScaling(long stepsScaling);
    long getStepsScaling();
#endif

   protected:
    typedef enum {
#if ACCELSTEPPER_BACKLASH_SUPPORT == true
        DIRECTION_NONE,
#endif
        // Counter-Clockwise
        DIRECTION_CCW,
        // Clockwise
        DIRECTION_CW
    } Direction;
    Direction _direction;

    void computeNewSpeed();

#if ACCELSTEPPER_BACKLASH_SUPPORT == true
    void applyBacklashCompensation(Direction newDir);
#endif

   private:
    uint8_t _dirPin;
    uint8_t _stepPin;

    boolean _invertDir;

    long _currentPos;
    long _targetPos;
    float _speed;
    float _maxSpeed;

#if ACCELSTEPPER_BACKLASH_SUPPORT == true
    long _backlash;
	long _currentBacklash;
	long _targetBacklash;
#endif

#if ACCELSTEPPER_EN_PIN_SUPPORT == true
    uint8_t _enablePin;
    boolean _enabled;
#if ACCELSTEPPER_AUTO_POWER == true
    unsigned long _autoPowerTimeout;
#endif
#endif

#if ACCELSTEPPER_STEPS_SCALING == true
    long _stepsScaling;
#endif

    float _acceleration;
    unsigned long _stepInterval;
    unsigned long _lastStepTime;
    long _n;
    float _c0;
    float _cn;
    float _cmin;
};

#endif
