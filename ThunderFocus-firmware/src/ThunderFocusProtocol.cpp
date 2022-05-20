#include "ThunderFocusProtocol.h"

namespace ThunderFocus {
#if FOCUSER_DRIVER != DISABLED
FocuserState lastFocuserState = FocuserState::POWER_SAVE;
unsigned long focuserSyncTime = 0;
#endif
#if TEMP_HUM_SENSOR != DISABLED
unsigned long sensorsSyncTime = 0;
#endif
#if RTC_SUPPORT != DISABLED
unsigned long sunSyncTime = 0;
#endif

void setup() {
    Serial.begin(SERIAL_SPEED);
    Serial.setTimeout(SERIAL_TIMEOUT);
#if SETTINGS_SUPPORT == true
    Settings::load();
#endif
#if FOCUSER_DRIVER != DISABLED
    Focuser::begin();
#endif
#if ENABLE_DEVMAN == true
    DevManager::begin();
    for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
        DevManager::Pin pin = Settings::settings.devManPins[i];
#if DEVMAN_HAS_AUTO_MODES
        if (pin.autoModeEn)
            DevManager::setPinAutoModeEn(pin.number, true);
        else
            DevManager::updatePin(pin.number, pin.enablePwm, pin.value);
#else
        DevManager::updatePin(pin.number, pin.enablePwm, pin.value);
#endif
    }
#if DEVMAN_HAS_AUTO_MODES
    DevManager::setAutoMode(Settings::settings.devManAutoMode);
#endif
#endif
}

#if FOCUSER_DRIVER == DISABLED
void run() {
#else
FocuserState run() {
#endif
    unsigned long t = millis();
#if FOCUSER_DRIVER != DISABLED
    FocuserState currentState;
    if (Focuser::stepper.run()) {
        currentState = FocuserState::MOVING;
    } else if (lastFocuserState == FocuserState::MOVING) {
        currentState = FocuserState::ARRIVED;
        Focuser::updateSettings();
    } else if (Focuser::stepper.isEnabled()) {
        currentState = FocuserState::HOLD;
    } else {
        currentState = FocuserState::POWER_SAVE;
    }

    if (currentState != lastFocuserState) {
        Serial.println((char)currentState);
        lastFocuserState = currentState;
    }
    if ((t - focuserSyncTime) >= THUNDERFOCUS_SEND_DELAY) {
        Serial.print(F("S"));
        Serial.println(Focuser::stepper.getPosition());
        focuserSyncTime = t;
    }
#endif

#if ENABLE_DEVMAN == true
    if (DevManager::run()) updatePins();
#if TEMP_HUM_SENSOR != DISABLED
    AmbientManger::run();
    if ((t - sensorsSyncTime) >= SENSORS_UPDATE_INTERVAL) {
        Serial.print(F("J"));
        Serial.print(AmbientManger::getTemperature(), 1);
        Serial.print(F(","));
        Serial.print(AmbientManger::getHumidity(), 1);
        Serial.print(F(","));
        Serial.println(AmbientManger::getDewPoint(), 1);
        sensorsSyncTime = t;
    }
#endif
#if RTC_SUPPORT != DISABLED
    if ((t - sunSyncTime) >= DEVMAN_UPDATE_INTERVAL) {
        updateSunPosition();
        sunSyncTime = t;
    }
#endif
#endif

#if FOCUSER_DRIVER != DISABLED
    return currentState;
#endif
}

boolean serialEvent() {
    while (Serial.available()) {
        if (Serial.read() == '$') {
#if defined(CMD_RECEIVE_WAIT) && (CMD_RECEIVE_WAIT > 0)
            delay(CMD_RECEIVE_WAIT);
#endif
            switch (Serial.read()) {
                case 'C': {
                    Serial.print(F("C"));
                    Serial.print(FIRMWARE_VERSION);
                    Serial.print(F(";"));
#if FOCUSER_DRIVER != DISABLED
                    Serial.print(F("F["));
                    Serial.print(Focuser::stepper.getPosition());
                    Serial.print(F(","));
                    Serial.print(speedToPercentage(Focuser::stepper.getMaxSpeed()));
                    Serial.print(F(","));
                    Serial.print(Focuser::stepper.getAutoPowerTimeout() != 0);
                    Serial.print(F(","));
                    Serial.print(Focuser::stepper.getBacklash());
                    Serial.print(F(","));
                    Serial.print(Focuser::stepper.isDirectionInverted());
                    Serial.print(F("]"));
#if (ENABLED_DEVMAN == true) || (FLAT_PANEL == true)
                    Serial.print(F(";"));
#endif
#endif
#if ENABLE_DEVMAN == true
                    Serial.print(F("D["));
                    Serial.print(F(","));
                    Serial.print(TEMP_HUM_SENSOR);
                    Serial.print(F(","));
                    Serial.print(RTC_SUPPORT);
                    Serial.print(F(","));
#if RTC_SUPPORT != DISABLED
                    Serial.print(Settings::settings.latitude, 3);
                    Serial.print(F(","));
                    Serial.print(Settings::settings.latitude, 3);
                    Serial.print(F(","));
#endif
#if DEVMAN_HAS_AUTO_MODES
                    Serial.print(DevManager::getAutoMode());
#else
                    Serial.print(DevManager::AutoMode::NONE);
#endif
                    Serial.print(F(","));
                    for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
                        DevManager::Pin pin = DevManager::getPin(i);
                        Serial.print(F("("));
                        Serial.print(pin.number);
                        Serial.print(F("%"));
                        Serial.print(pin.value);
                        Serial.print(F("%"));
                        Serial.print(pin.isPwm);
                        if (pin.isPwm) {
                            Serial.print(F("%"));
                            Serial.print(pin.enablePwm);
                        }
                        Serial.print(F("%"));
                        Serial.print(pin.autoModeEn);
                        Serial.print(F(")"));
                    }
                    Serial.print(F("]"));
#if FLAT_PANEL == true
                    Serial.print(F(";"));
#endif
#endif
#if FLAT_PANEL == true
                    Serial.print(F("P["));
                    Serial.print(FlatPanel::lightStatus);
                    Serial.print(F("%"));
                    Serial.print(FlatPanel::brightness);
                    Serial.print(F("%"));
#ifdef SERVO_PIN
                    Serial.print(F("1%"));
                    Serial.print(map(Settings::settings.openServoVal, OPEN_SERVO_170deg, OPEN_SERVO_290deg, 0, 100));
                    Serial.print(F("%"));
                    Serial.print(map(Settings::settings.closedServoVal, CLOSED_SERVO_m15deg, CLOSED_SERVO_15deg, 0, 100));
                    Serial.print(F("%"));
                    Serial.print(map(Settings::settings.servoDelay, SERVO_DELAY_MAX, SERVO_DELAY_MIN, 0, 10));
                    Serial.print(F("%"));
                    Serial.print(FlatPanel::coverStatus);
                    Serial.print(F("]"));
#else
                    Serial.print(F("0]"));
#endif
#endif
                    Serial.println();
#if RTC_SUPPORT != DISABLED
                    Serial.print(F("LStoredTime="));
                    Serial.print(hour());
                    Serial.print(F(":"));
                    Serial.print(minute());
                    Serial.print(F(":"));
                    Serial.print(second());
                    Serial.print(F(" "));
                    Serial.print(day());
                    Serial.print(F("/"));
                    Serial.print(month());
                    Serial.print(F("/"));
                    Serial.print(year());
                    Serial.println(F(" UTC"));
                    updateSunPosition();
#endif
                    break;
                }

#if FOCUSER_DRIVER != DISABLED
                case 'R': {
                    long n = Serial.parseInt();
                    Serial.print(F("LMove="));
                    Serial.println(n);
                    Focuser::stepper.move(n);
                    break;
                }

                case 'A': {
                    long n = Serial.parseInt();
                    Serial.print(F("LGoTo="));
                    Serial.println(n);
                    Focuser::stepper.moveTo(n);
                    break;
                }

                case 'S': {
                    Serial.println(F("LStop"));
                    Focuser::stepper.stop();
                    break;
                }

                case 'P': {
                    long n = Serial.parseInt();
                    Serial.print(F("LSetPos="));
                    Serial.println(n);
                    Focuser::stepper.setPosition(n);
                    return true;
                }

                case 'W': {
                    Serial.println(F("LSetZero"));
                    Focuser::stepper.setPosition(0);
                    return true;
                }

                case 'H': {
                    boolean b = Serial.parseInt();
                    Serial.print(F("LHoldControl="));
                    Serial.println(b);
                    Focuser::stepper.setAutoPowerTimeout(b ? FOCUSER_POWER_TIMEOUT : 0);
                    return true;
                }

                case 'V': {
                    long n = Serial.parseInt();
                    Serial.print(F("LSpeed="));
                    Serial.println(n);
                    Focuser::stepper.setMaxSpeed(percentageToSpeed(n));
                    return true;
                }

                case 'B': {
                    long n = Serial.parseInt();
                    Serial.print(F("LBacklash="));
                    Serial.println(n);
                    Focuser::stepper.setBacklash(n);
                    return true;
                }

                case 'D': {
                    boolean b = Serial.parseInt();
                    Serial.print(F("LDirReverse="));
                    Serial.println(b);
                    Focuser::stepper.setDirectionInverted(b);
                    return true;
                }
#endif

#if ENABLE_DEVMAN == true
                case 'X': {
                    byte pin = Serial.parseInt();
                    byte value = Serial.parseInt();
                    boolean enablePwm = Serial.parseInt();
                    Serial.print(F("LSetPin="));
                    Serial.print(pin);
                    Serial.print(F("@"));
                    Serial.println(value);
                    DevManager::updatePin(pin, enablePwm, value);
                    return true;
                }

#if DEVMAN_HAS_AUTO_MODES
                case 'K': {
                    DevManager::AutoMode mode = (DevManager::AutoMode)Serial.parseInt();
                    Serial.print(F("LSetAutoMode="));
                    Serial.println((int)mode);
                    if (DevManager::setAutoMode(mode)) updatePins();
                    return true;
                }

                case 'Y': {
                    byte pin = Serial.parseInt();
                    boolean autoModeEnabled = Serial.parseInt();
                    Serial.print(F("LSetPinAuto="));
                    Serial.print(pin);
                    Serial.print(F("@"));
                    Serial.println(autoModeEnabled);
                    if (DevManager::setPinAutoModeEn(pin, autoModeEnabled)) updatePins();
                    return true;
                }
#endif

#if RTC_SUPPORT != DISABLED
                case 'T': {
                    unsigned long time = Serial.parseInt();
                    long lat = Serial.parseInt();
                    long lng = Serial.parseInt();
                    if (time != 0) {
                        SunUtil::setRTCTime(time);
                        Serial.print(F("LSetTime="));
                        Serial.println(time);
                    }
                    if ((lat != 0) && (lng != 0)) {
                        Settings::settings.latitude = ((double)lat) / 1000.0;
                        Settings::settings.longitude = ((double)lng) / 1000.0;
                        Serial.print(F("LSetWorldCoord="));
                        Serial.print(lat);
                        Serial.print(F(","));
                        Serial.println(lng);
                        updateSunPosition();
                        return true;
                    }
                    break;
                }
#endif
#endif

#if FLAT_PANEL == true
                case 'Z': {
                    byte value = Serial.parseInt();
                    Serial.print(F("LSetBrightness="));
                    Serial.println(value);
                    FlatPanel::setBrightness(value);
                    break;
                }

                case 'L': {
                    boolean value = Serial.parseInt();
                    Serial.print(F("LSetLight="));
                    Serial.println(value);
                    FlatPanel::setLight(value);
                    break;
                }

#ifdef SERVO_PIN
                case 'Q': {
                    byte value = Serial.parseInt();
                    Serial.print(F("LSetCover="));
                    Serial.println(value);
                    if (value == 0)
                        FlatPanel::setShutter(FlatPanel::CLOSED);
                    else if (value == 1)
                        FlatPanel::setShutter(FlatPanel::OPEN);
                    break;
                }

                case 'F': {
                    Settings::settings.openServoVal = map(constrain(Serial.parseInt(), 0, 100), 0, 100, OPEN_SERVO_170deg, OPEN_SERVO_290deg);
                    Settings::settings.closedServoVal = map(constrain(Serial.parseInt(), 0, 100), 0, 100, CLOSED_SERVO_m15deg, CLOSED_SERVO_15deg);
                    Settings::settings.servoDelay = map(constrain(Serial.parseInt(), 0, 10), 0, 10, SERVO_DELAY_MAX, SERVO_DELAY_MIN);
                    return true;
                }
#endif
#endif
            }
        }
    }
    return false;
}

#if ENABLE_DEVMAN == true
void updatePins() {
    Serial.print(F("Y"));
#if DEVMAN_HAS_AUTO_MODES
    Serial.print(DevManager::getAutoMode());
#else
    Serial.print(DevManager::AutoMode::NONE);
#endif
    Serial.print(F(","));
    for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
        DevManager::Pin pin = DevManager::getPin(i);
        if (pin.autoModeEn) {
            Serial.print(F("("));
            Serial.print(pin.number);
            Serial.print(F("%"));
            Serial.print(pin.value);
            Serial.print(F(")"));
        }
    }
    Serial.println();
}
#endif

#if RTC_SUPPORT != DISABLED
void updateSunPosition() {
    double sunElev = SunUtil::getSunElevation();
    if (!isnan(sunElev)) {
        Serial.print(F("T"));
        Serial.println(sunElev, 2);
    }
}
#endif

inline int speedToPercentage(double speed) { return (speed - FOCUSER_PPS_MIN) * 100.0 / (FOCUSER_PPS_MAX - FOCUSER_PPS_MIN); }

inline double percentageToSpeed(int percentage) { return percentage * (FOCUSER_PPS_MAX - FOCUSER_PPS_MIN) / 100.0 + FOCUSER_PPS_MIN; }
}  // namespace ThunderFocus