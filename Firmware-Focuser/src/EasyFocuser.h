#ifndef EASY_FOCUSER_H
#define EASY_FOCUSER_H

#include <Arduino.h>
#include "Focuser.h"

#define EASYFOC_SERIAL_DELAY 100

class EasyFocuser {
public:
	EasyFocuser(Focuser *f);
	void begin();
	void flagReady();
	void manage();
	void flagState(FocuserState s);

private:
	Focuser *focuser;
	FocuserState state;
	unsigned long lastSendTime;
};

#endif
