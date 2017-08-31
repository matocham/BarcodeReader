package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.FilterRunner;

/**
 * Zmienia sposób przechowywania danych za pomocą różnych tablic dwu- i jednowymiarowych
 *
 * @author Mateusz
 */
public class StorageConventer {
    private static ExecutorService executor = ThreadExecutor.getExecutor();

    public static int[] toIntARGB(byte[] image, int channels) {
        int out[] = new int[image.length / channels];
        int R, G, B;
        if (channels == 4) {
            for (int i = 0; i < image.length; i += channels) {
                R = image[i + 1] & 0xff;
                G = image[i + 2] & 0xff;
                B = image[i + 3] & 0xff;
                out[i / channels] = 0xff000000 | (R << 16) | (G << 8) | B;
            }
        } else {
            for (int i = 0; i < image.length; i += channels) {
                R = image[i] & 0xff;
                G = image[i + 1] & 0xff;
                B = image[i + 2] & 0xff;
                out[i / channels] = 0xff000000 | (R << 16) | (G << 8) | B;
            }
        }
        return out;
    }

    public static int[][] to2DIntARGB(byte[] image, int width, int height, int channels) {
        int out[][] = new int[height][width];
        int R, G, B;
        int offset = 0;
        if (channels == 4) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    offset++;
                    R = image[offset++] & 0xff;
                    G = image[offset++] & 0xff;
                    B = image[offset++] & 0xff;
                    out[i][j] = 0xff000000 | (R << 16) | (G << 8) | B;
                }
            }
        } else {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    R = image[offset++] & 0xff;
                    G = image[offset++] & 0xff;
                    B = image[offset++] & 0xff;
                    out[i][j] = 0xff000000 | (R << 16) | (G << 8) | B;
                }
            }
        }
        return out;
    }

    public static int[][] to2DIntARGB(int[] image, int width, int height) {
        int out[][] = new int[height][width];
        int offset = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                out[i][j] = image[offset++];
            }
        }
        return out;
    }

    public static byte[] toByteARGB(int[][] image) {
        int channels = 3;
        byte[] out = new byte[image.length * image[0].length * channels];
        int offset = 0;
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++, offset += channels) {
                int pixel = image[i][j];
                out[offset] = (byte) ((pixel >> 16) & 0xff);//R
                out[offset + 1] = (byte) ((pixel >> 8) & 0xff);//G
                out[offset + 2] = (byte) ((pixel) & 0xff);//B
            }
        }
        return out;
    }

    public static int[] toIntARGB(int[][] image) {
        int out[] = new int[image.length * image[0].length];
        int offset = 0;
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
                out[offset++] = image[i][j];
            }
        }
        return out;
    }

    public static byte[] toByteARGB(int[] image) {
        int channels = 3;
        byte[] out = new byte[image.length * channels];
        for (int i = 0; i < out.length; i += channels) {
            int pixel = image[i / channels];
            out[i] = (byte) ((pixel >> 16) & 0xff);//R
            out[i + 1] = (byte) ((pixel >> 8) & 0xff);//G
            out[i + 2] = (byte) ((pixel) & 0xff);//B
        }
        return null;
    }

    public static int[][] to2DIntGray(int[] image, int width, int height) {
        return to2DIntARGB(image, width, height);
    }

    public static int[] toIntGray(int[][] image) {
        return toIntARGB(image);
    }

    public static byte[] toByteGray(int[][] image) {
        int channels = 3;
        byte[] out = new byte[image.length * image[0].length * channels];
        int offset = 0;
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++, offset += channels) {
                int pixel = image[i][j];
                out[offset] = (byte) (pixel & 0xff);//R
                out[offset + 1] = (byte) (pixel & 0xff);//G
                out[offset + 2] = (byte) (pixel & 0xff);//B
            }
        }
        return out;
    }

    public static byte[] toByteGray(int[] image) {
        int channels = 3;
        byte[] out = new byte[image.length * channels];
        for (int i = 0; i < out.length; i += channels) {
            int pixel = image[i / channels];
            out[i] = (byte) (pixel & 0xff);//R
            out[i + 1] = (byte) (pixel & 0xff);//G
            out[i + 2] = (byte) (pixel & 0xff);//B
        }
        return null;
    }

    /**
     * przeprowadza konwersję Bitmap do dwuwymiarowej tablicy zawierającej obraz w formacie ARGB
     *
     * @param image obraz do przetworzenia
     * @return piksele w formacie ARGB
     */
    public static int[][] to2DARGB(Bitmap image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixels(pixels, 0, width, 0, 0, width, height);

        return to2DARGB(pixels, width, height);
    }

    /**
     * przeprowadza konwersję Bitmap do dwuwymiarowej tablicy zawierającej obraz w formacie ARGB
     *
     * @return piksele w formacie ARGB
     */
    public static int[][] to2DARGB(final int[] pixels, final int width, int height) {


        final int[][] result = new int[height][width];

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(height * 1.0 / ThreadExecutor.THREAD_NUMBER); // aby zapewnić, że podział będzie pomiędzy pikselami
        int start, end;
        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > width) end = height;
            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int pixel = start * width;
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < width; j++) {
                            result[i][j] = pixels[pixel++];
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

        return result;
    }

    public static int convertYUVtoRGB(int y, int u, int v) {
        int r, g, b;

        r = y + (int) 1.402f * v;
        g = y - (int) (0.344f * u + 0.714f * v);
        b = y + (int) 1.772f * u;
        r = r > 255 ? 255 : r < 0 ? 0 : r;
        g = g > 255 ? 255 : g < 0 ? 0 : g;
        b = b > 255 ? 255 : b < 0 ? 0 : b;
        return 0xff000000 | (b << 16) | (g << 8) | r;
    }

    /**
     * konwersja z YUV do ARGB grayscale
     *
     * @param pixels
     * @param data
     * @param width
     * @param height
     */
    public static void YUVtoRGBGray(int[] pixels, byte[] data, int width, int height) {
        int p;
        int size = width * height;
        for (int i = 0; i < size; i++) {
            p = data[i] & 0xFF;
            //pixels[i] = 0xff000000 | p<<16 | p<<8 | p;
            pixels[i] = p;
        }
    }

    public static int[] YUVtoRGBGray(byte[] data, int width, int height) {
        int size = width * height;
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = data[i] & 0xFF;
        }
        return result;
    }

    public static int[][] YUVtoRGBGray2D(byte[] data, int width, int height) {
        int size = width * height;
        int[][] result = new int[height][width];
        int offset = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result[i][j] = data[offset++] & 0xFF;
            }
        }
        return result;
    }

    public static void YUVtoRGB(int[] pixels, byte[] data, int width, int height) {
        int p;
        int size = width * height;
        for (int i = 0; i < size; i++) {
            p = data[i] & 0xFF;
            pixels[i] = 0xff000000 | p << 16 | p << 8 | p;
        }
    }

    public static int[] convertYUV420_NV21toRGB8888(byte[] data, int width, int height) {
        int size = width * height;
        int offset = size;
        int[] pixels = new int[size];
        int u, v, y1, y2, y3, y4;

        // i percorre os Y and the final pixels
        // k percorre os pixles U e V
        for (int i = 0, k = 0; i < size; i += 2, k += 2) {
            y1 = data[i] & 0xff;
            y2 = data[i + 1] & 0xff;
            y3 = data[width + i] & 0xff;
            y4 = data[width + i + 1] & 0xff;

            u = data[offset + k] & 0xff;
            v = data[offset + k + 1] & 0xff;
            u = u - 128;
            v = v - 128;

            pixels[i] = convertYUVtoRGB(y1, u, v);
            pixels[i + 1] = convertYUVtoRGB(y2, u, v);
            pixels[width + i] = convertYUVtoRGB(y3, u, v);
            pixels[width + i + 1] = convertYUVtoRGB(y4, u, v);

            if (i != 0 && (i + 2) % width == 0)
                i += width;
        }

        return pixels;
    }

    public static Bitmap YUVtoBitmap(byte[] data, int width, int height) {

        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, os);
        byte[] jpegByteArray = os.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length);

        return bitmap;
    }
}
