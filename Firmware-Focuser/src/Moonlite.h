#ifndef MOONLITE_H
#define MOONLITE_H

#include <Arduino.h>

typedef enum {
	M_INIT_TEMP_CONV,
	M_STOP,
	M_GET_CURRENT_POS,
	M_SET_CURRENT_POS,
	M_GET_NEW_POS,
	M_SET_NEW_POS,
	M_GOTO_NEW_POS,
	M_IS_HALF_STEP,
	M_SET_FULL_STEP,
	M_SET_HALF_STEP,
	M_IS_MOVING,
	M_FIRMWARE_VERSION,
	M_BACKLIGHT_VALUE,
	M_GET_SPEED,
	M_SET_SPEED,
	M_GET_TEMP,
  M_GET_TEMP_COEFF,
  M_SET_TEMP_COEFF,
  M_ENABLE_TEMP_COMP,
  M_DISABLE_TEMP_COMP,
  M_SET_TEMP_CAL_OFFSET,
	M_ENABLE_HOLD,
	M_DISABLE_HOLD,
  M_GET_BOARD_TYPE,
  M_SET_PIN,
  M_RESET_PINS,
	M_UNRECOGNIZED
} MoonLiteCommand;

#define NUM_MOONLITE_COMMANDS 26

const static struct {
	MoonLiteCommand val;
	const char *str;

} MoonLiteMapping[] = {
	{ M_INIT_TEMP_CONV, "C" },
	{ M_STOP, "FQ" },
	{ M_GET_CURRENT_POS, "GP" },
	{ M_SET_CURRENT_POS, "SP" },
	{ M_GET_NEW_POS, "GN" },
	{ M_SET_NEW_POS, "SN" },
	{ M_GOTO_NEW_POS, "FG" },
	{ M_IS_HALF_STEP, "GH" },
	{ M_SET_FULL_STEP, "SF" },
	{ M_SET_HALF_STEP, "SH" },
	{ M_IS_MOVING, "GI" },
	{ M_FIRMWARE_VERSION, "GV" },
	{ M_BACKLIGHT_VALUE, "GB" },
	{ M_GET_SPEED, "GD" },
	{ M_SET_SPEED, "SD" },
	{ M_GET_TEMP, "GT" },
  { M_GET_TEMP_COEFF, "GC" },
  { M_SET_TEMP_COEFF, "SC" },
  { M_ENABLE_TEMP_COMP, "+" },
  { M_DISABLE_TEMP_COMP, "-" },
  { M_SET_TEMP_CAL_OFFSET, "PO" },
	{ M_ENABLE_HOLD, "HE" },
	{ M_DISABLE_HOLD, "HD" },
  { M_GET_BOARD_TYPE, "BT" },
  { M_SET_PIN, "AV" },
  { M_RESET_PINS, "RS" }
};

MoonLiteCommand moonliteStringToEnum(char* buffer) {
	for (int i = 0; i < NUM_MOONLITE_COMMANDS; ++i) {
		if (strncmp(buffer, MoonLiteMapping[i].str, 2) == 0) {
			return MoonLiteMapping[i].val;
		}
	}
	return M_UNRECOGNIZED;
}

uint16_t fourCharsToUint16(char* buffer) {
	long int result = strtol(buffer, NULL, 16);
	return (uint16_t) (result & 0xFFFF);
}

uint8_t twoCharsToUint8(char* buffer) {
	long int result = strtol(buffer, NULL, 16);
	return (uint8_t) (result & 0xFF);
}

uint8_t twoDecCharsToUint8(char* buffer) {
	long int result = strtol(buffer, NULL, 10);
	return (uint8_t) (result & 0xFF);
}

int8_t twoCharsToInt8(char* buffer) {
	long int result = strtol(buffer, NULL, 16);
	int8_t val = (int8_t) (result & 0xFF);
	if (val > 127) {
		val = ~(val - 1);
	}
	return val;
}

void clearBuffer(char* buffer, uint16_t length) {
  memset(buffer, 0, length);
}

#endif
