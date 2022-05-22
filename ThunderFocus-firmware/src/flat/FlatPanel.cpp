#include "FlatPanel.h"
#if FLAT_PANEL == true

namespace FlatPanel {

#if SERVO_MOTOR != DISABLED
int targetVal = 0;
int currentVal = 0;
ServoHack servo;
CoverStatus coverStatus = CLOSED;
MotorDirection motorDirection = NONE;
#endif
boolean lightStatus = 0;
uint16_t brightness = 0;
uint16_t targetBrightness = 0;
uint16_t currentBrightness = 0;
unsigned long lastBrightnessAdj = 0L;

void begin() {
    servo.attach(SERVO_PIN);
    if (Settings::settings.coverStatus == OPEN) {
        servo.write(Settings::settings.openServoVal);
        currentVal = Settings::settings.openServoVal;
        coverStatus = OPEN;
    } else {
        servo.write(Settings::settings.closedServoVal);
        currentVal = Settings::settings.closedServoVal;
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

#if SERVO_MOTOR != DISABLED
void setShutter(int val) {
    if (val == OPEN && coverStatus != OPEN) {
        motorDirection = OPENING;
        targetVal = Settings::settings.openServoVal;
        targetBrightness = 0;
        currentBrightness = 0;
        analogWrite(EL_PANEL_PIN, 0);
    } else if (val == CLOSED && coverStatus != CLOSED) {
        motorDirection = CLOSING;
        targetVal = Settings::settings.closedServoVal;
    }
}
#endif

void setLight(boolean val) {
    lightStatus = val;
    if (val) {
#if SERVO_MOTOR != DISABLED
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
#if SERVO_MOTOR != DISABLED
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
#if SERVO_MOTOR != DISABLED
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