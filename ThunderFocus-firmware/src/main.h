/**
  * Focuser & powerbox by marcocipriani01
  * 
  * == Version 6.0 ==
  *    May 2022
  *    Complete refactoring
  *    Flat panel feature
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

#include <Arduino.h>

#include "config.h"
#include "ThunderFocusProtocol.h"

#if SETTINGS_SUPPORT == true
#include "settings/Settings.h"
#endif
#endif