#include "../definitions.h"
// ===================================================================================
// ============================== FOCUSER CONFIGURATION ==============================
// ===================================================================================
#define FOCUSER_DRIVER BIPOLAR
#define FOCUSER_DIR 2
#define FOCUSER_STEP 3
#define FOCUSER_EN 7
#define FOCUSER_MODE0 4
#define FOCUSER_MODE1 5
#define FOCUSER_MODE2 6
#define FOCUSER_ACCEL 1600.0
#define FOCUSER_PPS_MIN 100.0
#define FOCUSER_PPS_MAX 60000
#define FOCUSER_POWER_TIMEOUT 30000

#define HAND_CONTROLLER true
#define HAND_CONTROLLER_KNOB A0
#define HAND_CONTROLLER_LEFT 8
#define HAND_CONTROLLER_RIGHT 9
#define HAND_CONTROLLER_STEPS 25.0
