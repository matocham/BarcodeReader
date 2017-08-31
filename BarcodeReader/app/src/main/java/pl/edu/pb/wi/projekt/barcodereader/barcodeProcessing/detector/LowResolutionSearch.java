package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.detector;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.FilterRunner;


public class LowResolutionSearch {

    public static int THREAD_NUMBER = 4;

    /**
     * wyszukuje spójnych obszarów białych pikseli
     *
     * @param image  obraz do przetworzenia
     * @param scalex współczynnik pomniejszenia w kierunku poziomym
     * @param scaley współczynnik pomniejszenia w kierunku pionowym
     * @return tablica reprezentująca początek i koniec najszerszego obszaru
     */
    public static Range lowResolutionSearch(final int[][] image, final int scalex, final int scaley) {
        int startmax = 0, endmax = 0, row = 0;

        int begin, max = 0, counter;
        int intX = (int) Math.ceil(image[0].length * 1.0 / scalex);
        int intY = (int) Math.ceil(image.length * 1.0 / scaley);
        final int[][] out = new int[intY][intX];

        CountDownLatch latch = new CountDownLatch(THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(out.length * 1.0 / THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > out.length) end = out.length;
            (new Thread(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int sum;
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < out[0].length; j++) {
                            sum = 0;
                            for (int k = i * scaley; k < (i + 1) * scaley && k < image.length; k++) {
                                for (int l = j * scalex; l < (j + 1) * scalex && l < image[0].length; l++) {
                                    sum += image[k][l];
                                }
                            }
                            int color = (int) Math.round(sum * 1.0 / (scaley * scalex));
                            if (color < (scaley * scalex / 10)) {
                                color = 0;
                            } else {
                                color = 255;
                            }
                            out[i][j] = color;
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

        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < out[0].length; j++) {
                counter = 0;
                if (out[i][j] == 255) {
                    begin = j;
                    while (j < out[0].length && out[i][j] == 255) {
                        counter++;
                        j++;
                    }
                    j--;
                    if (counter > max) {
                        max = counter;
                        startmax = begin;
                        endmax = j;
                        row = i;
                    }
                }
            }
        }
        startmax *= scalex;
        endmax *= scalex;

        return new Range(startmax, endmax, row);
    }

    /**
     * wyszukuje spójnych obszarów białych pikseli
     *
     * @param image  obraz do przetworzenia
     * @param scalex współczynnik pomniejszenia w kierunku poziomym
     * @param scaley współczynnik pomniejszenia w kierunku pionowym
     * @param row    wiersz, w którym szukamy najszerszego elementu
     * @return najszerszy zakres
     */
    public static Range lowResolutionSearch(final int[][] image, final int scalex, final int scaley, int row) {
        int startmax = 0, endmax = 0;
        int begin = 0, max = 0, counter = 0;
        int intx = (int) Math.ceil(image[0].length * 1.0 / scalex);
        int inty = (int) Math.ceil(image.length * 1.0 / scaley);
        final int[][] out = new int[inty][intx];

        CountDownLatch latch = new CountDownLatch(THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(out.length * 1.0 / THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > out.length) end = out.length;
            (new Thread(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int sum;
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < out[0].length; j++) {
                            sum = 0;
                            for (int k = i * scaley; k < (i + 1) * scaley && k < image.length; k++) {
                                for (int l = j * scalex; l < (j + 1) * scalex && l < image[0].length; l++) {
                                    sum += image[k][l];
                                }
                            }
                            int color = (int) Math.round(sum * 1.0 / (scaley * scalex));
                            if (color < (scaley * scalex / 10)) {
                                color = 0;
                            } else {
                                color = 255;
                            }
                            out[i][j] = color;
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

        row = Math.round(row / scaley);
        for (int j = 0; j < out[0].length; j++) {
            counter = 0;
            if (out[row][j] == 255) {
                begin = j;
                while (j < out[0].length && out[row][j] == 255) {
                    counter++;
                    j++;
                }
                j--;
                if (counter > max) {
                    max = counter;
                    startmax = begin;
                    endmax = j;
                }
            }
        }

        startmax *= scalex;
        endmax *= scalex;

        return new Range(startmax, endmax, row);
    }

    /**
     * wyszukuje spójnych obszarów białych pikseli
     *
     * @param image  obraz do przetworzenia
     * @param scalex współczynnik pomniejszenia w kierunku poziomym
     * @param scaley współczynnik pomniejszenia w kierunku pionowym
     * @param rows   wiersze, w których szukamy najszerszego elementu
     * @return najszersze zakresy dla każdego wiersza
     */
    public static ArrayList<Range> lowResolutionSearch(final int[][] image, final int scalex, final int scaley, ArrayList<Integer> rows) {
        int startmax = 0, endmax = 0, row;
        int begin = 0, max = 0, counter = 0;
        int intx = (int) Math.ceil(image[0].length * 1.0 / scalex); // liczba kolumn
        int inty = (int) Math.ceil(image.length * 1.0 / scaley); // liczba wierszy
        final int[][] out = new int[inty][intx];
        ArrayList<Range> ranges = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(out.length * 1.0 / THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end > out.length) end = out.length;
            (new Thread(new FilterRunner(latch, start, end) {
                @Override
                public void doCalc() {
                    int sum;
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < out[0].length; j++) {
                            sum = 0;
                            for (int k = i * scaley; k < (i + 1) * scaley && k < image.length; k++) {
                                for (int l = j * scalex; l < (j + 1) * scalex && l < image[0].length; l++) {
                                    sum += image[k][l];
                                }
                            }
                            int color = (int) Math.round(sum * 1.0 / (scaley * scalex));
                            if (color < (scaley * scalex / 10)) {
                                color = 0;
                            } else {
                                color = 255;
                            }
                            out[i][j] = color;
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
        // ze względu na niewielką złożoność czasową nie trzeba wykonywać w wielu wątkach
        for (Integer r : rows) {
            startmax = endmax = 0;
            row = r / scaley;
            max = -1;
            for (int j = 0; j < out[0].length; j++) {
                counter = 0;
                if (out[row][j] == 255) {
                    begin = j;
                    while (j < out[0].length && out[row][j] == 255) {
                        counter++;
                        j++;
                    }
                    j--;
                    if (counter > max) {
                        max = counter;
                        startmax = begin;
                        endmax = j;
                    }
                }
            }

            startmax *= scalex;
            endmax *= scalex;
            if (startmax < 0) startmax = 0;
            if (endmax > image[0].length - 1) endmax = image[0].length - 1;
            ranges.add(new Range(startmax, endmax, r));
        }

        return ranges;
    }
}
