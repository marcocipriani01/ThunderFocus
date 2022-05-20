package io.github.marcocipriani01.thunderfocus.board;

import io.github.marcocipriani01.thunderfocus.Main;

import static io.github.marcocipriani01.thunderfocus.Main.i18n;

public class Focuser {

    int pos;
    int targetPos;
    int speed;
    int backlash;
    boolean invertDir;
    boolean powerSaver;
    FocuserState state;

    Focuser(int pos, int speed, int backlash,
            boolean invertDir, boolean powerSaver) {
        this.pos = this.targetPos = pos;
        this.speed = speed;
        this.backlash = backlash;
        this.invertDir = invertDir;
        this.powerSaver = powerSaver;
        this.state = FocuserState.NONE;
    }

    public static int ticksToSteps(int ticks) {
        return (int) ((((double) ticks) / ((double) Main.settings.focuserTicksCount)) * ((double) Main.settings.getFocuserMaxTravel()));
    }

    public static int stepsToTicks(int steps) {
        return (int) ((((double) steps) / ((double) Main.settings.getFocuserMaxTravel())) * ((double) Main.settings.focuserTicksCount));
    }

    public int getPos() {
        return pos;
    }

    public int getPosTicks() {
        return stepsToTicks(pos);
    }

    public int getTargetPos() {
        return targetPos;
    }

    public int getSpeed() {
        return speed;
    }

    public int getBacklash() {
        return backlash;
    }

    public boolean isDirInverted() {
        return invertDir;
    }

    public boolean isPowerSaverEnabled() {
        return powerSaver;
    }

    public FocuserState getState() {
        return state;
    }

    public void clearRequestedPositions() {
        targetPos = pos;
    }

    public enum FocuserState {
        MOVING(i18n("moving")),
        HOLD_MOTOR(i18n("not.moving")),
        POWER_SAVE(i18n("power.saving")),
        NONE(i18n("none"));

        private final String label;

        FocuserState(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}