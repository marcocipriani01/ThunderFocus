#include "DevManager.h"
#if ENABLE_DEVMAN == true

using namespace DevManager;

Pin pins[] = MANAGED_PINS;
AutoMode autoMode = AutoMode::NONE;
unsigned long lastUpdateTime = 0L;
#if RTC_SUPPORT != DISABLED
double sunElevation = 0;
#endif

void DevManager::begin() {
#if RTC_SUPPORT != DISABLED
    SunUtil::begin();
#endif
#if TEMP_HUM_SENSOR != DISABLED
    AmbientManger::begin();
#endif
    for (uint8_t i = 0; i < MANAGED_PINS_COUNT; i++) {
        pinMode(pins[i].number, OUTPUT);
        pins[i].value = 0;
        digitalWrite(pins[i].number, LOW);
    }

#if ENABLE_PFI == true
    pinMode(PFI_LED, OUTPUT);
#endif
}

boolean DevManager::run() {
#if ENABLE_PFI == true
    int val = analogRead(PFI_KNOB);
    analogWrite(PFI_LED, (val > PFI_THRESHOLD) ? map(val, PFI_THRESHOLD, 1023, 0, 255) : 0);
#endif
    return processAutoMode(false);
}

boolean DevManager::processAutoMode(boolean force) {
    boolean hasChanged = false;
    if (autoMode != AutoMode::NONE) {
        unsigned long t = millis();
        if (force || ((t - lastUpdateTime) >= DEVMAN_UPDATE_INTERVAL)) {
            switch (autoMode) {
#if RTC_SUPPORT != DISABLED
                case AutoMode::NIGHT_ASTRONOMICAL: {
                    double sunElevation = SunUtil::getSunElevation();
                    hasChanged = forEachAutoPin(pwmMap(sunElevation, -15.0, -21.0), sunElevation <= -18.0);
                    break;
                }
                case AutoMode::NIGHT_CIVIL: {
                    double sunElevation = SunUtil::getSunElevation();
                    hasChanged = forEachAutoPin(pwmMap(sunElevation, -3.0, -9.0), sunElevation <= -6.0);
                    break;
                }
                case AutoMode::DAYTIME: {
                    double sunElevation = SunUtil::getSunElevation();
                    hasChanged = forEachAutoPin(pwmMap(sunElevation, 0.0, 3.0), sunElevation > 0.0);
                    break;
                }
#endif

#if TEMP_HUM_SENSOR != DISABLED
                case AutoMode::DEW_POINT_DIFF1: {
                    hasChanged = processDewPoint(1.0);
                    break;
                }
                case AutoMode::DEW_POINT_DIFF2: {
                    hasChanged = processDewPoint(2.0);
                    break;
                }
                case AutoMode::DEW_POINT_DIFF3: {
                    hasChanged = processDewPoint(3.0);
                    break;
                }
                case AutoMode::DEW_POINT_DIFF5: {
                    hasChanged = processDewPoint(5.0);
                    break;
                }
                case AutoMode::DEW_POINT_DIFF7: {
                    hasChanged = processDewPoint(7.0);
                    break;
                }

                case AutoMode::HUMIDITY_90: {
                    hasChanged = processHumidity(90.0);
                    break;
                }
                case AutoMode::HUMIDITY_80: {
                    hasChanged = processHumidity(80.0);
                    break;
                }
                case AutoMode::HUMIDITY_70: {
                    hasChanged = processHumidity(70.0);
                    break;
                }

                case AutoMode::TEMP_FREEZE: {
                    double temperature = AmbientManger::getTemperature();
                    if (temperature != TEMP_ABSOLUTE_ZERO) {
                        hasChanged = forEachAutoPin(pwmMap(temperature, 4.0, 2.0), temperature <= 3.0);
                    }
                    break;
                }
#endif
                default: {
                    autoMode = AutoMode::NONE;
                    break;
                }
            }
            lastUpdateTime = t;
        }
    }
    if (hasChanged) updateSettings();
    return hasChanged;
}

Pin DevManager::getPin(uint8_t index) { return pins[index]; }

void DevManager::updatePin(uint8_t pin, uint8_t value) {
    for (uint8_t i = 0; i < MANAGED_PINS_COUNT; i++) {
        if (pin == pins[i].number) {
            if (pins[i].value == value) return;
            pins[i].autoModeEn = false;
            if (pins[i].isPwm) {
                pins[i].value = value;
                switch (value) {
                    case 0:
                        digitalWrite(pin, LOW);
                        break;
                    case 255:
                        digitalWrite(pin, HIGH);
                        break;
                    default:
                        analogWrite(pin, value);
                        break;
                }
            } else {
                boolean bVal = (value > 100);
                pins[i].value = bVal ? 255 : 0;
                digitalWrite(pins[i].number, bVal);
            }
            updateSettings();
            return;
        }
    }
}

boolean DevManager::setPinAutoModeEn(uint8_t pin, boolean enabled) {
    for (uint8_t i = 0; i < MANAGED_PINS_COUNT; i++) {
        if (pin == pins[i].number) {
            pins[i].autoModeEn = enabled;
            return processAutoMode(true);
        }
    }
    return false;
}

AutoMode DevManager::getAutoMode() { return autoMode; }

boolean DevManager::setAutoMode(AutoMode am) {
    switch (am) {
#if RTC_SUPPORT != DISABLED
        case AutoMode::NIGHT_ASTRONOMICAL:
        case AutoMode::NIGHT_CIVIL:
        case AutoMode::DAYTIME: {
            autoMode = am;
            return updateAutoMode();
        }
#endif
#if TEMP_HUM_SENSOR != DISABLED
        case AutoMode::DEW_POINT_DIFF1:
        case AutoMode::DEW_POINT_DIFF2:
        case AutoMode::DEW_POINT_DIFF3:
        case AutoMode::DEW_POINT_DIFF5:
        case AutoMode::DEW_POINT_DIFF7:
        case AutoMode::HUMIDITY_90:
        case AutoMode::HUMIDITY_80:
        case AutoMode::HUMIDITY_70:
        case AutoMode::TEMP_FREEZE: {
            autoMode = am;
            return updateAutoMode();
        }
        default: {
            autoMode = AutoMode::NONE;
            return updateAutoMode();
        }
#endif
    }
    return false;
}

boolean DevManager::updateAutoMode() {
    Settings::settings.devManAutoMode = DevManager::autoMode;
    Settings::requestSave = true;
    return processAutoMode(true);
}

void DevManager::updateSettings() {
    for (uint8_t i = 0; i < MANAGED_PINS_COUNT; i++) {
        Settings::settings.devManPins[i] = DevManager::getPin(i);
    }
    Settings::settings.devManAutoMode = DevManager::autoMode;
    Settings::requestSave = true;
}

int DevManager::pwmMap(double in, double min, double max) {
    if (in <= min) return 0;
    if (in >= max) return 255;
    return (int)((in - min) * (255.0 - DEVMAN_PWM_THRESHOLD) / (max - min) + DEVMAN_PWM_THRESHOLD);
}

boolean DevManager::forEachAutoPin(int pwm, boolean digital) {
    boolean hasChanged = false;
    for (int i = 0; i < MANAGED_PINS_COUNT; i++) {
        if (pins[i].autoModeEn) {
            if (pins[i].isPwm) {
                if (pwm != pins[i].value) {
                    pins[i].value = pwm;
                    switch (pwm) {
                        case 0:
                            digitalWrite(pins[i].number, LOW);
                            break;
                        case 255:
                            digitalWrite(pins[i].number, HIGH);
                            break;
                        default:
                            analogWrite(pins[i].number, pwm);
                            break;
                    }
                    hasChanged = true;
                }
            } else {
                int val = digital ? 255 : 0;
                if (val != pins[i].value) {
                    pins[i].value = val;
                    digitalWrite(pins[i].number, digital);
                    hasChanged = true;
                }
            }
        }
    }
    return hasChanged;
}

#if TEMP_HUM_SENSOR != DISABLED
boolean DevManager::processDewPoint(double triggerDiff) {
    double dewPoint = AmbientManger::getDewPoint(), temperature = AmbientManger::getTemperature();
    if ((temperature != TEMP_ABSOLUTE_ZERO) && (dewPoint != TEMP_ABSOLUTE_ZERO))
        return forEachAutoPin(pwmMap(dewPoint, temperature - triggerDiff, temperature - (triggerDiff * DEVMAN_OFFSET_FACTOR)),
                              (temperature - dewPoint - (triggerDiff * DEVMAN_OFFSET_FACTOR)) <= triggerDiff);
    return false;
}

boolean DevManager::processHumidity(double triggerHum) {
    double humidity = AmbientManger::getHumidity();
    if (humidity != HUMIDITY_INVALID) return forEachAutoPin(pwmMap(humidity, triggerHum - 5.0, triggerHum + 5.0), humidity >= triggerHum);
    return false;
}
#endif
#endif