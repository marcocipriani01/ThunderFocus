<img align="left" width="100" src="assets/logo.png" style="position: relative; top: 8px; margin-bottom: 8px;" alt="OpenFocuser logo">

# OpenFocuser by Marco Cipriani

<span class="no-print">**[Online page](https://marcocipriani01.github.io/projects/OpenFocuser) |**</span> OpenFocuser is a MoonLite-compatible motorized focuser controller with absolute and relative positioning and half and full step switching. It is Unix/Linux (INDI) and Windows (MoonLite and ASCOM) compatible. Two editions are available: standard and Plus, which has a non-MoonLite extra function to control all the Arduino's digital and PWM pins, allowing the final user to turn on and off (or regulate in current) other devices like dew heaters, mirror coolers, or even a Raspberry Pi, directly from your computer, or remotely with Telescope-Pi. OpenFocuser also comes with a firmware update utility (via `avrdude`), a command line server for remote control (using INDI) and a control panel to manage the pins (name and value at startup). 3D mounting brackets and circuit/PCB projects are also provided.

<aside>
<div class="contents no-print">
<b style="margin-left:16px;">Table of contents</b>

[](TOC)

- [OpenFocuser by Marco Cipriani](#openfocuser-by-marco-cipriani)
  - [Usage](#usage)
    - [Focuser](#focuser)
    - [OpenFocuser-Manager](#openfocuser-manager)
  - [Advanced usage and troubleshooting](#advanced-usage-and-troubleshooting)
    - [Sending configuration to another computer](#sending-configuration-to-another-computer)
    - [Stand-alone CLI server](#stand-alone-cli-server)
    - [INDI driver in another INDI server](#indi-driver-in-another-indi-server)
    - [Starting from the command line](#starting-from-the-command-line)
    - [Common errors](#common-errors)
  - [Hardware](#hardware)
    - [Autodesk Eagle circuit](#autodesk-eagle-circuit)
    - [Motor holders](#motor-holders)
    - [Focuser motor and drivers](#focuser-motor-and-drivers)
  - [Developer's guide](#developers-guide)
    - [Development status](#development-status)
    - [Arduino configuration](#arduino-configuration)
    - [Compiling the Manager from sources](#compiling-the-manager-from-sources)
    - [HTML version of this document](#html-version-of-this-document)
  - [License](#license)
  - [Forking and issues](#forking-and-issues)

</div>
</aside>

## Usage

### Focuser

OpenFocuser is fully compatible with MoonLite softwares, so please refer to the official user guides for MoonLite focusers, or to the documentations for INDI (indi_moonlite_focus), KStars, ASCOM or whatever software you use for astrophotography. Recommended stad-alone software for Windows: [MoonLite Single Focuser V1.4](https://focuser.com/downloads.php)

### OpenFocuser-Manager

OpenFocuser-Manager is a Java 12 desktop application that allows the end user to update the Arduino firmware easily (eliminates the need of installing the whole Arduino IDE, compiling and uploading the sketch) and to control the digital pins of the board if using the Plus edition.
It comes bundled with `avrdude` and the latest `.hex` firmwares for Arduino Nano, serial libraries, INDI4Java server and self-updating utility.

#### Installation

-   Debian and Ubuntu:
    -   Install the Debian package: `dpkg` will automatically install `socat` and `avrdude` for you, a launcher icon will be created and you'll be ready to use it.
    -   Packages `openssh-client` and `openssh-server` are needed if you want to upload and download the configuration files, see below for information.
-   Other distros:
    -   Download the `jar` archive and launch it. `socat` and `avrdude` **must** be installed depending on you package manager and in path.
    -   `ssh` and its server are needed if you want to upload and download the configuration files, see below for information.
-   Windows:
    -   Download the `jar` archive and launch it. `avrdude.exe` and `libusb0.dll` are bundled in it, eliminating the need of installing them. The INDI server and driver and the settings sending won't be available, only firmware update.
-   MacOS: I don't have a Mac, so I can't provide a package for this OS. Feel free to help!

<a class="no-print" href="assets/FW-update.png"><img src="assets/FW-update.png" width="400" align="right"></a>

#### Updating the firmware

Firmware update via `avrdude` is supported in Windows (`avrdude.exe` is bundled in the executable `jar` and will be unzipped at runtime in the system-default temp folder) and in Linux, no matter the distribution, if the `avrdude` executable is in path (in Debian, install it with `sudo apt-get install avrdude`). `avrdude`'s config is selected automatically by the Manager.<br>
In the firmware update tab you can select the serial port of the target board, the board type (at the moment, the firmware is built only for Arduino Nano, new and old bootloaders), and the firmware edition: standard (focuser only) or Plus (controllable pins and polar finder illuminator). A label below the firmware selection shows the version of the selected software. Press Update to flash the board.

<a class="no-print" href="assets/Plus-config.png"><img src="assets/Plus-config.png" width="400" align="left"></a>

#### Plus edition pin management

**Note: standard MoonLite focusers do NOT support pin management! They aren't compatible with OpenFocuser**
<br>If you are new to the INDI protocol please read more in the <a href="http://indilib.org/about/discover-indi.html">INDI website</a> and in <a href="https://en.wikipedia.org/wiki/Instrument_Neutral_Distributed_Interface">Wikipedia</a>.
<br>In the "Plus edition configuration" tab you'll be able to select the port for the INDI server (default 7625 to allow other INDI clients like KStars to use 7624) and define the list of digital and PWM pins.
You can click "Add" to add a pin definition: adding a digital pin means adding an INDI _switch element_ (a checkbox in the INDI control panel of the client) that allows the user to switch the state of the pin ON and OFF; instead, after creating a PWM pin, OpenFocuser will add an INDI _number element_ to its driver that allows the end user to write the pin value (0→100%) directly from the client INDI control panel.
You'll be asked about the pin port (for example, pin 13). **Note: you can add only the pins you have selected in the Arduino configuration before!**
Then you can click "Edit" to modify the pin's properties: a custom name (e.g. "Dew heater") and a default value, applied when the driver starts.
After defining all the pins, save the configuration and start the server directly from the control panel or close it to use the driver or the server from the command line (see advanced usage).
In order to use the pin manager driver you'll need an INDI client.
In KStars, open Ekos from the toolbar and create a new profile containing your telescope mount, CCD camera or reflex and a MoonLite focuser, and in the "Remote:" driver field write `INDI Arduino pin driver@localhost:7625`.
Be careful to replace `localhost:7625` with the right host and port.
**Uncheck** the auto-connect box and give the profile a name.
Now start the OpenFocuser server (from the control panel or from the command line).
Start the Ekos' INDI server and open the INDI control panel.
Connect your devices, go to the "INDI Arduino pin driver" tab and connect the driver.
In the "Serial connection" tab select a serial port and hit connect.
A new tab called "Manage pins" will show up, in which you can mange the PWM and digital pins you selected in the control panel.
Copy the MoonLite port to the clipboard and paste it in the "Port" field in the MoonLite driver tab.
Select baud speed to 9600 and connect the MoonLite device.
If everything is OK you'll get the full MoonLite control panel.
Otherwise check if the OpenFocuser server is running, if the virtual port exists and if the speed is 9600. Enjoy!

## Advanced usage and troubleshooting

### Sending configuration to another computer

You can send the pin configuration and all the settings to another computer.
Ensure OpenFocuser-Manager is installed on both computer alongside with the required dependencies.
The other computer must have a SSH server installed, while the sender a SSH client.
From the sender computer, open the control panel and click on "Send configuration".
You'll asked about the remote host, username and password.
If the process fails due a missing remote folder, open and close one time the control panel in the remote computer and retry (this will create the required config folder in the remote user directory, which must be present in order to send the settings file).

### Stand-alone CLI server

Use `openfocuser -p=xxxx`, replacing `xxxx` with whatever port you want (or `0` to use the saved port), to start the server without GUI in the terminal.
The configuration will be the same saved before in the control panel.

### INDI driver in another INDI server

The INDI driver can be run inside another INDI server executing `openfocuser -d`.
No GUI will be loaded, nor the server will be set up: the driver will communicate with your external server with stdin/out, just like any other INDI driver.

### Starting from the command line

-   `bash`: `openfocuser <options>`
-   Windows: `java -jar OpenFocuser-Manager.jar <options>`

| Short option | Long option       | Param               | Description                                                                                           |
| ------------ | ----------------- | ------------------- | ----------------------------------------------------------------------------------------------------- |
| `-a`         | `--serial-port`   | e.g. `/dev/ttyUSB0` | Specifies a serial port and connects to it if possible. Otherwise it will be stored to settings only. |
| `-c`         | `--control-panel` |                     | Shows the control panel.                                                                              |
| `-d`         | `--driver`        |                     | Driver-only mode (no server, stdin/stdout)                                                            |
| `-p`         | `--indi-port`     | e.g. `7625`         | Stand-alone server mode, CLI. If port=0, fetch the last used port from the settings.                  |
| `-v`         | `--verbose`       |                     | Verbose logging mode.                                                                                 |

### Common errors

| Exit code | Error                                  | Solution                                                                                                                                                                                                                   |
| --------- | -------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 0         | Java not found                         | Install Java >8 and ensure it's in path                                                                                                                                                                                    |
| 3         | `socat` not found                      | Install `socat` (Linux)                                                                                                                                                                                                    |
| 4         | INDI not found                         | Install `indiserver` (Linux)                                                                                                                                                                                               |
| 5         | `avrdude` not found                    | Install `avrdude` (Linux)                                                                                                                                                                                                  |
| 6         | `jar` not found                        | Where did you put `OpenFocuser-Manager.jar`? Did you build the project and artifacts?                                                                                                                                      |
| 8         | Config folder could not be initialized | OpenFocuser-Manager wasn't able to create the folder where it'll put the configuration. Using your file manager, go to you home directory and check if the `.config/OpenFocuser-Manager` folders exist and are directories |
| 9         | Unable to parse parameters             | Check the command line arguments                                                                                                                                                                                           |
| 10        | Invalid options                        | Invalid combination of command line arguments (for example, you can't run the driver and the control panel or the stand-alone server at the same time)                                                                     |
| 11        | `socat` error                          | `socat` could not be started. Check if it's installed, up-to-date and in path. No solution for operating systems other that Linux, but shouldn't happen: report an issue if so.                                            |

<a class="no-print" href="assets/Circuit-1.jpg"><img align="left" src="assets/Circuit-1.jpg" width="250"></a>

## Hardware

### Autodesk Eagle circuit

In the "Eagle" directory you can find the full circuit project, both schematics and PCB for the standard and Plus editions.
Feel free to modify it to accomplish your necessities: for example, you could add another dew heater controller, or remove the Newton mirror cooler MOSFET.
<br>**Made with Eagle 9.4.1 Premium**

<a class="no-print" href="assets/Motor-holder-1.jpg"><img align="right" src="assets/Motor-holder-1.jpg" width="250"></a>

### Motor holders

I included the mounting brackets I made for the common SkyWatcher
Dual-Speed Crayford focuser (I have a 200mm f/5 Newton OTA).
<br>**AutoCAD 2019** project, STL and IGS exported files ready for 3D printing.

### Focuser motor and drivers

I use a Nema 11 motor that moves the focuser knob using a belt.
If you don't have a heavy focuser, also consider using a Nema 8.
A 20 teeth pulley and a 6mm wide 160mm long belt are enough.
Supported motor drivers via the [StepperDriver](https://github.com/laurb9/StepperDriver) library:

-   Generic 2-pin drivers
-   DRV8825
-   A4988
-   DRV8834

## Developer's guide

<div class="no-print">

### Development status

-   OpenFocuser-Manager:
    -   Version: 1.0
    -   Tested on: _Ubuntu 18.04_, _Windows 10_
    -   Bundled with `avrdude.exe` version 6.0.1, compiled on Sep 18 2013
    -   Compiled with the following `hex` firmwares:
        -   Standard, _v1.0_
        -   Plus, _v1.0_
        -   Arduino Nano with old or new bootloaders
-   Arduino firmware:
    -   Standard, _v1.0_
    -   Plus, _v1.0_
    -   Arduino Nano with old or new bootloaders
-   Circuits:
    -   Standard, _rev1_
    -   Plus, _rev1_
-   3D motor holders:
    -   SkyWatcher Dual-Speed Crayford focuser, _rev1_

</div>

### Arduino configuration

#### Compiling from sources

The Arduino sketch manages the communication with the computer and sends the appropriate commands to the motor driver. To configure the pins and enable/disable features refer to the "Using the Config file" section below.
<br>_**Required:**_ the Arduino IDE for editing (or compiling manually) and a Linux machine.
<br>`Builder/build-fw.sh` is a `bash` script that automates the process of configuring, compiling, renaming and updating the `hex` files in the Manager sources.
To run it, execute `Builder/build-fw.sh`, no arguments required.
The script will create and place in the Manager sources the firmwares for both the standard and Plus editions of OpenFocuser, compiled for Arduino Nano with the new and old bootloaders.
<br>[Arduino CLI](https://github.com/arduino/arduino-cli) is used to compile the sketch. It's part of the [Arduino](https://www.arduino.cc/) project. The Arduino trademark and this executable belong to its owner and I'm not affiliated with it. [License of Arduino CLI](https://github.com/arduino/arduino-cli/blob/master/LICENSE.txt).

#### Libraries

-   [AccelStepper](http://www.airspayce.com/mikem/arduino/AccelStepper/) by Mike McCauley, [license](https://www.gnu.org/licenses/gpl-2.0.html)
-   [StepperDriver](https://github.com/laurb9/StepperDriver) by Laurentiu Badea, [license](https://github.com/laurb9/StepperDriver/blob/master/LICENSE)
-   [Button Debounce](https://github.com/maykon/ButtonDebounce) by maykon, [license](https://github.com/maykon/ButtonDebounce/blob/master/LICENSE)

#### Contributors

This firmware uses parts of the sketches by Orly Andico and Daniel Franzén:

-   Inspired by [arduino-focuser-moonlite](https://github.com/orlyandico/arduino-focuser-moonlite) by Orly Andico, [blog post](http://orlygoingthirty.blogspot.co.nz/2014/04/arduino-based-motor-focuser-controller.html)
-   Modified for INDI, easydriver support (_removed_) by Cees Lensink
-   Added sleep function by Daniel Franzén, [GitHub repo]((<https://github.com/FranzenD/arduinofocus>)

#### Using the Config file

In the firmware folder, the `Config.h` defines:

-   the pins and type of motor driver
-   the status LED pin
-   serial speed
-   hand control buttons and potentiometer
-   whether or not the polar finder illuminator is enabled
-   the customizable pins

##### Motor drivers

-   `STEPPER_TYPE`
    0.  BasicStepperDriver
    1.  DRV8825
    2.  A4988
    3.  DRV8834
-   `DRIVER_DIR`: stepper driver DIR pin
-   `DRIVER_STEP`: stepper driver STEP pin
-   `M0`, `M1`, `M2`: pins for setting the microstepping mode (not used by `BasicStepperDriver`)

##### Hand controller

-   `ENABLE_HC` to enable it
-   `HC_SPEED_POT` the potentiometer that controls the number of steps the focuser moves every time you press a button
-   `BUTTON_UP` and `BUTTON_DOWN`: pins of the buttons

##### Pin management

To change the default list of customizable pins, turn on this function with `ENABLE_PIN_CONTROL` and then define the customizable pins in the `CUSTOMIZABLE_PINS` array. You'll be able to control them using OpenFocuser-Manager, see below for further information.

##### Polar finder illuminator

You can include a LED output to illuminate the polar finder of you mount enabling `ENABLE_PFI` and setting

-   `PFI_POT`: the analog input of the potentiometer that dims the LED
-   `PFI_LED`: the pin of the LED (I suggest a red one)

### Compiling the Manager from sources

**_Required:_** IntelliJ 2018.2 with Bash Support plugin, Java >12
- To build the Debian installer, run configuration "Generate Debian package"
	- The utput Debian package will be generated in `OpenFocuser-Manager/deb-builder/OpenFocuser-Manager.deb`
- To compile the universal `jar` executable, start configuration _Run Windows-compatible Jar_
	- The output archive will be generated in `OpenFocuser-Manager/out/artifacts/OpenFocuser_Manager_Windows_jar/OpenFocuser-Manager.jar`
- To create Javadocs, use the dedicated tool in IntelliJ

#### OpenFocuser-Manager used libraries and resources

OpenFocuser-Manager uses some third party libraries. Their `jar` Packages can be found in the `OpenFocuser-Manager/lib` folder.

-   [Gson](https://github.com/google/gson") by Google, [license](https://github.com/google/gson/blob/master/LICENSE)
-   [Commons CLI](https://commons.apache.org/proper/commons-cli/) by Apache Commons, [license](http://www.apache.org/licenses/)
-   [INDI for Java](http://indiforjava.sourceforge.net/stage/) by Zerjillo, in module `OpenFocuser-Manager/I4J` with the following dependencies:
    -   [Ostermiller Java Utilities](https://ostermiller.org/utils/) by Stephen Ostermiller, [license](https://ostermiller.org/utils/license.html)
-   [jSSC](https://github.com/scream3r/java-simple-serial-connector) by scream3r, [license](http://www.gnu.org/licenses/lgpl.html)
-   [JSch](http://www.jcraft.com/jsch/) by JCraft, [license](http://www.jcraft.com/jsch/LICENSE.txt)
-   [Radiance, Neon and Substance L&F](https://github.com/kirill-grouchnikov/radiance) by kirill-grouchnikov, [license](https://github.com/kirill-grouchnikov/radiance/blob/master/LICENSE)
-   [Materia Design icons](https://material.io/tools/icons/) by Google (icons are in the `OpenFocuser-Manager/src/marcocipriani/openfocuser/manager/res` folder), [license](https://www.apache.org/licenses/LICENSE-2.0.html)
- [GitHub logo](https://github.com/logos), logo and Octocat terms of use:
	- _GITHUB®, the GITHUB® logo design, OCTOCAT® and the OCTOCAT® logo design are exclusive trademarks registered in the United States by GitHub, Inc._

#### Behind the scenes of the Plus edition

To work, the OpenFocuser Plus INDI driver uses `socat` and creates two virtual devices (sockets).
Let's say, for example, that `/dev/port1` and `/dev/port2` are created: the first virtual port is used to read whatever is sent to the second one and the end user will be asked to connect the MoonLite driver to /dev/port2.
OpenFocuser will forward **every** byte sent to port2 to the real Arduino, plus command `:AVxxxx#`, where `xxxx` is an hex number that represent the target pin and its new value, to change the state of a pin, and command `:RS#` to reset the board.

<div class="no-print">

### HTML version of this document

- Run `Docs/readme2html.sh`
- Convert the output file (`Readme.html`) in PDF using Chrome.

</div>

## License

OpenFocuser is a project by Marco Cipriani<span class="no-print"> - [GitHub profile](https://github.com/marcocipriani01) - [website](https://marcocipriani01.github.io/)</span>
<br>Licensed under the [Apache License, Version 2.0](LICENSE.md)
<br>Google, The Apache Software Foundation, Java, GitHub and MoonLite trademarks belong to their respective owners. I'm not affiliated with these manufacturers, companies and software foundations.

## Forking and issues

Feel free to submit pull requests, report an issue or suggest new features!
Also, new mounting brackets are welcome!
