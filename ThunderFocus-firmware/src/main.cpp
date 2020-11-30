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

Focuser focuser;

#if PROTOCOL == PROTOCOL_MOONLITE
// Size for the serial buffer
#define SERIAL_BUFFER_LENGTH 8
// Serial commands are stored in this buffer for parsing
char serialBuffer[SERIAL_BUFFER_LENGTH];
#endif

// Hand controller
#if FOK1_ENABLE_HC == true
HandController hc(&focuser);
#endif

#if SETTINGS_SUPPORT == true
// Settings data for EEPROM storage
struct Settings settings;
// Flag for requesting settings to be saved
boolean needToSaveSettings = false;

void loadSettings() {
	uint8_t* dst = (uint8_t*)&settings;
	for (unsigned int i = 0; i < sizeof(Settings); i++) {
		dst[i] = EEPROM.read(i);
	}
	if (settings.marker == EEPROM_VERSION) {
		focuser.begin((boolean) settings.fok1HoldControl, settings.fok1Speed,
			settings.fok1Backlash, settings.fok1Reverse);
		if (settings.fok1Pos != 0) {
			focuser.setCurrentPos(settings.fok1Pos);
		}
#if ENABLE_DEVMAN == true
		for (uint8_t i = 0; i < getManagedPinsCount(); i++) {
			Pin pin = settings.devManPins[i];
			if (pin.autoModeEn) {
				setPinAutoMode(pin.number, true);
			} else {
				updatePin(pin.number, pin.value);
			}
		}
		setDevManAutoMode(settings.devManAutoMode);
#endif
#if TIME_CONTROL == true
		setWorldCoord(settings.worldLat, settings.worldLong);
#endif
	} else {
		settings.marker = EEPROM_VERSION;
		focuser.begin();
	}
}

void saveSettings() {
	settings.marker = EEPROM_VERSION;
	settings.fok1Pos = focuser.getCurrentPos();
	settings.fok1Speed = focuser.getSpeed();
	settings.fok1HoldControl = focuser.isHoldControlEnabled();
	settings.fok1Backlash = focuser.getBacklash();
	settings.fok1Reverse = focuser.getDirReverse();
#if ENABLE_DEVMAN == true
	for (uint8_t i = 0; i < getManagedPinsCount(); i++) {
		settings.devManPins[i] = getManagedPin(i);
	}
	settings.devManAutoMode = getDevManAutoMode();
#endif
#if TIME_CONTROL == true
	settings.worldLat = getWorldLat();
	settings.worldLong = getWorldLong();
#endif
	uint8_t* dst = (uint8_t*) &settings;
	for (unsigned int i = 0; i < sizeof(Settings); ++i) {
		EEPROM.update(i, dst[i]);
	}
}
#endif

void setup() {
	// Serial port and protocol
	Serial.begin(SERIAL_SPEED);
	Serial.setTimeout(SERIAL_TIMEOUT);
#if PROTOCOL == PROTOCOL_MOONLITE
	clearBuffer(serialBuffer, 8);
#endif

#if ENABLE_DEVMAN == true
	beginDevMan();
#endif

#if SETTINGS_SUPPORT == true
	loadSettings();
#else
	focuser.begin();
#endif

#if FOK1_ENABLE_HC == true
	hc.begin();
#endif

#if defined(__AVR_ATmega32U4__)
	while (!Serial) {
		;
	}
#endif
}

void loop() {
#if PROTOCOL == PROTOCOL_THUNDERFOCUS
	if (thunderFocusManage(&focuser) == FocuserState::FOCUSER_ARRIVED) {
		flagSettings();
	}
#else
	if (focuser.run() == FocuserState::FOCUSER_ARRIVED) {
		flagSettings();
	}
	handleSerial();
#if ENABLE_DEVMAN == true
	devManage();
#endif
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
}

#if PROTOCOL == PROTOCOL_MOONLITE
void handleSerial() {
	if (Serial.available() && Serial.read() == ':') {
		clearBuffer(serialBuffer, SERIAL_BUFFER_LENGTH);
		Serial.readBytesUntil('#', serialBuffer, 8);
		MoonLiteCommand command = moonliteStringToEnum(serialBuffer);
		switch (command) {
			case M_STOP: {
				focuser.brake();
				break;
			}

			case M_GET_CURRENT_POS: {
				char currentPosString[4];
				sprintf(currentPosString, "%04X", (unsigned int) focuser.getCurrentPos());
				Serial.print(currentPosString);
				Serial.print("#");
				break;
			}

			case M_SET_CURRENT_POS: {
				focuser.setCurrentPos(fourCharsToUint16(serialBuffer + 2));
				flagSettings();
				break;
			}

			case M_GET_NEW_POS: {
				char newPositionString[4];
				sprintf(newPositionString, "%04X", (unsigned int) focuser.getTargetPos());
				Serial.print(newPositionString);
				Serial.print("#");
				break;
			}

			case M_SET_NEW_POS: {
				focuser.moveToTargetPos((unsigned long) fourCharsToUint16(serialBuffer + 2));
				break;
			}

			case M_IS_HALF_STEP: {
				Serial.print("FF#");
				break;
			}

			case M_IS_MOVING: {
				Serial.print(focuser.isRunning() ? "01#" : "00#");
				break;
			}

			case M_GET_SPEED: {
				char speedString[2];
				sprintf(speedString, "%02X", focuser.getSpeed());
				Serial.print(speedString);
				Serial.print("#");
				break;
			}

			case M_SET_SPEED: {
				focuser.setSpeed(map(twoDecCharsToUint8(serialBuffer + 2), 2, 20, 100, 10));
				flagSettings();
				break;
			}

			case M_ENABLE_HOLD: {
				focuser.setHoldControlEnabled(true);
				flagSettings();
				break;
			}

			case M_DISABLE_HOLD: {
				focuser.setHoldControlEnabled(false);
				flagSettings();
				break;
			}

			case M_GET_TEMP: {
				Serial.print("0000#");
				break;
			}

			case M_GET_TEMP_COEFF: {
				Serial.print("0000#");
				break;
			}
			
			case M_FIRMWARE_VERSION: {
				Serial.print(VERSION + '#');
				break;
			}

			case M_BACKLIGHT_VALUE: {
				Serial.print("00#");
				break;
			}
		}
	}
}
#endif

inline void flagSettings() {
#if SETTINGS_SUPPORT == true
	needToSaveSettings = true;
#endif
}

#if PROTOCOL == PROTOCOL_THUNDERFOCUS
void serialEvent() {
	thunderFocusSerialEvent(&focuser);
}
#endif