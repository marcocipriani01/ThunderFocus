#ifndef CONFIG_H
#define CONFIG_H

#define FIRMWARE_VERSION "50"

#define SERIAL_SPEED 115200
#define SERIAL_TIMEOUT 100

#include "boards/arduino_nano_prototype.h"

#if TIME_CONTROL == true and defined(CORE_TEENSY) == false
#error Time control enabled but Teensy not found
#endif
#endif
