#ifndef DEVMAN_H
#define DEVMAN_H

#include <Arduino.h>
#include "DevManagerConfig.h"

extern byte pwmPins[];
extern byte pwmPinsValues[];
extern byte dioPins[];
extern boolean dioPinsValues[];

void beginDevMan();
uint8_t getPwmPin(uint8_t index);
uint8_t getPwmPinValue(uint8_t index);
uint8_t getPwmPinCount();
uint8_t getDioPin(uint8_t index);
boolean getDioPinValue(uint8_t index);
uint8_t getDioPinCount();
void managePFI();
void resetPins();
void updatePin(byte pin, byte value);

#endif
