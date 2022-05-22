#ifndef SETTINGS_H
#define SETTINGS_H

#include <Arduino.h>

#include "../config.h"
#if SETTINGS_SUPPORT == true
#include <EEPROM.h>
#include <math.h>
#if ENABLE_DEVMAN == true
#include "../devman/DevManagerDefinitions.h"
#endif
#if (FLAT_PANEL == true) && (SERVO_MOTOR != DISABLED)
#include "../flat/FlatPanelEnums.h"
#include "../flat/ServoModels.h"
#endif

#define SETTINGS_SAVE_INTERVAL 15000

namespace Settings {
struct Struct {
    uint8_t marker;
#if FOCUSER_DRIVER != DISABLED
    long focuserPosition;
    double focuserSpeed;
    long focuserBacklash;
    boolean focuserPowerSave;
    boolean focuserReverse;
#endif

#if ENABLE_DEVMAN == true
    DevManager::Pin devManPins[MANAGED_PINS_COUNT];
    DevManager::AutoMode devManAutoMode;
#if RTC_SUPPORT != DISABLED
    double latitude;
    double longitude;
#endif
#endif

#if (FLAT_PANEL == true) && (SERVO_MOTOR != DISABLED)
    uint16_t servoDelay;
    uint16_t openServoVal;
    uint16_t closedServoVal;
    FlatPanel::CoverStatus coverStatus;
#endif
};

extern Struct settings;
extern boolean requestSave;
extern unsigned long lastSaveTime;

void reset();
void load();
void save();
}  // namespace Settings
#endif
#endif