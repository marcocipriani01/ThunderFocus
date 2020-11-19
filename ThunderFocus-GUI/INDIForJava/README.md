# INDI for Java library

Project home: https://sourceforge.net/projects/indiforjava/

## Original README

TO TAKE INTO ACCOUNT FOR THE 1.50 RELEASE:

  - In the client library some methods in the class INDIDevice have been renamed to comply wth code style recommendations: BLOBsEnable, BLOBsEnableNever, BLOBsEnableAlso, BLOBsEnableOnly... This change will break some existing client code. Fixing it is trivial, just changing the names.  However, we must clearly say so in the docs, maybe some small tutorial, etc.
  - A complete refactoring was done, you probably have to adapt your code a migration guide will be provided. 
  - See the sourceforce tickes for the details.
  - See http://indiforjava.sourceforge.net/stage/migration.html for migration descriptions
  - use the SNAPSHOT dependency from sonatype for the current state (deployed every monday):

	<dependencies>
		<dependency>
			<artifactId>i4j-xxxx</artifactId>
			<groupId>org.indilib.i4j</groupId>
			<version>1.50-SNAPSHOT</version>
		</dependency>
	</dependencies>
	<repositories>
		<repository>
			<id>sonatype-snapshots</id>
			<url>https://oss.sonatype.org/content/repositories/snapshots</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>
	</repositories>    
  

Note that this project uses code and resources from other free software packages. Particularly, it uses:

  + The Silk icon set 1.3 (http://www.famfamfam.com/lab/icons/silk/) by Mark James
  + Icons from Glypish (http://glyphish.com) 

Please check their websites for more information about their work and usage licenses.

Special thanks to:
  + Alexander Tuschen, Romain Fafet and Gerrit Viola for their input, testing and code.

Known issues / bugs:
  - The Android UI is a first try will be replaced soon.

The rest of this file describes the main changes in the INDI for Java Libraries. Minor fixes are not listed.

- Version 1.38 (July 22, 2014):

  + Base Library:
    - Solved a bug in SexagesimalFormatter that treated some negative numbers as positive (thanks to Romain Fafet for pointing this out).
    - Added a INDIDate class that helps handling dates for the future Telescope Drivers (thanks to Gerrit Viola).
  + Client Library:
    - In INDINumberElement the setValue(String valueS) does not currently throw the IllegalArgumentException when an incorrect value is passed (less han minimum or greater than maximum) as it seems that some not very well implemented drivers (QHY5 one, for example) might send incorrect values when the deviced are not initialized (thus breaking some clients). This should be later examined for a better solution if that drivers are not corrected.
  + Driver Library:
    - The INDIFocuserDriver can get updates about the speed from the device.


- Version 1.37 (January 11, 2014):

  + Raspberry Pi GPIO Driver:
    - One element in the System Info Group was repeated.
  + Driver Library:
    - Some method renaming.
  + Sky Quality Meter - LU Driver
    - Initial release of the driver.


- Version 1.36 (November 23, 2013):

  + Client Library:
    - Important bug corrected when creating Properties and Elements. I still don't know how it has previously worked :-P
  + ClientUI Library:
    - Some cosmetic changes for Properties with multiple Elements.
    - Added a new control for Switch Properties with One of Many rules (based on a combo box).
  + Driver Library:
    - Added convenience INDIOneElementXXXProperty classes for properties with only one element.
  + Raspberry Pi GPIO Driver:
    - Added this driver to control the GPIO ports of the Raspberry Pi.
    

- Version 1.35 (November 12, 2013):
  + Seletek Driver: 
    - Corrected the "model" property for focusers. Now it has the "DC" option.
    - Added a third port for the Platypus model.
    - Added some power settings for focusers.
  + QHY Filter Wheel Driver:
    - The port property now is stored in the server.
  + Driver Library:
    - Added a convenience method "setValues" to INDIProperty to avoid dealing with elementsAndValues arrays in many typical situations.
    - Corrected a bug that allowed to add new elements to initialized properties.



- Version 1.34 (November 10, 2013):
  + QHY Filter Wheel Driver:
    - Small bug corrected: now the filter slots are not read when filter names change.
    - Added some documentation about the Filter Wheel.
  + Driver Library:
    - Class INDISwitchOneOfManyProperty class created to easily manage One Of Many Switches (no Switch elements direct dealing).
    - Class INDISwitchOneOrNoneProperty class created to easily manage One or None Switches (aka one simple button).
    - Fixed bug about the default value in the INDIPortProperty
    - INDINotLoadableDriver is now an Interface (it was an error design to make it as a subclass)
    - Added the mechanism to register and unregister subdrivers
    - Important bug corrected in INDIPortProperty that prevented changes to its value
    - INDIFocuserDriver class added to create focuser drivers
    - Improved the mechanism to save / load properties to files: the Property classes have some factories that may be used to create auto - saveable properties. In addition, when using those factories it automatically loads the properties if they exist. Otherwise they are created with the standard constructor.
  + Server Library
    - Small changes to avoid loading INDINotLoadableDriver s
  + Seletek Driver:
    - Added this driver to control focusers based on the Seletek by Lunatico (http://lunatico.es)
    


- Version 1.33 (October 6, 2013):
  + Driver library:
    - Updates to the class INDIFilterWheelDriver to adjust to the standard properties in the INDI protocol.
  + QHY Filter Wheel Driver:
    - Updated driver to conform the new INDIFilterWheelDriver and added the posibility to configure the wheel (specify filter positions).
  + Server library:
    - Minimal corrections.

- Version 1.32 (July 25, 2013):
  + AndroidUI library:
    - Some refactoring. The Application and Activity classes are moved to the examples package.
    - "Better" looking Views
    - Some icons added.
    - Now application can change orientation without reseting interface.
    - Tabs can be removed dinamically. So we handle device removals and disconnections.
  + Client library:
    - Avoid some null pointer excetion in disconnect() in INDIServerConnection (fatal for Android).
    - Added some convenience methods to retrieve Properties by group name in INDIDevice and all Properties in an INDIDevice.
    - Added some convenience methods to get the value of a property as a String.
    - Important bug fixed: listeners could not be notified if some of them were removed while notifying
    - Added the getValueAsString() method to INDIElement s
  + Server library:
    - Basic Server has an added "r" command to reload all jarFiles (very useful for testing).
    - The servers avoid directly loading the INDINotLoadableDriver s
    - Several changes to be able to deal with drivers with subdrivers (unloading and reloading Java drivers not working properly if subdrivers exist).    
  + Driver library:
    - Added a method to get the standard Connection property if the driver implements INDIConnectionHandler and fixed some minor errors regarding the Connection property.
    - Added the class INDINotLoadableDriver to create drivers that should not be directly loaded by a Server.
    - Several changes to be able to deal with drivers with subdrivers.
    - Added methods to serialize and deserialaze properties to disk. This allow to save properties and values in different executions of the driver.
    - Added the class INDIPortProperty to ease dealing with the standard INDI DEVICE_PORT Property.
    - Added a convenience method to turn just one switch element ON and the rest OFF.
    - Added the class INDIFilterWheelDriver for easier development of Filter Wheel drivers.
  + QHY Filter Wheel Driver
    - Added this new driver.
    
- Version 1.31 (April 12, 2012):
  + Server library:
    - Now the Server handles correctly the enableBLOB policies.
    - Added some abstract methods in AbstractINDIServer to be able to handle new connections and broken connections with Clients and detecting when drivers are disconnected / stopped.
    - Added functionality to allow the server to stop listening to new connections and to break all current Client connections.
  + Basic library:
    - Addition of BLOBEnables to Constants
  + Client library:
    - INDIDevice refactored to better implement the enableBLOB messages.
    - INDIDeviceListener refactored to avoid ugly hacks with Strings.



- Version 1.3 (April 9, 2012)
  + Added the Android Client UI library: It allows to create ugly but functional Android Clients.
  + Driver library: 
    - The updateProperty() method in INDIDriver throws a INDIException in case of error (not previously added to the Driver or Switch Property not following its rule).
    - Changed the calls to Arrays.copyOf to be compatible with Android 8.
    - Changed the padLeft function in INDISexagesimalFormatter to be compatible with Android 8.




- Version 1.21 (April 4, 2012):
  + General changes: 
    - String.isEmpty() replaced by String.length() == 0. Android Java version does not allow the isEmpty() method.
    - Change the Project names to better accomodate the INDI for Java name. JAR files also renamed.
  + Client library:
    - UI classes moved to a different library. This refactoring is done to avoid problems with Android apps (Android does not support Swing). The methods "getDefaultUIPanel()" are changed to "getDefaultUIComponent()" that use reflection in order to obtain the best suited UI component (depending on the included UI libraries).
  + Driver library:
    - Added method isStarted() in INDIDriver to know if the Driver has started listening.
  + Server library:
    - Basic server moved to examples package.
    - Some small refactoring to allow to load INDI for Java Drivers that are already in the classpath (via the loadJavaDriver(Class cls)) method.
    - Some functionality of the BasicServer has been moved into a DefaultINDIServer. The INDIServer class has been renamed to AbstractINDIServer.
    - Added the method acceptClient(Socket) to AbstractINDIServer to allow the filtering of clients based on their Inet address.

  

- Version 1.2 (April 2, 2012):
  + Server library available!
    - We have a new library to develop INDI Servers. A basic server has been added that alows to dinamically load and unload INDI for Java Drivers, Native Drivers and Network Drivers (other Servers).
  + Client library changes:
    - Properties return the Elements of their correspondign type (easier coding).
    - UI for properties refactored to be able to create new Panels for properties.
    - New UI for standard CONNECTION property.
    - Corrected a bug that made the status of properties be BUSY if the answer of the Driver is very fast.
    - Added a waitForDevice() methos in INDIServerConnection (similar to the waitForProperty method in INDIDevice).
    - Added a waitForDevice() and waitForProperty() methods with a maximum wait time.
  + Driver library changes:
    - Removed unnecesary disconnect() method in INDIDriver
  + General INDI library changes:
    - Added isValidXXX() functions for PropertyStates, LightState, SwitchStatus, PropertyPermission and SwitchRule in Constants
    - Added XMLToString class.
    - Refactored the socket reading functions in all the driver, client and server libraries.



- Version 1.11 (March 26, 2012):
  + Driver library changes:
    - INDIElements must specify its owner Property. Thus, it is not necessary to add them to the property. At the time of its construction they are added to the property.
    - INDISwitchElement ensure that the other elements are not selected if their owning Property has a rule of ONE_OF_MANY or AT_MOST_ONE and the new value is ON.
    - The generic CONNECTION property is handled via the new INDIConnectionHandler interface. The Drivers will automatically add and handle the property if the Driver implements the interface.
    - Added a printMessage method to allow the easy printing of messages in the Server console.
    - Fixed a major bug that avoided the correct removal of properties when INDIDriver.removeProperty(...) was called.
  + General library
    - Added a INDIException from which all specific exceptions will inherit



- Version 1.1 (March 19, 2012):

  + Added the Driver Library. Now it is possible to create INDI Drivers!
  + A lots of refactoring in the INDI Client.
  + Added a generic Library with the classes shared by the Clients and Drivers.
  + The update Properties method for the Client has been improved. Now it is simpler to ask the Driver to change some Property values.
  + The complete directories of each one of the three libraries is released (source code, Netbeans project, Java Docs and dist files).

 
- Version 1.01 (March 07, 2012):

  + Added a new JApplet INDI Client example application.


- Version 1.0 (March 06, 2012):

  + Initial Version of the Library (just the Client).


