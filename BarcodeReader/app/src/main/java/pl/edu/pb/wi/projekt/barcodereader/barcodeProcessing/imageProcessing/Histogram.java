package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import java.util.Arrays;

/**
 * Created by Mateusz on 13.06.2016.
 */
public class Histogram {
    static public int[] getHistogram(int[][] image) {
        int[] hist = new int[256];
        Arrays.fill(hist, 0);
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
                hist[image[i][j]]++;
            }
        }
        return hist;
    }

    public static int[][] equalize(int[][] image) {
        int[] histogram = getHistogram(image);

        int sum = 0;
        double[] lut;
        double scaleFactor = 255.0 / (image.length * image[0].length);
        lut = new double[256];
        for (int i = 0; i < 256; ++i) {
            sum += histogram[i];
            lut[i] = sum * scaleFactor;
            if (lut[i] > 255) {
                lut[i] = 255;
            }
        }
        int color;
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
                color = image[i][j];
                image[i][j] = (int) lut[color];
            }
        }
        return image;
    }
}
