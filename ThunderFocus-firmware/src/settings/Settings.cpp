#include "Settings.h"
#if SETTINGS_SUPPORT == true

namespace Settings {
Struct settings;
boolean requestSave = false;
unsigned long lastSaveTime = 0L;

void reset() {
    settings.marker = EEPROM_MARKER;
#if FOCUSER_DRIVER != DISABLED
    settings.focuserPosition = 0L;
    settings.focuserSpeed = (FOCUSER_PPS_MAX + FOCUSER_PPS_MIN) / 2.0;
    settings.focuserBacklash = 0L;
    settings.focuserPowerSave = true;
    settings.focuserReverse = false;
#endif

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
#if (FLAT_PANEL == true) && defined(SERVO_PIN)
    settings.servoDelay = SERVO_DELAY_DEFAULT;
    settings.openServoVal = OPEN_SERVO_DEFAULT;
    settings.closedServoVal = CLOSED_SERVO_DEFAULT;
    settings.coverStatus = FlatPanel::CLOSED;
#endif
}

void load() {
    uint8_t* bytes = (uint8_t*)&settings;
    for (unsigned int i = 0; i < sizeof(Struct); i++) {
        bytes[i] = EEPROM.read(i);
    }
    if (settings.marker != EEPROM_MARKER) reset();
#if FOCUSER_DRIVER != DISABLED
    settings.focuserSpeed = constrain(settings.focuserSpeed, FOCUSER_PPS_MIN, FOCUSER_PPS_MAX);
    if (settings.focuserBacklash < 0) settings.focuserBacklash = 0;
#endif
#if (FLAT_PANEL == true) && defined(SERVO_PIN)
        settings.servoDelay = constrain(settings.servoDelay, SERVO_DELAY_MIN, SERVO_DELAY_MAX);
        settings.closedServoVal = constrain(settings.closedServoVal, CLOSED_SERVO_15deg, CLOSED_SERVO_m15deg);
        settings.openServoVal = constrain(settings.openServoVal, OPEN_SERVO_290deg, OPEN_SERVO_170deg);
        if (!((settings.coverStatus == FlatPanel::CLOSED) || (settings.coverStatus == FlatPanel::OPEN)))
            settings.coverStatus = FlatPanel::CLOSED;
#endif
}

void save() {
    uint8_t* bytes = (uint8_t*)&settings;
    for (unsigned int i = 0; i < sizeof(Struct); i++) {
        EEPROM.update(i, bytes[i]);
    }
    requestSave = false;
}
}  // namespace Settings

#endif