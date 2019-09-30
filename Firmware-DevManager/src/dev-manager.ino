/**
   INDI compatible device manager.

	 ** Version 1.0 **
	 	First release.
 */

#include <Arduino.h>

// Firmware version - 1.0
const String VERSION = "10";

// Configuration
#include "config.h"

// ----- Serial protocol -----
#define CMD_LENGHT 8
char inChar;
char cmd[CMD_LENGHT];
char param[CMD_LENGHT];
char line[CMD_LENGHT];
boolean eoc = false;
int idx = 0;

// ----- Customizable pins -----
int customPins[] = CUSTOMIZABLE_PINS;

// ----- Status LED -----
boolean ledState = false;
long blinkStartTime = 0;

void setup() {
	// Serial connection
	Serial.begin(SERIAL_SPEED);
	// Status LED
	pinMode(LED, OUTPUT);

	// Customizable pins
	for (int i = 0; i < (sizeof(customPins) / sizeof(*customPins)); i++) {
		pinMode(customPins[i], OUTPUT);
	}

	// Polar finder illuminator
#if ENABLE_PFI == true
	pinMode(PFI_LED, OUTPUT);
#endif

	blinkStartTime = millis();
	memset(line, 0, CMD_LENGHT);
}

void loop() {
	unsigned long currentMillis = millis();
	if (currentMillis - blinkStartTime >= BLINK_PERIOD) {
		blinkStartTime = currentMillis;
		ledState = !ledState;
		digitalWrite(LED, ledState);
	}

	int val = analogRead(PFI_POT);
	if (val > 20) {
		analogWrite(PFI_LED, map(val, 20, 1023, 0, 250));

	} else {
		analogWrite(PFI_LED, 0);
	}

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

		if (!strcasecmp(cmd, "RS")) {
			for (int i = 0; i < (sizeof(customPins) / sizeof(*customPins)); i++) {
				analogWrite(customPins[i], 0);
			}
		}

		if (!strcasecmp(cmd, "AV")) {
			long pinCmd = hexToLong(param);
			if (pinCmd > 2000) {
				int pin = pinCmd / 1000;
				int value = pinCmd - (pin * 1000);
				for (int i = 0; i < (sizeof(customPins) / sizeof(*customPins)); i++) {
					if (customPins[i] == pin) {
						analogWrite(pin, value);
						break;
					}
				}
			}
		}

		if (!strcasecmp(cmd, "BT")) {
			Serial.print(BOARD_TYPE);
			Serial.print("#");
		}

		if (!strcasecmp(cmd, "GV")) {
			Serial.print(VERSION + '#');
		}
	}
}

long hexToLong(char *line) {
	return strtol(line, NULL, 16);
}
