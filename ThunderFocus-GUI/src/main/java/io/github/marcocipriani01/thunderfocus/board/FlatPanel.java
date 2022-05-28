package io.github.marcocipriani01.thunderfocus.board;

import static io.github.marcocipriani01.thunderfocus.Main.i18n;

public class FlatPanel {

    final boolean hasServo;
    boolean lightStatus;
    int brightness;
    int openServoVal;
    int closedServoVal;
    int servoSpeed;
    CoverStatus coverStatus;

    public FlatPanel(boolean lightStatus, int brightness) {
        this.hasServo = false;
        this.lightStatus = lightStatus;
        this.brightness = brightness;
        this.openServoVal = -1;
        this.closedServoVal = -1;
        this.servoSpeed = -1;
        this.coverStatus = CoverStatus.NEITHER_OPEN_NOR_CLOSED;
    }

    public FlatPanel(boolean lightStatus, int brightness, int openServoVal,
                     int closedServoVal, int servoSpeed, CoverStatus coverStatus) {
        this.hasServo = true;
        this.lightStatus = lightStatus;
        this.brightness = brightness;
        this.openServoVal = openServoVal;
        this.closedServoVal = closedServoVal;
        this.servoSpeed = servoSpeed;
        this.coverStatus = coverStatus;
    }

    public boolean hasServo() {
        return hasServo;
    }

    public boolean getLightStatus() {
        return lightStatus;
    }

    public int getBrightness() {
        return brightness;
    }

    public int getOpenServoVal() {
        return openServoVal;
    }

    public int getClosedServoVal() {
        return closedServoVal;
    }

    public int getServoSpeed() {
        return servoSpeed;
    }

    public CoverStatus getCoverStatus() {
        return coverStatus;
    }

    public enum CoverStatus {
        NEITHER_OPEN_NOR_CLOSED(i18n("cover.moving")),
        CLOSED(i18n("cover.closed")),
        OPEN(i18n("cover.open")),
        HALT(i18n("cover.halt"));

        private final String label;

        CoverStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}