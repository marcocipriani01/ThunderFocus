#ifndef DEVMAN_DEFINITIONS_H
#define DEVMAN_DEFINITIONS_H

#include <Arduino.h>

namespace DevManager {
enum AutoMode {
    NIGHT_ASTRONOMICAL,
    NIGHT_CIVIL,
    DAYTIME,
    DEW_POINT_DIFF1,
    DEW_POINT_DIFF2,
    DEW_POINT_DIFF3,
    DEW_POINT_DIFF5,
    DEW_POINT_DIFF7,
    HUMIDITY_90,
    HUMIDITY_80,
    HUMIDITY_70,
    TEMP_FREEZE,
    NONE
};

struct Pin {
    uint8_t number;
    uint8_t value;
    boolean isPwm;
    boolean enablePwm;
    boolean autoModeEn;
};
}  // namespace DevManager

#endif