package org.simulation.entity;

import org.simulation.event.ArrivalEvent;
import org.simulation.event.DepartureEvent;
import org.simulation.event.EventStack;
import org.simulation.event.Event;
import org.simulation.utils.Statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GasStation {
    private final int numServers;
    private final int queueLength;
    private final int numStates;
    private final int maxCars;
    private double meanArrivalInterval;
    private final double meanServiceTime;

//    private double totalCarsInSystem;
//    private double totalCarsInQueue;

    private List<Double> systemTimes;
    private List<Double> queueTimes;
    private List<Double> serviceTimes;
    //private List<double[]> systemTimesConfidences;
    private List<Double> systemTimeList;
    private List<Double> queueTimeList;

    private EventStack eventStack;
    private List<Car> servicedCars;
    private int numberOfBlockedCars;
    private int numberOfDeparturedCars;
    private int numberOfArrivedCars;
    private double time;
    private double previousTime;
//    private double[] systemTimeConfidence;

//    private double[] queueTimeConfidence;
//    private double[] serviceTimeConfidence;
    private List<Double> queueStartTimes;

    private List<Double> arrivalRateList;

//    ToDo
    private List<Double> meanSystemTimeList;

    public Statistics getStatistics() {
        return statistics;
    }

    private Statistics statistics;

    /* public List<double[]> getSystemTimesConfidences() {
        return systemTimesConfidences;
    }
    */

    public GasStation(int numServers, int queueLength, int numStates, int maxCars, double meanArrivalInterval, double meanServiceTime) {
        this.numServers = numServers;
        this.queueLength = queueLength;
        this.numStates = numStates;
        this.maxCars = maxCars;
        this.meanArrivalInterval = meanArrivalInterval;
        this.meanServiceTime = meanServiceTime;

        queueStartTimes = new ArrayList<>();
        systemTimeList = new ArrayList<>();

        systemTimes = new ArrayList<>();
        queueTimes = new ArrayList<>();
        serviceTimes = new ArrayList<>();
        //systemTimesConfidences = new ArrayList<>();

        queueTimeList = new ArrayList<>();

        arrivalRateList = new ArrayList<>();
        //meanSystemTimeList = new ArrayList<>();

        eventStack = new EventStack();
        servicedCars = new ArrayList<>();
//        numberOfBlockedCars = 0;
//        numberOfDeparturedCars = 0;
//        numberOfArrivedCars = 0;
//        time = 0.0;
//        previousTime = 0.0;
    }

    public void simulate() {

        System.out.println("\n#########################");
        System.out.println("Simulation Parameters");
        System.out.println("#########################");
        System.out.println("MeanInterArrivalTime " + meanArrivalInterval);
        System.out.println("MeanServiceTime " + meanServiceTime);
        System.out.println("Number of servers " + numServers);
        System.out.println("Queue length " + queueLength);
        numberOfBlockedCars = 0;
        numberOfDeparturedCars = 0;
        numberOfArrivedCars = 0;
        time = 0.0;
        previousTime = 0.0;

        servicedCars.clear();
        queueStartTimes.clear();

        systemTimeList.clear();
        queueTimeList.clear();

        systemTimes.clear();
        queueTimes.clear();
        serviceTimes.clear();

        arrivalRateList.clear();
        //meanSystemTimeList.clear();

        eventStack.getEvents().clear();
        eventStack.addEvent(new ArrivalEvent(0.0));   // insert initial arrival

        // Головний цикл моделювання
        while (!eventStack.isEmpty()) {
            // Отримуємо наступну подію зі стеку
            Event event = eventStack.getNextEvent();
            double eventTime = event.getTime();
            // Оновлюємо час моделювання та розраховуємо різницю з попереднім часом
            time = eventTime;
            double deltaTime = time - previousTime;

            // Оновлюємо часи перебування в системі та черзі для автомобілів
            updateCarTimes(deltaTime);
            // Обробляємо подію залежно від її типу
            if (event instanceof ArrivalEvent) {
                processArrivalEvent(eventTime);
            } else if (event instanceof DepartureEvent) {
                processDepartureEvent(eventTime);
            }
            // Оновлюємо попередній час
            previousTime = time;
        }
        // Генеруємо статистику симуляції
        statistics = new Statistics(numberOfArrivedCars, numberOfDeparturedCars, numberOfBlockedCars,
                queueTimes, serviceTimes, systemTimes);
        statistics.generateStatistics();
    }

    private void updateCarTimes(double deltaTime) {
        for (Car car : servicedCars) {
            car.updateCarTimes(deltaTime);
            // car.tinQueue += deltaTime;
        }

        //ToDo
        // Not used
//        totalCarsInSystem += servicedCars.size() * deltaTime;
//        totalCarsInQueue += Math.max(0, servicedCars.size() - numServers) * deltaTime;


        // Зберігаємо часи перебування в системі та черзі для обліку середніх значень
        /* if (!servicedCars.isEmpty()) {
            // double lastCarTime = servicedCars.get(servicedCars.size() - 1).tinSys;
            // double queueTime = Math.max(0, lastCarTime - servicedCars.get(0).tinSys);
            //systemTimes.add(lastCarTime);
            // queueTimes.add(queueTime);

            // Зберігаємо час перебування в системі для кожного автомобіля
            for (Car car : servicedCars) {
                systemTimeList.add(car.tinSys);
            }
            for (Car car : servicedCars) {
                queueTimeList.add(car.tinQueue);
            }
        } */
    }

    private void processArrivalEvent(double eventTime) {
        if (numberOfArrivedCars < maxCars) {
            double nextArrivalTime = eventTime + exponentialDistribution(meanArrivalInterval);
            eventStack.addEvent(new ArrivalEvent(nextArrivalTime));
        } else {
            return; // Повертаємося, якщо кількість автомобілів досягає максимального значення
        }
        // Збільшуємо лічильник обслужених автомобілі
        numberOfArrivedCars++;
        if (numServers > servicedCars.size()) {   // directly enter servive
            double serviceTime = exponentialDistribution(meanServiceTime);
            eventStack.addEvent(new DepartureEvent(eventTime + serviceTime));
            servicedCars.add(new Car(eventTime, 0.0, serviceTime));
        } else if (queueStartTimes.size() < queueLength) {   // enter waiting queue
            queueStartTimes.add(eventTime);
            // Зберігаємо час початку очікування в черзі для автомобіля

        } else {  // arrival is blocked (deflected)
            numberOfBlockedCars++;
        }
        arrivalRateList.add(1.0 / meanArrivalInterval); // Calculate arrival rate from mean arrival interval
        //meanSystemTimeList.add(calculateMean(systemTimeList));   // ?
    }

    private void processDepartureEvent(double eventTime) {
        // Збільшуємо лічильник обслуженихавтомобілів

        if (!servicedCars.isEmpty()) {
            Car departingCar = servicedCars.get(0);
            // systemTimes.add(departingCar.tinSys);
            // double carArrivalTime = departingCar.arrivalTime;
            systemTimes.add(departingCar.getTinQueue() + departingCar.getServiceTime());
            serviceTimes.add(departingCar.getServiceTime());
            servicedCars.remove(0); // Видаляємо перший автомобіль з черги
            numberOfDeparturedCars++;
        } else {
            System.out.println("Error: Departure from empty System!");
        }

        // Якщо стан системи перевищує кількість серверів, додаємо подію відправлення для автомобіля з черги до стеку подій та записуємо час обслуговування черги
        if (!queueStartTimes.isEmpty()) {
            double serviceTime = exponentialDistribution(meanServiceTime);
            eventStack.addEvent(new DepartureEvent(eventTime + serviceTime));
            // if (!queueStartTimes.isEmpty()) {
            // Додаємо час очікування в черзі до списку
            double queueTime = eventTime - queueStartTimes.get(0);
            queueTimes.add(queueTime);
            queueStartTimes.remove(0); // Видаляємо перший час початку очікування з черги
            servicedCars.add(new Car(eventTime, queueTime, serviceTime));
            // }
        }
    }

    private double exponentialDistribution(double mean) {
        Random random = new Random();
        return -mean * Math.log(1 - random.nextDouble());
    }

    public List<Double> getArrivalRateList() {
        return arrivalRateList;
    }

    public List<Double> getMeanSystemTimeList() {
        return meanSystemTimeList;
    }

    public void setMeanArrivalInterval(double meanArrivalInterval) {
        this.meanArrivalInterval = meanArrivalInterval;
    }

    public double getMeanArrivalInterval() {
        return meanArrivalInterval;
    }

    public double getMeanSystemTime() {
        double sum = 0.0;
        int i = 0;
        for (Double time : systemTimes) {
            sum += time;
            i++;
        }
        // sum /= systemTimes.size();
        sum /= i;
        return sum;
    }

    public double getMeanQueueTime() {
        double sum = 0.0;
        int i = 0;
        for (Double time : queueTimes) {
            sum += time;
            i++;
        }
        // sum /= systemTimes.size();
        sum /= i;
        return sum;
    }

    public double getMeanServiceTime() {
        double sum = 0.0;
        int i = 0;
        for (Double time : serviceTimes) {
            sum += time;
            i++;
        }
        // sum /= systemTimes.size();
        sum /= i;
        return sum;
    }
}



