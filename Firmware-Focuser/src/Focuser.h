#ifndef FOCUSER_LIB_H
#define FOCUSER_LIB_H

#include <Arduino.h>
// Configuration
#include "FocuserConfig.h"
// Stepper driver libraries
#include <AccelStepper.h>

#if STEPPER_TYPE == 0
#define FULL_STEP 1
#define HALF_STEP 1
#elif STEPPER_TYPE == 1
#define MODE0 6
#define MODE1 5
#define MODE2 4
#define FULL_STEP 16
#define HALF_STEP 32
#elif STEPPER_TYPE == 2
#define MODE0 6
#define MODE1 5
#define MODE2 4
#define FULL_STEP 8
#define HALF_STEP 16
#elif STEPPER_TYPE == 3
#define FULL_STEP 1
#define HALF_STEP 2
#endif

enum Speed {
  FOCSPEED_FASTEST = 2, FOCSPEED_FAST = 4, FOCSPEED_MID = 8,
  FOCSPEED_SLOW = 10, FOCSPEED_SLOWEST = 20
};

class Focuser {
public:
  Focuser();
  void begin(boolean initHoldControlEnabled, boolean initMicrostepEnabled, uint8_t initSpeed);
  void begin();
  void moveToTargetPos(long newPos);
  void move(long newPos);
  long getTargetPos();
  void brake();
  void setCurrentPos(long newPos);
  unsigned long getCurrentPos();
  uint8_t run();
  boolean hasToRun();
  void setHoldControlEnabled(boolean holdEnabledNew);
  boolean isHoldControlEnabled();
  boolean isPowerOn();
  void setMicrostepEnabled(boolean microstepEnabledNew);
  boolean isMicrostepEnabled();
  void setSpeed(Speed speedNew);
  void setSpeed(uint8_t speedNew);
  Speed getSpeed();
  void setHCSpeedInterval(unsigned int rpmMinNew, unsigned int rpmMaxNew);
  void setHCMaxStepsPerPush(unsigned long maxStepsPerPushNew);
  void hCMove(unsigned int analogValue, boolean reverse);
  boolean hasJustStopped();

private:
  // Driver declaration
  AccelStepper stepper;
  boolean microstepEnabled;
  boolean holdControlEnabled;
  unsigned long lastMovementTime;
  boolean powerOn;
  boolean isMoving;
  boolean isHcMoving;
  boolean justStopped;
  // Valid options are 2, 4, 8, 10 and 20
  // 2 is the fastest, 20 the slowest
  Speed speed;

  unsigned int hCRpmMin;
  unsigned int hCRpmMax;
  unsigned long maxStepsPerPush;

  // ----- Status LED -----
  boolean ledState;
  unsigned long blinkStartTime;

  void applySpeed();
  int rpmToSpeed(int rpm);
  void turnOff();
  void turnOn();
  uint8_t getMicrostepGear();
  void applyMicrostepGear();
  long stepsToSteps(long steps);
  long stepsToStepsRev(long steps);
  Speed valToSpeed(uint8_t val);
};

#endif
