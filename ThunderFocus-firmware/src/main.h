/**
  * Focuser & powerbox by marcocipriani01
  * 
  * == Version 5.1 ==
  *    January 2022
  *    Fixes and improvements
  * 
  * == Version 5.0 ==
  *    November 2021
  *    Removed hand controller support, lighter and faster code.
  * 
  * == Version 4.2 ==
  *    December 2020
  *    Dropped MoonLite support
  * 
  * == Version 4.0 ==
  *    November 2020
  * 
  * == Version 3.0 ==
  *    March 2020
 */
#ifndef MAIN_H
#define MAIN_H

#include "config.h"
#include <Arduino.h>
#include "ThunderFocusProtocol.h"
#include "AccelStepper.h"
#if ENABLE_DEVMAN == true
#include "DevManager.h"
#endif
#if TIME_CONTROL == true
#if defined(CORE_TEENSY) == false
#error Time control enabled but Teensy not found
#endif
#include "SunKeeper.h"
#endif
#if SETTINGS_SUPPORT == true
#include "Settings.h"
#endif
#endif
