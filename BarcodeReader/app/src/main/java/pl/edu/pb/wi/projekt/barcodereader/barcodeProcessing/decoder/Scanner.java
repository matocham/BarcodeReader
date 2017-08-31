package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.decoder;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private static final int STEP = 5; // co ile pikseli szukamy kodu kreskowego
    public static final double ADDITIVE_B = 0.5;// 0.3 0.4 0.45
    public static final double ADDITIVE_W = 0.5;// 0.3 0.4 0.45
    public static final double ADDITIVE_B_DIV = 0.4;// 0.4 0.45
    public static final double ADDITIVE_W_DIV = 0.6;// 0.5 0.55
    private IDecoder decoder;

    public Scanner() {
        decoder = new EAN13Decoder();
    }

    /**
     * odczytuje kod kreskowy z obrazu
     *
     * @param img obraz z kodem kreskowym
     * @return odczytany kond kreskowy
     */
    String scan(int[][] img) {
        // skanowanie zaczynamy od środka kodu kreskowego, w przypadku niepowodzeni wykonujemy skanowanie w pętli raz p pixel do góry raz do dołu
        // skanowanie obrazu i uzupełnienie listy, tablicy
        int center = img.length / 2;
        String strip;

        String result = null;
        strip = getStrip(img[center]);
        result = decoder.decode(strip);

        if (result == null) { // gdy nie zadziała pierwsze skanowanie
            for (int i = 1; i < img.length / 2 - 1; i += STEP) { // sprawdzamy wszystkie pixele do góry i do dołu

                // skanowanie poniżej
                strip = getStrip(img[center + i]);
                result = decoder.decode(strip);
                if (result != null) {
                    break;
                }

                // skanowanie powyżej
                strip = getStrip(img[center - i]);
                result = decoder.decode(strip);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    private Integer[] scanStrip(int[] img) {
        List<Integer> strip = new ArrayList<>();
        boolean black = true;
        int pos = 0, counter = 0;
        while (pos < img.length && img[pos] != 0) {
            pos++;
        }
        while (pos < img.length) {
            if (img[pos] == 0 && black || img[pos] == 255 && !black) { // gdy ciągle skanujemy ten sam kolor to podbijamy licznik
                counter++;
            } else { // w innym wypadku wstawiamy licznik do tablicy i ustawiamy go na 1
                strip.add(counter);
                counter = 1;
                black = !black;
            }
            pos++;
        }
        // przejście na tablicę - łatwiejsze operowanie zmiennymi
        Integer[] values = strip.toArray(new Integer[0]);
        double bmax = 0, wmax = 0;
        for (int i = 0; i < values.length; i++) {
            if (i % 2 == 1 && wmax < values[i]) {
                wmax = values[i];
            } else if (i % 2 == 0 && bmax < values[i]) {
                bmax = values[i];
            }
        }
        bmax =  (bmax+ADDITIVE_B) / 4.0; // przeliczenie szerkości pojedynczego paska
        wmax =  (wmax+ADDITIVE_W) / 4.0; // czarne sprawdzają się lepiej podczas ustalania szerkości - białe są zazwyczaj za szerokie( za niski próg?)
        double div;
        for (int i = 0; i < values.length; i++) {
            if (i % 2 == 1) { //białe
                div = (values[i]) / wmax;
                div+=ADDITIVE_B_DIV;
            } else { // czarne
                div = (values[i]) / bmax;
                div+=ADDITIVE_W_DIV;
            }

            if (div < 1) {
                if(div<0.5){
                    return new Integer[0];
                }
                div=1;
            } else if (div > 4.99) {
                return new Integer[0];
            }

            values[i] = (int) (div);
        }
        return values;
    }

    private String getStrip(int[] img) {
        StringBuilder sb = new StringBuilder();
        Integer[] strip = scanStrip(img);
        for (Integer ii : strip) {
            sb.append(ii);
        }
        return sb.toString();
    }

    IDecoder getDecoder() {
        return decoder;
    }

    void setDecoder(IDecoder decoder) {
        this.decoder = decoder;
    }
}
