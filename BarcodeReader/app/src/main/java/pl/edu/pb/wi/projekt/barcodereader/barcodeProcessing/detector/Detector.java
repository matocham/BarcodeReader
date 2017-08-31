package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.detector;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.decoder.Reader2DThreshold;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.EdgeDetector;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.Histogram;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.LinesEditor;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.MISbinarization;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.MorphologyFilters;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.Sharpen;

public class Detector extends Thread {
    Reader2DThreshold rd = new Reader2DThreshold();

    private final int STRIP_COUNT = 16; // najkrótszy możliwy kod - dla EAN5 16 - dla code 128 można spokojnie dać ponad 25
    private final int THRESHOLD = 30; // jezeli kolor < THESHOLD to jest czarny
    private final int VERTICAL_FILTER_SIZE = 15;
    private final int SCALEX = 18; // do wykrywania na podstawie gęstości
    private final int SCALEY = 8; // do wykrywania na podstawie gęstości
    private final int MAX_STRIP_DISTANCE = 25; // do usuwania zbędnych pasków
    private final int STRIP_SPACING = 5; // odstęp pomiędzy skanowanymi wierszami
    private final double SCALE_FACTOR = 1.4; // o ile powiększamy maksymalną dopuszczalną odległość pomiędzy elementami
    private final double BARCODE_MIN_WIDTH_SCALE = 0.6; // przy usuwaniu źle dopasowanych kodów
    private final int SCAN_HEIGHT = 30; // wysokość skanowanego paska na czerwonej linii

    /**
     * Zakładamy, ze obraz jest w odcieniach szarości
     *
     * @param img
     * @return
     */
    ArrayList<Barcode> detect(int[][] img) {
        ArrayList<Integer> rows = new ArrayList<>(); // przechowuje potencjalne wiersze z kodem kreskowym

        if (DetectorThread.DEBUG) {
            saveDebugImage(img);
        }

        int[][] out = Sharpen.sharpen(img, new int[][]{{-1, -1, -1},
                {-1, 12, -1},
                {-1, -1, -1}});
        out = EdgeDetector.edgeDetectionSubstract(out,
                new int[][]{{3, 10, 3}, // Y 0 stopni <- zero to pionowe linie
                        {0, 0, 0},
                        {-3, -10, -3}},
                new int[][]{{-3, 0, 3}, // X 90 stopni
                        {-10, 0, 10},
                        {-3, 0, 3}},
                THRESHOLD);
        // otwarcie
        out = MorphologyFilters.erodeVertical(out, VERTICAL_FILTER_SIZE);
        out = MorphologyFilters.dilateVertical(out, VERTICAL_FILTER_SIZE);
        /*
        //możliwe zastąpienie  powyższego wykrywania za pomocą binaryzacji
        //znacząco szybsze, nieznacznie gorsze ale bardziej podatne na błędy
        int thr = MISbinarization.computeThreshold(Histogram.getHistogram(img));
        thr*=1.15;
        int[][] out = new int[img.length][img[0].length];
        for(int i=0;i<img.length;i++){
            for(int j=0;j< img[0].length;j++){
                if(img[i][j]<thr) out[i][j]=255;
                else out[i][j]=0;
            }
        }*/

        out = LinesEditor.removeSingleLines(out, MAX_STRIP_DISTANCE);
        if (DetectorThread.DEBUG) {
            saveImageAfterRemove(img, out);
        }

        int counter;
        for (int i = 0; i < out.length; i += STRIP_SPACING) {
            counter = stripCount(out[i], 255);
            if (counter >= STRIP_COUNT) {
                rows.add(i);
            }
        }
        ArrayList<Range> ranges = LowResolutionSearch.lowResolutionSearch(out, SCALEX, SCALEY, rows); // dla każdego pos powinno zwracać listę zakresów
        Collections.sort(ranges);
        ArrayList<Range> filtered = filter(out, ranges, SCALE_FACTOR, STRIP_COUNT);
        ArrayList<Barcode> bars = findBarcode(out, filtered);
        bars = selectBarcode(bars, BARCODE_MIN_WIDTH_SCALE);

        return bars;
    }

    private void saveImageAfterRemove(int[][] img, int[][] out) {
        FileOutputStream outf = null;
        try {
            outf = new FileOutputStream(Utils.getOutputImageFile().getAbsolutePath() + new Random().nextInt());
            Bitmap bmp = Bitmap.createBitmap(img[0].length, img.length, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < img.length; i++) {
                for (int j = 0; j < img[0].length; j++) {
                    bmp.setPixel(j, i, Color.rgb(img[i][j], img[i][j], img[i][j]));
                }
            }
            for (int i = 0; i < out.length; i++) {
                for (int j = 0; j < out[0].length; j++) {
                    bmp.setPixel(j, i, Color.rgb(out[i][j], out[i][j], out[i][j]));
                }
            }
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outf != null) {
                    outf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveDebugImage(int[][] img) {
        FileOutputStream outf = null;
        try {
            outf = new FileOutputStream(Utils.getOutputImageFile().getAbsolutePath() + new Random().nextInt());
            Bitmap bmp = Bitmap.createBitmap(img[0].length, img.length, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < img.length; i++) {
                for (int j = 0; j < img[0].length; j++) {
                    bmp.setPixel(j, i, Color.rgb(img[i][j], img[i][j], img[i][j]));
                }
            }
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outf); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outf != null) {
                    outf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Barcode> detectFromCut(int[][] img, int cutLine) {
        int offset = cutLine - SCAN_HEIGHT / 2;
        if (cutLine < SCAN_HEIGHT / 2) offset += SCAN_HEIGHT / 2;
        int[][] newImage = new int[SCAN_HEIGHT][img[0].length];
        for (int i = offset; i < offset + SCAN_HEIGHT; i++) {
            for (int j = 0; j < img[0].length; j++) {
                newImage[i - offset][j] = img[i][j];
            }
        }
        if (DetectorThread.DEBUG) {
            saveDebugImage(img);
        }

        ArrayList<Barcode> bars = detect(newImage);
        for (Barcode bar : bars) {
            bar.y += offset;
        }
        return bars;
    }

    /**
     * wybiera reprezentacje fizycznych kodów kreskowych
     *
     * @param bars        kody znalezione we wcześniejszych etapach
     * @param scaleFactor o ile zmniejszamy najdłuższy pasek aby wyeliminować za krótkie
     * @return po jednym kodzie kreskowym na fizyczny kod
     */
    private ArrayList<Barcode> selectBarcode(ArrayList<Barcode> bars, double scaleFactor) {

        ArrayList<ArrayList<Barcode>> codes = new ArrayList<>(); // kody przydzielone do fizycznych kodów kreskowych
        ArrayList<Barcode> result = new ArrayList<>();

        if (!bars.isEmpty()) {
            Collections.sort(bars); // posortowane od góry do dołu i od lewej do prawej
            Barcode bar = bars.get(0);
            ArrayList<Barcode> tempBars = new ArrayList<>();

            for (Barcode barcode : bars) { // ponieważ na początku dokonujemy porównania kodu z samym sobą to zawsze coś zostanie dodane do zbioru rozwiązań
                if (bar.overlaps(barcode)) {
                    tempBars.add(barcode);
                } else { // gdy kody się nie pokrywają
                    codes.add(tempBars); // dodajemy już uzyskane rozwiązanie do zbioru rozwiązań
                    tempBars = new ArrayList<Barcode>(); // tworzymy nowy zbiór na nowy kod kreskowy
                    bar = barcode; // zapamiętujemy nowy wzór
                    tempBars.add(barcode); // dodajemy do nowego zbioru wzór
                }
            }
            codes.add(tempBars);

            //analiza dla każdego kodu kreskowego
            tempBars = new ArrayList<>();
            for (ArrayList<Barcode> array : codes) {
                tempBars.clear();
                int max = 0;
                // znalezienie maksimum
                for (Barcode barcode : array) {
                    if (barcode.width > max) {
                        max = barcode.width;
                    }
                }
                max = (int) Math.round(max * scaleFactor);

                // wyeliminowanie za krótkich kodów
                for (Barcode barcode : array) {
                    if (barcode.width >= max) {
                        tempBars.add(barcode);
                    }
                }

                // obliczenie średniego rozmiaru
                double meanValue = 0;
                for (Barcode barcode : tempBars) {
                    meanValue += barcode.width;
                }

                meanValue /= tempBars.size();
                meanValue = Math.round(meanValue * 1.1);
                int spacing = 999;
                Barcode temp = null;

                // wybranie kodu - alternatywnie bierzemy pierwszy z góry też powinien się sprawdzić
                for (Barcode barcode : tempBars) {
                    if (Math.abs(barcode.width - meanValue) <= spacing) {
                        if (barcode.width - meanValue == spacing) { // w ten sposób pomijamy elementy o takiej samej odległości ale mniejsze od średniej
                            // nic
                        } else if (Math.abs(barcode.width - meanValue) < spacing) {
                            spacing = (int) Math.round(Math.abs(barcode.width - meanValue));
                        }
                        temp = barcode;
                    }
                }
                result.add(temp);
            }
        }
        return result;
    }

    /**
     * wyszukuje kody kreskowe na podstawie dostarczonych zakresów
     *
     * @param image  przetwarzany obraz
     * @param ranges zakresy do analizy
     * @return lista proponowanych lokalizacji kodów kreskowych
     */
    private ArrayList<Barcode> findBarcode(int[][] image, ArrayList<Range> ranges) {
        int min = 999, max = 0; // wspólne dla wszystkich pasków w kodzie
        int space;
        ArrayList<Barcode> bars = new ArrayList<>();
        for (Range r : ranges) {
            int j = r.start;
            min = 999;
            max = 0; // dla każdego range jest oddzielne
            Barcode bar = new Barcode();
            while (j <= r.end) { // przeglądamy zakres w poszukiwaniu białych pasków
                if (image[r.row][j] == 255) {
                    int k = r.row;
                    int l = j;
                    while (k > 0 && image[k][l] == 255) {
                        if (image[k - 1][l] != 255) {
                            if (l > 0 && image[k - 1][l - 1] == 255) { // w razie potrzeby idziemy w lewo lub w prawo gdy paski są po skosie
                                l--;
                            } else if (image[k - 1][l + 1] == 255) {
                                l++;
                            }
                        }
                        k--;
                    }
                    if (k < min) { // jeżeli dotarliśmy wyżej niż jest minimum to zapisujemy nowy wynik
                        min = k;
                    }

                    k = r.row;
                    l = j;

                    while (k < image.length - 1 && image[k][l] == 255) { // w dół
                        if (image[k + 1][l] != 255) {
                            if (l > 0 && image[k + 1][l - 1] == 255) { // w razie potrzeby idziemy w lewo lub w prawo gdy paski są po skosie
                                l--;
                            } else if (image[k + 1][l + 1] == 255) {
                                l++;
                            }
                        }
                        k++;
                    }
                    if (k > max) { // jeżeli dotarliśmy wyżej niż jest minimum to zapisujemy nowy wynik
                        max = k;
                    }
                    while (j + 1 <= r.end && image[r.row][j + 1] == 255) {
                        j++;
                    }
                }
                j++;
            }
            if (min != max) { // może bez sensu ale warto zapisać
                space = getMaxSpace(image[r.row], 0, r.start, r.end);
                //space = getMeanSpace(image[r.row], 0, r.start, r.end);
                if (space > 15) space = 10; // zabezpieczenie przed nieprawidłowo położonymi paskami
                bar.x = r.start - space;
                bar.y = min;
                bar.width = r.end - r.start + 2 * space;
                bar.height = max - min;
                bar.base = r.row;

                if (bar.x < 0) bar.x = 0;
                if (bar.y < 0) bar.y = 0;
                if (bar.x + bar.width >= image[0].length) bar.width = image[0].length - 1 - bar.x;
                if (bar.y + bar.height >= image.length) bar.width = image.length - 1 - bar.y;
                bars.add(bar);
            }
        }
        return bars;
    }

    /**
     * filtruje wykryte obszary, tak aby uwzględnić wszystkie paski
     *
     * @param image         przetwarzany obraz
     * @param ranges        obliczone zakresy
     * @param scaleFactor   współczynnik skalowania przy rozszerzaniu zakresu
     * @param minStripCount minimalna ilość pasków, aby zostały uznane za kod
     * @return nowe zakresy dopasowane do kodów
     */
    private ArrayList<Range> filter(int[][] image, final ArrayList<Range> ranges, double scaleFactor, int minStripCount) {
        ArrayList<Range> filtred = new ArrayList<>();
        // ustalenie nowych zakresów
        int start, end;
        int max = 0, counter;
        for (Range r : ranges) {
            start = -1;
            max = 0;
            if (image[r.row][r.end] == 255) {
                while (r.end < image[0].length && image[r.row][r.end] == 255) { // tak aby end pokazywał na czarne
                    r.end++;
                }
            } else {
                while (r.end > 0 && image[r.row][r.end] == 0) {
                    r.end--;
                }
                r.end++; // piksel za ostatnim białym paskiem
            }
            if (image[r.row][r.start] == 255) {
                while (r.start > 0 && image[r.row][r.start] == 255) { // tak aby start pokazywał na czarne
                    r.start--;
                }
            } else {
                while (r.start < image[0].length && image[r.row][r.start] == 0) {
                    r.start++;
                }
                r.start--; // piksel przed pierwszym białym paskiem
            }
            if (stripCount(image[r.row], 255, r.start, r.end) < minStripCount)
                break; // jeżeli nie ma w zakresie odpowiedniej ilości pasków to nie bierzmy

            max = getMaxSpace(image[r.row], 0, r.start, r.end);

            // usuniecie ostatniego lub pierwszego elementu jeśli to on jest maksymalny
            while (removeFromEdges(image[r.row], r, max) == true) { // modyfikuje Range!
                max = getMaxSpace(image[r.row], 0, r.start, r.end);
            }
            max = (int) Math.round(max * scaleFactor); // bierzemy pod uwagę nawet większe elementy

            boolean expanded = false; // rozszerzamy zakres

            do {

                expanded = false;
                counter = 0;
                start = r.start;
                while (start >= 0 && image[r.row][start] == 0) {
                    start--;
                    counter++;
                }
                if (counter <= max && start >= 0) {
                    while (start >= 0 && image[r.row][start] == 255) {
                        start--;
                    }
                    if (start >= 0) {
                        r.start = start;
                        expanded = true;
                    }
                }

                counter = 0;
                end = r.end;
                while (end < image[0].length && image[r.row][end] == 0) {
                    end++;
                    counter++;
                }
                if (counter <= max && end < image[0].length) {
                    while (end < image[0].length && image[r.row][end] == 255) {
                        end++;
                    }
                    if (end < image[0].length) {
                        r.end = end;
                        expanded = true;
                    }
                }
            } while (expanded);
            if (stripCount(image[r.row], 255, r.start, r.end) < minStripCount) break;
            if (r.start < 0) r.start = 0;
            if (r.end > image[0].length - 1) r.end = image[0].length - 1;
            filtred.add(r);
        }
        return filtred;
    }

    /**
     * wyszukuje pasek o danym kolorze i maksymalnej szerokości
     *
     * @param row   wiersz do przetworzenia
     * @param color kolor paska
     * @param start pozycja początkowa
     * @param end   pozycja końcowa
     * @return pasek maksymalnej długości
     */
    private int getMaxSpace(int[] row, int color, int start, int end) {
        int counter;
        int max = 0;
        for (int j = start; j <= end && j < row.length; j++) {
            counter = 0;
            if (row[j] == color) {
                while (j <= end && row[j] == color) {
                    counter++;
                    j++;
                }
                if (counter > max) max = counter;
                j--;
            }
        }
        return max;
    }

    /**
     * wyszukuje pasek o danym kolorze i maksymalnej szerokości
     *
     * @param row   wiersz do przetworzenia
     * @param color kolor paska
     * @param start pozycja początkowa
     * @param end   pozycja końcowa
     * @return pasek maksymalnej długości
     */
    private int getMeanSpace(int[] row, int color, int start, int end) {
        int counter;
        int sum = 0;
        int spaceCount = 0;
        for (int j = start; j <= end; j++) {
            counter = 0;
            if (row[j] == color) {
                while (j <= end && row[j] == color) {
                    counter++;
                    j++;
                }
                sum += counter;
                spaceCount++;
                j--;
            }
        }
        return sum / spaceCount;
    }

    /**
     * usuwa elementy z brzegu, które mają maksymalne wartości
     *
     * @param row   wiersz, który analizujemy
     * @param r     analizowany zakres (jest modyfikowany w wyniku działania funkcji)
     * @param width maksymalna szerokość elementu
     * @return czy usunięto elementy
     */
    private boolean removeFromEdges(int[] row, final Range r, int width) {
        int counter;
        int start = r.start + 1;
        int end = r.end - 1;
        boolean removed = false;

        while (row[start] == 255 && start<row.length) { // ustawiamy się na czarnych
            start++;
        }
        while (row[end] == 255 && end>=0) { // ustawiamy się na czarnych
            end--;
        }

        counter = 0;
        while (start < row.length && row[start] == 0) {
            counter++; // zliczamy czarne
            start++;
        }

        if (counter >= width) { // jeżeli jest za dużo to pomijamy ten element
            r.start = start - 1;
            removed = true;
        }

        counter = 0;
        while (row[end] == 0 && end > 0) {
            counter++; // zliczamy czarne
            end--;
        }

        if (counter >= width) { // jeżeli jest za dużo to pomijamy ten element
            r.end = end + 1;
            removed = true;
        }
        return removed;
    }

    /**
     * oblicza ilość białych pasków w danym rzędzie
     *
     * @param row   rząd pikseli do sprawdzenia
     * @param color kolor paska
     * @return ilość białych pasków
     */
    private int stripCount(int[] row, int color) {
        int counter = 0;

        counter = 0;
        for (int j = 0; j < row.length; j++) {
            if (row[j] == color) {
                while (j < row.length && row[j] == color) {
                    j++;
                } // przechodzimy przez cały biały pasek
                j--;
                counter++;
            }
        }
        return counter;
    }

    /**
     * oblicza ilość białych pasków w danym rzędzie
     *
     * @param row   rząd pikseli do sprawdzenia
     * @param color kolor paska
     * @param from  początek przedziału
     * @param to    koniec przedziału
     * @return ilość białych pasków
     */
    private int stripCount(int[] row, int color, int from, int to) {
        int counter = 0;

        counter = 0;
        for (int j = from; j <= to && j < row.length; j++) {
            if (row[j] == color) {
                while (j < row.length && row[j] == color) {
                    j++;
                } // przechodzimy przez cały biały pasek
                j--;
                counter++;
            }
        }
        return counter;
    }
}

