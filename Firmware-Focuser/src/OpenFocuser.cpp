/**
   Moonlite-compatible focuser controller
   by marcocipriani01
   Copyright (c) Marco Cipriani, 2020

 ** Version 3.0 ***
   March 2020
 */

#include "OpenFocuser.h"
// Firmware version - 3.3
const String VERSION = "33";

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

// ----- Temperature compensation -----
#if ENABLE_TEMP_COMP == true
#if TEMP_SENSOR_TYPE == 1
#include <OneWire.h>
#include <DallasTemperature.h>
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature tempSensor(&oneWire);
float readTemperature() {
	if (!tempSensor.isConversionComplete()) {
		tempSensor.setWaitForConversion(true);
		tempSensor.requestTemperatures();
		tempSensor.setWaitForConversion(false);
	}
	float tempC = tempSensor.getTempCByIndex(DS18S20_INDEX);
	tempSensor.requestTemperatures();
	if (tempC == DEVICE_DISCONNECTED_C) {
		return ABSOLUTE_ZERO;
	}
	return tempC;
}
#endif
TemperatureCompensation tempCompensation(readTemperature);
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
	settings.microstepEnabled = focuser.isMicrostepEnabled();
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
#elif PROTOCOL == 2
	easyFocuser.begin();
#endif

#if ENABLE_FOCUSER == true
#if SETTINGS_SUPPORT == true
	loadSettings();
	if (settingsOk) {
		focuser.begin((boolean) settings.holdControlEnabled,
		              (boolean) settings.microstepEnabled,
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

#if ENABLE_TEMP_COMP == true
#if TEMP_SENSOR_TYPE == 1
	tempSensor.begin();
	tempSensor.setWaitForConversion(false);
#endif
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

#if PROTOCOL == 2
	easyFocuser.flagReady();
#endif
}

void handleSerial() {
#if PROTOCOL == 1
	if (Serial.available() && Serial.read() == ':') {
		clearBuffer(serialBuffer, SERIAL_BUFFER_LENGTH);
		Serial.readBytesUntil('#', serialBuffer, 8);
		MoonLiteCommand command = moonliteStringToEnum(serialBuffer);

		switch (command) {
#if ENABLE_FOCUSER == true
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

		case M_GOTO_NEW_POS: {

		}
		break;

		case M_IS_HALF_STEP: {
			Serial.print(focuser.isMicrostepEnabled() ? "FF#" : "00#");
		}
		break;

		case M_SET_FULL_STEP: {
			focuser.setMicrostepEnabled(false);
			flagSettings();
		}
		break;

		case M_SET_HALF_STEP: {
			focuser.setMicrostepEnabled(true);
			flagSettings();
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
			focuser.setSpeed(twoDecCharsToUint8(serialBuffer + 2));
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

#if ENABLE_TEMP_COMP == true
#if TEMP_SENSOR_TYPE == 1
		case M_INIT_TEMP_CONV: {
			tempSensor.requestTemperatures();
		}
		break;
#endif
#endif

		case M_GET_TEMP: {
#if ENABLE_TEMP_COMP == true
			char temperatureString[4];
			int16_t convertedTemp = (int16_t) tempCompensation.getTemperature();
			if (convertedTemp < 0) {
				convertedTemp = (~convertedTemp) + 1;
			}
			sprintf(temperatureString, "%04X", convertedTemp);
			Serial.print(temperatureString);
			Serial.print("#");
#else
			Serial.print("0000#");
#endif
		}
		break;

		case M_GET_TEMP_COEFF: {
#if ENABLE_TEMP_COMP == true
			char tempCoeffString[2];
			int8_t convertedCoeff = (int8_t) tempCompensation.getCompensationCoefficient();
			if (convertedCoeff < 0) {
				convertedCoeff = (~convertedCoeff) + 1;
			}
			sprintf(tempCoeffString, "%02X", convertedCoeff);
			Serial.print(tempCoeffString);
			Serial.print("#");
#else
			Serial.print("00#");
#endif
		}
		break;

#if ENABLE_TEMP_COMP == true
		case M_SET_TEMP_COEFF: {
			tempCompensation.setCompensationCoefficient(
				twoCharsToInt8(serialBuffer + 2));
		}
		break;

		case M_ENABLE_TEMP_COMP: {
			tempCompensation.enableCompensation();
		}
		break;

		case M_DISABLE_TEMP_COMP: {
			tempCompensation.disableCompensation();
		}
		break;

		case M_SET_TEMP_CAL_OFFSET: {
			tempCompensation.setTempetatureOffset(0.5 * ((float) twoCharsToInt8(serialBuffer + 2)));
		}
		break;
#endif
#endif

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

#if ENABLE_TEMP_COMP == true
	if (tempCompensation.manage() && !focuser.hasToRun()) {
		long correction = tempCompensation.getCompensatedMotorSteps();
		if (correction != 0) {
			focuser.move(correction);
		}
	}
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

#if ENABLE_DEVMAN
	managePFI();
#endif
}

inline void flagSettings() {
#if SETTINGS_SUPPORT == true
	needToSaveSettings = true;
#endif
}
