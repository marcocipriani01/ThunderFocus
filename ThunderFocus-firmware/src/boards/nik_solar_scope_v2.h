#include "../definitions.h"
// ===================================================================================
// ============================== FOCUSER CONFIGURATION ==============================
// ===================================================================================
#define FOCUSER_DRIVER BIPOLAR
#define FOCUSER_DIR 26
#define FOCUSER_STEP 32
#define FOCUSER_EN 12
#define FOCUSER_MODE0 4
//#define FOCUSER_MODE1 4
#define FOCUSER_MODE2 25
#define FOCUSER_ACCEL 1600.0
#define FOCUSER_PPS_MIN 100.0
#define FOCUSER_PPS_MAX 60000
#define FOCUSER_POWER_TIMEOUT 30000
//#define FOCUSER_MAX_TRAVEL 100

#define HAND_CONTROLLER true
#define HAND_CONTROLLER_POT 36
#define HAND_CONTROLLER_LEFT 5
#define HAND_CONTROLLER_RIGHT 18
#define HAND_CONTROLLER_STEPS_MIN 5.0
#define HAND_CONTROLLER_STEPS_MAX 500.0

// ===================================================================================
// ============================== ROTATOR CONFIGURATION ==============================
// ===================================================================================
// TODO: implement the rotator driver
#define ROTATOR_DRIVER BIPOLAR
#define ROTATOR_DIR 13
#define ROTATOR_STEP 15
#define ROTATOR_EN 27
#define ROTATOR_MODE0 4
//#define ROTATOR_MODE1 4
#define ROTATOR_MODE2 2
#define ROTATOR_ACCEL 1600.0
#define ROTATOR_PPS_MIN 100.0
#define ROTATOR_PPS_MAX 60000
#define ROTATOR_POWER_TIMEOUT 30000
