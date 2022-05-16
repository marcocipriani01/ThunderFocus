#include "FlatPanel.h"
#if FLAT_PANEL == true

namespace FlatPanel {

#ifdef SERVO_PIN
int targetVal = 0;
int currentVal = 0;
ServoHack servo;
CoverStatus coverStatus;
MotorDirection motorDirection;
#endif
boolean lightStatus;
uint16_t brightness;
uint16_t targetBrightness;
uint16_t currentBrightness;
unsigned long lastBrightnessAdj;

void begin() {
    servo.attach(SERVO_PIN);
    if (Settings::settings.coverStatus == OPEN) {
        servo.write(Settings::settings.openVal);
        currentVal = Settings::settings.openVal;
        coverStatus = OPEN;
    } else {
        servo.write(Settings::settings.closedVal);
        currentVal = Settings::settings.closedVal;
    }
#if EL_PANEL_ON_BOOT == true
    analogWrite(EL_PANEL_PIN, 255);
    currentBrightness = 255;
    targetBrightness = 255;
    lightStatus = false;
#else
    analogWrite(EL_PANEL_PIN, 0);
#endif
}

#ifdef SERVO_PIN
void setShutter(int val) {
    if (val == OPEN && coverStatus != OPEN) {
        motorDirection = OPENING;
        targetVal = Settings::settings.openVal;
        targetBrightness = 0;
        currentBrightness = 0;
        analogWrite(EL_PANEL_PIN, 0);
    } else if (val == CLOSED && coverStatus != CLOSED) {
        motorDirection = CLOSING;
        targetVal = Settings::settings.closedVal;
    }
}
#endif

void setLight(boolean val) {
    lightStatus = val;
    if (val) {
#ifdef SERVO_PIN
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
    brightness = constrain((int)(255.0 * log10(val + 1.0) / log10(256.0)), 0, 255);
#else
    brightness = val;
#endif
#ifdef SERVO_PIN
    if (lightStatus && (coverStatus == CLOSED)) targetBrightness = brightness;
#else
    if (lightStatus == ON) targetBrightness = brightness;
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
#ifdef SERVO_PIN
    if ((motorDirection != NONE) && ((t - lastMoveTime) >= Settings::settings.servoDelay)) {
        if ((currentVal > targetVal) && (motorDirection == OPENING)) {
            coverStatus = NEITHER_OPEN_NOR_CLOSED;
            currentVal -= SERVO_STEP_SIZE;
            servo.write(currentVal);
            if (currentVal <= targetVal) {
                motorDirection = NONE;
                coverStatus = OPEN;
                Settings::settings.coverStatus = OPEN;
                Settings::requestSave = true;
            }
        } else if ((currentVal < targetVal) && (motorDirection == CLOSING)) {
            coverStatus = NEITHER_OPEN_NOR_CLOSED;
            currentVal += SERVO_STEP_SIZE;
            servo.write(currentVal);
            if (currentVal >= targetVal) {
                motorDirection = NONE;
                coverStatus = CLOSED;
                Settings::settings.coverStatus = CLOSED;
                Settings::requestSave = true;
                if (lightStatus == true) targetBrightness = brightness;
            }
        } else {
            motorDirection = NONE;
        }
    }
#endif
}
}  // namespace FlatPanel

#endif