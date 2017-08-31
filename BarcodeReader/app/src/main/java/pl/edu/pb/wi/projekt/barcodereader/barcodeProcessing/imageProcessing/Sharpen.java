package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.MinMaxCaller;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.FilterRunner;

public class Sharpen {
    private static ExecutorService executor = ThreadExecutor.getExecutor();

    /**
     * wyostrza obraz w oparciu o podaną macierz( można stosować dla dowolnej macierzy jądra o wymiarach 3x3
     * obraz jest skalowany według największej i najmniejszej wartości
     *
     * @param image  obraz do przetworzenia
     * @param kernel jądro według którego realizowane jest wyostrzanie
     * @return przetworzony obraz
     */
    public static int[][] sharpen(final int[][] image, final int[][] kernel) {

        final int[][] out = new int[image.length][image[0].length];
        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;
        final int values[] = new int[2];
        values[0] = 9999;
        values[1] = -9999;

        ArrayList<Future<int[]>> results = new ArrayList<>();

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (start == 0) start = 1;
            if (end >= image.length) end = image.length - 1;
            results.add(executor.submit(new MinMaxCaller(latch, start, end) {
                int acc = 0;

                @Override
                public void doCalc() {
                    for (int i = start; i < end; i++) {
                        for (int j = 1; j < image[0].length - 1; j++) {
                            acc = 0;

                            acc += image[i - 1][j - 1] * kernel[2][2];
                            acc += image[i - 1][j] * kernel[2][1];
                            acc += image[i - 1][j + 1] * kernel[2][0];
                            acc += image[i][j - 1] * kernel[1][2];
                            acc += image[i][j] * kernel[1][1];
                            acc += image[i][j + 1] * kernel[1][0];
                            acc += image[i + 1][j - 1] * kernel[0][2];
                            acc += image[i + 1][j] * kernel[0][1];
                            acc += image[i + 1][j + 1] * kernel[0][0];

                            out[i][j] = acc;

                            if (out[i][j] < value[0]) {
                                value[0] = out[i][j];
                            }
                            if (out[i][j] > value[1]) {
                                value[1] = out[i][j];
                            }
                        }
                    }
                }

            }));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        try {
            for (Future<int[]> f : results) {
                if (f.get()[0] < values[0]) {
                    values[0] = f.get()[0];
                }
                if (f.get()[1] > values[1]) {
                    values[1] = f.get()[1];
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (start == 0) start = 1;
            if (end >= image.length) end = image.length - 1;
            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    double wsp = 255.0 / (values[1] - values[0]);
                    for (int i = start; i < end; i++) {
                        for (int j = 1; j < image[0].length - 1; j++) {
                            out[i][j] = (int) (((out[i][j]) - values[0]) * wsp);
                        }
                    }
                }

            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return out;
    }
}
