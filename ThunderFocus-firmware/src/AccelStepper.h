#ifndef AccelStepper_h
#define AccelStepper_h

#include <Arduino.h>
#define ACCELSTEPPER_PULSE_WIDTH_MICROS 0
#define ACCELSTEPPER_BACKLASH_SUPPORT true
#define ACCELSTEPPER_ENABLE_SUPPORT true
#define ACCELSTEPPER_AUTO_POWER true
#define ACCELSTEPPER_STEPS_SCALING true
#define ACCELSTEPPER_INVERT_DIR_SUPPORT true
#define ACCELSTEPPER_28BYJ_48_STEPPER false
#define ACCELSTEPPER_28BYJ_48_HALF_STEPPING true

#if ACCELSTEPPER_AUTO_POWER == true && ACCELSTEPPER_ENABLE_SUPPORT == false
#error "Can't use auto-power without the enabled pin support."
#endif

class AccelStepper {
   public:
#if ACCELSTEPPER_28BYJ_48_STEPPER == true
    AccelStepper(uint8_t in1, uint8_t in2, uint8_t in3, uint8_t in4);
#else
    AccelStepper(uint8_t stepPin, uint8_t dirPin);
#endif

    void stop();
    void move(long relative);
    void moveTo(long absolute);

    boolean run();
    boolean runSpeed();
    void runToPosition();
    boolean runSpeedToPosition();
    void runToNewPosition(long position);

    double getMaxSpeed();
    void setMaxSpeed(double speed);
    void setAcceleration(double acceleration);
    void setSpeed(double speed);
    double getSpeed();

    long distanceToGo();
    long getTarget();
    long getPosition();
    void setPosition(long position);

#if ACCELSTEPPER_BACKLASH_SUPPORT == true
    void setBacklash(long backlash);
	long getBacklash();
#endif

#if ACCELSTEPPER_INVERT_DIR_SUPPORT == true
    boolean isDirectionInverted();
    void setDirectionInverted(boolean inverted);
#endif

#if ACCELSTEPPER_ENABLE_SUPPORT == true
#if ACCELSTEPPER_28BYJ_48_STEPPER == false
    void setEnablePin(uint8_t enPin, boolean enabled);
#endif
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
    long distanceToGo0();

#if ACCELSTEPPER_28BYJ_48_STEPPER == true
    void setOutputPins(uint8_t mask);
#endif

#if ACCELSTEPPER_28BYJ_48_STEPPER == true
    uint8_t _in1;
    uint8_t _in2;
    uint8_t _in3;
    uint8_t _in4;
#else
    uint8_t _dirPin;
    uint8_t _stepPin;
#endif

#if ACCELSTEPPER_INVERT_DIR_SUPPORT == true
    boolean _invertDir;
#endif

    long _currentPos;
    long _targetPos;
    double _speed;
    double _maxSpeed;

#if ACCELSTEPPER_BACKLASH_SUPPORT == true
    long _backlash;
	long _currentBacklash;
	long _targetBacklash;
#endif

#if ACCELSTEPPER_ENABLE_SUPPORT == true
#if ACCELSTEPPER_28BYJ_48_STEPPER == false
    uint8_t _enablePin;
#endif
    boolean _enabled;
#if ACCELSTEPPER_AUTO_POWER == true
    unsigned long _autoPowerTimeout;
#endif
#endif

#if ACCELSTEPPER_STEPS_SCALING == true
    long _stepsScaling;
#endif

    double _acceleration;
    unsigned long _stepInterval;
    unsigned long _lastStepTime;
    long _n;
    double _c0;
    double _cn;
    double _cmin;
};

#endif
