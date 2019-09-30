/**
   Moonlite-compatible focuser controller

 ** Version 2.0 **
    Rewritten.

 ** Version 1.1 **
    Decreased default acceleration.

 ** Version 1.0 **
    First release.

 ** Before release **
    Inspired by (http://orlygoingthirty.blogspot.co.nz/2014/04/arduino-based-motor-focuser-controller.html)
    Modified for INDI, easydriver by Cees Lensink
    Added sleep function by Daniel Franzén
    Modified to be used with the cheaper DRV8825 (or A4988) driver and almost completely rewritten by Marco Cipriani
    Added functions to set pin value by Marco Cipriani
    Hand controller support by Marco Cipriani
 */

#include <Arduino.h>

// Firmware version - 2.0
const String VERSION = "20";

// Configuration
#include "config.h"

// ----- Focuser motor -----
// Stepper driver libraries
// AccelStepper, used to provide motor acceleration
#include <AccelStepper.h>
#if STEPPER_TYPE == 0
#include <BasicStepperDriver.h>
#elif STEPPER_TYPE == 1
#include <DRV8825.h>
#elif STEPPER_TYPE == 2
#include <A4988.h>
#elif STEPPER_TYPE == 3
#include <DRV8834.h>
#endif

// The period to wait before turning off the driver (in milliseconds)
boolean isRunning = false;
#ifdef DRIVER_EN
#define TIMER_DELAY 30000
boolean isPowerOn = false;
#endif
#if STEPPER_TYPE != 0
boolean isHalfStep = false;
#endif

#ifdef DRIVER_EN
#if STEPPER_TYPE == 0
BasicStepperDriver driver(STEPS_REV, DRIVER_DIR, DRIVER_STEP, DRIVER_EN);
#elif STEPPER_TYPE == 1
DRV8825 driver(STEPS_REV, DRIVER_DIR, DRIVER_STEP, MODE0, MODE1, MODE2, DRIVER_EN);
#elif STEPPER_TYPE == 2
A4988 driver(STEPS_REV, DRIVER_DIR, DRIVER_STEP, MODE0, MODE1, MODE2, DRIVER_EN);
#elif STEPPER_TYPE == 3
DRV8834 driver(STEPS_REV, DRIVER_DIR, DRIVER_STEP, MODE0, MODE1, DRIVER_EN);
#endif
#else
#if STEPPER_TYPE == 0
BasicStepperDriver driver(STEPS_REV, DRIVER_DIR, DRIVER_STEP);
#elif STEPPER_TYPE == 1
DRV8825 driver(STEPS_REV, DRIVER_DIR, DRIVER_STEP, MODE0, MODE1, MODE2);
#elif STEPPER_TYPE == 2
A4988 driver(STEPS_REV, DRIVER_DIR, DRIVER_STEP, MODE0, MODE1, MODE2);
#elif STEPPER_TYPE == 3
DRV8834 driver(STEPS_REV, DRIVER_DIR, DRIVER_STEP, MODE0, MODE1);
#endif
#endif
#define SINGLE_STEP 1
// Can be 1, 2, 4, 8 or 16
#if STEPPER_TYPE != 0
#if STEPPER_TYPE == 2
#define FULL_STEP 8
#define HALF_STEP 16
#else
#define FULL_STEP 16
#define HALF_STEP 32
#endif
#endif

// Motor control wrappers
// Forward step
void goForward() {
	int n = SINGLE_STEP;
#if STEPPER_TYPE != 0
	if (isHalfStep) {
		n *= HALF_STEP;

	} else {
		n *= FULL_STEP;
	}
#endif
	driver.move(n);
}
// Backward step
void goBackward() {
	int n = SINGLE_STEP;
#if STEPPER_TYPE != 0
	if (isHalfStep) {
		n *= HALF_STEP;

	} else {
		n *= FULL_STEP;
	}
#endif
	driver.move(-n);
}
#if REVERSE_DIR == false
AccelStepper stepper(goForward, goBackward);
#else
AccelStepper stepper(goBackward, goForward);
#endif

// ----- Hand controller -----
#if ENABLE_HC == true
#include <ButtonDebounce.h>
#define BUTTONS_DEBOUNCE 60
ButtonDebounce buttonUp(BUTTON_UP, BUTTONS_DEBOUNCE);
ButtonDebounce buttonDown(BUTTON_DOWN, BUTTONS_DEBOUNCE);
// Buttons wrappers
// Button up
void buttonUpChanged(int state) {
	if (state == 1) {
		stepper.move(map(analogRead(HC_SPEED_POT), 0, 1023, 1, 1000));
	}
}
// Button down
void buttonDownChanged(int state) {
	if (state == 1) {
		stepper.move(-map(analogRead(HC_SPEED_POT), 0, 1023, 1, 1000));
	}
}
#endif

// ----- MoonLite protocol -----
#define CMD_LENGHT 8
char inChar;
char cmd[CMD_LENGHT];
char param[CMD_LENGHT];
char line[CMD_LENGHT];
boolean eoc = false;
int idx = 0;
long timerStartTime = 0;
char tempString[10];

// ----- Status LED -----
boolean ledState = false;
long blinkStartTime = 0;

void setup() {
	// Serial connection
	Serial.begin(SERIAL_SPEED);
	// Status LED
	pinMode(LED, OUTPUT);

	// ----- Motor driver -----
	// Ignore Moonlite speed
	driver.begin(MOTOR_RPM, FULL_STEP);
	//stepper.setSpeed(MOTOR_RPM * STEPS_REV / 60);
	//stepper.setMaxSpeed(MOTOR_PPS);
	stepper.setAcceleration(MOTOR_ACCEL);
	turnOff();

	blinkStartTime = millis();
	memset(line, 0, CMD_LENGHT);
}

void loop() {
	// Only have to do this if stepper is on
	if (isRunning) {
		stepper.run();
		if (stepper.distanceToGo() == 0) {
			// Start timer to decide when to power off the board
			timerStartTime = millis();
			isRunning = false;
		}
	}
#ifdef DRIVER_EN
	else if (isPowerOn) {
		// Turn power off if active time period has passed
		if (millis() - timerStartTime >= TIMER_DELAY) {
			turnOff();
		}
	}
#endif
	else {
		unsigned long currentMillis = millis();
		if (currentMillis - blinkStartTime >= BLINK_PERIOD) {
			blinkStartTime = currentMillis;
			ledState = !ledState;
			digitalWrite(LED, ledState);
		}
	}

#if ENABLE_HC == true
	buttonUp.update();
	buttonDown.update();
#endif

	// Read the command until the terminating # character
	while (Serial.available() && !eoc) {
		inChar = Serial.read();
		if (inChar != '#' && inChar != ':') {
			line[idx++] = inChar;
			if (idx >= CMD_LENGHT) {
				idx = CMD_LENGHT - 1;
			}

		} else {
			if (inChar == '#') {
				eoc = true;
			}
		}
	}

	// We may not have a complete command set but there is no character coming in for now and mightas well loop in case stepper needs updating
	// eoc will flag if a full command is there to act upon
	// Process the command we got
	if (eoc) {
		memset(cmd, 0, CMD_LENGHT);
		memset(param, 0, CMD_LENGHT);

		int len = strlen(line);
		if (len >= 2) {
			strncpy(cmd, line, 2);
		}
		if (len > 2) {
			strncpy(param, line + 2, len - 2);
		}

		memset(line, 0, CMD_LENGHT);
		eoc = false;
		idx = 0;

		// Execute the command
		// The stand-alone program sends :C# :GB# on startup
		// :C# is a temperature conversion, doesn't require any response

		if (!strcasecmp(cmd, "GV")) {
			Serial.print(VERSION + '#');
		}

		// LED backlight value, always return "00"
		if (!strcasecmp(cmd, "GB")) {
			Serial.print("00#");
		}

		if (!strcasecmp(cmd, "BT")) {
			Serial.print(BOARD_TYPE);
			Serial.print("#");
		}

		// Immediately stop any focus motor movement. Returns nothing
		if (!strcasecmp(cmd, "FQ")) {
			if (!isRunning) {
				turnOn();
			}
			// Stop as fast as possible
			stepper.stop();
			// Blocks until the target position is reached and stopped
			stepper.runToPosition();
		}

		// Go to the new position as set by the ":SNYYYY#" command. Returns nothing.
		// Turn stepper on and flag it is running
		// is this the only command that should actually make the stepper run ?
		if (!strcasecmp(cmd, "FG")) {
			if (!isRunning) {
				turnOn();
			}
		}

		// Returns the temperature coefficient where XX is a two-digit signed (2’s complement)  number.
		if (!strcasecmp(cmd, "GC")) {
			Serial.print("02#");
		}

		// Returns the current stepping delay where XX is a two-digit unsigned  number. See the :SD# command for a list of possible return values.
		// Might turn this into AccelStepper acceleration at some point
		if (!strcasecmp(cmd, "GD")) {
			Serial.print("02#");
		}

		// Returns "FF#" if the focus motor is half-stepped otherwise return "00#"
		if (!strcasecmp(cmd, "GH")) {
#if STEPPER_TYPE != 0
			if (isHalfStep) {
				Serial.print("FF#");

			} else {
				Serial.print("00#");
			}
#else
			Serial.print("00#");
#endif
		}

		// Returns "00#" if the focus motor is not moving, otherwise return "01#",
		// AccelStepper returns Positive as clockwise
		if (!strcasecmp(cmd, "GI")) {
			if (stepper.distanceToGo() == 0) {
				Serial.print("00#");

			} else {
				Serial.print("01#");
			}
		}

		// Returns the new position previously set by a ":SNYYYY" command where YYYY is a four-digit unsigned hex number.
		if (!strcasecmp(cmd, "GN")) {
			sprintf(tempString, "%04X", stepper.targetPosition());
			Serial.print(tempString);
			Serial.print("#");
		}

		// Returns the current position where YYYY is a four-digit unsigned hex number.
		if (!strcasecmp(cmd, "GP")) {
			sprintf(tempString, "%04X", stepper.currentPosition());
			Serial.print(tempString);
			Serial.print("#");
		}

		// Returns the current temperature where YYYY is a four-digit signed (2’s complement) hex number.
		if (!strcasecmp(cmd, "GT")) {
			Serial.print("0020#");
		}

		// Set the new temperature coefficient where XX is a two-digit, signed (2’s complement) hex number.
		if (!strcasecmp(cmd, "SC")) {

		}

		// Set the new stepping delay where XX is a two-digit,unsigned hex number.
		if (!strcasecmp(cmd, "SD")) {

		}

		// Set full-step mode.
#if STEPPER_TYPE != 0
		if (!strcasecmp(cmd, "SF")) {
			driver.setMicrostep(FULL_STEP);
			isHalfStep = false;
		}
#endif

		// Set half-step mode.
#if STEPPER_TYPE != 0
		if (!strcasecmp(cmd, "SH")) {
			driver.setMicrostep(HALF_STEP);
			isHalfStep = true;
		}
#endif

		// Set the new position where YYYY is a four-digit
		if (!strcasecmp(cmd, "SN")) {
			if (!isRunning) {
				turnOn();
			}
			stepper.moveTo(hexToLong(param));
		}

		// Set the current position, where YYYY is a four-digit unsigned hex number.
		if (!strcasecmp(cmd, "SP")) {

		}
	}
}

long hexToLong(char *line) {
	return strtol(line, NULL, 16);
}

void turnOn() {
#ifdef DRIVER_EN
	driver.enable();
	isPowerOn = true;
#endif
	digitalWrite(LED, HIGH);
	isRunning = true;
}

void turnOff() {
#ifdef DRIVER_EN
	driver.disable();
	isPowerOn = false;
#endif
	isRunning = false;
}
