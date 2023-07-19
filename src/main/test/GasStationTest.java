public class GasStationTest {
    public static void main(String[] args) {
        testExponentialDistribution();
        testUniformDistribution();
        testErlangDistribution();
        testGeometricDistribution();
    }

    public static void testExponentialDistribution() {
        int count = 100000;
        double samples[] = new double[count];
        double pdf[] = new double[count];
        System.out.println("Test: Експоненційний розподіл");
        for (int i = 0; i < count; i++) {
            samples[i] = GasStationSimulation.exponentialDistribution(1.0);
        }
        GasStationSimulation.generateServiceTimeHistogram(samples, pdf);
        System.out.println();
    }

    public static void testUniformDistribution() {
        int count = 100000;
        double samples[] = new double[count];
        double pdf[] = new double[count];
        System.out.println("Test: Рівномірний розподіл [0, 2]");
        for (int i = 0; i < count; i++) {
            samples[i] = GasStationSimulation.uniformDistribution(0, 2);
        }
        GasStationSimulation.generateServiceTimeHistogram(samples, pdf);
        System.out.println();
    }

    public static void testErlangDistribution() {
        int count = 100000;
        double samples[] = new double[count];
        double pdf[] = new double[count];
        System.out.println("Test: Розподіл Ерланга (k = 1, λ = 2)");
        for (int i = 0; i < count; i++) {
            samples[i] = GasStationSimulation.erlangDistribution(1, 2);
        }
        GasStationSimulation.generateServiceTimeHistogram(samples, pdf);
        System.out.println();
    }

    public static void testGeometricDistribution() {
        int count = 100000;
        double samples[] = new double[count];
        double pdf[] = new double[count];
        System.out.println("Test: Геометричний розподіл (p = 0.5)");
        for (int i = 0; i < count; i++) {
            samples[i] = GasStationSimulation.geometricDistribution(0.5);
        }
        GasStationSimulation.generateServiceTimeHistogram(samples, pdf);
        System.out.println();
    }
}
