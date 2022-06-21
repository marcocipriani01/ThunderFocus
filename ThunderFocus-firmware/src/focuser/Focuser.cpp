#include "Focuser.h"
#if FOCUSER_DRIVER != OFF

namespace Focuser {
#if FOCUSER_DRIVER == BIPOLAR
AccelStepper stepper(FOCUSER_STEP, FOCUSER_DIR);
#elif FOCUSER_DRIVER == UNIPOLAR
AccelStepper stepper(FOCUSER_IN1, FOCUSER_IN2, FOCUSER_IN3, FOCUSER_IN4);
#endif

void begin() {
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
    stepper.setAcceleration(FOCUSER_ACCEL);
#ifdef FOCUSER_STEPS_SCALING
    stepper.setStepsScaling(FOCUSER_STEPS_SCALING);
#endif
#ifdef FOCUSER_EN
    stepper.setEnablePin(FOCUSER_EN, false);
#endif
#if SETTINGS_SUPPORT == true
    stepper.setPosition(Settings::settings.focuserPosition);
    stepper.setMaxSpeed(Settings::settings.focuserSpeed);
    stepper.setBacklash(Settings::settings.focuserBacklash);
    stepper.setDirectionInverted(Settings::settings.focuserReverse);
    stepper.setAutoPowerTimeout(Settings::settings.focuserPowerSave ? FOCUSER_POWER_TIMEOUT : 0);
#else
    stepper.setPosition(0L);
    stepper.setMaxSpeed((FOCUSER_PPS_MAX + FOCUSER_PPS_MIN) / 2.0);
    stepper.setBacklash(0L);
    stepper.setDirectionInverted(false);
#ifdef FOCUSER_EN
    stepper.setAutoPowerTimeout(FOCUSER_POWER_TIMEOUT);
#else
    stepper.setAutoPowerTimeout(0);
#endif
#endif
}

#if SETTINGS_SUPPORT == true
void updateSettings() {
    Settings::settings.focuserPosition = stepper.getPosition();
    Settings::settings.focuserSpeed = stepper.getMaxSpeed();
    Settings::settings.focuserPowerSave = (stepper.getAutoPowerTimeout() > 0L);
    Settings::settings.focuserBacklash = stepper.getBacklash();
    Settings::settings.focuserReverse = stepper.isDirectionInverted();
    Settings::requestSave = true;
}
#endif

}  // namespace Focuser
#endif