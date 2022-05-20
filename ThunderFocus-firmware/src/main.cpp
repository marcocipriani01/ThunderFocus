#include "main.h"

void setup() {
#ifdef STATUS_LED
    pinMode(STATUS_LED, OUTPUT);
#if STATUS_LED_MANAGED == true
    digitalWrite(STATUS_LED, HIGH);
    delay(400);
    digitalWrite(STATUS_LED, LOW);
#else
    digitalWrite(STATUS_LED, HIGH);
#endif
#endif
    ThunderFocus::setup();
}

void loop() {
    ThunderFocus::run();
#if SETTINGS_SUPPORT == true
    if (Settings::requestSave) {
        unsigned long t = millis();
        if ((t - Settings::lastSaveTime) >= SETTINGS_SAVE_INTERVAL) {
            Settings::save();
            Settings::lastSaveTime = t;
        }
    }
#endif
}

void serialEvent() {
    ThunderFocus::serialEvent();
}