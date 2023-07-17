package org.simulation.entity;

public class Car {
    private double tinSys;
    protected double tinQueue;
    private final double serviceTime;

    public Car(double tinSys, double tinQueue, double serviceTime) {
        this.tinSys = tinSys;
        this.tinQueue = tinQueue;
        this.serviceTime = serviceTime;
    }

    public void updateCarTimes(double deltaTime) {
        this.tinSys += deltaTime;
    }

    public double getTinQueue() {
        return tinQueue;
    }

    public double getServiceTime() {
        return serviceTime;
    }
}
