package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.decoder;

import java.util.Arrays;

/**
 * odpowiada za odczyt kodu kreskowego z przekazanego zdjęcia - zdjęcie w
 * odcieniach szarości
 *
 * @author Mateusz
 */
public class Reader {
    private static final int INTERVALS = 10; // liczba przedziałów, na jaki dzielimy obraz podczas obliczania progów
    private static final double THRES_STEP = 0.04; // co ile procent zmieniamy próg przy binaryzacji
    private int[][] image; // wycinek obrazu, który będzie przetwarzany
    private int[] threshold;
    private Scanner scanner;
    private Decoder d2;
    int counterR = 0;

    public Reader() {
        d2 = new Decoder();
        scanner = new Scanner();
    }

    /**
     * odczytuje kod kreskowy z obrazu
     *
     * @param img    cały obraz w odcieniach szarości
     * @param x      współrzędna x obszaru kodu kreskowego
     * @param y      współrzędna y obszaru kodu kreskowego
     * @param width  szerokość kodu kreskowego
     * @param height wysokość kodu kreskowego
     * @return odczytany kod kreskowy
     */
    public String read(int[][] img, int x, int y, int width, int height) {
        prepareImage(img, x, y, width, height);

        int[][] out = new int[height][width];
        for (int i = 0; i < height; i++) { // ze względu na błędy zaokrągleń zakres nie obejmuje ostatnich pixeli
            Arrays.fill(out[i], 255);
        }

        computeThreshold();
        threshold(image, out, threshold);

        String result;
        result = scanner.scan(out);
        if (result == null) {
            int th[] = new int[INTERVALS];

            // List<String>cum = new ArrayList<>(); // zbiera rozwiązania dla wszystkich zakresów - na potrzeby testów potem usunąć
            for (double d = 0.02; d <= 0.2; d += THRES_STEP) { //wersja z naprzemiennym sprawdzaniem // for (double d = 0.2; d >= -0.2; d -= 0.02) {
                for (int m = 0; m < 2; m++) {
                    Arrays.fill(th, 0);
                    for (int i = 0; i < INTERVALS; i++) {
                        if (m % 2 == 0) {
                            th[i] = (int) (threshold[i] - threshold[i] * d);
                        } else {
                            th[i] = (int) (threshold[i] + threshold[i] * d);
                        }
                    }
                    // wyznaczenie nowego obrazu
                    threshold(image, out, th);

                    result = scanner.scan(out);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } else {
            return result;
        }
        return null;
    }

    private void threshold(int[][] image, int[][] out, int[] thres) {
        int interval = (int) Math.ceil(image[0].length * 1.0 / INTERVALS);

        for (int i = 0; i < thres.length; i++) {
            for (int j = 0; j < image.length; j++) {
                for (int k = i * interval; k < image[0].length && k < (i + 1) * interval; k++) {
                    if (image[j][k] > thres[i]) {
                        out[j][k] = 255;
                    } else {
                        out[j][k] = 0;
                    }
                }
            }
        }
    }

    private void computeThreshold() {
        // tablica progów - podział na różne rozmiary domyślnie 10
        threshold = new int[INTERVALS];
        int min[] = new int[INTERVALS], max[] = new int[INTERVALS];

        Arrays.fill(min, 300);
        Arrays.fill(max, 0);
        int interval = (int) Math.ceil(image[0].length * 1.0 / INTERVALS);

        for (int i = 0; i < threshold.length; i++) {
            for (int j = 0; j < image.length; j++) {
                for (int k = i * interval; k < image[0].length && k < (i + 1) * interval; k++) {
                    if (image[j][k] > max[i]) {
                        max[i] = image[j][k];
                    }
                    if (image[j][k] < min[i]) {
                        min[i] = image[j][k];
                    }
                }
            }
        }

        for (int i = 0; i < INTERVALS; i++) {
            threshold[i] = (min[i] + max[i]) / 2;
        }
        // wyznacznie nowego progu na podstawie poprzednio obliczonych wartości
        long suma = 0, sumb = 0;
        long licza = 0, liczb = 0;

        for (int i = 0; i < threshold.length; i++) {
            for (int j = 0; j < image.length; j++) {
                for (int k = i * interval; k < image[0].length && k < (i + 1) * interval; k++) {
                    if (image[j][k] < threshold[i]) {
                        suma += image[j][k];
                        licza++;
                    } else {
                        sumb += image[j][k];
                        liczb++;
                    }
                }
            }
            if (licza == 0) licza = 1;
            if (liczb == 0) liczb = 1;
            threshold[i] = (int) (((suma / licza * 1.0) + (sumb / liczb * 1.0)) / 2); // nowa wartość progu
            suma = sumb = licza = liczb = 0;
        }
    }

    /**
     * przycina obraz do odpowiednich rozmiarów - można wykorzystać funkcje
     * biblioteczne do operowania na obrazach!!
     *
     * @param img    cały obraz w odcieniach szarości
     * @param x      współrzędna x obszaru kodu kreskowego
     * @param y      współrzędna y obszaru kodu kreskowego
     * @param width  szerokość kodu kreskowego
     * @param height wysokość kodu kreskowego
     */
    private void prepareImage(int[][] img, int x, int y, int width, int height) {
        image = new int[height][width];
        for (int i = y; i < height + y; i++) {
            for (int j = x; j < width + x; j++) {
                image[i - y][j - x] = img[i][j];
            }
        }
    }
}
