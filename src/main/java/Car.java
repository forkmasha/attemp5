public class Car {
    protected double tinSys;
    protected double tinQueue;
    private double serviceTime;

    public Car(double tinSys, double tinQueue, double serviceTime) {
        this.tinSys = tinSys;
        this.tinQueue = tinQueue;
        this.serviceTime = serviceTime;
    }

    public double getTinSys() {
        return tinSys;
    }

    public double getTinQueue() {
        return tinQueue;
    }

    public double getServiceTime() {
        return serviceTime;
    }
}