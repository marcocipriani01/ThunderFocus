#include "ThunderFocusProtocol.h"

namespace ThunderFocus {
#if FOCUSER_DRIVER != DISABLED
FocuserState lastFocuserState = FocuserState::POWER_SAVE;
unsigned long focuserSyncTime = 0L;
#endif
#if TEMP_HUM_SENSOR != DISABLED
unsigned long sensorsSyncTime = 0L;
#endif
#if RTC_SUPPORT != DISABLED
unsigned long sunSyncTime = 0L;
#endif
#if FLAT_PANEL != false
unsigned long flatPanelSyncTime = 0L;
#endif

void setup() {
    Serial.begin(SERIAL_SPEED);
    Serial.setTimeout(SERIAL_TIMEOUT);
#if DEBUG_EN
    Serial.println(F(">Boot"));
#endif
#if SETTINGS_SUPPORT == true
    Settings::load();
#if DEBUG_EN
    Serial.println(F(">Settings loading"));
#endif
#endif
#if FOCUSER_DRIVER != DISABLED
    Focuser::begin();
#endif
#if ENABLE_DEVMAN == true
    DevManager::begin();
    for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
        DevManager::Pin pin = Settings::settings.devManPins[i];
        DevManager::setPinPwmEn(pin.number, pin.enablePwm);
#if DEVMAN_HAS_AUTO_MODES
        if (pin.autoModeEn)
            DevManager::setPinAutoModeEn(pin.number, true);
        else
            DevManager::updatePin(pin.number, pin.value);
#else
        DevManager::updatePin(pin.number, pin.value);
#endif
    }
#if DEVMAN_HAS_AUTO_MODES
    DevManager::setAutoMode(Settings::settings.devManAutoMode);
#endif
#endif
#if FLAT_PANEL == true
    FlatPanel::begin();
#endif
#if DEBUG_EN
    Serial.println(F(">Setup done"));
#endif
}

void run() {
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
    if ((t - focuserSyncTime) >= FOCUSER_SYNC_INTERVAL) {
        Serial.print(F("S"));
        Serial.println(Focuser::stepper.getPosition());
        focuserSyncTime = t;
    }
#endif

#if ENABLE_DEVMAN == true
    if (DevManager::run()) updatePins();
#if TEMP_HUM_SENSOR != DISABLED
    AmbientManger::run();
    if ((t - sensorsSyncTime) >= SENSORS_SYNC_INTERVAL) {
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

#if FLAT_PANEL != false
    if ((t - flatPanelSyncTime) >= FLAT_SYNC_INTERVAL) {
        Serial.print(F("E"));
        Serial.println(FlatPanel::coverStatus);
        flatPanelSyncTime = t;
    }
#endif
}

void serialEvent() {
    while (Serial.available()) {
        if (Serial.read() != '$') continue;
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
                Serial.print(Focuser::stepper.getBacklash());
                Serial.print(F(","));
                Serial.print(Focuser::stepper.isDirectionInverted());
                Serial.print(F(","));
                Serial.print(Focuser::stepper.getAutoPowerTimeout() != 0);
                Serial.print(F("]"));
#if (ENABLED_DEVMAN == true) || (FLAT_PANEL == true)
                Serial.print(F(";"));
#endif
#endif
#if ENABLE_DEVMAN == true
                Serial.print(F("D["));
                Serial.print(TEMP_HUM_SENSOR != DISABLED);
                Serial.print(F(","));
                Serial.print(RTC_SUPPORT != DISABLED);
                Serial.print(F(","));
#if RTC_SUPPORT != DISABLED
                Serial.print(Settings::settings.latitude, 3);
                Serial.print(F(","));
                Serial.print(Settings::settings.longitude, 3);
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
                Serial.print(F(","));
                Serial.print(FlatPanel::brightness);
                Serial.print(F(","));
#if SERVO_MOTOR != DISABLED
                Serial.print(F("1,"));
                Serial.print(map(Settings::settings.openServoVal, OPEN_SERVO_170deg, OPEN_SERVO_290deg, 170, 290));
                Serial.print(F(","));
                Serial.print(map(Settings::settings.closedServoVal, CLOSED_SERVO_m15deg, CLOSED_SERVO_15deg, -15, 15));
                Serial.print(F(","));
                Serial.print(map(Settings::settings.servoDelay, SERVO_DELAY_MAX, SERVO_DELAY_MIN, 0, 10));
                Serial.print(F(","));
                Serial.print(FlatPanel::coverStatus);
                Serial.print(F("]"));
#else
                Serial.print(F("0]"));
#endif
#endif
                Serial.println();
#if RTC_SUPPORT != DISABLED
                updateSunPosition();
#endif
                break;
            }

#if FOCUSER_DRIVER != DISABLED
            case 'R': {
                long n = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">Move="));
#endif
                Serial.println(n);
                Focuser::stepper.move(n);
                break;
            }

            case 'A': {
                long n = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">GoTo="));
                Serial.println(n);
#endif
                Focuser::stepper.moveTo(n);
                break;
            }

            case 'S': {
#if DEBUG_EN
                Serial.println(F(">Stop"));
#endif
                Focuser::stepper.stop();
                break;
            }

            case 'P': {
                long n = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">SetPos="));
                Serial.println(n);
#endif
                Focuser::stepper.setPosition(n);
                Settings::requestSave = true;
                break;
            }

            case 'W': {
#if DEBUG_EN
                Serial.println(F(">SetZero"));
#endif
                Focuser::stepper.setPosition(0);
                Settings::requestSave = true;
                break;
            }

            case 'H': {
                boolean b = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">HoldControl="));
                Serial.println(b);
#endif
                Focuser::stepper.setAutoPowerTimeout(b ? FOCUSER_POWER_TIMEOUT : 0);
                Settings::requestSave = true;
                break;
            }

            case 'V': {
                long n = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">Speed="));
                Serial.println(n);
#endif
                Focuser::stepper.setMaxSpeed(percentageToSpeed(n));
                Settings::requestSave = true;
                break;
            }

            case 'B': {
                long n = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">Backlash="));
                Serial.println(n);
#endif
                Focuser::stepper.setBacklash(n);
                Settings::requestSave = true;
                break;
            }

            case 'D': {
                boolean b = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">DirReverse="));
                Serial.println(b);
#endif
                Focuser::stepper.setDirectionInverted(b);
                Settings::requestSave = true;
                break;
            }
#endif

#if ENABLE_DEVMAN == true
            case 'X': {
                byte pin = Serial.parseInt();
                byte value = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">SetPin="));
                Serial.print(pin);
                Serial.print(F("->"));
                Serial.println(value);
#endif
                DevManager::updatePin(pin, value);
                Settings::requestSave = true;
                break;
            }

            case 'J': {
                byte pin = Serial.parseInt();
                boolean en = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">SetPinPwmEn="));
                Serial.print(pin);
                Serial.print(F("->"));
                Serial.println(en);
#endif
                if (DevManager::setPinPwmEn(pin, en)) updatePins();
                Settings::requestSave = true;
            }

#if DEVMAN_HAS_AUTO_MODES
            case 'K': {
                DevManager::AutoMode mode = (DevManager::AutoMode)Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">SetAutoMode="));
                Serial.println((int)mode);
#endif
                if (DevManager::setAutoMode(mode)) updatePins();
                Settings::requestSave = true;
                break;
            }

            case 'Y': {
                byte pin = Serial.parseInt();
                boolean autoModeEnabled = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">SetPinAuto="));
                Serial.print(pin);
                Serial.print(F("->"));
                Serial.println(autoModeEnabled);
#endif
                if (DevManager::setPinAutoModeEn(pin, autoModeEnabled)) updatePins();
                Settings::requestSave = true;
                break;
            }
#endif

#if RTC_SUPPORT != DISABLED
            case 'T': {
                unsigned long time = Serial.parseInt();
                long lat = Serial.parseInt();
                long lng = Serial.parseInt();
                if (time != 0) {
                    SunUtil::setRTCTime(time);
#if DEBUG_EN
                    Serial.print(F(">RTC time: "));
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
#endif
                }
                if ((lat != 0) && (lng != 0)) {
                    Settings::settings.latitude = ((double)lat) / 1000.0;
                    Settings::settings.longitude = ((double)lng) / 1000.0;
#if DEBUG_EN
                    Serial.print(F(">SetWorldCoord="));
                    Serial.print(lat);
                    Serial.print(F(","));
                    Serial.println(lng);
#endif
                    Settings::requestSave = true;
                }
                updateSunPosition();
                break;
            }
#endif
#endif

#if FLAT_PANEL == true
            case 'Z': {
                byte value = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">SetBrightness="));
                Serial.println(value);
#endif
                FlatPanel::setBrightness(value);
                break;
            }

            case 'L': {
                boolean value = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">SetLight="));
                Serial.println(value);
#endif
                FlatPanel::setLight(value);
                break;
            }

#if SERVO_MOTOR != DISABLED
            case 'Q': {
                byte value = Serial.parseInt();
#if DEBUG_EN
                Serial.print(F(">SetCover="));
                Serial.println(value);
#endif
                if (value == 0)
                    FlatPanel::setShutter(FlatPanel::CLOSED);
                else if (value == 1)
                    FlatPanel::setShutter(FlatPanel::OPEN);
                break;
            }

            case 'F': {
                Settings::settings.openServoVal = map(constrain(Serial.parseInt(), 170, 290), 170, 290, OPEN_SERVO_170deg, OPEN_SERVO_290deg);
                Settings::settings.closedServoVal = map(constrain(Serial.parseInt(), -15, 15), -15, 15, CLOSED_SERVO_m15deg, CLOSED_SERVO_15deg);
                Settings::settings.servoDelay = map(constrain(Serial.parseInt(), 0, 10), 0, 10, SERVO_DELAY_MAX, SERVO_DELAY_MIN);
#if DEBUG_EN
                Serial.print(F(">SetServoConfig="));
                Serial.print(Settings::settings.openServoVal);
                Serial.print(F(", "));
                Serial.print(Settings::settings.closedServoVal);
                Serial.print(F(", "));
                Serial.println(Settings::settings.servoDelay);
#endif
                Settings::requestSave = true;
                break;
            }
#endif
#endif
        }
    }
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
        Serial.print(F("("));
        Serial.print(pin.number);
        Serial.print(F("%"));
        Serial.print(pin.value);
        Serial.print(F(")"));
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

#if FOCUSER_DRIVER != DISABLED
inline int speedToPercentage(double speed) { return (speed - FOCUSER_PPS_MIN) * 100.0 / (FOCUSER_PPS_MAX - FOCUSER_PPS_MIN); }

inline double percentageToSpeed(int percentage) { return percentage * (FOCUSER_PPS_MAX - FOCUSER_PPS_MIN) / 100.0 + FOCUSER_PPS_MIN; }
#endif
}  // namespace ThunderFocus