<img width="100" src="Resources/ThunderFocus.png" style="margin-bottom: 8px" alt="ThunderFocus logo">

# ThunderFocus

ThunderFocus is an open-source telescope focuser developed by Marco Cipriani. The source code is available on [GitHub](https://github.com/marcocipriani01/ThunderFocus) and is issued under the [GNU General Public License version 3](https://www.gnu.org/licenses/gpl-3.0.html). By using this software, you accept all the terms of its license.

## Showcase of controllers and focusers [on my website](https://marcocipriani01.github.io/projects/ThunderFocus)!

## Description of the project

ThunderFocus is an all-in-one focuser and power box (dew heater controller) designed to be fully customizable and feature-rich. It has its own control software written in Java and is both [INDI](https://indilib.org/) and [ASCOM](https://ascom-standards.org/) compatible.

The firmware can run on both Arduino and Teensy, with the only difference being the RTC support and the speed of the MCU. Thanks to the firmware configuration file, you can select only the features you need and remove the unnecessary: the control software will detect the available features and display only the options supported by the board, so you won't need to modify other files.

The user interface is platform-independent and works seamless across computers and boards like Raspberry Pi, with the only requirement being Java 15 (bundled in the Windows installer): in this way, you get the same experience no matter what OS you run. Moreover, it uses [INDIForJava](http://indiforjava.sourceforge.net/stage/index.html) for INDI/KStars compatibility and a special ASCOM driver for Windows compatibility. As a bonus, it can also be controlled using the [IPARCOS Android app](https://marcocipriani01.github.io/projects/IPARCOS) when the INDI server is enabled!

## Features

- INDI/ASCOM compatibility, cross platform interface
- Full telescope focuser control
  - Can control either one main telescope focuser or one guidescope/off-axis guider focuser
  - Backlash compensation
  - Precise micro-stepping control for pin-point stars
  - Displays the focuser position in millimeters, ticks or any unit of your liking
  - Remembers the last position
- Advanced dew heaters control, which can also turn on and off the telescope mount or other accessories.
  - Various automatic modes: temperature, dew point, humidity, sunset/sunrise
  - Manual control/override of outputs
  - Completely autonomous after configuration: can work without a PC
  - Restores the last state after power up
  - High efficiency, does not require heatsinks or cooling
- Ambient monitoring
  - Temperature, humidity, dew point
  - Graphs of the whole observing night

## Contributing to the project

I really appreciate help, feel free to fork, open issues, start pull requests...

## Installing the software

### Windows x64

Get [the latest release](https://github.com/marcocipriani01/ThunderFocus/releases) and install it. You're done!

### Debian, Ubuntu and Raspberry Pi OS

**Skip the Java installation if you already have it installed!**

Install the latest Java Development Kit available, version 15 or greater. On Ubuntu:

- `sudo apt update`
- `sudo apt install openjdk-15-jdk`

Raspberry Pi OS or Astroberry:

- Download the latest [AdoptOpenJDK](https://adoptopenjdk.net/releases.html) JRE or JDK (recommended: version 15, HotSpot JVM)
- Run `sudo mkdir /usr/java/`
- Extract the archive: `sudo tar -zxf AdoptOpenJDK.tar -C /usr/java/` (change the filename accordingly to what you downloaded)
- Run `echo 'export PATH=$PATH:/usr/java/' | sudo tee -a /etc/profile`

### macOS, Windows 32-bit and other Linux distros

Install Java version 15 or greater depending on your OS and then use the `.jar` ThunderFocus release. Someday I will add a macOS installer...

## Using the control software

Coming soon...

## The electronics

I created several circuits for ThunderFocus, all sharing more or less the same functionalities:

1. The prototype I use on my own telescope (not in this repository).
2. A Teensy 4 "overkill" board, with RTC and dual focuser support, not yet implemented in software. The PCB also includes a flip-flap circuit for [my other project](https://github.com/marcocipriani01/ArduinoFlatBox).
3. The **recommended Arduino Nano PCB**, which supports:
   - One focuser (bipolar motor)
   - Hand controller
   - Two PWM outputs and one digital for dew heaters and telescope mount power (with relay) respectively.
   - I2C RTC clock predisposition, to be implemented in software
   - Protection against over-current and reverse polarity

If you want to replicate this project, please build the third PCB. You can use the included CAM job to generate the gerber files and order them using services like [JLCPCB](https://jlcpcb.com/). The board is designed to be cut (following a line on the silkscreen) if you don't want the output controller: in this way, you can order 5 identical PCBs (the minimum on JLCPCB) and choose which one to cut and which not to. Maybe give the remaining boards to your friends and avoid wastes!

Building the board is an easy job, just add the components you see in the schematics. For reference, here's the list of what you need:

| Number   |      Component                |
|----------|:-----------------------------:|
| C1       |  100uF electrolytic capacitor |
| C2       | 0.33uF electrolytic capacitor |
| C3       |  0.1uF electrolytic capacitor |
| C4       |  47uF electrolytic capacitor  |
| C5       |  100uF electrolytic capacitor |
| CNx      |  2.1x5.5mm barrel plug socket |
| D1       |          1N4004 diode         |
| D2       |          1N4004 diode         |
| DRV1     |     DRV8825 stepper driver    |
| F1       |        5x20 fuse holder       |
| IC1      |    LM7805 linear regulator    |
| J2       |   RJ11/RJ12 female connector  |
| K1       |           12V relay           |
| LED1     |          5mm red LED          |
| Q2       |    IRF540 N-channel MOSFET    |
| Q3       |    IRF540 N-channel MOSFET    |
| Q5       |      BC547 NPN transistor     |
| R1       |        330 ohm resistor       |
| R2       |          1K resistor          |
| R3       |         4.7K resistor         |
| R4       |         4.7K resistor         |
| R5       |          10K resistor         |
| U1       |       Arduino Nano board      |

## 3D-printed brackets

At the moment, the only bracket in the repository is for the Sky-Watcher 200 f/5 newtonian (2017 model, newer ones may be different) and uses a circular connector. I'm looking forward to add new 3D models with the right connector - the RJ11. Feel free to submit yours using pull requests!

## The stepper motor

The firmware is pretty much motor/driver-agnostic. Just make sure to select the right driver type in the configuration files (`DRIVER_POLOLU` if you're using the Arduino Nano PCB). On my newtonian OTA, which has a crayford focuser and only moves a DSLR, I'm using a small NEMA 11 stepper motor. My recommendation is to use a geared (1:4 ~ 1:14) stepper if you're attaching the motor directly to the focuser axis or a direct stepper if on the knob of a dual-speed focuser. Use properly sized timing belt and pulley or a shaft coupler to connect the motor to the focuser.

## Development

### Building the firmware

1. Install [Visual Studio Code](https://code.visualstudio.com/) and the [PlatformIO](https://platformio.org/install/ide?install=vscode) extension
2. Install the Arduino or Teensy core in the PlatformIO boards manager
3. Open the `ThunderFocus-firmware` folder inside VS Code
4. Select the PlatformIO Project Environment (`nanoatmega328` or `teensy40`) from the status bar, or add a custom one in `platformio.ini`
5. Open `src/config.h` and enable **one and only one** of the following lines (according to the PCB you built):
   - `#include "boards/arduino_nano_pcb.h"`
   - `#include "boards/teensy_max_pcb.h"`
6. Open the configuration file you enabled and make the appropriate changes (comments in the file will guide you)
   - Select the pins according to your circuit
   - Enable or disable features
   - Eventual errors or incompatible combinations of features will stop the compilation
7. Done! Upload the firmware

### Building the control software `jar` files

1. Install a [Java Development Kit](https://www.oracle.com/java/technologies/javase-downloads.html), version 15 or greater
2. Install [IntelliJ IDEA community](https://www.jetbrains.com/idea/)
3. Open `ThunderFocus-GUI` inside IntelliJ
4. Just run the only configuration in the project, it will build the artifacts for you and place them in the right places

### Creating the Windows installer

Requires the `jar` files. Make sure to follow the previous step.

1. Install [Inno Setup](https://jrsoftware.org/isinfo.php) and [Visual Studio](https://visualstudio.microsoft.com/it/) with Visual Basic .NET support
2. Open `ThunderFocusASCOM` inside Visual Studio
3. Compile the _Release_ configuration (do not run it)
4. Extract a copy of the [AdoptOpenJDK](https://adoptopenjdk.net/releases.html) in the `JRE-bundle` folder
    - Recommended: _OpenJDK 15_ with _HotSpot VM_, Windows x64
5. Open `InnoSetup.iss` and make the necessary changes to the file:
    - Replace `D:\ThunderFocus\` with the path to the ThunderFocus repository in your computer
6. Compile and run the Inno Setup file: it will create the Windows installer in the `Installers` folder

### Creating the Debian package

Requires the `jar` files and a Debian installation with `dpkg-deb`. Note: the Debian package will not include a bundled JDK. Make sure to install it and make the necessary changes to the `PATH` env variable.

1. Run `cd ThunderFocus-GUI/deb-builder`
2. `./build.sh` (requires `root` access to set the appropriate permissions)
3. You're done, the installer will be located in the usual `Installers` folder

## Used libraries

- [OpenJDK 15](https://openjdk.java.net/) by Oracle, [GPL v2 license with linking exception](https://openjdk.java.net/legal/gplv2+ce.html)
- [AdoptOpenJDK builds](https://adoptopenjdk.net/index.html) with HotSpot VM, by AdoptOpenJDK, [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html)
- [INDIForJava](http://indiforjava.sourceforge.net/stage/index.html) by Sergio Alonso and Richard van Nieuwenhoven, [LGPL v3 license](http://indiforjava.sourceforge.net/stage/license.html)
- [ASCOM .NET libraries](https://www.ascom-standards.org/) by The ASCOM Initiative, [ASCOM Open Source License](https://github.com/ASCOMInitiative/ASCOMPlatform/blob/master/LICENSE.txt)
- [jssc](https://github.com/java-native/jssc) by java-native and scream3r, [LGPL v3 license](https://github.com/java-native/jssc/blob/master/LICENSE.txt)
- [FlatLaf](https://github.com/JFormDesigner/FlatLaf/) by JFormDesigner, [Apache 2.0 license](https://github.com/JFormDesigner/FlatLaf/blob/master/LICENSE)
- [Gson](https://github.com/google/gson) by Google, [Apache 2.0 license](https://github.com/google/gson/blob/master/LICENSE)
- [Material icons](https://material.io/resources/icons/) by Google, [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html)
- [Arduino Core APIs](https://github.com/arduino/Arduino) by Arduino, [LGPL v3 license](https://github.com/arduino/Arduino/blob/master/license.txt)
- [AccelStepper](https://www.airspayce.com/mikem/arduino/AccelStepper/) by Mike McCauley (with modifications), [GPL v3 license](https://www.gnu.org/licenses/gpl-3.0.html)
- [Arduino Time Library](https://github.com/PaulStoffregen/Time) by Michael Margolis, [LGPL v2.1 license](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)
- [SolarPosition library](https://github.com/KenWillmott/SolarPosition) by Ken Willmott
  - Based on [_Arduino Uno and Solar Position Calculations_](http://www.instesre.org/ArduinoDocuments.htm), © David R. Brooks, issued under the [Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License](https://creativecommons.org/licenses/by-nc-nd/4.0/)
  - Uses equations from the book [_Astronomical Algorithms_ by Jean Meeus](https://www.willbell.com/math/mc1.HTM), published by Willmann-Bell, Inc., Richmond, VA
- [DHT sensor library](https://github.com/adafruit/DHT-sensor-library) by Adafruit, [MIT license](https://github.com/adafruit/DHT-sensor-library/blob/master/license.txt)
- [Telescope icon](https://www.flaticon.com/free-icon/telescope_547425?term=telescope&page=1&position=9&related_item_id=547425) by Freepik, [Flaticon license](https://www.freepikcompany.com/legal#nav-flaticon)
