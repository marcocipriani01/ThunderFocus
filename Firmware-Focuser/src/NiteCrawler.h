#ifndef NITECRAWLER_H
#define NITECRAWLER_H

#include <Arduino.h>

#define NC_35_STEPS 505960
#define NC_EDITION "3.5 NC"
#define NC_FOCUSER_CHANNEL 1
#define NC_ROTATOR_CHANNEL 2

typedef enum {
	NC_,
	NC_UNRECOGNIZED
} MoonLiteCommand;

#define NUM_NITECRAWLER_COMMANDS 0

const static struct {
	MoonLiteCommand val;
	const char *str;

} NiteCrawlerMapping[] = {
	{ NC_, "?" },
};

MoonLiteCommand moonliteStringToEnum(char* buffer) {
	for (int i = 0; i < NUM_NITECRAWLER_COMMANDS; ++i) {
		if (strncmp(buffer, NiteCrawlerMapping[i].str, 2) == 0) {
			return NiteCrawlerMapping[i].val;
		}
	}
	return NC_UNRECOGNIZED;
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
