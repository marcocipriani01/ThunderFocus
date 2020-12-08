/**
   Focuser & powerbox by marcocipriani01

** Version 4.2 ***
   December 2020
   Dropped MoonLite support

** Version 4.0 ***
   November 2020

 ** Version 3.0 ***
   March 2020
 */

#include "main.h"

Focuser focuser;

// Hand controller
#if FOK1_ENABLE_HC == true
HandController hc(&focuser);
#endif

#if SETTINGS_SUPPORT == true
// Settings data for EEPROM storage
struct Settings settings;
// Flag for requesting settings to be saved
boolean needToSaveSettings = false;
unsigned long lastTimeSettingsSave = 0;

void loadSettings() {
	uint8_t* dst = (uint8_t*)&settings;
	for (unsigned int i = 0; i < sizeof(Settings); i++) {
		dst[i] = EEPROM.read(i);
	}
	if (settings.marker == EEPROM_VERSION) {
		focuser.begin((boolean) settings.fok1HoldControl, settings.fok1Speed,
			settings.fok1Backlash, settings.fok1Reverse);
		if (settings.fok1Pos != 0) {
			focuser.setCurrentPos(settings.fok1Pos);
		}
#if ENABLE_DEVMAN == true
		for (uint8_t i = 0; i < getManagedPinsCount(); i++) {
			Pin pin = settings.devManPins[i];
			if (pin.autoModeEn) {
				setPinAutoMode(pin.number, true);
			} else {
				updatePin(pin.number, pin.value);
			}
		}
		setDevManAutoMode(settings.devManAutoMode);
#endif
#if TIME_CONTROL == true
		if ((!isnan(settings.worldLat)) && (!isnan(settings.worldLong))) {
			setWorldCoord(settings.worldLat, settings.worldLong);
		}
#endif
	} else {
		settings.marker = EEPROM_VERSION;
		focuser.begin();
		saveSettings();
	}
}

void saveSettings() {
	settings.marker = EEPROM_VERSION;
	settings.fok1Pos = focuser.getCurrentPos();
	settings.fok1Speed = focuser.getSpeed();
	settings.fok1HoldControl = focuser.isHoldControlEnabled();
	settings.fok1Backlash = focuser.getBacklash();
	settings.fok1Reverse = focuser.getDirReverse();
#if ENABLE_DEVMAN == true
	for (uint8_t i = 0; i < getManagedPinsCount(); i++) {
		settings.devManPins[i] = getManagedPin(i);
	}
	settings.devManAutoMode = getDevManAutoMode();
#endif
#if TIME_CONTROL == true
	settings.worldLat = getWorldLat();
	settings.worldLong = getWorldLong();
#endif
	uint8_t* dst = (uint8_t*) &settings;
	for (unsigned int i = 0; i < sizeof(Settings); ++i) {
		EEPROM.update(i, dst[i]);
	}
}
#endif

void setup() {
	// Serial port and protocol
	Serial.begin(SERIAL_SPEED);
	Serial.setTimeout(SERIAL_TIMEOUT);

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

#if ENABLE_DEVMAN == true
	beginDevMan();
#endif

#if SETTINGS_SUPPORT == true
	loadSettings();
#else
	focuser.begin();
#endif

#if FOK1_ENABLE_HC == true
	hc.begin();
#endif

#if defined(__AVR_ATmega32U4__)
	while (!Serial) {
		;
	}
#endif
}

void loop() {
	if (thunderFocusManage(&focuser) == FocuserState::FOCUSER_ARRIVED) {
		flagSettings();
	}

#if ENABLE_HC == true
	hc.manage();
#endif

#if SETTINGS_SUPPORT == true
	unsigned long currentTime = millis();
	if (currentTime - lastTimeSettingsSave >= SETTINGS_SAVE_DELAY) {
		if (needToSaveSettings) {
			saveSettings();
			needToSaveSettings = false;
		}
		lastTimeSettingsSave = currentTime;
	}
#endif
}

inline void flagSettings() {
#if SETTINGS_SUPPORT == true
	needToSaveSettings = true;
#endif
}

void serialEvent() {
	if (thunderFocusSerialEvent(&focuser)) {
		flagSettings();
	}
}