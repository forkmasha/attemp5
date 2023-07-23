import java.lang.reflect.Type;

public class ElectricCar {
    protected double tinSys;
    protected double tinQueue;
    private double serviceTime;
    private double meanServiceTime;
    private Distribution serviceTimeDistribution;

    public void setTinSys(double tinSys) {
        this.tinSys = tinSys;
    }

    public void setTinQueue(double tinQueue) {
        this.tinQueue = tinQueue;
    }

    public void setServiceTime(double serviceTime) {
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

    public ElectricCar(double meanServiceTime, DistributionType type) {
        this.tinSys = tinSys;
        this.tinQueue = tinQueue;
        this.meanServiceTime = meanServiceTime;
        this.serviceTimeDistribution = Distribution.create(type);
        this.serviceTime = serviceTimeDistribution.getSample(meanServiceTime);
    }
}