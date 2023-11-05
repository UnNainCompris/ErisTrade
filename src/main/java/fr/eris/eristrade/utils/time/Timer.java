package fr.eris.eristrade.utils.time;

import java.util.concurrent.TimeUnit;

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

    public String getElapseTime() {
        long nanoSecondElapse = System.nanoTime() - startTime;
        return String.format("%02d:%02d:%02d:%03d", TimeUnit.NANOSECONDS.toHours(nanoSecondElapse),
                TimeUnit.NANOSECONDS.toMinutes(nanoSecondElapse) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.NANOSECONDS.toSeconds(nanoSecondElapse) % TimeUnit.MINUTES.toSeconds(1),
                TimeUnit.NANOSECONDS.toMillis(nanoSecondElapse) % TimeUnit.SECONDS.toMillis(1));
    }
}
