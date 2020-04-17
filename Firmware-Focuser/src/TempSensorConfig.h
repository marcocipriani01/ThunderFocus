#ifndef TEMP_SENSOR_CONFIG_H
#define TEMP_SENSOR_CONFIG_H

// The model of the temperature sensor.
// 1 is to select the Maxim Integrated DS18S20
//  - To use the DS18S20, place a 4.7K resistor between DATA and Vdd
#define TEMP_SENSOR_TYPE 1

#if TEMP_SENSOR_TYPE == 1
#define ONE_WIRE_BUS 2
#define DS18S20_INDEX 0
#endif

float readTemperature();

#endif
