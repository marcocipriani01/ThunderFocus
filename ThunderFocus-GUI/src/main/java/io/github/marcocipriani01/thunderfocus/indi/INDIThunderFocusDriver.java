package io.github.marcocipriani01.thunderfocus.indi;

import io.github.marcocipriani01.thunderfocus.board.Focuser;
import io.github.marcocipriani01.thunderfocus.config.Settings;
import io.github.marcocipriani01.thunderfocus.board.ArduinoPin;
import io.github.marcocipriani01.thunderfocus.board.PowerBox;
import io.github.marcocipriani01.thunderfocus.board.Board;
import io.github.marcocipriani01.thunderfocus.io.SerialPortImpl;
import jssc.SerialPortException;
import org.indilib.i4j.Constants;
import org.indilib.i4j.driver.*;
import org.indilib.i4j.driver.focuser.INDIFocuserDriver;
import org.indilib.i4j.driver.util.INDIElementBuilder;
import org.indilib.i4j.properties.INDIStandardElement;
import org.indilib.i4j.properties.INDIStandardProperty;
import org.indilib.i4j.protocol.api.INDIConnection;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static io.github.marcocipriani01.thunderfocus.Main.board;
import static io.github.marcocipriani01.thunderfocus.Main.settings;

/**
 * INDI ThunderFocuser driver (focuser + power box).
 *
 * @author marcocipriani01
 * @version 4.0
 */
public class INDIThunderFocusDriver extends INDIFocuserDriver implements Board.Listener, Settings.SettingsListener {

    public static final String DRIVER_NAME = "ThunderFocus";
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
    private final INDISwitchProperty focusReverseP;
    private final INDISwitchElement focusReverseEnE;
    private final INDISwitchElement focusReverseDisE;
    private final INDINumberProperty syncFocusPositionP;
    private final INDINumberElement syncFocusPositionE;
    /**
     * Prop to choose a port to which this driver will attempt to connect.
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
     * Map that bins all the INDI elements of {@link #digitalPinProps} and
     * {@link #pwmPinsProp} to their correspondent pins.
     */
    private HashMap<INDIElement<?>, ArduinoPin> pinsMap;
    private boolean focusRelDirection = false;

    /**
     * Class constructor. Initializes the INDI properties and elements and looks for available serial ports.
     */
    public INDIThunderFocusDriver(INDIConnection connection) {
        super(connection);

        connectionProp = newSwitchProperty().name(INDIStandardProperty.CONNECTION).label(INDIDriver.GROUP_MAIN_CONTROL).group(INDIDriver.GROUP_MAIN_CONTROL)
                .state(Constants.PropertyStates.OK).create();
        connectElem = new INDIElementBuilder<>(INDISwitchElement.class, connectionProp).name(INDIStandardElement.CONNECT)
                .label("Connect").switchValue(Constants.SwitchStatus.OFF).create();
        disconnectElem = new INDIElementBuilder<>(INDISwitchElement.class, connectionProp).name(INDIStandardElement.DISCONNECT)
                .label("Disconnect").switchValue(Constants.SwitchStatus.ON).create();
        addProperty(connectionProp);
        serialPortFieldProp = newTextProperty().name(INDIStandardProperty.DEVICE_PORT).label("Port").group(INDIDriver.GROUP_MAIN_CONTROL)
                .state(Constants.PropertyStates.OK).create();
        serialPortFieldElem = new INDIElementBuilder<>(INDITextElement.class, serialPortFieldProp)
                .name(INDIStandardElement.PORT).label("Port").textValue(settings.getSerialPort()).create();
        addProperty(serialPortFieldProp);
        refreshSerialPorts();

        focusRelPositionP = newNumberProperty().name(INDIStandardProperty.REL_FOCUS_POSITION).label("Relative").group(INDIDriver.GROUP_MAIN_CONTROL).create();
        focusRelPositionE = focusRelPositionP.newElement().name(INDIStandardElement.FOCUS_RELATIVE_POSITION).label("Focus Movement").step(1d).numberFormat("%.0f")
                .numberValue(settings.relativeStepSize).minimum(0).maximum(2147483647).create();

        focusDirectionP = newSwitchProperty().name(INDIStandardProperty.FOCUS_MOTION).label("Direction").group(INDIDriver.GROUP_MAIN_CONTROL).create();
        focusDirectionInE = focusDirectionP.newElement().name(INDIStandardElement.FOCUS_INWARD).label("Focus in").switchValue(Constants.SwitchStatus.OFF).create();
        focusDirectionP.newElement().name(INDIStandardElement.FOCUS_OUTWARD).label("Focus out").switchValue(Constants.SwitchStatus.ON).create();

        syncFocusPositionP = newNumberProperty().name(INDIStandardProperty.FOCUS_SYNC).label("Sync position").group(INDIDriver.GROUP_MAIN_CONTROL).create();
        syncFocusPositionE = syncFocusPositionP.newElement().name(INDIStandardElement.FOCUS_SYNC_VALUE).label("Sync position").step(1d).numberFormat("%.0f")
                .numberValue(0).minimum(0).maximum(2147483647).create();

        focuserMaxPositionP = newNumberProperty().name(INDIStandardProperty.FOCUS_MAX).label("Max position").group(INDIDriver.GROUP_OPTIONS).create();
        focuserMaxPositionE = focuserMaxPositionP.newElement().name(INDIStandardElement.FOCUS_MAX_VALUE).label("Max position").step(1d).numberFormat("%.0f")
                .numberValue(settings.getFocuserMaxTravel()).minimum(0).maximum(2147483647).create();

        focusReverseP = newSwitchProperty().name(INDIStandardProperty.FOCUS_REVERSE_MOTION).label("Reverse directions").group(INDIDriver.GROUP_OPTIONS).create();
        focusReverseEnE = focusReverseP.newElement().name(INDIStandardElement.ENABLED).label("Enabled").switchValue(Constants.SwitchStatus.OFF).create();
        focusReverseDisE = focusReverseP.newElement().name(INDIStandardElement.DISABLED).label("Disabled").switchValue(Constants.SwitchStatus.ON).create();

        board.addListener(this);
        if (board.isReady()) onBoardConnected();
    }

    @Override
    public void isBeingDestroyed() {
        System.out.println("Destroying ThunderFocus INDI driver...");
        board.removeListener(this);
        super.isBeingDestroyed();
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
        return board.focuser().getPos();
    }

    @Override
    public void absolutePositionHasBeenChanged() {
        try {
            board.run(Board.Commands.FOCUSER_ABS_MOVE, this, getDesiredAbsPosition());
        } catch (IOException | SerialPortException e) {
            e.printStackTrace();
            connectionProp.setState(Constants.PropertyStates.ALERT);
            updateProperty(connectionProp);
            absFocusPositionP.setState(Constants.PropertyStates.ALERT);
            updateProperty(absFocusPositionP);
        } catch (IllegalArgumentException e) {
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
            board.run(Board.Commands.FOCUSER_SET_SPEED, this, getCurrentSpeed());
            desiredSpeedSet();
        } catch (IOException | SerialPortException e) {
            e.printStackTrace();
            connectionProp.setState(Constants.PropertyStates.ALERT);
            updateProperty(connectionProp);
            focusSpeedP.setState(Constants.PropertyStates.ALERT);
            updateProperty(focusSpeedP);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            focusSpeedP.setState(Constants.PropertyStates.ALERT);
            updateProperty(focusSpeedP);
        }
    }

    @Override
    public void stopHasBeenRequested() {
        try {
            board.run(Board.Commands.FOCUSER_STOP, this);
            stopped();
        } catch (IOException | SerialPortException e) {
            e.printStackTrace();
            connectionProp.setState(Constants.PropertyStates.ALERT);
            updateProperty(connectionProp);
            stopFocusingP.setState(Constants.PropertyStates.ALERT);
            updateProperty(stopFocusingP);
        } catch (IllegalArgumentException e) {
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
        portsListProp = newSwitchProperty().name("AVAILABLE_PORTS").label("Available ports").group(INDIDriver.GROUP_MAIN_CONTROL)
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
                    element.setValue((double) pin.getValuePWM());
                    board.run(Board.Commands.POWER_BOX_SET, this, pin.getNumber(), pin.getValuePWM());
                }
                pwmPinsProp.setState(Constants.PropertyStates.OK);
                updateProperty(pwmPinsProp);
            } catch (IOException | SerialPortException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                pwmPinsProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(pwmPinsProp);
            } catch (IllegalArgumentException e) {
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
                        board.run(Board.Commands.FOCUSER_REL_MOVE, this, (focusRelDirection ? (-1) : 1) * value.intValue());
                        break;
                    }
                }
                focusRelPositionP.setState(Constants.PropertyStates.OK);
                updateProperty(focusRelPositionP);
            } catch (IOException | SerialPortException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                focusRelPositionP.setState(Constants.PropertyStates.ALERT);
                updateProperty(focusRelPositionP);
            } catch (IllegalArgumentException e) {
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
                    settings.setFokMaxTravel(value.intValue(), this);
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
                        board.run(Board.Commands.FOCUSER_SET_POS, this, value.intValue());
                        break;
                    }
                }
                syncFocusPositionP.setState(Constants.PropertyStates.OK);
                updateProperty(syncFocusPositionP);
            } catch (IOException | SerialPortException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                syncFocusPositionP.setState(Constants.PropertyStates.ALERT);
                updateProperty(syncFocusPositionP);
            } catch (IllegalArgumentException e) {
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
                    settings.setSerialPort(value, this);
                    element.setValue(value);
                    try {
                        settings.save();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
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
                            board.connect(settings.getSerialPort());
                        } else if (element == disconnectElem) {
                            board.disconnect();
                        }
                        break;
                    }
                }
                updateProperty(connectionProp);
            } catch (SerialPortException e) {
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
                        String serialPort = portsListElements.get(element);
                        settings.setSerialPort(serialPort, this);
                        serialPortFieldElem.setValue(serialPort);
                        try {
                            settings.save();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
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
                    pin.setValue(val);
                    element.setValue(val);
                    board.run(Board.Commands.POWER_BOX_SET, this, pin.getNumber(), pin.getValuePWM());
                }
                digitalPinProps.setState(Constants.PropertyStates.OK);
                updateProperty(digitalPinProps);
            } catch (IOException | SerialPortException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                digitalPinProps.setState(Constants.PropertyStates.ALERT);
                updateProperty(digitalPinProps);
            } catch (IllegalArgumentException e) {
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
                        board.run(Board.Commands.FOCUSER_REVERSE_DIR, this, (element == focusReverseEnE) ? 1 : 0);
                        break;
                    }
                }
                focusReverseP.setState(Constants.PropertyStates.OK);
                updateProperty(focusReverseP);
            } catch (IOException | SerialPortException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                focusReverseP.setState(Constants.PropertyStates.ALERT);
                updateProperty(focusReverseP);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                focusReverseP.setState(Constants.PropertyStates.ALERT);
                updateProperty(focusReverseP);
            }
        }
    }

    @Override
    protected int getSpeedStep() {
        return 5;
    }

    @Override
    protected int getInitialSpeed() {
        return board.focuser().getSpeed();
    }

    private void onBoardConnected() {
        pinsMap = new HashMap<>();
        if (digitalPinProps != null) removeProperty(digitalPinProps);
        if (pwmPinsProp != null) removeProperty(pwmPinsProp);
        if (board.hasPowerBox()) {
            PowerBox powerBox = board.powerBox();
            if (powerBox.countDigitalPins() > 0) {
                digitalPinProps = newSwitchProperty().name(DIGITAL_PINS_PROP).label(DIGITAL_PINS_PROP)
                        .group(MANAGE_PINS_GROUP).switchRule(Constants.SwitchRules.ANY_OF_MANY).create();
                for (ArduinoPin pin : powerBox.listOnlyDigital()) {
                    String pinName = pin.getName();
                    pinsMap.put(new INDIElementBuilder<>(INDISwitchElement.class, digitalPinProps).name(pinName)
                            .label(pinName).switchValue(pin.getValueINDI()).create(), pin);
                }
                addProperty(digitalPinProps);
            }
            if (powerBox.countPWMEnabledPins() > 0) {
                pwmPinsProp = newNumberProperty().name(PWM_PINS_PROP).label(PWM_PINS_PROP)
                        .group(MANAGE_PINS_GROUP).create();
                for (ArduinoPin pin : powerBox.listOnlyPWMEnabled()) {
                    String pinName = pin.getName();
                    pinsMap.put(new INDIElementBuilder<>(INDINumberElement.class, pwmPinsProp).name(pinName).label(pinName)
                            .step(1).numberFormat("%.0f").maximum(255.0).numberValue(pin.getValuePWM()).create(), pin);
                }
                addProperty(pwmPinsProp);
            }
        }

        initializeStandardProperties();
        showSpeedProperty();
        showStopFocusingProperty();
        Focuser focuser = board.focuser();
        if (focuser.isDirInverted()) {
            focusReverseEnE.setValue(Constants.SwitchStatus.ON);
        } else {
            focusReverseDisE.setValue(Constants.SwitchStatus.ON);
        }
        focuserMaxPositionE.setValue(settings.getFocuserMaxTravel());
        connectElem.setValue(Constants.SwitchStatus.ON);
        disconnectElem.setValue(Constants.SwitchStatus.OFF);
        connectionProp.setState(Constants.PropertyStates.OK);
        addProperty(focusRelPositionP);
        addProperty(focusDirectionP);
        addProperty(syncFocusPositionP);
        addProperty(focuserMaxPositionP);
        addProperty(focusReverseP);
        updateProperty(connectionProp);
        positionChanged(focuser.getPos());
    }

    @Override
    public void onReachedPos() {
        finalPositionReached();
    }

    @Override
    public void updateParam(Board.Parameters p) {
        switch (p) {
            case CURRENT_POS -> positionChanged(board.focuser().getPos());
            case SPEED -> speedChanged(board.focuser().getSpeed());
            case REQUESTED_POS -> {
                absFocusPositionE.setValue(board.focuser().getTargetPos());
                updateProperty(absFocusPositionP);
            }
            case REVERSE_DIR -> {
                boolean reverseDir = board.focuser().isDirInverted();
                focusReverseEnE.setValue(reverseDir ? Constants.SwitchStatus.ON : Constants.SwitchStatus.OFF);
                focusReverseDisE.setValue(reverseDir ? Constants.SwitchStatus.OFF : Constants.SwitchStatus.ON);
            }
        }
    }

    @Override
    public void updateFocuserState(Focuser.FocuserState focuserState) {
        if (focuserState == Focuser.FocuserState.NONE) {
            connectionProp.setState(Constants.PropertyStates.IDLE);
        }
    }

    @Override
    public void updateConnectionState(Board.ConnectionState connectionState) {
        switch (connectionState) {
            case CONNECTED_READY -> onBoardConnected();
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
        } catch (Exception e) {
            System.err.println("INDI driver updateProperty() error.");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateFocuserMaxTravel(int value) {
        focuserMaxPositionE.setValue(value);
        updateProperty(focuserMaxPositionP);
    }

    @Override
    public void updateSerialPort(String value) {
        serialPortFieldElem.setValue(value);
        updateProperty(serialPortFieldProp);
    }
}