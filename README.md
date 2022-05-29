<img width="100" src="Resources/ThunderFocus.png" style="margin-bottom: 8px" alt="ThunderFocus logo">

# ThunderFocus

ThunderFocus is an open-source telescope focuser developed by Marco Cipriani. The source code is available on [GitHub](https://github.com/marcocipriani01/ThunderFocus) and is issued under the [GNU General Public License version 3](https://www.gnu.org/licenses/gpl-3.0.html).

## Showcase of controllers and focusers [on my website](https://marcocipriani01.github.io/projects/ThunderFocus)!

## Description of the project

ThunderFocus is an all-in-one focuser and power box (dew heater controller) designed to be fully customizable and feature-rich. It has its own control software written in Java and is both [INDI](https://indilib.org/) and [ASCOM](https://ascom-standards.org/) compatible.

The firmware can run on both Arduino and Teensy, with the only difference being the RTC support and the speed of the MCU. Thanks to the firmware configuration file, you can select only the features you need and remove the unnecessary: the control software will detect the available features and display only the options supported by the board, so you won't need to modify other files.

The user interface is platform-independent and works seamless across computers and boards like Raspberry Pi, with the only requirement being Java 15 (bundled in the Windows installer): in this way, you get the same experience no matter what OS you run. Moreover, it uses [INDIForJava](http://indiforjava.sourceforge.net/stage/index.html) for INDI/KStars compatibility and a special ASCOM driver for Windows compatibility. As a bonus, it can also be controlled using the [Telescope.Touch Android app](https://github.com/marcocipriani01/Telescope.Touch) when the INDI server is enabled!

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

## Installing the software

### Windows x64

Get [the latest release](https://github.com/marcocipriani01/ThunderFocus/releases) and install it. You're done!

### Debian, Ubuntu and Raspberry Pi OS

**Skip the Java installation if you already have it installed!**

Install the latest Java Development Kit available, version 17 or greater. On Ubuntu:

- `sudo apt update`
- `sudo apt install openjdk-17-jdk`

Raspberry Pi OS or Astroberry:

- Download the latest [Eclipse Temurin build](https://adoptium.net/temurin/releases/) JRE or JDK (recommended: version 17, HotSpot JVM)
- Run `sudo mkdir /usr/java/`
- Extract the archive using `sudo tar -zxf OpenJDK17_RELEASE.tar.gz -C /usr/java/` (change the filename accordingly to what you downloaded)
- Run `echo 'export PATH=$PATH:/usr/java/' | sudo tee -a /etc/profile`

### macOS, Windows 32-bit and other Linux distros

Install Java version 17 or greater and then use the `.jar` ThunderFocus release.

## Development

### Building the firmware

1. Install [Visual Studio Code](https://code.visualstudio.com/) and the [PlatformIO](https://platformio.org/install/ide?install=vscode) extension
2. Install the Arduino or Teensy core in the PlatformIO boards manager
3. Open the `ThunderFocus-firmware` folder inside VS Code
4. Select the PlatformIO Project Environment (`nanoatmega328`, `teensy40`, etc) from the status bar, or add a custom one in `platformio.ini`
5. Open `src/config.h` and enable **one and only one** board config file
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
2. Open `ASCOM.ThunderFocus` inside Visual Studio
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

- [Eclipse Temurin](https://adoptium.net/) by The Eclipse Foundation, [GPL v2 license with classpath exception](https://openjdk.java.net/legal/gplv2+ce.html)
- [INDIForJava](https://indiforjava.github.io/) by Marco Cipriani, Sergio Alonso and Richard van Nieuwenhoven, [LGPL v3 license](https://github.com/INDIForJava/INDIForJava-core/blob/main/LICENSE.txt)
- [ASCOM .NET libraries](https://www.ascom-standards.org/) by The ASCOM Initiative, [ASCOM Open Source License](https://github.com/ASCOMInitiative/ASCOMPlatform/blob/master/LICENSE.txt)
- [jssc](https://github.com/java-native/jssc) by java-native and scream3r, [LGPL v3 license](https://github.com/java-native/jssc/blob/master/LICENSE.txt)
- [FlatLaf](https://github.com/JFormDesigner/FlatLaf/) by JFormDesigner, [Apache 2.0 license](https://github.com/JFormDesigner/FlatLaf/blob/master/LICENSE)
- [Gson](https://github.com/google/gson) by Google, [Apache 2.0 license](https://github.com/google/gson/blob/master/LICENSE)
- [Material icons](https://material.io/resources/icons/) by Google, [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html)
- [Arduino Core APIs](https://github.com/arduino/Arduino) by Arduino, [LGPL v3 license](https://github.com/arduino/Arduino/blob/master/license.txt)
- [AccelStepper](https://www.airspayce.com/mikem/arduino/AccelStepper/) by Mike McCauley (with modifications), [GPL v3 license](https://www.gnu.org/licenses/gpl-3.0.html)
- [Arduino Time Library](https://github.com/PaulStoffregen/Time) by Michael Margolis, [LGPL v2.1 license](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)
- [Adafruit BME280 Library](https://github.com/adafruit/Adafruit_BME280_Library) by Adafruit Industries, [3-Clause BSD license](https://github.com/adafruit/Adafruit_BME280_Library/blob/master/LICENSE.md)
- [Adafruit HTU21D-F Library](https://github.com/adafruit/Adafruit_HTU21DF_Library) by Adafruit Industries
- [SolarPosition library](https://github.com/KenWillmott/SolarPosition) by Ken Willmott
  - Based on [_Arduino Uno and Solar Position Calculations_](http://www.instesre.org/ArduinoDocuments.htm), © David R. Brooks, issued under the [Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License](https://creativecommons.org/licenses/by-nc-nd/4.0/)
  - Uses equations from the book [_Astronomical Algorithms_ by Jean Meeus](https://www.willbell.com/math/mc1.HTM), published by Willmann-Bell, Inc., Richmond, VA
- [Telescope icon](https://www.flaticon.com/free-icon/telescope_547425?term=telescope&page=1&position=9&related_item_id=547425) by Freepik, [Flaticon license](https://www.freepikcompany.com/legal#nav-flaticon)
