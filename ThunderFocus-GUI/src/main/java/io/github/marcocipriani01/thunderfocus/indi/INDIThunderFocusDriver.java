package io.github.marcocipriani01.thunderfocus.indi;

import io.github.marcocipriani01.thunderfocus.board.*;
import io.github.marcocipriani01.thunderfocus.config.Settings;
import io.github.marcocipriani01.thunderfocus.serial.SerialPortImpl;
import jssc.SerialPortException;
import org.indilib.i4j.Constants;
import org.indilib.i4j.driver.*;
import org.indilib.i4j.driver.focuser.INDIFocuserDriver;
import org.indilib.i4j.driver.util.INDIElementBuilder;
import org.indilib.i4j.properties.INDIStandardElement;
import org.indilib.i4j.properties.INDIStandardProperty;
import org.indilib.i4j.protocol.api.INDIConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static io.github.marcocipriani01.thunderfocus.Main.board;
import static io.github.marcocipriani01.thunderfocus.Main.settings;
import static io.github.marcocipriani01.thunderfocus.board.PowerBox.ABSOLUTE_ZERO;
import static io.github.marcocipriani01.thunderfocus.board.PowerBox.INVALID_HUMIDITY;

/**
 * INDI ThunderFocuser driver (focuser + power box).
 *
 * @author marcocipriani01
 * @version 4.0
 */
public class INDIThunderFocusDriver extends INDIFocuserDriver implements Board.Listener, Settings.SettingsListener {

    public static final String DRIVER_NAME = "ThunderFocus";
    public static final String DIGITAL_PINS_PROP = "Digital outputs";
    public static final String PWM_PINS_PROP = "Analog outputs";

    private final INDITextProperty serialPortFieldProp;
    private final INDITextElement serialPortFieldElem;
    private final INDISwitchProperty connectionProp;
    private final INDISwitchElement connectElem;
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
    private final INDINumberProperty ambientP;
    private final INDINumberElement temperatureE;
    private final INDINumberElement humidityE;
    private final INDISwitchProperty flatLightP;
    private final INDISwitchElement flatLightOnE;
    private final INDISwitchElement flatLightOffE;
    private final INDINumberProperty flatBrightnessP;
    private final INDINumberElement flatBrightnessE;
    private final INDISwitchProperty dustCapP;
    private final INDISwitchElement dustCapOpenE;
    private final INDISwitchElement dustCapCloseE;
    private INDISwitchProperty portsListProp;
    private INDISwitchElement searchElem;
    private HashMap<INDISwitchElement, String> portsListElements;
    private INDISwitchProperty digitalPinsProps;
    private INDINumberProperty pwmPinsProp;
    private HashMap<INDIElement<?>, ArduinoPin> pinsMap;
    private boolean relativeFocusDirection = false;

    /**
     * Class constructor. Initializes the INDI properties and elements and looks for available serial ports.
     */
    public INDIThunderFocusDriver(INDIConnection connection) {
        super(connection);

        connectionProp = newSwitchProperty().name(INDIStandardProperty.CONNECTION).label(GROUP_MAIN_CONTROL).group(GROUP_MAIN_CONTROL)
                .state(Constants.PropertyStates.OK).create();
        connectElem = new INDIElementBuilder<>(INDISwitchElement.class, connectionProp).name(INDIStandardElement.CONNECT)
                .label("Connect").switchValue(Constants.SwitchStatus.OFF).create();
        disconnectElem = new INDIElementBuilder<>(INDISwitchElement.class, connectionProp).name(INDIStandardElement.DISCONNECT)
                .label("Disconnect").switchValue(Constants.SwitchStatus.ON).create();
        addProperty(connectionProp);
        serialPortFieldProp = newTextProperty().name(INDIStandardProperty.DEVICE_PORT).label("Port").group(GROUP_MAIN_CONTROL)
                .state(Constants.PropertyStates.OK).create();
        serialPortFieldElem = new INDIElementBuilder<>(INDITextElement.class, serialPortFieldProp)
                .name(INDIStandardElement.PORT).label("Port").textValue(settings.getSerialPort()).create();
        addProperty(serialPortFieldProp);
        refreshSerialPorts();

        focusRelPositionP = newNumberProperty().name(INDIStandardProperty.REL_FOCUS_POSITION).label("Relative").group(GROUP_MAIN_CONTROL).create();
        focusRelPositionE = focusRelPositionP.newElement().name(INDIStandardElement.FOCUS_RELATIVE_POSITION).label("Focus Movement").step(1d)
                .numberFormat("%.0f")
                .numberValue(settings.relativeStepSize).minimum(0).maximum(2147483647).create();

        focusDirectionP = newSwitchProperty().name(INDIStandardProperty.FOCUS_MOTION).label("Direction").group(GROUP_MAIN_CONTROL).create();
        focusDirectionInE = focusDirectionP.newElement().name(INDIStandardElement.FOCUS_INWARD).label("Focus in").switchValue(Constants.SwitchStatus.OFF).create();
        focusDirectionP.newElement().name(INDIStandardElement.FOCUS_OUTWARD).label("Focus out").switchValue(Constants.SwitchStatus.ON).create();

        syncFocusPositionP = newNumberProperty().name(INDIStandardProperty.FOCUS_SYNC).label("Sync position").group(GROUP_MAIN_CONTROL).create();
        syncFocusPositionE = syncFocusPositionP.newElement().name(INDIStandardElement.FOCUS_SYNC_VALUE).label("Sync position").step(1d)
                .numberFormat("%.0f").numberValue(0).minimum(0).maximum(2147483647).create();

        focuserMaxPositionP = newNumberProperty().name(INDIStandardProperty.FOCUS_MAX).label("Max position").group(GROUP_OPTIONS).create();
        focuserMaxPositionE = focuserMaxPositionP.newElement().name(INDIStandardElement.FOCUS_MAX_VALUE).label("Max position").step(1d)
                .numberFormat("%.0f").numberValue(settings.getFocuserMaxTravel()).minimum(0).maximum(2147483647).create();

        focusReverseP = newSwitchProperty().name(INDIStandardProperty.FOCUS_REVERSE_MOTION).label("Reverse directions").group(GROUP_OPTIONS).create();
        focusReverseEnE = focusReverseP.newElement().name(INDIStandardElement.ENABLED).label("Enabled").switchValue(Constants.SwitchStatus.OFF).create();
        focusReverseDisE = focusReverseP.newElement().name(INDIStandardElement.DISABLED).label("Disabled").switchValue(Constants.SwitchStatus.ON).create();

        ambientP = newNumberProperty().name(INDIStandardProperty.ATMOSPHERE).label("Sensors data").group(GROUP_MAIN_CONTROL)
                .permission(Constants.PropertyPermissions.RO).create();
        temperatureE = ambientP.newElement().name(INDIStandardElement.TEMPERATURE).label("Temperature").step(0.01).numberFormat("%.1f")
                .numberValue(ABSOLUTE_ZERO).minimum(ABSOLUTE_ZERO).maximum(1000.0).create();
        humidityE = ambientP.newElement().name(INDIStandardElement.HUMIDITY).label("Humidity").step(0.01).numberFormat("%.1f")
                .numberValue(INVALID_HUMIDITY).minimum(INVALID_HUMIDITY).maximum(100.0).create();

        flatLightP = newSwitchProperty().name("FLAT_LIGHT_CONTROL").label("Flat light").group(GROUP_MAIN_CONTROL).create();
        flatLightOnE = flatLightP.newElement().name("FLAT_LIGHT_ON").label("On").switchValue(Constants.SwitchStatus.ON).create();
        flatLightOffE = flatLightP.newElement().name("FLAT_LIGHT_OFF").label("Off").switchValue(Constants.SwitchStatus.OFF).create();

        flatBrightnessP = newNumberProperty().name("FLAT_LIGHT_INTENSITY").label("Light intensity").group(GROUP_MAIN_CONTROL).create();
        flatBrightnessE = flatBrightnessP.newElement().name("FLAT_LIGHT_INTENSITY_VALUE").label("Value")
                .step(1d).numberFormat("%.0f").numberValue(0.0).minimum(0.0).maximum(255.0).create();

        dustCapP = newSwitchProperty().name("CAP_PARK").label("Dust cover").group(GROUP_MAIN_CONTROL).create();
        dustCapOpenE = dustCapP.newElement().name("UNPARK").label("Open").switchValue(Constants.SwitchStatus.ON).create();
        dustCapCloseE = dustCapP.newElement().name("PARK").label("Close").switchValue(Constants.SwitchStatus.OFF).create();

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
        return settings.getFocuserMaxTravel();
    }

    @Override
    public int getMinimumAbsPos() {
        return 0;
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
        portsListProp = newSwitchProperty().name("AVAILABLE_PORTS").label("Available ports").group(GROUP_MAIN_CONTROL)
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
                    board.run(Board.Commands.POWER_BOX_SET_PIN, this, pin.getNumber(),
                            ArduinoPin.constrain(eAV.getValue().intValue()));
                    element.setValue((double) pin.getValuePWM());
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
                        board.run(Board.Commands.FOCUSER_REL_MOVE, this, (relativeFocusDirection ? (-1) : 1) * value.intValue());
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

        } else if (property == flatBrightnessP) {
            try {
                for (INDINumberElementAndValue eAV : elementsAndValues) {
                    INDINumberElement element = eAV.getElement();
                    Double value = eAV.getValue();
                    if (element == flatBrightnessE) {
                        element.setValue(value);
                        board.run(Board.Commands.FLAT_SET_BRIGHTNESS, this, value.intValue());
                        break;
                    }
                }
                flatBrightnessP.setState(Constants.PropertyStates.OK);
                updateProperty(flatBrightnessP);
            } catch (IOException | SerialPortException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                flatBrightnessP.setState(Constants.PropertyStates.ALERT);
                updateProperty(flatBrightnessP);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                flatBrightnessP.setState(Constants.PropertyStates.ALERT);
                updateProperty(flatBrightnessP);
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
                        if (element == connectElem) board.connect(settings.getSerialPort());
                        else if (element == disconnectElem) board.disconnect();
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

        } else if (property == digitalPinsProps) {
            try {
                for (INDISwitchElementAndValue eAV : elementsAndValues) {
                    INDISwitchElement element = eAV.getElement();
                    ArduinoPin pin = pinsMap.get(element);
                    Constants.SwitchStatus val = eAV.getValue();
                    board.run(Board.Commands.POWER_BOX_SET_PIN, this, pin.getNumber(),
                            (val == Constants.SwitchStatus.ON) ? 255 : 0);
                    element.setValue(val);
                }
                digitalPinsProps.setState(Constants.PropertyStates.OK);
                updateProperty(digitalPinsProps);
            } catch (IOException | SerialPortException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                digitalPinsProps.setState(Constants.PropertyStates.ALERT);
                updateProperty(digitalPinsProps);
            } catch (IllegalArgumentException e) {
                digitalPinsProps.setState(Constants.PropertyStates.ALERT);
                updateProperty(digitalPinsProps);
            }

        } else if (property == focusDirectionP) {
            for (INDISwitchElementAndValue eAV : elementsAndValues) {
                INDISwitchElement element = eAV.getElement();
                Constants.SwitchStatus value = eAV.getValue();
                element.setValue(value);
                if (value == Constants.SwitchStatus.ON) {
                    relativeFocusDirection = (element == focusDirectionInE);
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

        } else if (property == flatLightP) {
            try {
                for (INDISwitchElementAndValue eAV : elementsAndValues) {
                    INDISwitchElement element = eAV.getElement();
                    Constants.SwitchStatus value = eAV.getValue();
                    element.setValue(value);
                    if (value == Constants.SwitchStatus.ON) {
                        board.run(Board.Commands.FLAT_SET_LIGHT, this, (element == flatLightOnE) ? 1 : 0);
                        break;
                    }
                }
                flatLightP.setState(Constants.PropertyStates.OK);
                updateProperty(flatLightP);
            } catch (IOException | SerialPortException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                flatLightP.setState(Constants.PropertyStates.OK);
                updateProperty(flatLightP);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                flatLightP.setState(Constants.PropertyStates.OK);
                updateProperty(flatLightP);
            }

        } else if (property == dustCapP) {
            try {
                for (INDISwitchElementAndValue eAV : elementsAndValues) {
                    INDISwitchElement element = eAV.getElement();
                    Constants.SwitchStatus value = eAV.getValue();
                    element.setValue(value);
                    if (value == Constants.SwitchStatus.ON) {
                        board.run(Board.Commands.FLAT_SET_COVER, this, (element == dustCapOpenE) ? 1 : 0);
                        break;
                    }
                }
                dustCapP.setState(Constants.PropertyStates.BUSY);
                updateProperty(dustCapP);
            } catch (IOException | SerialPortException e) {
                e.printStackTrace();
                connectionProp.setState(Constants.PropertyStates.ALERT);
                updateProperty(connectionProp);
                dustCapP.setState(Constants.PropertyStates.OK);
                updateProperty(dustCapP);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                dustCapP.setState(Constants.PropertyStates.OK);
                updateProperty(dustCapP);
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
        connectionProp.setState(Constants.PropertyStates.OK);
        connectElem.setValue(Constants.SwitchStatus.ON);
        disconnectElem.setValue(Constants.SwitchStatus.OFF);
        updateProperty(connectionProp);

        Focuser focuser = board.focuser();
        if (focuser != null) {
            initializeStandardProperties();
            showSpeedProperty();
            showStopFocusingProperty();
            if (focuser.isDirInverted()) focusReverseEnE.setValue(Constants.SwitchStatus.ON);
            else focusReverseDisE.setValue(Constants.SwitchStatus.ON);
            focuserMaxPositionE.setValue(settings.getFocuserMaxTravel());
            addProperty(focusRelPositionP);
            addProperty(focusDirectionP);
            addProperty(syncFocusPositionP);
            addProperty(focuserMaxPositionP);
            addProperty(focusReverseP);
            positionChanged(focuser.getPos());
        }

        updatePowerBoxProperties();

        FlatPanel flat = board.flat();
        if (flat != null) {
            boolean lightStatus = flat.getLightStatus();
            flatLightOnE.setValue(lightStatus ? Constants.SwitchStatus.ON : Constants.SwitchStatus.OFF);
            flatLightOffE.setValue(lightStatus ? Constants.SwitchStatus.OFF : Constants.SwitchStatus.ON);
            flatLightP.setState(Constants.PropertyStates.OK);
            addProperty(flatLightP);
            flatBrightnessE.setValue(flat.getBrightness());
            flatBrightnessP.setState(Constants.PropertyStates.OK);
            addProperty(flatBrightnessP);
            if (flat.hasServo()) {
                updateCoverStatus(flat);
                addProperty(dustCapP);
            }
        }
    }

    private void updateCoverStatus(FlatPanel flat) {
        switch (flat.getCoverStatus()) {
            case OPEN -> {
                dustCapOpenE.setValue(Constants.SwitchStatus.ON);
                dustCapCloseE.setValue(Constants.SwitchStatus.OFF);
                dustCapP.setState(Constants.PropertyStates.OK);
            }
            case CLOSED -> {
                dustCapOpenE.setValue(Constants.SwitchStatus.OFF);
                dustCapCloseE.setValue(Constants.SwitchStatus.ON);
                dustCapP.setState(Constants.PropertyStates.OK);
            }
            case NEITHER_OPEN_NOR_CLOSED -> dustCapP.setState(Constants.PropertyStates.BUSY);
            case HALT -> dustCapP.setState(Constants.PropertyStates.ALERT);
        }
    }

    private void updatePowerBoxProperties() {
        pinsMap = new HashMap<>();
        if (digitalPinsProps != null) removeProperty(digitalPinsProps);
        if (pwmPinsProp != null) removeProperty(pwmPinsProp);
        removeProperty(ambientP);
        PowerBox powerBox = board.powerBox();
        if (powerBox != null) {
            ArrayList<ArduinoPin> digitalPins = powerBox.filter(pin ->
                    pin.isDigitalPin() && (!pin.isAutoModeEn()) && (!pin.isOnWhenAppOpen()));
            if (digitalPins.size() > 0) {
                digitalPinsProps = newSwitchProperty().name(DIGITAL_PINS_PROP).label(DIGITAL_PINS_PROP)
                        .group(GROUP_MAIN_CONTROL).switchRule(Constants.SwitchRules.ANY_OF_MANY).create();
                for (ArduinoPin pin : digitalPins) {
                    String pinName = pin.getName();
                    pinsMap.put(new INDIElementBuilder<>(INDISwitchElement.class, digitalPinsProps).name(pinName)
                            .label(pinName).switchValue(pin.getValueINDI()).create(), pin);
                }
                digitalPinsProps.setState(Constants.PropertyStates.OK);
                addProperty(digitalPinsProps);
            }
            ArrayList<ArduinoPin> pwmPins = powerBox.filter(pin ->
                    pin.isPWMEnabled() && (!pin.isAutoModeEn()) && (!pin.isOnWhenAppOpen()));
            if (pwmPins.size() > 0) {
                pwmPinsProp = newNumberProperty().name(PWM_PINS_PROP).label(PWM_PINS_PROP)
                        .group(GROUP_MAIN_CONTROL).create();
                for (ArduinoPin pin : pwmPins) {
                    String pinName = pin.getName();
                    pinsMap.put(new INDIElementBuilder<>(INDINumberElement.class, pwmPinsProp).name(pinName)
                            .label(pinName).step(1).numberFormat("%.0f")
                            .maximum(255.0).numberValue(pin.getValuePWM()).create(), pin);
                }
                pwmPinsProp.setState(Constants.PropertyStates.OK);
                addProperty(pwmPinsProp);
            }
            if (powerBox.hasAmbientSensors()) {
                double temperature = powerBox.getTemperature(),
                        humidity = powerBox.getHumidity();
                temperatureE.setValue(temperature);
                humidityE.setValue(humidity);
                ambientP.setState(((temperature == ABSOLUTE_ZERO) || (humidity == INVALID_HUMIDITY)) ?
                        Constants.PropertyStates.BUSY : Constants.PropertyStates.OK);
                addProperty(ambientP);
            }
        }
    }

    @Override
    public void onFocuserReachedPos() {
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
            case POWERBOX_PINS -> updatePowerBoxProperties();
            case POWERBOX_AMBIENT_DATA -> {
                PowerBox powerBox = board.powerBox();
                double temperature = powerBox.getTemperature(),
                        humidity = powerBox.getHumidity();
                temperatureE.setValue(temperature);
                humidityE.setValue(humidity);
                ambientP.setState(((temperature == ABSOLUTE_ZERO) || (humidity == INVALID_HUMIDITY)) ?
                        Constants.PropertyStates.BUSY : Constants.PropertyStates.OK);
                updateProperty(ambientP);
            }
            case FLAT_BRIGHTNESS -> {
                flatBrightnessE.setValue(board.flat().getBrightness());
                updateProperty(flatBrightnessP);
            }
            case FLAT_COVER_STATUS -> {
                updateCoverStatus(board.flat());
                updateProperty(dustCapP);
            }
            case FLAT_LIGHT_STATUS -> {
                boolean lightStatus = board.flat().getLightStatus();
                flatLightOnE.setValue(lightStatus ? Constants.SwitchStatus.ON : Constants.SwitchStatus.OFF);
                flatLightOffE.setValue(lightStatus ? Constants.SwitchStatus.OFF : Constants.SwitchStatus.ON);
                updateProperty(flatLightP);
            }
        }
    }

    @Override
    public void updateFocuserState(Focuser.FocuserState focuserState) {
        if (focuserState == Focuser.FocuserState.NONE)
            connectionProp.setState(Constants.PropertyStates.IDLE);
    }

    @Override
    public void updateConnectionState(Board.ConnectionState connectionState) {
        switch (connectionState) {
            case CONNECTED_READY -> onBoardConnected();
            case DISCONNECTED -> {
                if (digitalPinsProps != null) {
                    removeProperty(digitalPinsProps);
                    digitalPinsProps = null;
                }
                if (pwmPinsProp != null) {
                    removeProperty(pwmPinsProp);
                    pwmPinsProp = null;
                }
                if (pinsMap != null) {
                    pinsMap.clear();
                    pinsMap = null;
                }

                removeProperty(flatLightP);
                removeProperty(flatBrightnessP);
                removeProperty(dustCapP);
                removeProperty(focusRelPositionP);
                removeProperty(focusDirectionP);
                removeProperty(syncFocusPositionP);
                removeProperty(focuserMaxPositionP);
                removeProperty(focusReverseP);
                removeProperty(absFocusPositionP);
                removeProperty(ambientP);
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
        absFocusPositionE.setMax(value);
        updateProperty(absFocusPositionP);
    }

    @Override
    public void updateSerialPort(String value) {
        serialPortFieldElem.setValue(value);
        updateProperty(serialPortFieldProp);
    }
}