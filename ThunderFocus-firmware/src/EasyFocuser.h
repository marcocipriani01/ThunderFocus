#ifndef EASY_FOCUSER_H
#define EASY_FOCUSER_H

#include <Arduino.h>
#include "Focuser.h"
#include "config.h"
#if ENABLE_DEVMAN == true
#include "DevManager.h"
#endif

#define EASYFOC_SERIAL_DELAY 150
#define EASYFOC_UUID "a537d6e0-c155-405a-9234-7a6ef62913a9"

class EasyFocuser {
public:
	EasyFocuser(Focuser *f);
	void manage();
	void flagState(FocuserState s);

private:
	Focuser *focuser;
	FocuserState state;
	unsigned long lastSendTime;
};

#endif
