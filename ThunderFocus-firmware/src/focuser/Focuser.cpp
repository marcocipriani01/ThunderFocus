#include "Focuser.h"
#if FOCUSER_DRIVER != DISABLED

using namespace Focuser;

#if FOCUSER_DRIVER == BIPOLAR
AccelStepper stepper(FOCUSER_STEP, FOCUSER_DIR);
#elif FOCUSER_DRIVER == UNIPOLAR
AccelStepper stepper(FOCUSER_IN1, FOCUSER_IN2, FOCUSER_IN3, FOCUSER_IN4);
#endif

void Focuser::begin() {
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
    stepper.setPosition(Settings::settings.focuserPosition);
    stepper.setMaxSpeed(Settings::settings.focuserSpeed);
    stepper.setBacklash(Settings::settings.focuserBacklash);
    stepper.setDirectionInverted(Settings::settings.focuserReverse);
#ifdef FOCUSER_EN
    stepper.setEnablePin(FOCUSER_EN, false);
    stepper.setAutoPowerTimeout(FOCUSER_POWER_TIMEOUT);
#else
    stepper.setAutoPowerTimeout(0);
#endif
}

#endif