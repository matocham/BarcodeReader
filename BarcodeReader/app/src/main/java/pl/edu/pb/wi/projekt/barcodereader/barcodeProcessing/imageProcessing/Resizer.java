package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.FilterRunner;

public class Resizer {
    private static ExecutorService executor = ThreadExecutor.getExecutor();

    // synchronized znacznie spowalnia wykonanie kodu!
    static synchronized byte getpixel(byte[] in, int width, int height, int y, int x, int channel) {
        if (x < width && y < height && x >= 0 && y >= 0)
            return in[(y * 3 * width) + (3 * x) + channel];
        return 0;
    }

    static byte[] bicubicResize(final byte[] in, final int srcWidth, final int srcHeight, int destWidth, final int destHeight) {
        final byte[] out = new byte[destWidth * destHeight * 3];

        final float x_scale = (float) (srcWidth) / destWidth;
        final float y_scale = (float) (srcHeight) / destHeight;
        final int channels = 3;
        final int row_stride = destWidth * channels;

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(destWidth * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;
        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > destWidth) end = destWidth;
            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    byte[] C = new byte[5];

                    for (int i = 0; i < destHeight; ++i) {
                        for (int j = start; j < end; ++j) {
                            int x = (int) (x_scale * j);
                            int y = (int) (y_scale * i);
                            float dx = x_scale * j - x;
                            float dy = y_scale * i - y;

                            for (int k = 0; k < 3; ++k) {
                                byte a0;
                                byte d0;
                                byte d2;
                                byte d3;
                                byte a1;
                                byte a2;
                                byte a3;
                                for (int jj = 0; jj < 4; ++jj) {
                                    int z = y - 1 + jj;
                                    //a0 = getpixel(in, srcWidth, srcHeight, z, x, k); // wyraz wolny
                                    //d0 = (byte) (getpixel(in, srcWidth, srcHeight, z, x - 1, k) - a0);
                                    //d2 = (byte) (getpixel(in, srcWidth, srcHeight, z, x + 1, k) - a0);
                                    //d3 = (byte) (getpixel(in, srcWidth, srcHeight, z, x + 2, k) - a0);
                                    // alternatywna wersja bez synchronizowanej funkcji
                                    if (x >= 0 && x < srcWidth && z < srcHeight && z >= 0) {
                                        a0 = in[(z * 3 * srcWidth) + (3 * x) + k];
                                    } else {
                                        a0 = 0;
                                    }

                                    if (x - 1 >= 0 && x - 1 < srcWidth && z < srcHeight && z >= 0) {
                                        d0 = in[(z * 3 * srcWidth) + (3 * (x - 1)) + k];
                                    } else {
                                        d0 = 0;
                                    }
                                    d0 -= a0;

                                    if (x + 1 >= 0 && x + 1 < srcWidth && z < srcHeight && z >= 0) {
                                        d2 = in[(z * 3 * srcWidth) + (3 * (x + 1)) + k];
                                    } else {
                                        d2 = 0;
                                    }
                                    d2 -= a0;

                                    if (x + 2 >= 0 && x < srcWidth && z < srcHeight && z >= 0) {
                                        d3 = in[(z * 3 * srcWidth) + (3 * (x + 2)) + k];
                                    } else {
                                        d3 = 0;
                                    }
                                    d3 -= a0;

                                    a1 = (byte) (-1.0 / 3 * d0 + d2 - 1.0 / 6 * d3); // ax
                                    a2 = (byte) (1.0 / 2 * d0 + 1.0 / 2 * d2);//ax^2
                                    a3 = (byte) (-1.0 / 6 * d0 - 1.0 / 2 * d2 + 1.0 / 6 * d3); //ax^3
                                    C[jj] = (byte) (a0 + a1 * dx + a2 * dx * dx + a3 * dx * dx * dx); // w poziomie interpolacja
                                }
                                d0 = (byte) (C[0] - C[1]);
                                d2 = (byte) (C[2] - C[1]);
                                d3 = (byte) (C[3] - C[1]);
                                a0 = C[1];
                                a1 = (byte) (-1.0 / 3 * d0 + d2 - 1.0 / 6 * d3);
                                a2 = (byte) (1.0 / 2 * d0 + 1.0 / 2 * d2);
                                a3 = (byte) (-1.0 / 6 * d0 - 1.0 / 2 * d2 + 1.0 / 6 * d3);
                                out[i * row_stride + j * channels + k] = (byte) (a0 + a1 * dy + a2 * dy * dy + a3 * dy * dy * dy); // interpolacja w pionie
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

    static int[] bicubicResizeToInt(final byte[] in, final int srcWidth, final int srcHeight, final int destWidth, final int destHeight) {
        final int[] out = new int[destWidth * destHeight];

        final float x_scale = (float) (srcWidth) / destWidth;
        final float y_scale = (float) (srcHeight) / destHeight;
        final int channels = 3;

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(destWidth * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;
        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > destWidth) end = destWidth;
            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    byte[] C = new byte[5];
                    int[] cum = new int[3];
                    for (int i = 0; i < destHeight; ++i) {
                        for (int j = start; j < end; ++j) {
                            int x = (int) (x_scale * j);
                            int y = (int) (y_scale * i);
                            float dx = x_scale * j - x;
                            float dy = y_scale * i - y;

                            for (int k = 0; k < channels; ++k) {
                                byte a0;
                                byte d0;
                                byte d2;
                                byte d3;
                                byte a1;
                                byte a2;
                                byte a3;
                                for (int jj = 0; jj < 4; ++jj) {
                                    int z = y - 1 + jj;
                                    //a0 = getpixel(in, src_width, src_height, z, x, k); // wyraz wolny
                                    //d0 = (byte) (getpixel(in, src_width, src_height, z, x - 1, k) - a0);
                                    //d2 = (byte) (getpixel(in, src_width, src_height, z, x + 1, k) - a0);
                                    //d3 = (byte) (getpixel(in, src_width, src_height, z, x + 2, k) - a0);
                                    if (x >= 0 && x < srcWidth && z < srcHeight && z >= 0) {
                                        a0 = in[(z * 3 * srcWidth) + (3 * x) + k];
                                    } else {
                                        a0 = 0;
                                    }

                                    if (x - 1 >= 0 && x - 1 < srcWidth && z < srcHeight && z >= 0) {
                                        d0 = in[(z * 3 * srcWidth) + (3 * (x - 1)) + k];
                                    } else {
                                        d0 = 0;
                                    }
                                    d0 -= a0;

                                    if (x + 1 >= 0 && x + 1 < srcWidth && z < srcHeight && z >= 0) {
                                        d2 = in[(z * 3 * srcWidth) + (3 * (x + 1)) + k];
                                    } else {
                                        d2 = 0;
                                    }
                                    d2 -= a0;

                                    if (x + 2 >= 0 && x < srcWidth && z < srcHeight && z >= 0) {
                                        d3 = in[(z * 3 * srcWidth) + (3 * (x + 2)) + k];
                                    } else {
                                        d3 = 0;
                                    }
                                    d3 -= a0;

                                    a1 = (byte) (-1.0 / 3 * d0 + d2 - 1.0 / 6 * d3); // ax
                                    a2 = (byte) (1.0 / 2 * d0 + 1.0 / 2 * d2);//ax^2
                                    a3 = (byte) (-1.0 / 6 * d0 - 1.0 / 2 * d2 + 1.0 / 6 * d3); //ax^3
                                    C[jj] = (byte) (a0 + a1 * dx + a2 * dx * dx + a3 * dx * dx * dx); // w poziomie interpolacja
                                }
                                d0 = (byte) (C[0] - C[1]);
                                d2 = (byte) (C[2] - C[1]);
                                d3 = (byte) (C[3] - C[1]);
                                a0 = C[1];
                                a1 = (byte) (-1.0 / 3 * d0 + d2 - 1.0 / 6 * d3);
                                a2 = (byte) (1.0 / 2 * d0 + 1.0 / 2 * d2);
                                a3 = (byte) (-1.0 / 6 * d0 - 1.0 / 2 * d2 + 1.0 / 6 * d3);
                                cum[k] = (int) (a0 + a1 * dy + a2 * dy * dy + a3 * dy * dy * dy) & 0xff; // interpolacja w pionie
                            }
                            out[i * destWidth + j] = 0xff000000 | (cum[0] << 16) | (cum[1] << 8) | cum[2];
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

    static int[][] bicubicResizeTo2DInt(final byte[] in, final int srcWidth, final int srcHeight, int destWidth, final int destHeight) {
        final int[][] out = new int[destHeight][destWidth];

        final float x_scale = (float) (srcWidth) / destWidth;
        final float y_scale = (float) (srcHeight) / destHeight;
        final int channels = 3;

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(destWidth * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;
        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > destWidth) end = destWidth;
            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    byte[] C = new byte[5];
                    int[] cum = new int[3];
                    for (int i = 0; i < destHeight; ++i) {
                        for (int j = start; j < end; ++j) {
                            int x = (int) (x_scale * j);
                            int y = (int) (y_scale * i);
                            float dx = x_scale * j - x;
                            float dy = y_scale * i - y;

                            for (int k = 0; k < channels; ++k) {
                                byte a0;
                                byte d0;
                                byte d2;
                                byte d3;
                                byte a1;
                                byte a2;
                                byte a3;
                                for (int jj = 0; jj < 4; ++jj) {
                                    int z = y - 1 + jj;
                                    //a0 = getpixel(in, src_width, src_height, z, x, k); // wyraz wolny
                                    //d0 = (byte) (getpixel(in, src_width, src_height, z, x - 1, k) - a0);
                                    //d2 = (byte) (getpixel(in, src_width, src_height, z, x + 1, k) - a0);
                                    //d3 = (byte) (getpixel(in, src_width, src_height, z, x + 2, k) - a0);
                                    if (x >= 0 && x < srcWidth && z < srcHeight && z >= 0) {
                                        a0 = in[(z * 3 * srcWidth) + (3 * x) + k];
                                    } else {
                                        a0 = 0;
                                    }

                                    if (x - 1 >= 0 && x - 1 < srcWidth && z < srcHeight && z >= 0) {
                                        d0 = in[(z * 3 * srcWidth) + (3 * (x - 1)) + k];
                                    } else {
                                        d0 = 0;
                                    }
                                    d0 -= a0;

                                    if (x + 1 >= 0 && x + 1 < srcWidth && z < srcHeight && z >= 0) {
                                        d2 = in[(z * 3 * srcWidth) + (3 * (x + 1)) + k];
                                    } else {
                                        d2 = 0;
                                    }
                                    d2 -= a0;

                                    if (x + 2 >= 0 && x < srcWidth && z < srcHeight && z >= 0) {
                                        d3 = in[(z * 3 * srcWidth) + (3 * (x + 2)) + k];
                                    } else {
                                        d3 = 0;
                                    }
                                    d3 -= a0;

                                    a1 = (byte) (-1.0 / 3 * d0 + d2 - 1.0 / 6 * d3); // ax
                                    a2 = (byte) (1.0 / 2 * d0 + 1.0 / 2 * d2);//ax^2
                                    a3 = (byte) (-1.0 / 6 * d0 - 1.0 / 2 * d2 + 1.0 / 6 * d3); //ax^3
                                    C[jj] = (byte) (a0 + a1 * dx + a2 * dx * dx + a3 * dx * dx * dx); // w poziomie interpolacja
                                }
                                d0 = (byte) (C[0] - C[1]);
                                d2 = (byte) (C[2] - C[1]);
                                d3 = (byte) (C[3] - C[1]);
                                a0 = C[1];
                                a1 = (byte) (-1.0 / 3 * d0 + d2 - 1.0 / 6 * d3);
                                a2 = (byte) (1.0 / 2 * d0 + 1.0 / 2 * d2);
                                a3 = (byte) (-1.0 / 6 * d0 - 1.0 / 2 * d2 + 1.0 / 6 * d3);
                                cum[k] = (int) (a0 + a1 * dy + a2 * dy * dy + a3 * dy * dy * dy) & 0xff; // interpolacja w pionie
                            }
                            out[i][j] = 0xff000000 | (cum[0] << 16) | (cum[1] << 8) | cum[2];
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

    static int[][] bicubicResizeTo2DIntGray(final byte[] in, final int srcWidth, final int srcHeight, int destWidth, final int destHeight) {
        final int[][] out = new int[destHeight][destWidth];

        final float x_scale = (float) (srcWidth) / destWidth;
        final float y_scale = (float) (srcHeight) / destHeight;
        final int channels = 3;

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(destWidth * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;
        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > destWidth) end = destWidth;
            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    byte[] C = new byte[5];
                    int[] cum = new int[3];
                    for (int i = 0; i < destHeight; ++i) {
                        for (int j = start; j < end; ++j) {
                            int x = (int) (x_scale * j);
                            int y = (int) (y_scale * i);
                            float dx = x_scale * j - x;
                            float dy = y_scale * i - y;

                            for (int k = 0; k < channels; ++k) {
                                byte a0;
                                byte d0;
                                byte d2;
                                byte d3;
                                byte a1;
                                byte a2;
                                byte a3;
                                for (int jj = 0; jj < 4; ++jj) {
                                    int z = y - 1 + jj;
                                    //a0 = getpixel(in, src_width, src_height, z, x, k); // wyraz wolny
                                    //d0 = (byte) (getpixel(in, src_width, src_height, z, x - 1, k) - a0);
                                    //d2 = (byte) (getpixel(in, src_width, src_height, z, x + 1, k) - a0);
                                    //d3 = (byte) (getpixel(in, src_width, src_height, z, x + 2, k) - a0);
                                    if (x >= 0 && x < srcWidth && z < srcHeight && z >= 0) {
                                        a0 = in[(z * 3 * srcWidth) + (3 * x) + k];
                                    } else {
                                        a0 = 0;
                                    }

                                    if (x - 1 >= 0 && x - 1 < srcWidth && z < srcHeight && z >= 0) {
                                        d0 = in[(z * 3 * srcWidth) + (3 * (x - 1)) + k];
                                    } else {
                                        d0 = 0;
                                    }
                                    d0 -= a0;

                                    if (x + 1 >= 0 && x + 1 < srcWidth && z < srcHeight && z >= 0) {
                                        d2 = in[(z * 3 * srcWidth) + (3 * (x + 1)) + k];
                                    } else {
                                        d2 = 0;
                                    }
                                    d2 -= a0;

                                    if (x + 2 >= 0 && x < srcWidth && z < srcHeight && z >= 0) {
                                        d3 = in[(z * 3 * srcWidth) + (3 * (x + 2)) + k];
                                    } else {
                                        d3 = 0;
                                    }
                                    d3 -= a0;

                                    a1 = (byte) (-1.0 / 3 * d0 + d2 - 1.0 / 6 * d3); // ax
                                    a2 = (byte) (1.0 / 2 * d0 + 1.0 / 2 * d2);//ax^2
                                    a3 = (byte) (-1.0 / 6 * d0 - 1.0 / 2 * d2 + 1.0 / 6 * d3); //ax^3
                                    C[jj] = (byte) (a0 + a1 * dx + a2 * dx * dx + a3 * dx * dx * dx); // w poziomie interpolacja
                                }
                                d0 = (byte) (C[0] - C[1]);
                                d2 = (byte) (C[2] - C[1]);
                                d3 = (byte) (C[3] - C[1]);
                                a0 = C[1];
                                a1 = (byte) (-1.0 / 3 * d0 + d2 - 1.0 / 6 * d3);
                                a2 = (byte) (1.0 / 2 * d0 + 1.0 / 2 * d2);
                                a3 = (byte) (-1.0 / 6 * d0 - 1.0 / 2 * d2 + 1.0 / 6 * d3);
                                cum[k] = (int) (a0 + a1 * dy + a2 * dy * dy + a3 * dy * dy * dy) & 0xff; // interpolacja w pionie
                            }
                            out[i][j] = (int) (0.299 * cum[0] + 0.587 * cum[1] + 0.114 * cum[2]);
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
     * Bilinear resize ARGB image.
     * pixels is an array of size w * h.
     * Target dimension is w2 * h2.
     * w2 * h2 cannot be zero.
     *
     * @param pixels     Image pixels.
     * @param srcWidth   Image width.
     * @param srcHeight  Image height.
     * @param destWidth  New width.
     * @param destHeight New height.
     * @return New array with size w2 * h2.
     */
    public static int[][] resizeBilinear(final byte[] pixels, final int srcWidth, final int srcHeight, final int destWidth, final int destHeight, final int channels) {
        final int[][] out = new int[destHeight][destWidth];

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(destHeight * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;
        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > destHeight) end = destHeight;
            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int a, b, c, d, rowStride;
                    int x, y, index = 0;
                    float x_ratio = ((float) (srcWidth - 1)) / destWidth;
                    float y_ratio = ((float) (srcHeight - 1)) / destHeight;
                    float x_diff, y_diff, value;
                    rowStride = channels * srcWidth;
                    float res[] = new float[3];
                    if (channels == 4) {
                        for (int i = start; i < end; i++) {
                            for (int j = 0; j < destWidth; j++) {
                                x = (int) (x_ratio * j);
                                y = (int) (y_ratio * i);
                                x_diff = (x_ratio * j) - x;
                                y_diff = (y_ratio * i) - y;
                                index = (y * srcWidth + x) * channels;
                                index++;// pomijamy alpha
                                for (int k = 0; k < channels - 1; k++, index++) {
                                    a = pixels[index];
                                    b = pixels[index + channels];
                                    c = pixels[index + rowStride];
                                    d = pixels[index + rowStride + channels];

                                    // blue element
                                    // Yb = Ab(1-w)(1-h) + Bb(w)(1-h) + Cb(h)(1-w) + Db(wh)
                                    value = (a) * (1 - x_diff) * (1 - y_diff) + (b) * (x_diff) * (1 - y_diff) +
                                            (c) * (y_diff) * (1 - x_diff) + (d) * (x_diff * y_diff);

                                    res[k] = value;
                                }

                                out[i][j] =
                                        0xff000000 | // hardcode alpha
                                                ((((int) res[2]) << 16) & 0xff0000) |
                                                ((((int) res[1]) << 8) & 0xff00) |
                                                ((int) res[0]);
                            }
                        }
                    } else {
                        for (int i = start; i < end; i++) {
                            for (int j = 0; j < destWidth; j++) {
                                x = (int) (x_ratio * j);
                                y = (int) (y_ratio * i);
                                x_diff = (x_ratio * j) - x;
                                y_diff = (y_ratio * i) - y;
                                index = (y * srcWidth + x) * channels;
                                for (int k = 0; k < channels; k++, index++) {
                                    a = pixels[index];
                                    b = pixels[index + channels];
                                    c = pixels[index + rowStride];
                                    d = pixels[index + rowStride + channels];
                                    // Yb = Ab(1-w)(1-h) + Bb(w)(1-h) + Cb(h)(1-w) + Db(wh)
                                    value = (a & 0xff) * (1 - x_diff) * (1 - y_diff) + (b & 0xff) * (x_diff) * (1 - y_diff) +
                                            (c & 0xff) * (y_diff) * (1 - x_diff) + (d & 0xff) * (x_diff * y_diff);

                                    res[k] = value;
                                }

                                out[i][j] =
                                        0xff000000 | // hardcode alpha
                                                ((((int) res[2]) << 16) & 0xff0000) |
                                                ((((int) res[1]) << 8) & 0xff00) |
                                                ((int) res[0] & 0xff);
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

    /**
     * Bilinear resize ARGB image.
     * pixels is an array of size w * h.
     * Target dimension is w2 * h2.
     * w2 * h2 cannot be zero.
     *
     * @param pixels     Image pixels.
     * @param srcWidth   Image width.
     * @param srcHeight  Image height.
     * @param destWidth  New width.
     * @param destHeight New height.
     * @return New array with size w2 * h2.
     */
    public static int[][] resizeBilinearGray(final byte[] pixels, final int srcWidth, final int srcHeight, final int destWidth, final int destHeight, final int channels) {
        final int[][] out = new int[destHeight][destWidth];

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(destHeight * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;
        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > destHeight) end = destHeight;
            executor.execute(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int a, b, c, d, rowStride = channels * srcWidth;
                    ;
                    int x, y, index = 0;
                    float x_ratio = ((float) (srcWidth - 1)) / destWidth;
                    float y_ratio = ((float) (srcHeight - 1)) / destHeight;
                    float x_diff, y_diff, value;
                    float res[] = new float[3];
                    if (channels == 4) {
                        for (int i = start; i < end; i++) {
                            for (int j = 0; j < destWidth; j++) {
                                x = (int) (x_ratio * j);
                                y = (int) (y_ratio * i);
                                x_diff = (x_ratio * j) - x;
                                y_diff = (y_ratio * i) - y;
                                index = (y * srcWidth + x) * channels;
                                index++;// pomijamy alpha
                                for (int k = 0; k < channels - 1; k++, index++) {
                                    a = pixels[index];
                                    b = pixels[index + channels];
                                    c = pixels[index + rowStride];
                                    d = pixels[index + rowStride + channels];

                                    // blue element
                                    // Yb = Ab(1-w)(1-h) + Bb(w)(1-h) + Cb(h)(1-w) + Db(wh)
                                    value = (a) * (1 - x_diff) * (1 - y_diff) + (b) * (x_diff) * (1 - y_diff) +
                                            (c) * (y_diff) * (1 - x_diff) + (d) * (x_diff * y_diff);

                                    res[k] = value;
                                }

                                out[i][j] = (int) (0.299 * res[2] + 0.587 * res[1] + 0.114 * res[0]);
                            }
                        }
                    } else {
                        for (int i = start; i < end; i++) {
                            for (int j = 0; j < destWidth; j++) {
                                x = (int) (x_ratio * j);
                                y = (int) (y_ratio * i);
                                x_diff = (x_ratio * j) - x;
                                y_diff = (y_ratio * i) - y;
                                index = (y * srcWidth + x) * channels;
                                for (int k = 0; k < channels; k++, index++) {
                                    a = pixels[index];
                                    b = pixels[index + channels];
                                    c = pixels[index + rowStride];
                                    d = pixels[index + rowStride + channels];
                                    // Yb = Ab(1-w)(1-h) + Bb(w)(1-h) + Cb(h)(1-w) + Db(wh)
                                    value = (a & 0xff) * (1 - x_diff) * (1 - y_diff) + (b & 0xff) * (x_diff) * (1 - y_diff) +
                                            (c & 0xff) * (y_diff) * (1 - x_diff) + (d & 0xff) * (x_diff * y_diff);

                                    res[k] = value;
                                }

                                out[i][j] = (int) (0.299 * res[2] + 0.587 * res[1] + 0.114 * res[0]);
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
