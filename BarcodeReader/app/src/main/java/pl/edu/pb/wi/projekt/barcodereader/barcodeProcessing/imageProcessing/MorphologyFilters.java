package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.FilterRunner;

public class MorphologyFilters {

    private static ExecutorService executor = ThreadExecutor.getExecutor();

    /**
     * rozszerza w kierunku pionowym białe piksele
     *
     * @param image obraz do przetworzenia
     * @param size  rozmiar filtra
     * @return przetworzony obraz
     */
    public static int[][] dilateVertical(final int[][] image, final int size) {
        final int[][] out = new int[image.length][image[0].length];

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image[0].length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end >= image[0].length) end = image[0].length;
            executor.execute(new FilterRunner(latch, start, end) {
                int maximum = 0;

                @Override
                public void doCalc() {
                    for (int i = size / 2; i < image.length - size / 2; i++) {
                        for (int j = start; j < end; j++) {
                            maximum = 0;
                            for (int k = -(size / 2); k < size / 2 + 1; k++) {
                                if (image[i + k][j] > maximum) {
                                    maximum = image[i + k][j];
                                }
                            }
                            out[i][j] = maximum;
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

    /**
     * zwęża białe piksele w kierunku pionowym
     *
     * @param image obraz do przetworzenia
     * @param size  rozmiar filtra
     * @return przetworzony obraz
     */
    public static int[][] erodeVertical(final int[][] image, final int size) {
        final int[][] out = new int[image.length][image[0].length];

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image[0].length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end >= image[0].length) end = image[0].length;
            executor.execute(new FilterRunner(latch, start, end) {
                int minimum = 255;

                @Override
                public void doCalc() {
                    for (int i = size / 2; i < image.length - size / 2; i++) {
                        for (int j = start; j < end; j++) {
                            minimum = 255;
                            for (int k = -(size / 2); k < size / 2 + 1; k++) {
                                if (image[i + k][j] < minimum) {
                                    minimum = image[i + k][j];
                                }
                                //System.out.println((i+k)+" i="+i+" j="+j+" ");
                            }
                            out[i][j] = minimum;//new Color(minimum, minimum, minimum).getRGB();
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

    /**
     * rozszerza w kierunku poziomym białe piksele
     *
     * @param image obraz do przetworzenia
     * @param size  rozmiar filtra
     * @return przetworzony obraz
     */
    public static int[][] dilateHorizontal(final int[][] image, final int size) {
        final int[][] out = new int[image.length][image[0].length];

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end >= image.length) end = image.length;
            executor.execute(new FilterRunner(latch, start, end) {
                int maximum = 0;

                @Override
                public void doCalc() {
                    for (int i = start; i < end; i++) {
                        for (int j = size / 2; j < image[0].length - size / 2; j++) {
                            maximum = 0;
                            for (int k = -(size / 2); k < size / 2 + 1; k++) {
                                if (image[i][j + k] > maximum) {
                                    maximum = image[i][j + k];
                                }
                            }
                            out[i][j] = maximum;
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

    /**
     * zwęża białe piksele w kierunku poziomym
     *
     * @param image obraz do przetworzenia
     * @param size  rozmiar filtra
     * @return przetworzony obraz
     */
    public static int[][] erodeHorizontal(final int[][] image, final int size) {
        final int[][] out = new int[image.length][image[0].length];

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end >= image.length) end = image.length;
            executor.execute(new FilterRunner(latch, start, end) {
                int minimum = 255;

                @Override
                public void doCalc() {
                    for (int i = start; i < end; i++) {
                        for (int j = size / 2; j < image[0].length - size / 2; j++) {
                            minimum = 255;
                            for (int k = -(size / 2); k < size / 2 + 1; k++) {
                                if (image[i][j + k] < minimum) {
                                    minimum = image[i][j + k];
                                }
                            }
                            out[i][j] = minimum;//new Color(minimum, minimum, minimum).getRGB();
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
