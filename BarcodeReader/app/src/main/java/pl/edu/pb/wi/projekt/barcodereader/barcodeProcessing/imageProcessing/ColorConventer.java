package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import android.graphics.Bitmap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ColorConventer {
    private static ExecutorService executor = ThreadExecutor.getExecutor();

    /**
     * konwertuje obraz do odcieni szarości
     *
     * @param image obraz do obróbki, każdy piksel zapisany jako ARGB
     * @return obraz w odcieniach szarości
     */
    public static int[][] toGrayScale(final int[][] image) {

        final int[][] result = new int[image.length][image[0].length];


        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > image.length) end = image.length;
            (new Thread(new GrayScaleRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < image[0].length; j++) {
                            int argb = image[i][j] & 0xffffff;
                            int R, G, B;
                            B = argb & 0xff;
                            G = (argb >> 8) & 0xff;
                            R = (argb >> 16) & 0xff;
                            argb = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                            result[i][j] = argb;
                        }
                    }
                }
            })).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return result;
    }

    public static int[][] toGrayScale(final int[] image, final int width, final int height) {

        final int[][] result = new int[height][width];


        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(height * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > height) end = height;
            (new Thread(new GrayScaleRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int offset = start * height;
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < width; j++) {
                            int argb = image[offset++] & 0xffffff;
                            int R, G, B;
                            B = argb & 0xff;
                            G = (argb >> 8) & 0xff;
                            R = (argb >> 16) & 0xff;
                            argb = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                            result[i][j] = argb;
                        }
                    }
                }
            })).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return result;
    }

    public static int[][] toGrayScale(final byte[] image, final int width, final int height, final int channels) {

        final int[][] result = new int[height][width];


        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(height * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > height) end = height;
            (new Thread(new GrayScaleRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int offset = start * height * channels;
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < width; j++) {
                            int argb;
                            int R, G, B;
                            if (channels == 4) {
                                offset++;//A
                            }
                            R = image[offset++] & 0xff;
                            G = image[offset++] & 0xff;
                            B = image[offset++] & 0xff;

                            argb = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                            result[i][j] = argb;
                        }
                    }
                }
            })).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return result;
    }

    public static int[] toGrayScale1D(final byte[] image, final int width, final int height, final int channels) {

        final int[] result = new int[height * width];

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(height * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > height) end = height;
            (new Thread(new GrayScaleRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int offset = start * height * channels;
                    if (channels == 4) {
                        for (int i = start; i < end; i++) {
                            for (int j = 0; j < width; j++) {
                                int argb;
                                int R, G, B;
                                R = image[offset + 1] & 0xff;
                                G = image[offset + 2] & 0xff;
                                B = image[offset + 3] & 0xff;

                                argb = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                                result[offset / channels] = argb;
                                offset += channels;
                            }
                        }
                    } else {
                        for (int i = start; i < end; i++) {
                            for (int j = 0; j < width; j++) {
                                int argb;
                                int R, G, B;
                                R = image[offset] & 0xff;
                                G = image[offset + 1] & 0xff;
                                B = image[offset + 2] & 0xff;

                                argb = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                                result[offset / channels] = argb;
                                offset += channels;
                            }
                        }
                    }
                }
            })).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return result;
    }

    public static byte[] toGrayScale1DByte(final byte[] image, final int width, final int height, final int channels) {

        final byte[] result = new byte[height * width];


        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(height * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > height) end = height;
            (new Thread(new GrayScaleRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int offset = start * height * channels;
                    if (channels == 4) {
                        for (int i = start; i < end; i++) {
                            for (int j = 0; j < width; j++) {
                                int argb;
                                int R, G, B;
                                R = image[offset + 1] & 0xff;
                                G = image[offset + 2] & 0xff;
                                B = image[offset + 3] & 0xff;

                                argb = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                                result[offset / channels] = (byte) (argb & 0xff);
                                offset += channels;
                            }
                        }
                    } else {
                        for (int i = start; i < end; i++) {
                            for (int j = 0; j < width; j++) {
                                int argb;
                                int R, G, B;
                                R = image[offset] & 0xff;
                                G = image[offset + 1] & 0xff;
                                B = image[offset + 2] & 0xff;

                                argb = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                                result[offset / channels] = (byte) (argb & 0xff);
                                offset += channels;
                            }
                        }
                    }
                }
            })).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return result;
    }

    /**
     * przeprowadza konwersję Bitmap do dwuwymiarowej tablicy zawierającej obraz w odcieniach szarości
     *
     * @param image obraz do przetworzenia
     * @return piksele w odcieniach szarości
     */
    public static int[][] to2Dgreyscale(Bitmap image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixels(pixels, 0, width, 0, 0, width, height);
        return to2Dgreyscale(pixels, width, height);
    }


    /**
     * przeprowadza konwersję Bitmap do dwuwymiarowej tablicy zawierającej obraz w odcieniach szarości
     *
     * @return piksele w odcieniach szarości
     */
    public static int[][] to2Dgreyscale(final int[] pixels, final int width, int height) {

        final int[][] result = new int[height][width];

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(height * 1.0 / ThreadExecutor.THREAD_NUMBER); // aby zapewnić, że podział będzie pomiędzy pikselami
        int start, end;
        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > width) end = height;
            (new Thread(new GrayScaleRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int offset = start * width;
                    int r,g,b;
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < width; j++) {
                            int pixel=pixels[offset++];
                            r = (pixel >> 16) & 0xff;//R
                            g = (pixel >> 8) & 0xff;//G
                            b = (pixel) & 0xff;//B
                            result[i][j]=(int) (0.299 * r + 0.587 * g + 0.114 * b);
                        }
                    }
                }

            })).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        return result;
    }

    private static abstract class GrayScaleRunner implements Runnable {
        int start, end;
        CountDownLatch l;

        GrayScaleRunner(CountDownLatch latch, int start, int end) {
            this.start = start;
            this.end = end;
            l = latch;
        }

        @Override
        public void run() {
            doCalc();
            l.countDown();
        }

        public abstract void doCalc();
    }
}
