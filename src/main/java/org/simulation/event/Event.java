package org.simulation.event;

public abstract class Event {
    private final double time;

    protected Event(double time) {
        this.time = time;
    }

    public double getTime() {
        return time;
    }
}
