package marcocipriani01.thunder.focus;

import marcocipriani01.thunder.focus.EasyFocuser;
import marcocipriani01.thunder.focus.Main;
import marcocipriani01.thunder.focus.io.ConnectionException;
import marcocipriani01.thunder.focus.io.SerialPortImpl;
import marcocipriani01.thunder.focus.powerbox.ArduinoPin;
import marcocipriani01.thunder.focus.powerbox.PinArray;
import org.indilib.i4j.Constants;
import org.indilib.i4j.driver.*;
import org.indilib.i4j.driver.focuser.INDIFocuserDriver;
import org.indilib.i4j.driver.util.INDIElementBuilder;
import org.indilib.i4j.properties.INDIStandardElement;
import org.indilib.i4j.properties.INDIStandardProperty;
import org.indilib.i4j.protocol.api.INDIConnection;

import java.util.Date;
import java.util.HashMap;

/**
 * INDI ThunderFocuser driver (focuser + power box).
 *
 * @author marcocipriani01
 * @version 4.0
 */
public class INDIThunderFocuserDriver extends INDIFocuserDriver implements EasyFocuser.Listener {

    public static final String DRIVER_NAME = "ThunderFocuser";
    public static final String MANAGE_PINS_GROUP = "Manage Pins";
    public static final String DIGITAL_PINS_PROP = "Digital pins";
    public static final String PWM_PINS_PROP = "PWM pins";

    private final INDITextProperty serialPortFieldProp;
    private final INDITextElement serialPortFieldElem;
    /**
     * Prop to connect or disconnect from the serial port.
     */
    private final INDISwitchProperty connectionProp;
    /**
     * {@link #connectionProp}'s "Connect" element.
     */
    private final INDISwitchElement connectElem;
    /**
     * {@link #connectionProp}'s "Disconnect" element.
     */
    private final INDISwitchElement disconnectElem;
    private final INDINumberProperty focusRelPositionP;
    private final INDINumberElement focusRelPositionE;
    private final INDINumberProperty focuserMaxPositionP;
    private final INDINumberElement focuserMaxPositionE;
    private final INDISwitchProperty focusDirectionP;
    private final INDISwitchElement focusDirectionInE;
    private final INDISwitchElement focusDirectionOutE;
    private final INDISwitchProperty focusReverseP;
    private final INDISwitchElement focusReverseEnE;
    private final INDISwitchElement focusReverseDisE;
    private final INDINumberProperty syncFocusPositionP;
    private final INDINumberElement syncFocusPositionE;
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
    private boolean focusRelDirection = false;

    /**
     * Class constructor. Initializes the INDI properties and elements and looks for available serial ports.
     */
    public INDIThunderFocuserDriver(INDIConnection connection) {
        super(connection);

        connectionProp = newSwitchProperty().name(INDIStandardProperty.CONNECTION).label("Connection").group("Connection")
                .state(Constants.PropertyStates.OK).create();
        connectElem = new INDIElementBuilder<>(INDISwitchElement.class, connectionProp).name(INDIStandardElement.CONNECT)
                .label("Connect").switchValue(Constants.SwitchStatus.OFF).create();
        disconnectElem = new INDIElementBuilder<>(INDISwitchElement.class, connectionProp).name(INDIStandardElement.DISCONNECT)
                .label("Disconnect").switchValue(Constants.SwitchStatus.ON).create();
        addProperty(connectionProp);
        serialPortFieldProp = newTextProperty().name(INDIStandardProperty.DEVICE_PORT).label("Port").group("Connection")
                .state(Constants.PropertyStates.OK).create();
        serialPortFieldElem = new INDIElementBuilder<>(INDITextElement.class, serialPortFieldProp)
                .name(INDIStandardElement.PORT).label("Port").textValue(Main.settings.serialPort).create();
        addProperty(serialPortFieldProp);
        refreshSerialPorts();

        focusRelPositionP = newNumberProperty().name(INDIStandardProperty.REL_FOCUS_POSITION).label("Relative").group("Control").create();
        focusRelPositionE = focusRelPositionP.newElement().name(INDIStandardElement.FOCUS_RELATIVE_POSITION).label("Focus Movement").step(1d).numberFormat("%.0f")
                .numberValue(100).minimum(0).maximum(2147483647).create();

        focusDirectionP = newSwitchProperty().name(INDIStandardProperty.FOCUS_MOTION).label("Direction").group("Control").create();
        focusDirectionInE = focusDirectionP.newElement().name(INDIStandardElement.FOCUS_INWARD).label("Focus in").switchValue(Constants.SwitchStatus.OFF).create();
        focusDirectionOutE = focusDirectionP.newElement().name(INDIStandardElement.FOCUS_OUTWARD).label("Focus out").switchValue(Constants.SwitchStatus.ON).create();

        syncFocusPositionP = newNumberProperty().name("FOCUS_SYNC").label("Sync position").group("Control").create();
        syncFocusPositionE = syncFocusPositionP.newElement().name("FOCUS_SYNC_VALUE").label("Sync position").step(1d).numberFormat("%.0f")
                .numberValue(0).minimum(0).maximum(2147483647).create();

        focuserMaxPositionP = newNumberProperty().name("FOCUS_MAX").label("Max position").group("Configuration").create();
        focuserMaxPositionE = focuserMaxPositionP.newElement().name("FOCUS_MAX_VALUE").label("Max position").step(1d).numberFormat("%.0f")
                .numberValue(Main.settings.fokMaxTravel).minimum(0).maximum(2147483647).create();

        focusReverseP = newSwitchProperty().name("FOCUS_REVERSE_MOTION").label("Reverse directions").group("Configuration").create();
        focusReverseEnE = focusReverseP.newElement().name("ENABLED").label("Enabled").switchValue(Constants.SwitchStatus.OFF).create();
        focusReverseDisE = focusReverseP.newElement().name("DISABLED").label("Disabled").switchValue(Constants.SwitchStatus.ON).create();

        Main.focuser.addListener(this);
        if (Main.focuser.isReady()) {
            onReady();
        }
    }

    @Override
    public int getMaximumAbsPos() {
        return 2147483647;
    }

    @Override
    public int getMinimumAbsPos() {
        return -2147483647;
    }

    @Override
    public int getInitialAbsPos() {
        return Main.focuser.getCurrentPos();
    }

    @Override
    public void absolutePositionHasBeenChanged() {
        try {
            Main.focuser.run(EasyFocuser.Commands.FOK_ABS_MOVE, this, getDesiredAbsPosition());
        } catch (ConnectionException e) {
            e.printStackTrace();
            connectionProp.setState(Constants.PropertyStates.ALERT);
            updateProperty(connectionProp);
            absFocusPositionP.setState(Constants.PropertyStates.ALERT);
            updateProperty(absFocusPositionP);
        } catch (EasyFocuser.InvalidParamException e) {
            e.printStackTrace();
            absFocusPositionP.setState(Constants.PropertyStates.ALERT);
            updateProperty(absFocusPositionP);
        }
    }

    @Override
    protected int getMaximumSpeed() {
        return 100;
    }

    @Override
    public void speedHasBeenChanged() {
        try {
            Main.focuser.run(EasyFocuser.Commands.FOK_SET_SPEED, this, getCurrentSpeed());
            desiredSpeedSet();
        } catch (ConnectionException e) {
            e.printStackTrace();
            connectionProp.setState(Constants.PropertyStates.ALERT);
            updateProperty(connectionProp);
            focusSpeedP.setState(Constants.PropertyStates.ALERT);
            updateProperty(focusSpeedP);
        } catch (EasyFocuser.InvalidParamException e) {
            e.printStackTrace();
            focusSpeedP.setState(Constants.PropertyStates.ALERT);
            updateProperty(focusSpeedP);
        }
    }

    @Override
    public void stopHasBeenRequested() {
        try {
            Main.focuser.run(EasyFocuser.Commands.FOK_STOP, this);
            stopped();
        } catch (ConnectionException e) {
            e.printStackTrace();
            connectionProp.setState(Constants.PropertyStates.ALERT);
            updateProperty(connectionProp);
            stopFocusingP.setState(Constants.PropertyStates.ALERT);
            updateProperty(stopFocusingP);
        } catch (EasyFocuser.InvalidParamException e) {
            e.printStackTrace();
            stopFocusingP.setState(Constants.PropertyStates.ALERT);
            updateProperty(stopFocusingP);
        }
    }

    /**
     * Looks for serial ports and displays the available one to the user in the INDI properties.
     */
    private void refreshSerialPorts() {
        if (portsListProp != null && getPropertiesAsList().contains(portsListProp)) {
            removeProperty(portsListProp);
        }
        portsListProp = newSwitchProperty().name("AVAILABLE_PORTS").label("Available ports").group("Connection")
                .state(Constants.PropertyStates.OK).create();
        searchElem = new INDIElementBuilder<>(INDISwitchElement.class, portsListProp).name("REFRESH_PORTS").label("Refresh")
                .switchValue(Constants.SwitchStatus.ON).create();
        portsListElements = new HashMap<>();
        for (String port : SerialPortImpl.scanSerialPorts()) {
            portsListElements.put(
                    new INDIElementBuilder<>(INDISwitchElement.class, portsListProp).name(port).label(port)
                            .switchValue(Constants.SwitchStatus.OFF).create(), port);
        }
        addProperty(portsListProp);
    }

    /**
     * Returns the name of the driver
     */
    @Override
    public String getName() {
        return DRIVER_NAME;
    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
        super.processNewNumberValue(property, timestamp, elementsAndValues);
        if (property == pwmPinsProp) {
            try {
                for (INDINumberElementAndValue eAV : elementsAndValues) {
                    INDINumberElement element = eAV.getElement();
                    ArduinoPin pin = pinsMap.get(element);
                    pin.setValue(eAV.getValue().intValue());
                    element.setValue((double) pin.getValuePwm());
                    Main.focuser.run(EasyFocuser.Commands.POWER_BOX_SET, this, pin.getPin(), pin.getValuePwm());
                }
                pwmPinsProp.setState(Constants.PropertyStates.OK);
                updateProperty(pwmPinsProp);
            } catch (ConnectionException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                pwmPinsProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(pwmPinsProp);
            } catch (EasyFocuser.InvalidParamException e) {
                e.printStackTrace();
                pwmPinsProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(pwmPinsProp);
            }

        } else if (property == focusRelPositionP) {
            try {
                for (INDINumberElementAndValue eAV : elementsAndValues) {
                    INDINumberElement element = eAV.getElement();
                    Double value = eAV.getValue();
                    if (element == focusRelPositionE) {
                        element.setValue(value);
                        Main.focuser.run(EasyFocuser.Commands.FOK_REL_MOVE, this, (focusRelDirection ? (-1) : 1) * value.intValue());
                        break;
                    }
                }
                focusRelPositionP.setState(Constants.PropertyStates.OK);
                updateProperty(focusRelPositionP);
            } catch (ConnectionException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                focusRelPositionP.setState(Constants.PropertyStates.ALERT);
                updateProperty(focusRelPositionP);
            } catch (EasyFocuser.InvalidParamException e) {
                e.printStackTrace();
                focusRelPositionP.setState(Constants.PropertyStates.ALERT);
                updateProperty(focusRelPositionP);
            }

        } else if (property == focuserMaxPositionP) {
            for (INDINumberElementAndValue eAV : elementsAndValues) {
                INDINumberElement element = eAV.getElement();
                Double value = eAV.getValue();
                if (element == focuserMaxPositionE) {
                    element.setValue(value);
                    Main.settings.fokMaxTravel = value.intValue();
                    break;
                }
            }
            focuserMaxPositionP.setState(Constants.PropertyStates.OK);
            updateProperty(focuserMaxPositionP);

        } else if (property == syncFocusPositionP) {
            try {
                for (INDINumberElementAndValue eAV : elementsAndValues) {
                    INDINumberElement element = eAV.getElement();
                    Double value = eAV.getValue();
                    if (element == syncFocusPositionE) {
                        element.setValue(value);
                        Main.focuser.run(EasyFocuser.Commands.FOK_SET_POS, this, value.intValue());
                        break;
                    }
                }
                syncFocusPositionP.setState(Constants.PropertyStates.OK);
                updateProperty(syncFocusPositionP);
            } catch (ConnectionException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                syncFocusPositionP.setState(Constants.PropertyStates.ALERT);
                updateProperty(syncFocusPositionP);
            } catch (EasyFocuser.InvalidParamException e) {
                e.printStackTrace();
                syncFocusPositionP.setState(Constants.PropertyStates.ALERT);
                updateProperty(syncFocusPositionP);
            }
        }
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
        super.processNewTextValue(property, timestamp, elementsAndValues);
        if (property == serialPortFieldProp) {
            for (INDITextElementAndValue eAV : elementsAndValues) {
                INDITextElement element = eAV.getElement();
                String value = eAV.getValue();
                if (element == serialPortFieldElem) {
                    Main.settings.serialPort = value;
                    element.setValue(Main.settings.serialPort);
                    break;
                }
            }
            serialPortFieldProp.setState(Constants.PropertyStates.OK);
            updateProperty(serialPortFieldProp);
        }
    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        super.processNewSwitchValue(property, timestamp, elementsAndValues);
        if (property == connectionProp) {
            try {
                for (INDISwitchElementAndValue eAV : elementsAndValues) {
                    INDISwitchElement element = eAV.getElement();
                    Constants.SwitchStatus value = eAV.getValue();
                    element.setValue(value);
                    if (value == Constants.SwitchStatus.ON) {
                        connectionProp.setState(Constants.PropertyStates.BUSY);
                        if (element == connectElem) {
                            Main.focuser.connect(Main.settings.serialPort);
                        } else if (element == disconnectElem) {
                            Main.focuser.disconnect();
                        }
                        break;
                    }
                }
                updateProperty(connectionProp);
            } catch (ConnectionException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
            }

        } else if (property == portsListProp) {
            for (INDISwitchElementAndValue eAV : elementsAndValues) {
                INDISwitchElement element = eAV.getElement();
                Constants.SwitchStatus value = eAV.getValue();
                element.setValue(value);
                if (value == Constants.SwitchStatus.ON) {
                    if (element == searchElem) {
                        refreshSerialPorts();
                    } else {
                        Main.settings.serialPort = portsListElements.get(element);
                        serialPortFieldElem.setValue(Main.settings.serialPort);
                    }
                }
            }
            portsListProp.setState(Constants.PropertyStates.OK);
            updateProperty(portsListProp);
            updateProperty(serialPortFieldProp);

        } else if (property == digitalPinProps) {
            try {
                for (INDISwitchElementAndValue eAV : elementsAndValues) {
                    INDISwitchElement element = eAV.getElement();
                    ArduinoPin pin = pinsMap.get(element);
                    Constants.SwitchStatus val = eAV.getValue();
                    pin.setValue(ArduinoPin.ValueType.INDI, val);
                    element.setValue(val);
                    Main.focuser.run(EasyFocuser.Commands.POWER_BOX_SET, this, pin.getPin(), pin.getValuePwm());
                }
                digitalPinProps.setState(Constants.PropertyStates.OK);
                updateProperty(digitalPinProps);
            } catch (ConnectionException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                digitalPinProps.setState(Constants.PropertyStates.ALERT);
                updateProperty(digitalPinProps);
            } catch (EasyFocuser.InvalidParamException e) {
                e.printStackTrace();
                digitalPinProps.setState(Constants.PropertyStates.ALERT);
                updateProperty(digitalPinProps);
            }

        } else if (property == focusDirectionP) {
            for (INDISwitchElementAndValue eAV : elementsAndValues) {
                INDISwitchElement element = eAV.getElement();
                Constants.SwitchStatus value = eAV.getValue();
                element.setValue(value);
                if (value == Constants.SwitchStatus.ON) {
                    focusRelDirection = (element == focusDirectionInE);
                    break;
                }
            }
            focusDirectionP.setState(Constants.PropertyStates.OK);
            updateProperty(focusDirectionP);

        } else if (property == focusReverseP) {
            try {
                for (INDISwitchElementAndValue eAV : elementsAndValues) {
                    INDISwitchElement element = eAV.getElement();
                    Constants.SwitchStatus value = eAV.getValue();
                    element.setValue(value);
                    if (value == Constants.SwitchStatus.ON) {
                        Main.focuser.run(EasyFocuser.Commands.FOK_REVERSE_DIR, this, (element == focusReverseEnE) ? 1 : 0);
                        break;
                    }
                }
                focusReverseP.setState(Constants.PropertyStates.OK);
                updateProperty(focusReverseP);
            } catch (ConnectionException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                focusReverseP.setState(Constants.PropertyStates.ALERT);
                updateProperty(focusReverseP);
            } catch (EasyFocuser.InvalidParamException e) {
                e.printStackTrace();
                focusReverseP.setState(Constants.PropertyStates.ALERT);
                updateProperty(focusReverseP);
            }
        }
    }

    @Override
    public void onReady() {
        pinsMap = new HashMap<>();
        if (digitalPinProps != null) removeProperty(digitalPinProps);
        if (pwmPinsProp != null) removeProperty(pwmPinsProp);
        if (Main.focuser.isPowerBox()) {
            PinArray digitalPins = Main.focuser.getDigitalPins();
            if (digitalPins.size() > 0) {
                digitalPinProps = newSwitchProperty().name(DIGITAL_PINS_PROP).label(DIGITAL_PINS_PROP)
                        .group(MANAGE_PINS_GROUP).switchRule(Constants.SwitchRules.ANY_OF_MANY).create();
                for (ArduinoPin pin : digitalPins) {
                    String pinName = pin.getName();
                    pinsMap.put(
                            new INDIElementBuilder<>(INDISwitchElement.class, digitalPinProps).name(pinName)
                                    .label(pinName).switchValue(pin.getValueIndi()).create(), pin);
                }
                addProperty(digitalPinProps);
            }
            PinArray pwmPins = Main.focuser.getPwmPins();
            if (pwmPins.size() > 0) {
                pwmPinsProp = newNumberProperty().name(PWM_PINS_PROP).label(PWM_PINS_PROP)
                        .group(MANAGE_PINS_GROUP).create();
                for (ArduinoPin pin : pwmPins) {
                    String pinName = pin.getName();
                    pinsMap.put(
                            new INDIElementBuilder<>(INDINumberElement.class, pwmPinsProp).name(pinName).label(pinName)
                                    .step(1).numberFormat("%.0f").maximum(255.0).numberValue(pin.getValuePwm()).create(), pin);
                }
                addProperty(pwmPinsProp);
            }
        }

        initializeStandardProperties();
        showSpeedProperty();
        showStopFocusingProperty();

        focusSpeedValueE.setValue(Main.focuser.getSpeed());
        if (Main.focuser.isReverseDir()) {
            focusReverseEnE.setValue(Constants.SwitchStatus.ON);
        } else {
            focusReverseDisE.setValue(Constants.SwitchStatus.ON);
        }
        focuserMaxPositionE.setValue(Main.settings.fokMaxTravel);

        connectElem.setValue(Constants.SwitchStatus.ON);
        disconnectElem.setValue(Constants.SwitchStatus.OFF);
        connectionProp.setState(Constants.PropertyStates.OK);

        addProperty(focusRelPositionP);
        addProperty(focusDirectionP);
        addProperty(syncFocusPositionP);
        addProperty(focuserMaxPositionP);
        addProperty(focusReverseP);
        updateProperty(focusSpeedP);
        updateProperty(connectionProp);
        positionChanged(Main.focuser.getCurrentPos());
    }

    @Override
    public void onReachedPos() {
        finalPositionReached();
    }

    @Override
    public void updateParam(EasyFocuser.Parameters p) {
        switch (p) {
            case CURRENT_POS -> positionChanged(Main.focuser.getCurrentPos());
            case SPEED -> speedChanged(Main.focuser.getSpeed());
            case REQUESTED_POS -> {
                focusAbsolutePositionE.setValue(Main.focuser.getRequestedPos());
                updateProperty(absFocusPositionP);
            }
            case REQUESTED_REL_POS -> {
                int relPos = Main.focuser.getRequestedRelPos();
                if (relPos >= 0) {
                    focusRelPositionE.setValue(relPos);
                    focusDirectionInE.setValue(Constants.SwitchStatus.OFF);
                    focusDirectionOutE.setValue(Constants.SwitchStatus.ON);
                    focusRelDirection = false;
                } else {
                    focusRelPositionE.setValue(- relPos);
                    focusDirectionInE.setValue(Constants.SwitchStatus.ON);
                    focusDirectionOutE.setValue(Constants.SwitchStatus.OFF);
                    focusRelDirection = true;
                }
                updateProperty(focusRelPositionP);
                updateProperty(focusDirectionP);
            }
            case REVERSE_DIR -> {
                boolean reverseDir = Main.focuser.isReverseDir();
                focusReverseEnE.setValue(reverseDir ? Constants.SwitchStatus.ON : Constants.SwitchStatus.OFF);
                focusReverseDisE.setValue(reverseDir ? Constants.SwitchStatus.OFF : Constants.SwitchStatus.ON);
            }
        }
    }

    @Override
    public void updateFocuserState(EasyFocuser.FocuserState focuserState) {
        switch (focuserState) {
            case ERROR -> connectionProp.setState(Constants.PropertyStates.ALERT);
            case NONE -> connectionProp.setState(Constants.PropertyStates.IDLE);
        }
    }

    @Override
    public void updateConnSate(EasyFocuser.ConnState connState) {
        switch (connState) {
            case DISCONNECTED -> {
                if (digitalPinProps != null) {
                    removeProperty(digitalPinProps);
                    digitalPinProps = null;
                }
                if (pwmPinsProp != null) {
                    removeProperty(pwmPinsProp);
                    pwmPinsProp = null;
                }
                if (pinsMap != null) {
                    pinsMap.clear();
                    pinsMap = null;
                }

                removeProperty(focusRelPositionP);
                removeProperty(focusDirectionP);
                removeProperty(syncFocusPositionP);
                removeProperty(focuserMaxPositionP);
                removeProperty(focusReverseP);
                removeProperty(absFocusPositionP);
                hideSpeedProperty();
                hideStopFocusingProperty();

                disconnectElem.setValue(Constants.SwitchStatus.ON);
                connectElem.setValue(Constants.SwitchStatus.OFF);
                connectionProp.setState(Constants.PropertyStates.OK);
            }
            case ERROR -> connectionProp.setState(Constants.PropertyStates.ALERT);
            case TIMEOUT -> connectionProp.setState(Constants.PropertyStates.BUSY);
        }
        updateProperty(connectionProp);
    }

    @Override
    public boolean updateProperty(INDIProperty<?> property) {
        try {
            return super.updateProperty(property);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onCriticalError(Exception e) {

    }
}