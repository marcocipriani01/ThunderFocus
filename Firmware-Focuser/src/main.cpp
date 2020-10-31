/**
   Moonlite-compatible focuser controller
   by marcocipriani01
   Copyright (c) Marco Cipriani, 2020

** Version 4.0 ***
   November 2020

 ** Version 3.0 ***
   March 2020
 */

#include "main.h"
// Firmware version - 4.0
const String VERSION = "40";

// Focuser
#if ENABLE_FOCUSER == true
Focuser focuser;
#endif

#if PROTOCOL == 1
// Size for the serial buffer
#define SERIAL_BUFFER_LENGTH 8
// Serial commands are stored in this buffer for parsing
char serialBuffer[SERIAL_BUFFER_LENGTH];
#elif PROTOCOL == 2
EasyFocuser easyFocuser(&focuser);
#endif

// Hand controller
#if ENABLE_HC == true
HandController hc(&focuser);
#endif

#if SETTINGS_SUPPORT == true
// Settings data for EEPROM storage
struct Settings settings;
// Flag for requesting settings to be saved
boolean needToSaveSettings = false;
boolean settingsOk = false;

void loadSettings() {
	uint8_t* dst = (uint8_t*)&settings;
	for (unsigned int i = 0; i < sizeof(Settings); i++) {
		dst[i] = EEPROM.read(i);
	}
	settingsOk = settings.marker == EEPROM_MARKER;
}

void saveSettings() {
	settings.marker = EEPROM_MARKER;
	settings.currentPosition = focuser.getCurrentPos();
	settings.speed = focuser.getSpeed();
	settings.holdControlEnabled = focuser.isHoldControlEnabled();
	settings.backlash = focuser.getBacklash();
	uint8_t* dst = (uint8_t*)&settings;
	for (unsigned int i = 0; i < sizeof(Settings); ++i) {
		EEPROM.update(i, dst[i]);
	}
}
#endif

void setup() {
	// Serial connection
	Serial.begin(SERIAL_SPEED);
	Serial.setTimeout(SERIAL_TIMEOUT);
#if PROTOCOL == 1
	clearBuffer(serialBuffer, 8);
#endif

#if ENABLE_FOCUSER == true
#if SETTINGS_SUPPORT == true
	loadSettings();
	if (settingsOk) {
		focuser.begin((boolean) settings.holdControlEnabled,
		              settings.speed,
		              (long) settings.backlash);
		if (settings.currentPosition != 0) {
			focuser.setCurrentPos((unsigned long) settings.currentPosition);
		}

	} else {
		focuser.begin();
	}
#else
	focuser.begin();
#endif

#if ENABLE_HC == true
	hc.begin();
#endif
#endif

#if ENABLE_DEVMAN == true
	beginDevMan();
#endif

#if defined(__AVR_ATmega32U4__) || defined(__AVR_ATmega16U4__)
	while (!Serial) {
		;
	}
#endif
}

void handleSerial() {
#if PROTOCOL == 1
	if (Serial.available() && Serial.read() == ':') {
		clearBuffer(serialBuffer, SERIAL_BUFFER_LENGTH);
		Serial.readBytesUntil('#', serialBuffer, 8);
		MoonLiteCommand command = moonliteStringToEnum(serialBuffer);

		switch (command) {
		case M_STOP: {
			focuser.brake();
		}
		break;

		case M_GET_CURRENT_POS: {
			char currentPosString[4];
			sprintf(currentPosString, "%04X", (unsigned int) focuser.getCurrentPos());
			Serial.print(currentPosString);
			Serial.print("#");
		}
		break;

		case M_SET_CURRENT_POS: {
			focuser.setCurrentPos(fourCharsToUint16(serialBuffer + 2));
			flagSettings();
		}
		break;

		case M_GET_NEW_POS: {
			char newPositionString[4];
			sprintf(newPositionString, "%04X", (unsigned int) focuser.getTargetPos());
			Serial.print(newPositionString);
			Serial.print("#");
		}
		break;

		case M_SET_NEW_POS: {
			focuser.moveToTargetPos((unsigned long) fourCharsToUint16(serialBuffer + 2));
		}
		break;

		case M_IS_HALF_STEP: {
			Serial.print("FF#");
		}
		break;

		case M_IS_MOVING: {
			Serial.print(focuser.hasToRun() ? "01#" : "00#");
		}
		break;

		case M_GET_SPEED: {
			char speedString[2];
			sprintf(speedString, "%02X", focuser.getSpeed());
			Serial.print(speedString);
			Serial.print("#");
		}
		break;

		case M_SET_SPEED: {
			focuser.setSpeed(map(twoDecCharsToUint8(serialBuffer + 2), 2, 20, 100, 10));
			flagSettings();
		}
		break;

		case M_ENABLE_HOLD: {
			focuser.setHoldControlEnabled(true);
			flagSettings();
		}
		break;

		case M_DISABLE_HOLD: {
			focuser.setHoldControlEnabled(false);
			flagSettings();
		}
		break;

		case M_GET_TEMP: {
			Serial.print("0000#");
		}
		break;

		case M_GET_TEMP_COEFF: {
			Serial.print("0000#");
		}
		break;

#if ENABLE_DEVMAN == true
		case M_SET_PIN: {
			long pinCmd = fourCharsToUint16(serialBuffer + 2);
			if (pinCmd > 2000) {
				int pin = pinCmd / 1000;
				updatePin(pin, pinCmd - (pin * 1000));
			}
		}
		break;

		case M_RESET_PINS: {
			resetPins();
		}
		break;
#endif

		case M_FIRMWARE_VERSION: {
			Serial.print(VERSION + '#');
		}
		break;

		case M_BACKLIGHT_VALUE: {
			Serial.print("00#");
		}
		break;

		case M_GET_BOARD_TYPE: {
			Serial.print(BOARD_TYPE);
			Serial.print("#");
		}
		break;

		case M_UNRECOGNIZED: {
			break;
		}

		default: {
			break;
		}
		}
	}
#elif PROTOCOL == 2
	easyFocuser.manage();
#endif
}

void loop() {
	handleSerial();

#if ENABLE_FOCUSER == true
	FocuserState state = focuser.run();
	if (state == FS_JUST_ARRIVED) {
		flagSettings();
	}
#if PROTOCOL == 2
	easyFocuser.flagState(state);
#endif

#if ENABLE_HC == true
	hc.manage();
#endif

#if SETTINGS_SUPPORT == true
	if (needToSaveSettings) {
		saveSettings();
		needToSaveSettings = false;
	}
#endif
#endif

#if ENABLE_DEVMAN == true
	managePFI();
#endif
}

inline void flagSettings() {
#if SETTINGS_SUPPORT == true
	needToSaveSettings = true;
#endif
}
