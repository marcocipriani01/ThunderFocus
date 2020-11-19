#ifndef FOCUSER_LIB_H
#define FOCUSER_LIB_H

#include <Arduino.h>
// Configuration
#include "FocuserConfig.h"
// Stepper driver libraries
#include <AccelStepper.h>

enum FocuserState
{
	FS_MOVING = (int)'M',
	FS_HOLD_MOTOR = (int)'H',
	FS_JUST_ARRIVED = (int)'A',
	FS_POWERSAVE = (int)'P'
};

class Focuser
{
public:
	Focuser();
	void begin(boolean initHoldControlEnabled,
			   uint8_t initSpeed,
			   long backlash,
			   boolean reverseDir);
	void begin();
	void moveToTargetPos(long newPos);
	void move(long newPos);
	long getTargetPos();
	void brake();
	void setCurrentPos(long newPos);
	long getCurrentPos();
	FocuserState run();
	boolean isRunning();
	void setHoldControlEnabled(boolean holdEnabledNew);
	boolean isHoldControlEnabled();
	boolean isPowerOn();
	void setSpeed(uint8_t speedNew);
	uint8_t getSpeed();
	void setDirReverse(boolean b);
	boolean getDirReverse();
	void setBacklash(long backlash);
	long getBacklash();
	void setHCSpeedInterval(unsigned int ppsMin, unsigned int ppsMax);
	void setHCMaxStepsPerPush(long maxStepsPerPushNew);
	void hCMove(unsigned int analogValue, boolean reverse);
	boolean hasJustStopped();

private:
	// Driver declaration
	AccelStepper stepper;
	boolean holdControlEnabled;
	unsigned long lastMovementTime;
	boolean powerOn;
	boolean isMoving;
	boolean isHcMoving;
	uint8_t speed;

	unsigned int hCMinPPS;
	unsigned int hCMaxPPS;
	long maxStepsPerPush;

	// ----- Status LED -----
	boolean ledState;
	unsigned long blinkStartTime;

	void applySpeed();
	int rpmToSpeed(int rpm);
	void turnOff();
	void turnOn();
};

#endif
