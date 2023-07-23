public class DistributionTest {
    public static void main(String[] args) {
        testExponentialDistribution();
        testUniformDistribution();
        testErlangDistribution();
        testGeometricDistribution();
        testBetaDistribution();

    }

  //  @Test
    public static void testExponentialDistribution() {
        int count = 100000;
        double samples[] = new double[count];

        double pdf[] = new double[count];
        System.out.println("Test: Експоненційний розподіл");
        for (int i = 0; i < count; i++) {
            samples[i] = ExponentialDistribution.createSample(1);
           // samples[i] = GasStationSimulation.exponentialDistribution(1.0);
        }
        Distribution.generateHistogram(samples, pdf);
        System.out.println();
    }

   // @Test
    public static void testUniformDistribution() {
        int count = 100000;
        int bins = 100;
        double samples[] = new double[count];
        int frequency[] = new int[bins];
        double deviation[] = new double[bins];
        double pdf[] = new double[bins];

        System.out.println("Test: Рівномірний розподіл [0, 2]");
        for (int i = 0; i < count; i++) {
            samples[i] = UniformDistribution.createSample(1);
        }
        for (int i = 0; i < bins; i++) {
            pdf[i] = 1 / bins;
            frequency[i] = 0;
            for (double sample : samples) {
                if ((sample > i / bins) && (sample < (i + 1) / bins)) {
                    frequency[i]++;
                }
            }
            deviation[i] = Math.abs(pdf[i] - frequency[i] / count);
        }
        Distribution.generateHistogram(bins, samples, pdf);
       // double sampleError = Math.sum(deviation);
        double sampleError=0.0;
        for (double value:deviation){
            sampleError +=value;
        }

        if (sampleError > 0.01) {
            System.out.println("Total Deviation = " + sampleError);
            // FAILED throw an error?
        } else {
            // PASSED
            return;
        }
        System.out.println("Total Deviation = " + sampleError);
    }

   // @Test
    public static void testErlangDistribution() {
        int count = 100000;
        double samples[] = new double[count];
        double pdf[] = new double[count];
        System.out.println("Test: Розподіл Ерланга (k = 1, λ = 2)");
        for (int i = 0; i < count; i++) {
            samples[i] = ErlangDistribution.createSample(1);
        }
       Distribution.generateHistogram(samples, pdf);
        System.out.println();
    }

   // @Test
    public static void testGeometricDistribution() {
        int count = 100000;
        double samples[] = new double[count];
        double pdf[] = new double[count];
        System.out.println("Test: Геометричний розподіл (p = 0.5)");
        for (int i = 0; i < count; i++) {
            samples[i] = GeometricDistribution.createSample(0.5);
        }
        Distribution.generateHistogram(samples, pdf);
        System.out.println();
    }

   // @Test
    public static void testBetaDistribution() {
        int count = 100000;
        double samples[] = new double[count];
        double pdf[] = new double[count];
        System.out.println("Test: Beta Distribution");
        for (int i = 0; i < count; i++) {
            samples[i] = BetaDistribution.createSample(1);
        }
        Distribution.generateHistogram(samples, pdf);
        System.out.println();

    }
}
