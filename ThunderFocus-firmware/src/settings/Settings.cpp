#include "Settings.h"
#if SETTINGS_SUPPORT == true

namespace Settings {
Struct settings;
boolean requestSave = false;
unsigned long lastSaveTime = 0L;

void reset() {
#if DEBUG_EN
    Serial.println(F(">Settings reset!"));
#endif
    settings.marker = EEPROM_MARKER;
#if FOCUSER_DRIVER != OFF
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
#if RTC_SUPPORT != OFF
    settings.latitude = 0.0;
    settings.longitude = 0.0;
#endif
#endif
#if (FLAT_PANEL == true) && (SERVO_MOTOR != OFF)
    settings.servoDelay = SERVO_DELAY_DEFAULT;
    settings.openServoVal = OPEN_SERVO_DEFAULT;
    settings.closedServoVal = CLOSED_SERVO_DEFAULT;
    settings.coverStatus = FlatPanel::CLOSED;
#endif
    requestSave = true;
}

void load() {
#ifdef ESP32
    EEPROM.begin(sizeof(Struct) + 1);
#else
#if DEBUG_EN
    Serial.print(F(">EEPROM size = "));
    Serial.println(EEPROM.length());
    Serial.print(F(">Struct size = "));
    Serial.println((int)sizeof(Struct));
    Serial.print(F(">Compiled EEPROM marker = "));
    Serial.println(EEPROM_MARKER);
#endif
    if (sizeof(Struct) >= EEPROM.length()) {
        while (true) {
            Serial.println(F(">Fatal error, the EEPROM is too small!"));
#ifdef STATUS_LED
            digitalWrite(STATUS_LED, HIGH);
            delay(500);
            digitalWrite(STATUS_LED, LOW);
            delay(500);
#else
            delay(1000);
#endif
        }
    }
#endif
#if DEBUG_EN
    Serial.println(F(">Loading settings..."));
#endif
    EEPROM.get(EEPROM_START, settings);
#if DEBUG_EN
    Serial.print(F(">Stored EEPROM marker = "));
    Serial.println(settings.marker);
#endif
    if (settings.marker != EEPROM_MARKER) reset();
#if FOCUSER_DRIVER != OFF
    if (isnan(settings.focuserSpeed)) reset();
    settings.focuserSpeed = constrain(settings.focuserSpeed, FOCUSER_PPS_MIN, FOCUSER_PPS_MAX);
    if (settings.focuserBacklash < 0) settings.focuserBacklash = 0;
#endif
#if (ENABLE_DEVMAN == true) && (RTC_SUPPORT != OFF)
    if (isnan(settings.latitude) || isnan(settings.longitude)) reset();
#endif
#if (FLAT_PANEL == true) && (SERVO_MOTOR != OFF)
    settings.servoDelay = constrain(settings.servoDelay, SERVO_DELAY_MIN, SERVO_DELAY_MAX);
    settings.closedServoVal = constrain(settings.closedServoVal, CLOSED_SERVO_15deg, CLOSED_SERVO_m15deg);
    settings.openServoVal = constrain(settings.openServoVal, OPEN_SERVO_290deg, OPEN_SERVO_170deg);
    if (!((settings.coverStatus == FlatPanel::CLOSED) || (settings.coverStatus == FlatPanel::OPEN)))
        settings.coverStatus = FlatPanel::CLOSED;
#endif
}

void save() {
    EEPROM.put(EEPROM_START, settings);
    requestSave = false;
}
}  // namespace Settings

#endif