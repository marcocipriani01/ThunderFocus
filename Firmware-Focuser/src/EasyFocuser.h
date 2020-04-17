#ifndef EASY_FOCUSER_H
#define EASY_FOCUSER_H

#include <Arduino.h>

#define EASYFOC_SERIAL_DELAY 100

typedef enum {
	EF_RELATIVE,
  EF_ABSOLUTE,
  EF_STOP,
  EF_SET_ZERO,
	EF_POWERSAVE,
	EF_SPEED,
	EF_UNRECOGNIZED
} EasyFocuserCommand;

struct EasyFocuserResult {
	EasyFocuserCommand cmd;
	long n;
};

void efBoot() {
	Serial.println("syncme");
  Serial.println("LBootingUp");
}

void efReady() {
  Serial.println("LReady");
}

EasyFocuserResult waitSerialCmd() {
  if (Serial.available() && Serial.find('G')) {
    Serial.print("LCmdFound: ");
    String type = Serial.readStringUntil('N');
    if (type.equals("R")) {
      Serial.print("RelativeMovement = ");
      // Cmd: GRN-1234# -> move stepper by -1234
      long n = Serial.parseInt();
      Serial.println(n);
      struct EasyFocuserResult res = { EF_RELATIVE, n};
      return res;

    } else if (type.equals("A")) {
      Serial.print("AbsoluteGoTo = ");
      // Cmd: GAN-1234# -> move stepper to -1234
      long n = Serial.parseInt();
      Serial.println(n);
      struct EasyFocuserResult res = { EF_ABSOLUTE, n};
      return res;

    } else if (type.equals("S")) {
      Serial.println("LStopASAP");
      // Cmd: GSN -> stop stepper
      struct EasyFocuserResult res = { EF_STOP, 0};
      return res;

    } else if (type.equals("Z")) {
      Serial.println("LSetZero");
      // Cmd: GSN -> stop stepper
      struct EasyFocuserResult res = { EF_SET_ZERO, 0};
      return res;

    } else if (type.equals("P")) {
			Serial.print("PowerSaver = ");
      long n = Serial.parseInt();
      Serial.println(n);
      struct EasyFocuserResult res = { EF_POWERSAVE, n};
      return res;

    }  else if (type.equals("V")) {
			Serial.print("Speed = ");
      long n = Serial.parseInt();
      Serial.println(n);
      struct EasyFocuserResult res = { EF_SPEED, n};
      return res;

    } else {
      Serial.println("LError");
      struct EasyFocuserResult res = { EF_UNRECOGNIZED, 0};
      return res;
    }
  }
  struct EasyFocuserResult res = { EF_UNRECOGNIZED, 0};
  return res;
}

#endif
