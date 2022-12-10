#include "DevManager.h"
#if ENABLE_DEVMAN == true

namespace DevManager {
Pin pins[] = MANAGED_PINS;
#if DEVMAN_HAS_AUTO_MODES
AutoMode autoMode = AutoMode::NONE;
unsigned long lastUpdateTime = 0L;
#endif

void begin() {
#if RTC_SUPPORT != OFF
    SunUtil::begin();
#endif
#if TEMP_HUM_SENSOR != OFF
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

boolean run() {
#if ENABLE_PFI == true
    int val = analogRead(PFI_KNOB);
    analogWrite(PFI_LED, (val > PFI_THRESHOLD) ? map(val, PFI_THRESHOLD, ANALOG_READ_MAX_VALUE, 0, 255) : 0);
#endif
#if DEVMAN_HAS_AUTO_MODES
    return processAutoMode(false);
#else
    return false;
#endif
}

void updateSettings() {
    for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
        Settings::settings.devManPins[i] = getPin(i);
    }
#if DEVMAN_HAS_AUTO_MODES
    Settings::settings.devManAutoMode = autoMode;
#else
    Settings::settings.devManAutoMode = AutoMode::NONE;
#endif
    Settings::requestSave = true;
}

Pin getPin(uint8_t index) { return pins[index]; }

void updatePin(uint8_t pin, uint8_t value) {
    for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
        if (pin == pins[i].number) {
            pins[i].autoModeEn = false;
            if (pins[i].value == value) return;
            if (pins[i].enablePwm) {
                pins[i].value = value;
                switch (value) {
                    case 0:
                        analogWrite(pin, 0);
                        digitalWrite(pin, LOW);
                        break;
                    case 255:
                        analogWrite(pin, 0);
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

#if DEVMAN_HAS_AUTO_MODES
boolean processAutoMode(boolean force) {
    boolean hasChanged = false;
    if (autoMode != AutoMode::NONE) {
        unsigned long t = millis();
        if (force || ((t - lastUpdateTime) >= DEVMAN_UPDATE_INTERVAL)) {
            switch (autoMode) {
#if RTC_SUPPORT != OFF
                case AutoMode::NIGHT_ASTRONOMICAL: {
                    double sunElevation = SunUtil::getSunElevation();
                    if (!isnan(sunElevation))
                        hasChanged = forEachAutoPin(pwmMap(sunElevation, -15.0, -21.0), sunElevation <= -18.0);
                    break;
                }
                case AutoMode::NIGHT_CIVIL: {
                    double sunElevation = SunUtil::getSunElevation();
                    if (!isnan(sunElevation))
                        hasChanged = forEachAutoPin(pwmMap(sunElevation, -3.0, -9.0), sunElevation <= -6.0);
                    break;
                }
                case AutoMode::DAYTIME: {
                    double sunElevation = SunUtil::getSunElevation();
                    if (!isnan(sunElevation))
                        hasChanged = forEachAutoPin(pwmMap(sunElevation, 0.0, 3.0), sunElevation > 0.0);
                    break;
                }
#endif

#if TEMP_HUM_SENSOR != OFF
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

boolean setPinAutoModeEn(uint8_t pin, boolean enabled) {
    for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
        if (pin == pins[i].number) {
            if (pins[i].autoModeEn == enabled)
                return false;
            pins[i].autoModeEn = enabled;
            if (enabled) {
                return processAutoMode(true);
            } else {
                boolean upd = (pins[i].value != 0);
                pins[i].value = 0;
                if (pins[i].isPwm)
                    analogWrite(pin, 0);
                digitalWrite(pin, LOW);
                return upd;
            }
        }
    }
    return false;
}

boolean setPinPwmEn(uint8_t pin, boolean pwmEn) {
    for (byte i = 0; i < MANAGED_PINS_COUNT; i++) {
        if (pin == pins[i].number) {
            if (pins[i].enablePwm == pwmEn)
                return false;
            if (pins[i].isPwm) {
                pins[i].enablePwm = pwmEn;
                if (pins[i].autoModeEn) {
                    return processAutoMode(true);
                } else if (pwmEn) {
                    return false;
                } else {
                    uint8_t oldVal = pins[i].value;
                    boolean newVal = (oldVal > 100);
                    pins[i].value = newVal ? 255 : 0;
                    if (oldVal != pins[i].value) {
                        analogWrite(pins[i].number, 0);
                        digitalWrite(pins[i].number, newVal);
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
    }
    return false;
}

AutoMode getAutoMode() { return autoMode; }

boolean setAutoMode(AutoMode am) {
    switch (am) {
#if RTC_SUPPORT != OFF
        case AutoMode::NIGHT_ASTRONOMICAL:
        case AutoMode::NIGHT_CIVIL:
        case AutoMode::DAYTIME: {
            autoMode = am;
            return updateAutoMode();
        }
#endif
#if TEMP_HUM_SENSOR != OFF
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
#endif
        default: {
            autoMode = AutoMode::NONE;
            return updateAutoMode();
        }
    }
    return false;
}

boolean forEachAutoPin(int pwm, boolean digital) {
    boolean hasChanged = false;
    for (int i = 0; i < MANAGED_PINS_COUNT; i++) {
        if (pins[i].autoModeEn) {
            int pin = pins[i].number;
            if (pins[i].isPwm && pins[i].enablePwm) {
                if (pwm != pins[i].value) {
                    pins[i].value = pwm;
                    switch (pwm) {
                        case 0:
                            analogWrite(pin, 0);
                            digitalWrite(pin, LOW);
                            break;
                        case 255:
                            analogWrite(pin, 0);
                            digitalWrite(pin, HIGH);
                            break;
                        default:
                            analogWrite(pin, pwm);
                            break;
                    }
                    hasChanged = true;
                }
            } else {
                int val = digital ? 255 : 0;
                if (val != pins[i].value) {
                    pins[i].value = val;
                    digitalWrite(pin, digital);
                    hasChanged = true;
                }
            }
        }
    }
    return hasChanged;
}

boolean updateAutoMode() {
    Settings::settings.devManAutoMode = autoMode;
    Settings::requestSave = true;
    return processAutoMode(true);
}

int pwmMap(double in, double min, double max) {
    if (in <= min) return 0;
    if (in >= max) return 255;
    return (int)((in - min) * (255.0 - DEVMAN_PWM_THRESHOLD) / (max - min) + DEVMAN_PWM_THRESHOLD);
}
#endif

#if TEMP_HUM_SENSOR != OFF
boolean processDewPoint(double triggerDiff) {
    double dewPoint = AmbientManger::getDewPoint(), temperature = AmbientManger::getTemperature();
    if ((temperature != TEMP_ABSOLUTE_ZERO) && (dewPoint != TEMP_ABSOLUTE_ZERO))
        return forEachAutoPin(pwmMap(dewPoint, temperature - triggerDiff, temperature - (triggerDiff * DEVMAN_OFFSET_FACTOR)),
                              (temperature - dewPoint - (triggerDiff * DEVMAN_OFFSET_FACTOR)) <= triggerDiff);
    return false;
}

boolean processHumidity(double triggerHum) {
    double humidity = AmbientManger::getHumidity();
    if (humidity != HUMIDITY_INVALID) return forEachAutoPin(pwmMap(humidity, triggerHum - 5.0, triggerHum + 5.0), humidity >= triggerHum);
    return false;
}
#endif
}  // namespace DevManager
#endif