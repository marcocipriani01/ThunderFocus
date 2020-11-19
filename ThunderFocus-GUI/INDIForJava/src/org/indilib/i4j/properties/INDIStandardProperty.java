package org.indilib.i4j.properties;

/*
 * #%L
 * INDI for Java Base Library
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.indilib.i4j.properties.INDIStandardElement.*;

/**
 * The following tables describe standard properties pertaining to generic
 * devices and class-specific devices like telescope, CCDs...etc. The name of a
 * standard property and its members must be strictly reserved in all drivers.
 * However, it is permissible to change the label element of properties. You can
 * find numerous uses of the standard properties in the INDI library driver
 * repository. We use enum instead of constants to be better able to trace the
 * references.
 * 
 * @see http://indilib.org/develop/developer-manual/101-standard-properties.html
 * @author Richard van Nieuwenhoven
 */
public enum INDIStandardProperty {
    /**
     * property is no General Property.
     */
    NONE,
    /**
     * the switch property to connect the driver to the device.
     */
    CONNECTION(CONNECT, DISCONNECT),
    /**
     * the device text property of the connection port of to the device.
     */
    DEVICE_PORT(PORT),
    /**
     * number property of the Local sidereal time HH:MM:SS.
     */
    TIME_LST(LST),
    /**
     * text property of the UTC Time & Offset.
     */
    TIME_UTC(UTC, OFFSET),
    /**
     * number property of the Earth geodetic coordinate.
     */
    GEOGRAPHIC_COORD(LAT, LONG, ELEV),
    /**
     * Equatorial astrometric epoch of date coordinate.
     */
    EQUATORIAL_EOD_COORD(RA, DEC),
    /**
     * Equatorial astrometric J2000 coordinate.
     */
    EQUATORIAL_COORD(RA, DEC),
    /**
     * Weather conditions.
     */
    ATMOSPHERE(TEMPERATURE, PRESSURE, HUMIDITY),
    /**
     * upload settings for blobs.
     */
    UPLOAD_MODE(UPLOAD_CLIENT, UPLOAD_LOCAL, UPLOAD_BOTH),
    /**
     * settings for the upload mode local.
     */
    UPLOAD_SETTINGS(UPLOAD_DIR, UPLOAD_PREFIX),
    /**
     * topocentric coordinate.
     */
    HORIZONTAL_COORD(ALT, AZ),
    /**
     * Action device takes when sent any *_COORD property.
     */
    ON_COORD_SET(SLEW, TRACK, SYNC),
    /**
     * Move telescope north or south.
     */
    TELESCOPE_MOTION_NS(MOTION_NORTH, MOTION_SOUTH),
    /**
     * Move telescope west or east.
     */
    TELESCOPE_MOTION_WE(MOTION_WEST, MOTION_EAST),
    /**
     * Timed pulse guide in north/south direction.
     */
    TELESCOPE_TIMED_GUIDE_NS(TIMED_GUIDE_N, TIMED_GUIDE_S),
    /**
     * Timed pulse guide in west/east direction.
     */
    TELESCOPE_TIMED_GUIDE_WE(TIMED_GUIDE_W, TIMED_GUIDE_E),
    /**
     * Multiple switch slew rate. The driver can define as many switches as
     * desirable, but at minimum should implement the four switches below.
     */
    TELESCOPE_SLEW_RATE(SLEW_GUIDE, SLEW_CENTERING, SLEW_FIND, SLEW_MAX),
    /**
     * Park and unpark the telescope.
     */
    TELESCOPE_PARK(PARK, UNPARK),
    /**
     * Stop telescope rapidly, but gracefully.
     */
    TELESCOPE_ABORT_MOTION(ABORT_MOTION),
    /**
     * tracking speed of the scope.
     */
    TELESCOPE_TRACK_RATE(TRACK_SIDEREAL, TRACK_SOLAR, TRACK_LUNAR, TRACK_CUSTOM),
    /**
     * information about the telescope.
     */
    TELESCOPE_INFO(TELESCOPE_APERTURE, TELESCOPE_FOCAL_LENGTH, GUIDER_APERTURE, GUIDER_FOCAL_LENGTH),
    /**
     * Expose the CCD chip for CCD_EXPOSURE_VALUE seconds.
     */
    CCDn_EXPOSURE(CCD_EXPOSURE_VALUE),
    /**
     * Abort CCD exposure.
     */
    CCDn_ABORT_EXPOSURE(ABORT),
    /**
     * CCD frame size.
     */
    CCDn_FRAME(X, Y, WIDTH, HEIGHT),
    /**
     * CCD chip temperature in degrees Celsius.
     */
    CCDn_TEMPERATURE(CCD_TEMPERATURE_VALUE),
    /**
     * CCD Cooler control.
     */
    CCDn_COOLER(COOLER_ON, COOLER_OFF),
    /**
     * Percentage % of Cooler Power utilized.
     */
    CCDn_COOLER_POWER(CCD_COOLER_VALUE),
    /**
     * frame exposure type.
     */
    CCDn_FRAME_TYPE(FRAME_LIGHT, FRAME_BIAS, FRAME_DARK, FRAME_FLAT),
    /**
     * ccd binning.
     */
    CCDn_BINNING(HOR_BIN, VER_BIN),
    /**
     * ccd frame compression.
     */
    CCDn_COMPRESSION(CCD_COMPRESS, CCD_RAW),
    /**
     * Reset CCD frame to default X,Y,W, and H settings. Set binning to 1x1.
     */
    CCDn_FRAME_RESET(RESET),
    /**
     * CCD informations.
     */
    CCDn_INFO(CCD_MAX_X, CCD_MAX_Y, CCD_PIXEL_SIZE, CCD_PIXEL_SIZE_X, CCD_PIXEL_SIZE_Y, CCD_BITSPERPIXEL),
    /**
     * Color Filter Array information if the CCD produces a bayered image.
     * Debayering performed at client side.
     */
    CCDn_CFA(CFA_OFFSET_X, CFA_OFFSET_Y, CFA_TYPE),
    /**
     * CCD1 for primary CCD, CCD2 for guider CCD.Binary fits data encoded in
     * base64. The CCD1.format is used to indicate the data type (e.g. ".fits").
     */
    CCDn,
    /**
     * The filter wheel's current slot number. Important: Filter numbers start
     * from 1 to N.
     */
    FILTER_SLOT(FILTER_SLOT_VALUE),
    /**
     * The filter wheel's current slot name.
     */
    FILTER_NAME(FILTER_NAME_VALUE),
    /**
     * Select focus speed from 0 to N where 0 maps to no motion, and N maps to
     * the fastest speed possible.
     */
    FOCUS_SPEED(FOCUS_SPEED_VALUE),
    /**
     * focuser motion.
     */
    FOCUS_MOTION(FOCUS_INWARD, FOCUS_OUTWARD),
    /**
     * Focus in the direction of FOCUS_MOTION at rate FOCUS_SPEED for
     * FOCUS_TIMER_VALUE milliseconds.
     */
    FOCUS_TIMER(FOCUS_TIMER_VALUE),
    /**
     * Relative position.
     */
    REL_FOCUS_POSITION(FOCUS_RELATIVE_POSITION),
    /**
     * Absolute position.
     */
    ABS_FOCUS_POSITION(FOCUS_ABSOLUTE_POSITION),

    /**
     * abort the focuser motion.
     */
    FOCUS_ABORT_MOTION(ABORT),
    /**
     * Set dome speed in RPM.
     */
    DOME_SPEED(DOME_SPEED_VALUE),
    /**
     * Move dome, looking down.
     */
    DOME_MOTION(DOME_CW, DOME_CCW),
    /**
     * Move the dome in the direction of DOME_MOTION at rate DOME_SPEED for
     * DOME_TIMER_VALUE milliseconds.
     */
    DOME_TIMER(DOME_TIMER_VALUE),
    /**
     * Relative position.
     */
    REL_DOME_POSITION(DOME_RELATIVE_POSITION),
    /**
     * Absolute position.
     */
    ABS_DOME_POSITION(DOME_ABSOLUTE_POSITION),
    /**
     * Abort dome motion.
     */
    DOME_ABORT_MOTION(ABORT),
    /**
     * dome shutter controll.
     */
    DOME_SHUTTER(SHUTTER_OPEN, SHUTTER_CLOSE),
    /**
     * Dome go to position.
     */
    DOME_GOTO(DOME_HOME, DOME_PARK),
    /**
     * Dome position parameters.
     */
    DOME_PARAMS(HOME_POSITION, PARK_POSITION, AUTOSYNC_THRESHOLD),
    /**
     * (Dis/En)able dome slaving.
     */
    DOME_AUTOSYNC(DOME_AUTOSYNC_ENABLE, DOME_AUTOSYNC_DISABLE),
    /**
     * Dome mesurements / dimentions.
     */
    DOME_MEASUREMENTS(DM_DOME_RADIUS, DOME_SHUTTER_WIDTH, DM_NORTH_DISPLACEMENT, DM_EAST_DISPLACEMENT, DM_UP_DISPLACEMENT, DM_OTA_OFFSET),
    /**
     * text property of the Name of active devices. If defined, at least one
     * member below must be defined in the vector.ACTIVE_DEVICES is used to aid
     * clients in automatically providing the users with a list of active
     * devices (i.e. CONNECTION is ON) whenever needed. For example, a CCD
     * driver may define ACTIVE_DEVICES property with one member:
     * ACTIVE_TELESCOPE. Suppose that the client is also running LX200 Basic
     * driver to control the telescope. If the telescope is connected, the
     * client may automatically fill the ACTIVE_TELESCOPE field or provide a
     * drop-down list of active telescopes to select from. Once set, the CCD
     * driver may record, for example, the telescope name, RA, DEC, among other
     * metadata once it captures an image. Therefore, ACTIVE_DEVICES is
     * primarily used to link together different classes of devices to exchange
     * information if required.
     */
    ACTIVE_DEVICES(ACTIVE_TELESCOPE, ACTIVE_CCD, ACTIVE_FILTER, ACTIVE_FOCUSER, ACTIVE_DOME, ACTIVE_LOCATION, ACTIVE_WEATHER, ACTIVE_TIME, ACTIVE_SWITCH),
    /**
     * generic SWICH property.
     */
    SWITCH_MODULE(SWITCHn);

    /**
     * standard elements of this property.
     */
    private final INDIStandardElement[] elements;

    /**
     * constructor.
     * 
     * @param elements
     *            standard elements of the property.
     */
    INDIStandardProperty(INDIStandardElement... elements) {
        this.elements = elements;
    }

    /**
     * @return the array of elements this property generally has.
     */
    public final INDIStandardElement[] elements() {
        return this.elements;
    }
}
