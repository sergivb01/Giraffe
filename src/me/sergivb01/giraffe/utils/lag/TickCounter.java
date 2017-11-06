package me.sergivb01.giraffe.utils.lag;

public class TickCounter implements Runnable {

    private long tick = 0L;

    @Override
    public void run() {
        tick++;
    }

    public long getTick() {
        return tick;
    }

}