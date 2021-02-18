#ifndef CONFIG_H
#define CONFIG_H

#define FIRMWARE_VERSION "43"

#define SERIAL_SPEED 115200
#define SERIAL_TIMEOUT 100
#define THUNDERFOCUS_SEND_DELAY 150

//#include "boards/nik_coli.h"
//#include "boards/arduino_nano_pcb.h"
//#include "boards/arduino_nano_prototype.h"
#include "boards/teensy_max_pcb.h"

// Config checks
#if FOK1_STEPPER == DRIVER_POLOLU
#if (!defined(FOK1_DIR)) || (!defined(FOK1_STEP)) || defined(FOK1_IN1) || defined(FOK1_IN2) || defined(FOK1_IN3) || defined(FOK1_IN4)
#error Wrong pin configuration for ULN2003
#endif
#elif FOK1_STEPPER == DRIVER_ULN2003
#if defined(FOK1_DIR) || defined(FOK1_STEP) || (!defined(FOK1_IN1)) || (!defined(FOK1_IN2)) || (!defined(FOK1_IN3)) || (!defined(FOK1_IN4))
#error Wrong pin configuration for ULN2003
#endif
#ifdef FOK1_EN
#error Stepper driver ULN2003 does not support the EN pin
#endif
#if FOK1_uSTEPS != 1
#error Stepper driver ULN2003 does not support microstepping
#endif
#endif

#if TIME_CONTROL == true and defined(CORE_TEENSY) == false
#error Time control enabled but Teensy not found
#endif

#endif