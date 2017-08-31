package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.FilterRunner;


public class Threshold {
    private static ExecutorService executor = ThreadExecutor.getExecutor();

    public static void threshold(final int[][] image, final int max, final int threshold) {
        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (start == 0) start = 1;
            if (end >= image.length) end = image.length - 1;

            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    double wsp = 255.0 / (max);
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < image[0].length; j++) {
                            int imagev = (int) ((image[i][j]) * wsp);

                            image[i][j] = imagev;
                            if (image[i][j] < threshold) {
                                image[i][j] = 0;
                            } else {
                                image[i][j] = 255;
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
    }

    public static void threshold(final int[][] image, final int min, final int max, final int threshold) {
        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (start == 0) start = 1;
            if (end >= image.length) end = image.length - 1;

            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    double wsp = 255.0 / (max - min);
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < image[0].length; j++) {

                            image[i][j] = (int) ((image[i][j] - min) * wsp);
                            if (image[i][j] < threshold) {
                                image[i][j] = 0;
                            } else {
                                image[i][j] = 255;
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
    }

    public static void threshold(final int[][] image, final int threshold) {
        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (start == 0) start = 1;
            if (end >= image.length) end = image.length - 1;

            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < image[0].length; j++) {
                            //if(image[i][j]<0) image[i][j]=0; else if(image[i][j]>255) image[i][j]=255;
                            if (image[i][j] < threshold) {
                                image[i][j] = 0;
                            } else {
                                image[i][j] = 255;
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
    }
}
