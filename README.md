<img width="100" src="Resources/ThunderFocus.png" style="margin-bottom: 8px" alt="ThunderFocus logo">

# ThunderFocus

ThunderFocus is an open-source telescope focuser developed by Marco Cipriani. The source code is available on [GitHub](https://github.com/marcocipriani01/ThunderFocus) and is issued under the [GNU General Public License version 3](https://www.gnu.org/licenses/gpl-3.0.html). By using this software, you accept all the terms of its license.

## Showcase of controllers and focusers [on my website](https://marcocipriani01.github.io/projects/ThunderFocus)!

## Description of the project

ThunderFocus is an all-in-one focuser and power box (dew heater controller) designed to be fully customizable and feature-rich. It has its own control software written in Java and is both [INDI](https://indilib.org/) and [ASCOM](https://ascom-standards.org/) compatible.

The firmware can run on both Arduino and Teensy, with the only difference being the RTC support and the speed of the MCU. Thanks to the firmware configuration file, you can select only the features you need and remove the unnecessary: the control software will detect the available features and display only the options supported by the board, so you won't need to modify other files.

The user interface is platform-independent and works seamless across computers and boards like Raspberry Pi, with the only requirement being Java 14 (bundled in the Windows installer): in this way, you get the same experience no matter what OS you run. Moreover, it uses [INDIForJava](http://indiforjava.sourceforge.net/stage/index.html) for INDI/KStars compatibility and a special ASCOM driver for Windows compatibility. As a bonus, it can also be controlled using the [IPARCOS Android app](https://marcocipriani01.github.io/projects/IPARCOS) when the INDI server is enabled!

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

## The electronics

I will be soon provide more info about building it. For the moment, you'll find the Eagle CAD files in the `Circuits/` folder. Make sure to enable the local libraries in each project.

## The motor and the 3D-printed bracket

I will be soon provide more info about them. For the moment, you'll find one 3D bracket in the `Motor brackets/SkyWatcher Dual-Speed Crayford` folder.

## Development

### Building the firmware

1. Install [Visual Studio Code](https://code.visualstudio.com/) and the [PlatformIO](https://platformio.org/install/ide?install=vscode) extension
2. Install the Arduino or Teensy core in the PlatformIO boards manager
3. Open the `ThunderFocus-firmware` folder inside VS Code
4. Select the PlatformIO Project Environment (`nanoatmega328` or `teensy40`) from the status bar, or add a custom one in `platformio.ini`
5. Open `src/config.h` and enable **one and only one** of the following lines:
   - `#include "boards/ard_nano_proto.h"`
   - `#include "boards/teensy_max_pcb.h"`
6. Open the configuration file you enabled and make the appropriate changes (comments in the file will guide you)
   - Select the pins according to your circuit
   - Enable or disable features
   - Eventual errors or incompatible combinations of features will stop the compilation
7. Done! Upload the firmware

### Building the control software `jar` files

1. Install a [Java Development Kit](https://www.oracle.com/java/technologies/javase-downloads.html), version 14 or greater
2. Install [IntelliJ IDEA community](https://www.jetbrains.com/idea/)
3. Open `ThunderFocus-GUI` inside IntelliJ
4. Just run the only configuration in the project, it will build the artifacts for you and place them in the right places

### Creating the Windows installer

Requires the `jar` files. Make sure to follow the previous step.

1. Install [Inno Setup](https://jrsoftware.org/isinfo.php) and [Visual Studio](https://visualstudio.microsoft.com/it/) with Visual Basic .NET support
2. Open `ThunderFocusASCOM` inside Visual Studio
3. Compile the _Release_ configuration (do not run it)
4. Extract a copy of the [AdoptOpenJDK](https://adoptopenjdk.net/releases.html) in the `JRE-bundle` folder
    - Recommended: _OpenJDK 14_ with _HotSpot VM_, Windows x64
5. Open `InnoSetup.iss` and make the necessary changes to the file:
    - Replace `D:\ThunderFocus\` with the path to the ThunderFocus repository in your computer
6. Compile and run the Inno Setup file: it will create the Windows installer in the `Installers` folder

### Creating the Debian package

Requires the `jar` files and a Debian installation with `dpkg-deb`. Note: the Debian package will not include a bundled JDK. Make sure to install it and make the necessary changes to the `PATH` env variable.

1. Run `cd ThunderFocus-GUI/deb-builder`
2. `./build.sh` (requires `root` access to set the appropriate permissions)
3. You're done, the installer will be located in the usual `Installers` folder

## Used libraries

- [OpenJDK 14](https://openjdk.java.net/) by Oracle, [GPL v2 license with linking exception](https://openjdk.java.net/legal/gplv2+ce.html)
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
