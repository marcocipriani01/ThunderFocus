package marcocipriani.openfocuser.manager.indi;

import laazotea.indi.Constants;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIException;
import laazotea.indi.driver.*;
import marcocipriani.openfocuser.manager.Main;
import marcocipriani.openfocuser.manager.io.ConnectionException;
import marcocipriani.openfocuser.manager.io.SerialMessageListener;
import marcocipriani.openfocuser.manager.io.SerialPortImpl;
import marcocipriani.openfocuser.manager.io.SerialPortMultiplexer;
import marcocipriani.openfocuser.manager.plus.ArduinoPin;
import marcocipriani.openfocuser.manager.plus.PinArray;
import marcocipriani.openfocuser.manager.plus.PinValue;
import marcocipriani.openfocuser.manager.plus.Settings;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * INDI Arduino pin driver.
 *
 * @author marcocipriani01
 * @version 2.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class INDIArduinoDriver extends INDIDriver implements INDIConnectionHandler, SerialMessageListener {

    /**
     * The board to control.
     */
    private SerialPortImpl serialPort;
    /**
     * Serial port multiplexer, used to create a virtual device which allows the user to connect
     * to the MoonLite focuser.
     */
    private SerialPortMultiplexer multiplexer;
    /**
     * Stores the currently chosen serial port.
     */
    private String serialPortString;
    // The properties and elements of this driver
    /**
     * Serial port text field - prop.
     */
    private INDITextProperty serialPortFieldProp;
    /**
     * Serial port text field - elem.
     */
    private INDITextElement serialPortFieldElem;
    /**
     * Prop to connect or disconnect from the serial port.
     */
    private INDISwitchProperty connectionProp;
    /**
     * {@link #connectionProp}'s "Connect" element.
     */
    private INDISwitchElement connectElem;
    /**
     * {@link #connectionProp}'s "Disconnect" element.
     */
    private INDISwitchElement disconnectElem;
    /**
     * Prop to chose a port to which this driver will attempt to connect.
     */
    private INDISwitchProperty portsListProp;
    /**
     * {@link #portsListProp}'s element to scan the available serial ports again.
     */
    private INDISwitchElement searchElem;
    /**
     * {@link #portsListProp}'s elements representing available serial ports.
     */
    private HashMap<INDISwitchElement, String> portsListElements;
    /**
     * The property of the digital pins.
     */
    private INDISwitchProperty digitalPinProps;
    /**
     * The property of the PWM pins.
     */
    private INDINumberProperty pwmPinsProp;
    /**
     * Map that bins all the INDI elements of {@link #digitalPinProps} and {@link #pwmPinsProp} to their correspondent pins.
     */
    private HashMap<INDIElement, ArduinoPin> pinsMap;
    /**
     * MoonLite virtual serial port RO field.
     */
    private INDITextProperty moonLitePortProp;

    /**
     * Class constructor. Initializes the INDI properties and elements and looks for available serial ports.
     *
     * @param inputStream        an input stream from which this driver will retrieve the messages from the server.
     * @param outputStream       an output stream to which this driver will send messages to the server.
     * @param connectImmediately if {@code true} the driver will attempt to connect to the default serial port immediately.
     */
    public INDIArduinoDriver(InputStream inputStream, OutputStream outputStream, boolean connectImmediately) {
        this(inputStream, outputStream);
        if (connectImmediately) {
            serialInit();
            try {
                updateProperty(connectionProp);

            } catch (INDIException e) {
                Main.err(e.getMessage(), e, false);
            }
        }
    }

    /**
     * Class constructor. Initializes the INDI properties and elements and looks for available serial ports.
     *
     * @param inputStream  an input stream from which this driver will retrieve the messages from the server.
     * @param outputStream an output stream to which this driver will send messages to the server.
     */
    public INDIArduinoDriver(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        serialPortString = Main.getSettings().getUsbPort();
        serialPortFieldProp = new INDITextProperty(this, "Serial port", "Serial port", "Serial connection",
                PropertyStates.OK, PropertyPermissions.RW);
        serialPortFieldElem = new INDITextElement(serialPortFieldProp, "Serial port", "Serial port", serialPortString);
        scanSerialPorts();
        connectionProp = new INDISwitchProperty(this, "Connection", "Serial connection", "Serial connection",
                PropertyStates.OK, PropertyPermissions.RW, Constants.SwitchRules.ONE_OF_MANY);
        connectElem = new INDISwitchElement(connectionProp, "Connect", "Connect", Constants.SwitchStatus.OFF);
        disconnectElem = new INDISwitchElement(connectionProp, "Disconnect", "Disconnect", Constants.SwitchStatus.ON);
    }

    /**
     * Looks for serial ports and displays the available one to the user in the INDI properties.
     */
    private void scanSerialPorts() {
        boolean propertyAdded;
        if (propertyAdded = (portsListProp != null && getPropertiesAsList().contains(portsListProp))) {
            removeProperty(portsListProp);
        }
        portsListProp = new INDISwitchProperty(this, "Available ports", "Available ports", "Serial connection",
                PropertyStates.OK, PropertyPermissions.RW, Constants.SwitchRules.ONE_OF_MANY);
        searchElem = new INDISwitchElement(portsListProp, "Refresh", Constants.SwitchStatus.ON);
        portsListElements = new HashMap<>();
        for (String port : SerialPortImpl.scanSerialPorts()) {
            portsListElements.put(new INDISwitchElement(portsListProp, port, port,
                    Constants.SwitchStatus.OFF), port);
        }
        if (propertyAdded) {
            addProperty(portsListProp);
        }
    }

    /**
     * Forces the board to restart and cleans all the pins' values.
     * Attention: may reset the focuser status!
     */
    public void forceReboot() {
        if (!serialPort.isConnected()) {
            throw new ConnectionException(ConnectionException.Type.NOT_CONNECTED);
        }
        Main.info("Force reboot invoked!");
        serialPort.print(":RS#");
        Main.err("Cleaning all the values of the map...");
        for (INDIElement element : pinsMap.keySet()) {
            if (element instanceof INDINumberElement) {
                element.setValue(0.0);

            } else if (element instanceof INDISwitchElement) {
                element.setValue(Constants.SwitchStatus.OFF);
            }
        }
        for (ArduinoPin pin : pinsMap.values()) {
            pin.setPinValueObj(new PinValue());
        }
        try {
            ArrayList<INDIProperty> properties = getPropertiesAsList();
            // Avoid updating properties that haven't been added (if this driver hasn't been connected yet to a server)
            for (INDIProperty property : properties) {
                if (property == digitalPinProps) {
                    updateProperty(digitalPinProps);

                } else if (property == pwmPinsProp) {
                    updateProperty(pwmPinsProp);
                }
            }

        } catch (INDIException e) {
            Main.err(e.getMessage(), e, false);
        }
    }

    /**
     * Updates a pin's value.
     *
     * @param pin the pin ID.
     */
    private void updatePin(ArduinoPin pin) {
        StringBuilder hex = new StringBuilder(Integer.toHexString(Integer.valueOf(pin.getPin() +
                String.format("%03d", pin.getValuePwm()))));
        while (hex.length() < 4) {
            hex.insert(0, "0");
        }
        serialPort.print(":AV" + hex + "#");
    }

    /**
     * Attempt to connect to the stored serial port.
     */
    private void serialInit() {
        if ((serialPort == null) && (!serialPortString.equals(""))) {
            try {
                Main.err("Connecting to the Serial port...");
                serialPort = new SerialPortImpl(serialPortString);
                serialPort.addListener(this);
                // Restart the board to ensure that all the pins are turned off.
                serialPort.print(":RS#");

                Settings settings = Main.getSettings();
                ArduinoPin[] switchPins = settings.getDigitalPins().toArray(),
                        pwmPins = settings.getPwmPins().toArray();
                // Look for duplicated pins
                try {
                    if (!PinArray.checkPins(switchPins, pwmPins)) {
                        throw new IllegalStateException("Duplicated pins found, please fix this in order to continue.");
                    }

                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }

                pinsMap = new HashMap<>();
                digitalPinProps = new INDISwitchProperty(this, "Digital pins", "Digital pins", "Manage Pins",
                        PropertyStates.OK, PropertyPermissions.RW, Constants.SwitchRules.ANY_OF_MANY);
                for (ArduinoPin pin : switchPins) {
                    Main.err("Defining digital pin: " + pin);
                    updatePin(pin);
                    pinsMap.put(new INDISwitchElement(digitalPinProps, "Pin " + pin.getPin(),
                            pin.getName(), pin.getValueIndi()), pin);
                }
                pwmPinsProp = new INDINumberProperty(this, "PWM pins", "PWM pins", "Manage Pins",
                        PropertyStates.OK, PropertyPermissions.RW);
                for (ArduinoPin pin : pwmPins) {
                    Main.err("Defining PWM pin: " + pin);
                    updatePin(pin);
                    pinsMap.put(new INDINumberElement(pwmPinsProp, "PWM pin" + pin.getPin(), pin.getName(),
                            (double) pin.getValuePercentage(), 0.0, 100.0, 1.0, "%f"), pin);
                }

                Main.err("Loading port forwarder (socat)...");
                multiplexer = new SerialPortMultiplexer(serialPort);
                String mockedPort = multiplexer.getMockedPort();
                moonLitePortProp = new INDITextProperty(this, "MoonLite port", "MoonLite port",
                        "Serial connection", PropertyStates.OK, PropertyPermissions.RO);
                new INDITextElement(moonLitePortProp, "MoonLite port", "MoonLite port", mockedPort);
                addProperty(moonLitePortProp);
                addProperty(digitalPinProps);
                addProperty(pwmPinsProp);
                connectElem.setValue(Constants.SwitchStatus.ON);
                disconnectElem.setValue(Constants.SwitchStatus.OFF);
                connectionProp.setState(PropertyStates.OK);
                Main.info("Connect MoonLite to port " + mockedPort);

            } catch (ConnectionException e) {
                connectionProp.setState(PropertyStates.ALERT);
                if (serialPort != null) {
                    try {
                        serialPort.removeListener(this);
                        serialPort.disconnect();
                        serialPort = null;

                    } catch (ConnectionException de) {
                        Main.err(de.getMessage(), de, false);
                    }
                }
                Main.err(e.getMessage(), e, false);
            }

        } else {
            connectionProp.setState(PropertyStates.ALERT);
        }
    }

    /**
     * Returns the name of the driver
     */
    @Override
    public String getName() {
        return "INDI Arduino pin driver";
    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
        if (property == pwmPinsProp) {
            for (INDINumberElementAndValue eAV : elementsAndValues) {
                INDINumberElement element = eAV.getElement();
                ArduinoPin pin = pinsMap.get(element);
                PinValue newValue = new PinValue(PinValue.ValueType.PERCENTAGE, eAV.getValue().intValue());
                if (!newValue.equals(pin.getPinValueObj())) {
                    pin.setPinValueObj(newValue);
                    element.setValue((double) pin.getValuePercentage());
                    updatePin(pin);
                }
            }
            pwmPinsProp.setState(PropertyStates.OK);
            try {
                updateProperty(pwmPinsProp);

            } catch (INDIException e) {
                Main.err(e.getMessage(), e, false);
            }
        }
    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {

    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
        if (property == serialPortFieldProp) {
            for (INDITextElementAndValue eAV : elementsAndValues) {
                if (eAV.getElement() == serialPortFieldElem) {
                    serialPortString = eAV.getValue();
                    eAV.getElement().setValue(serialPortString);
                    break;
                }
            }
            serialPortFieldProp.setState(PropertyStates.OK);
            try {
                updateProperty(serialPortFieldProp);

            } catch (INDIException e) {
                Main.err(e.getMessage(), e, false);
            }
        }
    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        if (property == digitalPinProps) {
            for (INDISwitchElementAndValue eAV : elementsAndValues) {
                INDISwitchElement element = eAV.getElement();
                ArduinoPin pin = pinsMap.get(element);
                PinValue newValue = new PinValue(PinValue.ValueType.INDI, eAV.getValue());
                if (!newValue.equals(pin.getPinValueObj())) {
                    pin.setPinValueObj(newValue);
                    element.setValue(newValue.getValueIndi());
                    updatePin(pin);
                }
            }
            digitalPinProps.setState(PropertyStates.OK);
            try {
                updateProperty(digitalPinProps);

            } catch (INDIException e) {
                Main.err(e.getMessage(), e, false);
            }

        } else if (property == connectionProp) {
            for (INDISwitchElementAndValue eAV : elementsAndValues) {
                INDISwitchElement element = eAV.getElement();
                Constants.SwitchStatus value = eAV.getValue();
                if (value == Constants.SwitchStatus.ON) {
                    if (element == connectElem) {
                        serialInit();

                    } else if (element == disconnectElem) {
                        serialDisconnect();
                    }
                    break;
                }
            }
            try {
                updateProperty(connectionProp);

            } catch (INDIException e) {
                Main.err(e.getMessage(), e, false);
            }

        } else if (property == portsListProp) {
            for (INDISwitchElementAndValue eAV : elementsAndValues) {
                INDISwitchElement element = eAV.getElement();
                Constants.SwitchStatus value = eAV.getValue();
                element.setValue(value);
                if (value == Constants.SwitchStatus.ON) {
                    if (element == searchElem) {
                        scanSerialPorts();

                    } else {
                        serialPortString = portsListElements.get(element);
                        serialPortFieldElem.setValue(serialPortString);
                    }
                }
            }
            portsListProp.setState(PropertyStates.OK);
            try {
                updateProperty(portsListProp);
                updateProperty(serialPortFieldProp);

            } catch (INDIException e) {
                Main.err(e.getMessage(), e, false);
            }
        }
    }

    private void serialDisconnect() {
        if (serialPort != null) {
            try {
                serialPort.removeListener(this);
                multiplexer.stop();
                serialPort = null;
                multiplexer = null;
                removeProperty(moonLitePortProp);
                moonLitePortProp = null;
                removeProperty(digitalPinProps);
                digitalPinProps = null;
                removeProperty(pwmPinsProp);
                pwmPinsProp = null;
                pinsMap.clear();
                pinsMap = null;
                disconnectElem.setValue(Constants.SwitchStatus.ON);
                connectElem.setValue(Constants.SwitchStatus.OFF);
                connectionProp.setState(PropertyStates.OK);

            } catch (ConnectionException e) {
                Main.err(e.getMessage(), e, false);
                connectionProp.setState(PropertyStates.ALERT);
            }

        } else {
            connectionProp.setState(PropertyStates.ALERT);
        }
    }

    @Override
    public void driverConnect(Date timestamp) {
        Main.info("Driver connection");
        addProperty(connectionProp);
        addProperty(serialPortFieldProp);
        if (!getPropertiesAsList().contains(portsListProp)) {
            addProperty(portsListProp);
        }
    }

    @Override
    public void driverDisconnect(Date timestamp) {
        Main.info("Driver disconnection");
        serialDisconnect();
        removeProperty(connectionProp);
        removeProperty(serialPortFieldProp);
        if (getPropertiesAsList().contains(portsListProp)) {
            removeProperty(portsListProp);
        }
    }

    /**
     * Called when a new message is received from the Arduino.
     *
     * @param msg the received message.
     */
    @Override
    public void onPortMessage(String msg) {

    }

    /**
     * Called when an error occurs.
     *
     * @param e the {@code Exception}.
     */
    @Override
    public void onPortError(Exception e) {
        Main.err(e.getMessage(), e, false);
    }
}