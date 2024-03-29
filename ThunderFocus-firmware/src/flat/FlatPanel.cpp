#include "FlatPanel.h"
#if FLAT_PANEL == true

namespace FlatPanel {

#if SERVO_MOTOR != OFF
ServoHack servo;
uint16_t targetVal = 0;
uint16_t currentVal = 0;
unsigned long lastMoveTime = 0L;
CoverStatus coverStatus = CLOSED;
MotorDirection motorDirection = NONE;
#endif
boolean lightStatus = false;
uint16_t brightness = 255;
uint16_t targetBrightness = 0;
uint16_t currentBrightness = 0;
unsigned long lastBrightnessAdj = 0L;

void begin() {
#if SERVO_MOTOR != OFF
    servo.attach(SERVO_PIN);
    if (Settings::settings.coverStatus == OPEN) {
        servo.write(Settings::settings.openServoVal);
        currentVal = Settings::settings.openServoVal;
        coverStatus = OPEN;
    } else {
        servo.write(Settings::settings.closedServoVal);
        currentVal = Settings::settings.closedServoVal;
    }
#endif
    pinMode(EL_PANEL_PIN, OUTPUT);
#if EL_PANEL_ON_BOOT == true
    analogWrite(EL_PANEL_PIN, 255);
    currentBrightness = 255;
    targetBrightness = 255;
    lightStatus = true;
#else
    analogWrite(EL_PANEL_PIN, 0);
#endif
}

#if SERVO_MOTOR != OFF
void setShutter(int val) {
    if ((val == OPEN) && (coverStatus != OPEN)) {
        motorDirection = OPENING;
        coverStatus = NEITHER_OPEN_NOR_CLOSED;
        targetVal = Settings::settings.openServoVal;
        targetBrightness = 0;
    } else if ((val == CLOSED) && (coverStatus != CLOSED)) {
        motorDirection = CLOSING;
        coverStatus = NEITHER_OPEN_NOR_CLOSED;
        targetVal = Settings::settings.closedServoVal;
    }
}

void halt() {
    if (motorDirection != NONE) {
        motorDirection = NONE;
        coverStatus = HALT;
        targetVal = currentVal;
    }
}
#endif

void setLight(boolean val) {
    lightStatus = val;
    if (val) {
#if SERVO_MOTOR != OFF
        if (coverStatus == CLOSED) targetBrightness = brightness;
#else
        targetBrightness = brightness;
#endif
    } else {
        targetBrightness = 0;
    }
}

void setBrightness(int val) {
#if EL_PANEL_LOG_SCALE == true
    brightness = constrain((int)(exp(log(256.0) * (val / 255.0)) - 1.0), 0, 255);
#else
    brightness = val;
#endif
#if SERVO_MOTOR != OFF
    if (lightStatus && (coverStatus == CLOSED)) targetBrightness = brightness;
#else
    if (lightStatus) targetBrightness = brightness;
#endif
}

void run() {
    unsigned long t = millis();
    if ((t - lastBrightnessAdj) >= EL_PANEL_FADE_DELAY) {
        if (currentBrightness < targetBrightness) {
            currentBrightness++;
            analogWrite(EL_PANEL_PIN, currentBrightness);
        } else if (currentBrightness > targetBrightness) {
            currentBrightness--;
            analogWrite(EL_PANEL_PIN, currentBrightness);
        }
        lastBrightnessAdj = t;
    }
#if SERVO_MOTOR != OFF
    if ((motorDirection != NONE) && ((t - lastMoveTime) >= Settings::settings.servoDelay)) {
        if ((currentVal > targetVal) && (motorDirection == OPENING)) {
            currentVal -= SERVO_STEP_SIZE;
            servo.write(currentVal);
            if (currentVal <= targetVal) {
                motorDirection = NONE;
                coverStatus = OPEN;
                Settings::settings.coverStatus = OPEN;
                Settings::requestSave = true;
            }
        } else if ((currentVal < targetVal) && (motorDirection == CLOSING)) {
            currentVal += SERVO_STEP_SIZE;
            servo.write(currentVal);
            if (currentVal >= targetVal) {
                motorDirection = NONE;
                coverStatus = CLOSED;
                Settings::settings.coverStatus = CLOSED;
                Settings::requestSave = true;
                if (lightStatus) targetBrightness = brightness;
            }
        } else {
            motorDirection = NONE;
        }
        lastMoveTime = t;
    }
#endif
}
}  // namespace FlatPanel

#endif