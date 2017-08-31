package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.IntegerCaller;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.FilterRunner;

public class EdgeDetector {
    private static ExecutorService executor = ThreadExecutor.getExecutor();

    /**
     * realizuje wykrywanie krawędzi połączone z zastosowaniem odpowiedniego progu
     *
     * @param image     obraz do przetworzenia
     * @param kernelV   jądro do wykrywania linii pionowych
     * @param kernelH   jądro do wykrywania linii poziomych
     * @param threshold próg do zastosowania po wykrywaniu krawędzi
     * @return przetworzony obraz
     */
    public static int[][] edgeDetection(final int[][] image, final int[][] kernelV, final int[][] kernelH, int threshold) {

        final int[][] out = new int[image.length][image[0].length];
        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;
        int max = 0;

        ArrayList<Future<Integer>> results = new ArrayList<>();

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (start == 0) start = 1;
            if (end >= image.length) end = image.length - 1;

            results.add(executor.submit(new IntegerCaller(latch, start, end) {

                int acc = 0;
                int acc2 = 0;

                @Override
                public void doCalc() {
                    for (int i = this.start; i < this.end; i++) {
                        for (int j = 1; j < image[0].length - 1; j++) {
                            acc = acc2 = 0;

                            acc += image[i - 1][j - 1] * kernelV[2][2];
                            acc += image[i - 1][j] * kernelV[2][1];
                            acc += image[i - 1][j + 1] * kernelV[2][0];
                            acc += image[i][j - 1] * kernelV[1][2];
                            acc += image[i][j] * kernelV[1][1];
                            acc += image[i][j + 1] * kernelV[1][0];
                            acc += image[i + 1][j - 1] * kernelV[0][2];
                            acc += image[i + 1][j] * kernelV[0][1];
                            acc += image[i + 1][j + 1] * kernelV[0][0];

                            acc2 += image[i - 1][j - 1] * kernelH[2][2];
                            acc2 += image[i - 1][j] * kernelH[2][1];
                            acc2 += image[i - 1][j + 1] * kernelH[2][0];
                            acc2 += image[i][j - 1] * kernelH[1][2];
                            acc2 += image[i][j] * kernelH[1][1];
                            acc2 += image[i][j + 1] * kernelH[1][0];
                            acc2 += image[i + 1][j - 1] * kernelH[0][2];
                            acc2 += image[i + 1][j] * kernelH[0][1];
                            acc2 += image[i + 1][j + 1] * kernelH[0][0];
                            //acc = (int) Math.sqrt(acc * acc + acc2 * acc2);
                            acc = Math.abs(acc)+Math.abs(acc2);
                            if (acc > 255) acc = 255;
                            out[i][j] = acc;

                            if (acc > value) {
                                value = acc;
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
            for (Future<Integer> f : results) {
                if (f.get() > max) {
                    max = f.get();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return MISbinarization.binarize(out);
    }

    /**
     * realizuje wykrywanie krawędzi połączone z zastosowaniem odpowiedniego progu
     *
     * @param image     obraz do przetworzenia
     * @param kernelV   jądro do wykrywania linii pionowych
     * @param kernelH   jądro do wykrywania linii poziomych
     * @param threshold próg do zastosowania po wykrywaniu krawędzi
     * @return przetworzony obraz
     */
    public static int[][] edgeDetectionSubstract(final int[][] image, final int[][] kernelV, final int[][] kernelH, final int threshold) {

        final int[][] out = new int[image.length][image[0].length];
        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (start == 0) start = 1;
            if (end >= image.length) end = image.length - 1;
            executor.execute(new FilterRunner(latch, start, end) {

                int acc = 0;
                int acc2 = 0;

                @Override
                public void doCalc() {
                    for (int i = this.start; i < this.end; i++) {
                        for (int j = 1; j < image[0].length - 1; j++) {
                            acc = acc2 = 0;

                            acc += image[i - 1][j - 1] * kernelV[2][2];
                            acc += image[i - 1][j] * kernelV[2][1];
                            acc += image[i - 1][j + 1] * kernelV[2][0];
                            acc += image[i][j - 1] * kernelV[1][2];
                            acc += image[i][j] * kernelV[1][1];
                            acc += image[i][j + 1] * kernelV[1][0];
                            acc += image[i + 1][j - 1] * kernelV[0][2];
                            acc += image[i + 1][j] * kernelV[0][1];
                            acc += image[i + 1][j + 1] * kernelV[0][0];

                            acc2 += image[i - 1][j - 1] * kernelH[2][2];
                            acc2 += image[i - 1][j] * kernelH[2][1];
                            acc2 += image[i - 1][j + 1] * kernelH[2][0];
                            acc2 += image[i][j - 1] * kernelH[1][2];
                            acc2 += image[i][j] * kernelH[1][1];
                            acc2 += image[i][j + 1] * kernelH[1][0];
                            acc2 += image[i + 1][j - 1] * kernelH[0][2];
                            acc2 += image[i + 1][j] * kernelH[0][1];
                            acc2 += image[i + 1][j + 1] * kernelH[0][0];
                            acc = acc2 - acc;//>acc-acc2?acc2 -acc:acc-acc2;

                            out[i][j] = acc;
                            if (out[i][j] < threshold) {
                                out[i][j] = 0;
                            } else {
                                out[i][j] = 255;
                            }
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
