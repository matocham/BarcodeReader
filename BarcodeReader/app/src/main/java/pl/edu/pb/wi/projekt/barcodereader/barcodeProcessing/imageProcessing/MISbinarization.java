package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

/**
 * Created by Mateusz on 13.06.2016.
 */
public class MISbinarization {
    public static double THRESHOLD_SCALE_FACTOR = 0.6;

    static public int[][] binarize(int[][] image) {
        int[] hist = Histogram.getHistogram(image);
        int threshold = computeThreshold(hist);

        threshold += THRESHOLD_SCALE_FACTOR * threshold;
        Threshold.threshold(image, threshold);
        return image;
    }

    static public int computeThreshold(int[] hist) {
        int threshold;
        int length = 0;

        for (int i = 0; i < hist.length; i++) {
            length += hist[i];
        }

        long mean = 0;
        for (int i = 0; i < hist.length; i++) {
            mean += i * hist[i];
        }
        mean /= length;
        threshold = (int) mean; // początkowa wartość progu
        int sum = 0, sum2 = 0;
        int oldTh = -1;
        long mean2 = 1;// średnia powyzej progu

        while (mean != mean2 && oldTh != threshold) {
            mean = mean2 = 0;
            sum = sum2 = 0;
            oldTh = threshold; // zanim obliczymy nową wartość należy zapamiędać starą
            for (int i = 0; i < threshold; i++) {
                mean += hist[i] * i;
                sum += hist[i];
            }
            for (int i = threshold; i < hist.length; i++) {
                mean2 += hist[i] * i;
                sum2 += hist[i];
            }
            if (sum == 0) return 225;
            else if (sum2 == 0) return 20;
            threshold = (int) ((mean / sum + mean2 / sum2) / 2);
        }
        return threshold;
    }
}

