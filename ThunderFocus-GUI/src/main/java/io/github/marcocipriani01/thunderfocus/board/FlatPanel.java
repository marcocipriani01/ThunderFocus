package io.github.marcocipriani01.thunderfocus.board;

public class FlatPanel {

    final boolean hasServo;
    boolean lightStatus;
    int brightness;
    int openServoVal;
    int closedServoVal;
    int servoDelay;
    CoverStatus coverStatus;

    public FlatPanel(boolean lightStatus, int brightness) {
        this.hasServo = false;
        this.lightStatus = lightStatus;
        this.brightness = brightness;
        this.openServoVal = -1;
        this.closedServoVal = -1;
        this.servoDelay = -1;
        this.coverStatus = CoverStatus.NEITHER_OPEN_NOR_CLOSED;
    }

    public FlatPanel(boolean lightStatus, int brightness, int openServoVal,
                     int closedServoVal, int servoDelay, CoverStatus coverStatus) {
        this.hasServo = true;
        this.lightStatus = lightStatus;
        this.brightness = brightness;
        this.openServoVal = openServoVal;
        this.closedServoVal = closedServoVal;
        this.servoDelay = servoDelay;
        this.coverStatus = coverStatus;
    }

    public enum CoverStatus {
        NEITHER_OPEN_NOR_CLOSED, CLOSED, OPEN
    }
}