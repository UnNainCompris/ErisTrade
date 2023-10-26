package fr.eris.eristrade.utils;

public class Timer {

    public long startTime;

    public Timer() {
        start();
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public boolean hasTimeElapse(long timeMs) {
        return startTime - System.currentTimeMillis() <= timeMs;
    }
}
