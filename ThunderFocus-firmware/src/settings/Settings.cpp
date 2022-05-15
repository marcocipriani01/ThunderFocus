#include "Settings.h"
#if SETTINGS_SUPPORT == true

using namespace Settings;

Struct settings;
boolean requestSave = false;
unsigned long lastSaveTime = 0L;

void Settings::reset() {
    settings.marker = EEPROM_MARKER;
    settings.focuserPosition = 0L;
    settings.focuserSpeed = (FOCUSER_PPS_MAX + FOCUSER_PPS_MIN) / 2.0;
    settings.focuserBacklash = 0L;
    settings.focuserPowerSave = true;
    settings.focuserReverse = false;

#if ENABLE_DEVMAN == true
    DevManager::Pin defaults[MANAGED_PINS_COUNT] = MANAGED_PINS;
    for (uint8_t i = 0; i < MANAGED_PINS_COUNT; i++) {
        settings.devManPins[i] = defaults[i];
    }
    settings.devManAutoMode = DevManager::AutoMode::NONE;
#if RTC_SUPPORT != DISABLED
    settings.latitude = 0.0;
    settings.longitude = 0.0;
#endif
#endif
}

void Settings::load() {
    uint8_t* bytes = (uint8_t*)&settings;
    for (unsigned int i = 0; i < sizeof(Struct); i++) {
        bytes[i] = EEPROM.read(i);
    }
    if (settings.marker != EEPROM_MARKER) reset();
    settings.focuserSpeed = constrain(settings.focuserSpeed, FOCUSER_PPS_MIN, FOCUSER_PPS_MAX);
    if (settings.focuserBacklash < 0)
        settings.focuserBacklash = 0;
}

void Settings::save() {
    settings.marker = EEPROM_MARKER;
    settings.focuserPosition = Focuser::stepper.getPosition();
    settings.focuserSpeed = Focuser::stepper.getMaxSpeed();
    settings.focuserPowerSave = (Focuser::stepper.getAutoPowerTimeout() > 0L);
    settings.focuserBacklash = Focuser::stepper.getBacklash();
    settings.focuserReverse = Focuser::stepper.isDirectionInverted();

    uint8_t* bytes = (uint8_t*)&settings;
    for (unsigned int i = 0; i < sizeof(Struct); i++) {
        EEPROM.update(i, bytes[i]);
    }
    requestSave = false;
}
#endif