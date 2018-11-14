#!/bin/bash
cd $(dirname "$0")/..
sed -i 's/#define EDITION 1/#define EDITION 0/g' Firmware/Config.h
Builder/Arduino-CLI-Linux-64 compile -b arduino:avr:nano:cpu=atmega328old -o OpenFocuser Firmware/
rm OpenFocuser.elf
mv OpenFocuser.hex OpenFocuser-Manager/hex/OpenFocuser_OB.hex
Builder/Arduino-CLI-Linux-64 compile -b arduino:avr:nano:cpu=atmega328 -o OpenFocuser Firmware/
rm OpenFocuser.elf
mv OpenFocuser.hex OpenFocuser-Manager/hex/OpenFocuser.hex

sed -i 's/#define EDITION 0/#define EDITION 1/g' Firmware/Config.h
Builder/Arduino-CLI-Linux-64 compile -b arduino:avr:nano:cpu=atmega328old -o OpenFocuser Firmware/
rm OpenFocuser.elf
mv OpenFocuser.hex OpenFocuser-Manager/hex/OpenFocuser_Plus_proto_OB.hex
Builder/Arduino-CLI-Linux-64 compile -b arduino:avr:nano:cpu=atmega328 -o OpenFocuser Firmware/
rm OpenFocuser.elf
mv OpenFocuser.hex OpenFocuser-Manager/hex/OpenFocuser_Plus_proto.hex

sed -i 's/#define EDITION 1/#define EDITION 2/g' Firmware/Config.h
Builder/Arduino-CLI-Linux-64 compile -b arduino:avr:nano:cpu=atmega328old -o OpenFocuser Firmware/
rm OpenFocuser.elf
mv OpenFocuser.hex OpenFocuser-Manager/hex/OpenFocuser_Plus_OB.hex
Builder/Arduino-CLI-Linux-64 compile -b arduino:avr:nano:cpu=atmega328 -o OpenFocuser Firmware/
rm OpenFocuser.elf
mv OpenFocuser.hex OpenFocuser-Manager/hex/OpenFocuser_Plus.hex
