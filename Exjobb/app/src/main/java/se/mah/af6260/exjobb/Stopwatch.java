package se.mah.af6260.exjobb;

/**
 * Created by oskar on 2018-04-03.
 */

public class Stopwatch {

    private long startNanoSeconds;
    private long pauseNanoSeconds;
    private String seconds, minutes, hours, milliseconds;

    public void startTimer(){
        startNanoSeconds = System.nanoTime();
    }

    public long getStartTime(){
        return startNanoSeconds;
    }

    public long getSeconds(){
        return (System.nanoTime() - startNanoSeconds) / 1000000000;
    }

    public void resumeTimer(){
        long dif = System.nanoTime() - pauseNanoSeconds;
        startNanoSeconds =+ dif;
    }

    public void pauseTimer(){
        pauseNanoSeconds = System.nanoTime();
    }
}
