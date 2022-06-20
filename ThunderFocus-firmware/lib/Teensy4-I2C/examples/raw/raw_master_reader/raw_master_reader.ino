// Copyright © 2019-2020 Richard Gemmell
// Released under the MIT License. See license.txt. (https://opensource.org/licenses/MIT)

// This example WILL NOT work unless you have an INA260
// current sensor connected to pins 18 and 19.
//
// Demonstrates use of the raw I2C driver.
// Creates an I2C master and reads a device with it.
//
// This is an advanced example. Use the "simple" examples
// instead if you want to follow the typical usage pattern
// for I2C.

#include <Arduino.h>
#include <i2c_driver.h>
#include "imx_rt1060/imx_rt1060_i2c_driver.h"

// Blink the LED to make sure the Teensy hasn't hung
IntervalTimer blink_timer;
volatile bool led_high = false;
void blink_isr();

// The slave is an INA 260 current sensor
const uint8_t slave_address = 0x40;
const uint8_t manufacturer_id_register = 0xFE;
const uint8_t die_id_register = 0xFF;
I2CMaster& master = Master;

// Create a buffer to receive data from the slave.
uint8_t rx_buffer[2] = {};

void finish();
bool ok(const char* message);
uint16_t get_int_from_buffer();

void setup() {
    // Turn the LED on
    pinMode(LED_BUILTIN, OUTPUT);
    digitalWrite(LED_BUILTIN, true);

    // Create a timer to blink the LED
    blink_timer.begin(blink_isr, 500000);

    // Initialise the master
    master.begin(100 * 1000U);

    // Enable the serial port for debugging
    Serial.begin(9600);
    Serial.println("Started");
}

void loop() {
    Serial.println("");
    // Request the Manufacturer ID
    master.write_async(slave_address, (uint8_t*)&manufacturer_id_register, sizeof(manufacturer_id_register), false);
    finish();
    if (master.get_bytes_transferred() != sizeof(manufacturer_id_register)) {
        // Show that we can count bytes transmitted
        Serial.printf("Transmitted %d bytes but expected to send %d.\n", master.get_bytes_transferred(), sizeof(manufacturer_id_register));
    }
    if(ok("Failed to send manufacture id register value")) {
        master.read_async(slave_address, rx_buffer, sizeof(rx_buffer), true);
        finish();
        if (ok("Failed to read manufacture ID.")) {
            if (master.get_bytes_transferred() != 2) {
                // Show that we can count bytes received
                Serial.println("Expected to receive 2 bytes");
            }
            uint16_t manufacturerID = get_int_from_buffer();
            const uint16_t expected = 0x5449;
            if (manufacturerID == expected) {
                Serial.println("Got correct Manufacturer ID.");
            } else {
                Serial.printf("Manufacturer ID is 0x%X. Expected 0x%X.\n", manufacturerID, expected);
            }
        }
    }

    // Request the Die ID
    master.write_async(slave_address, (uint8_t*)&die_id_register, sizeof(die_id_register), false);
    finish();
    if(ok("Failed to send die id register value")) {
        master.read_async(slave_address, rx_buffer, sizeof(rx_buffer), true);
        finish();
        if (ok("Failed to read die ID.")) {
            uint16_t dieId = get_int_from_buffer();
            const uint16_t expected = 0x2270;
            if (dieId == expected) {
                Serial.println("Got correct Die ID.");
            } else {
                Serial.printf("Die ID is 0x%X. Expected 0x%X.\n", dieId, expected);
            }
        }
    }

    delay(1000);
}

uint16_t get_int_from_buffer() {
    uint16_t result = ((uint16_t)rx_buffer[0] << 8U) + ((uint16_t)rx_buffer[1]);
    return result;
}

bool ok(const char* message) {
    if (master.has_error()) {
        Serial.print(message);
        Serial.print(" Error: ");
        Serial.println((int)master.error());
        return false;
    }
    return true;
}

void finish() {
    elapsedMillis timeout;
    while (timeout < 200) {
        if (master.finished()){
            return;
        }
    }
    Serial.println("Master: ERROR timed out waiting for transfer to finish.");
}

void blink_isr() {
    led_high = !led_high;
    digitalWrite(LED_BUILTIN, led_high);
}
