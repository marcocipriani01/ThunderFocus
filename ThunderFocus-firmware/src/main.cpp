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
#include "DevManager.h"

AccelStepper stepper(FOCUSER_STEP, FOCUSER_DIR);

// Hand controller
#if FOCUSER_ENABLE_HC == true
HandController hc(&focuser);
#endif

#if SETTINGS_SUPPORT == true
// Settings data for EEPROM storage
struct Settings settings;
// Flag for requesting settings to be saved
boolean needToSaveSettings = false;
unsigned long lastTimeSettingsSave = 0;

void resetSettings() {
	settings.position = 0;
	settings.speed = (FOCUSER_PPS_MAX + FOCUSER_PPS_MIN) / 2;
	settings.backlash = 0;
	settings.powerTimeout = 30000;
	settings.reverse = false;
	settings.scaling = FOCUSER_SCALING_DEFAULT;

#if ENABLE_DEVMAN == true
	Pin defaults[MANAGED_PINS_COUNT] = MANAGED_PINS;
	for (uint8_t i = 0; i < getManagedPinsCount(); i++) {
		settings.powerPins[i] = defaults[i];
	}
	settings.powerPinsMode = DevManAutoModes::NONE;
#endif

#if TIME_CONTROL == true
	worldLat = 0.0;
	worldLong = 0.0;
#endif
}

void loadSettings() {
	uint8_t* dst = (uint8_t*)&settings;
	for (unsigned int i = 0; i < sizeof(Settings); i++) {
		dst[i] = EEPROM.read(i);
	}
	if (settings.marker != EEPROM_VERSION) {
		settings.marker = EEPROM_VERSION;
		saveSettings();
	}
}

void saveSettings() {
	settings.marker = EEPROM_VERSION;
	settings.position = stepper.getPosition();
	settings.speed = stepper.getMaxSpeed();
	settings.powerTimeout = stepper.getAutoPowerTimeout();
	settings.backlash = stepper.getBacklash();
	settings.reverse = stepper.isDirectionInverted();
	settings.scaling = stepper.getStepsScaling();

#if ENABLE_DEVMAN == true
	for (uint8_t i = 0; i < getManagedPinsCount(); i++) {
		settings.powerPins[i] = getManagedPin(i);
	}
	settings.powerPinsMode = getDevManAutoMode();
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

#if SETTINGS_SUPPORT == true
	loadSettings();
#endif

#ifdef FOCUSER_MODE0
	pinMode(FOCUSER_MODE0, OUTPUT);
	digitalWrite(FOCUSER_MODE0, HIGH);
#endif
#ifdef FOCUSER_MODE1
	pinMode(FOCUSER_MODE1, OUTPUT);
	digitalWrite(FOCUSER_MODE1, HIGH);
#endif
#ifdef FOCUSER_MODE2
	pinMode(FOCUSER_MODE2, OUTPUT);
	digitalWrite(FOCUSER_MODE2, HIGH);
#endif
	stepper.setPosition(settings.position);
	stepper.setMaxSpeed(settings.speed);
	stepper.setBacklash(settings.backlash);
	stepper.setDirectionInverted(settings.reverse);
	stepper.setStepsScaling(settings.scaling);
#ifdef FOCUSER_EN
	stepper.setEnablePin(FOCUSER_EN, false);
	stepper.setAutoPowerTimeout(settings.powerTimeout);
#endif
#if FOCUSER_ENABLE_HC == true
	hc.begin();
#endif

#if ENABLE_DEVMAN == true
	for (uint8_t i = 0; i < getManagedPinsCount(); i++) {
		Pin pin = settings.powerPins[i];
		if (pin.autoMode)
			setPinAutoMode(pin.number, true);
		else
			updatePin(pin.number, pin.value);
	}
	setDevManAutoMode(settings.powerPinsMode);
	beginDevMan();
#endif

#if TIME_CONTROL == true
	if ((!isnan(settings.worldLat)) && (!isnan(settings.worldLong))) {
		setWorldCoord(settings.worldLat, settings.worldLong);
	}
#endif
}

void loop() {
	if (thunderFocusManage(&stepper) == FocuserState::ARRIVED)
		flagSettings();

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
	if (thunderFocusSerialEvent(&stepper))
		flagSettings();
}
