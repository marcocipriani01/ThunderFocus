#ifndef HAND_CONTROLLER_H
#define HAND_CONTROLLER_H

#include <Arduino.h>
// Configuration
#include "HandControllerConfig.h"
#include "Focuser.h"

class HandController {
public:
  HandController(Focuser *focuser);
  void begin();
  void manage();

private:
  Focuser *f;
  unsigned long lastButtonTime;
};

#endif
