package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.decoder;

import java.util.Arrays;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.Contrast;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.MISbinarization;

/**
 * odpowiada za odczyt kodu kreskowego z przekazanego zdjęcia - zdjęcie w
 * odcieniach szarości
 *
 * @author Mateusz
 */
public class Reader2DThreshold {
    private static int INTERVALS = 7; // liczba przedziałów, na jaki dzielimy obraz podczas obliczania progów
    private static final int INTERVALS_V = 4; //2
    private static final double THRES_STEP = 0.03; // co ile procent zmieniamy próg przy binaryzacji
    private int[][] image; // wycinek obrazu, który będzie przetwarzany
    int threshold[][];
    Scanner scanner;
    int counterR = 0;

    public Reader2DThreshold() {
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
        String terss = ""; // na podtrzeby logów
        for (int i = 0; i < INTERVALS_V; i++) terss += Arrays.toString(threshold[i]) + "\n";
        // System.out.println("progi "+terss);
        result = scanner.scan(out);
        if (result == null) {
            int th[][] = new int[INTERVALS_V][INTERVALS];

            // List<String>cum = new ArrayList<>(); // zbiera rozwiązania dla wszystkich zakresów - na potrzeby testów potem usunąć
            for (double d = 0.02; d <= 0.1; d += THRES_STEP) { //wersja z naprzemiennym sprawdzaniem // for (double d = 0.2; d >= -0.2; d -= 0.02) {
                for (int m = 0; m < 2; m++) {
                    for (int i = 0; i < INTERVALS_V; i++) {
                        Arrays.fill(th[i], 0);
                    }
                    for (int i = 0; i < INTERVALS; i++) {
                        for (int j = 0; j < INTERVALS_V; j++) {
                            if (m % 2 == 0) {
                                th[j][i] = (int) (threshold[j][i] - threshold[j][i] * d);
                            } else {
                                th[j][i] = (int) (threshold[j][i] + threshold[j][i] * d);
                            }
                        }
                    }
                    // wyznaczenie nowego obrazu
                    threshold(image, out, th);
                    result = scanner.scan(out);
                    if (result != null) {
                        return result;
                    } else {
                        terss = ""; // na podtrzeby logów
                        for (int i = 0; i < INTERVALS_V; i++)
                            terss += Arrays.toString(th[i]) + "\n";
                    }
                }
            }
        } else {
            return result;
        }
        return null;
    }

    private void computeThreshold() {
        // tablica progów - podział na różne rozmiary domyślnie 10
        threshold = new int[INTERVALS_V][INTERVALS];

        int[] histogram = new int[256];

        int intervalH = (int) Math.ceil(image[0].length * 1.0 / INTERVALS);
        int intervalV = (int) Math.ceil(image.length * 1.0 / INTERVALS_V);
        // wyznaczenie wartości minimalnych i maksymalnych w przedziałach
        for (int i = 0; i < INTERVALS_V; i++) {
            for (int j = 0; j < INTERVALS; j++) {
                int counter = 0;
                for (int k = i * intervalV; k < (i + 1) * intervalV && k < image.length; k++) {
                    for (int l = j * intervalH; l < (j + 1) * intervalH && l < image[0].length; l++) {
                        histogram[image[k][l]]++;
                        counter++;
                    }
                }
                if (counter > 0) {
                    threshold[i][j] = MISbinarization.computeThreshold(histogram);
                    threshold[i][j] += threshold[i][j] * 0.1;
                }
                Arrays.fill(histogram, 0);
            }
        }
    }

    private void threshold(int[][] image, int[][] out, int[][] threshold) {
        int intervalH = (int) Math.ceil(image[0].length * 1.0 / threshold[0].length);
        int intervalV = (int) Math.ceil(image.length * 1.0 / threshold.length);

        for (int i = 0; i < threshold.length; i++) {
            for (int j = 0; j < threshold[0].length; j++) {
                for (int k = i * intervalV; k < (i + 1) * intervalV && k < image.length; k++) {
                    for (int l = j * intervalH; l < (j + 1) * intervalH && l < image[0].length; l++) {
                        if (image[k][l] > threshold[i][j]) {
                            out[k][l] = 255;
                        } else {
                            out[k][l] = 0;
                        }
                    }
                }
            }
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
        image = Contrast.changeContrast(image, 100);
    }
}
