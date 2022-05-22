#include "Settings.h"
#if SETTINGS_SUPPORT == true

namespace Settings {
Struct settings;
boolean requestSave = false;
unsigned long lastSaveTime = 0L;

void reset() {
    Serial.println(F(">Settings reset!"));
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
    for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
        settings.devManPins[i] = defaults[i];
    }
    settings.devManAutoMode = DevManager::AutoMode::NONE;
#if RTC_SUPPORT != DISABLED
    settings.latitude = 0.0;
    settings.longitude = 0.0;
#endif
#endif
#if (FLAT_PANEL == true) && (SERVO_MOTOR != DISABLED)
    settings.servoDelay = SERVO_DELAY_DEFAULT;
    settings.openServoVal = OPEN_SERVO_DEFAULT;
    settings.closedServoVal = CLOSED_SERVO_DEFAULT;
    settings.coverStatus = FlatPanel::CLOSED;
#endif
    requestSave = true;
}

void load() {
#if DEBUG_EN
    Serial.print(F(">EEPROM size = "));
    Serial.println(EEPROM.length());
    Serial.print(F(">Struct size = "));
    Serial.println((int) sizeof(Struct));
#endif
    if (sizeof(Struct) >= EEPROM.length()) {
        Serial.println(F(">EEPROM size is too small, aborting."));
        while (true)
            ;
    }
#if DEBUG_EN
    Serial.println(F(">Loading settings..."));
#endif
    uint8_t* bytes = (uint8_t*)&settings;
#ifdef EEPROM_END_CROP
    for (unsigned int i = 0; i < (sizeof(Struct) - EEPROM_END_CROP); i++) {
#else
    for (unsigned int i = 0; i < sizeof(Struct); i++) {
#endif
        bytes[i] = EEPROM.read(i + EEPROM_START);
#if DEBUG_EN
        Serial.print(F(">Byte "));
        Serial.print(i);
        Serial.print(F(" = "));
        Serial.println(bytes[i]);
#endif
    }
    if (settings.marker != EEPROM_MARKER) reset();
#if FOCUSER_DRIVER != DISABLED
    settings.focuserSpeed = constrain(settings.focuserSpeed, FOCUSER_PPS_MIN, FOCUSER_PPS_MAX);
    if (settings.focuserBacklash < 0) settings.focuserBacklash = 0;
#endif
#if (FLAT_PANEL == true) && (SERVO_MOTOR != DISABLED)
        settings.servoDelay = constrain(settings.servoDelay, SERVO_DELAY_MIN, SERVO_DELAY_MAX);
        settings.closedServoVal = constrain(settings.closedServoVal, CLOSED_SERVO_15deg, CLOSED_SERVO_m15deg);
        settings.openServoVal = constrain(settings.openServoVal, OPEN_SERVO_290deg, OPEN_SERVO_170deg);
        if (!((settings.coverStatus == FlatPanel::CLOSED) || (settings.coverStatus == FlatPanel::OPEN)))
            settings.coverStatus = FlatPanel::CLOSED;
#endif
}

void save() {
    uint8_t* bytes = (uint8_t*)&settings;
#ifdef EEPROM_END_CROP
    for (unsigned int i = 0; i < (sizeof(Struct) - EEPROM_END_CROP); i++) {
#else
    for (unsigned int i = 0; i < sizeof(Struct); i++) {
#endif
        EEPROM.update(i + EEPROM_START, bytes[i]);
#if defined(EEPROM_IO_DELAY) && (EEPROM_IO_DELAY > 0)
        delay(EEPROM_IO_DELAY);
#endif
    }
    requestSave = false;
}
}  // namespace Settings

#endif