package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.decoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import android.util.Log;

public class DecoderCorrect extends Decoder {
    HashSet<String> blackPos;
    HashSet<String> whitePos;

    @Override
    protected List<Item> getAlternatives(String base, Map<String, Item> set) {
        blackPos = new HashSet<>();
        whitePos = new HashSet<>();
        if (base.length() == 7) {
            ArrayList<Item> items = new ArrayList<>();
            items.add(set.get("2331112"));
            return items;
        }
        int[] white = new int[3], black = new int[3];
        for (int i = 0; i < base.length(); i += 2) {
            black[i / 2] = Integer.parseInt(String.valueOf(base.charAt(i)));
            white[i / 2] = Integer.parseInt(String.valueOf(base.charAt(i + 1)));
        }
        int blackSum = 0, whiteSum = 0, sum;
        for (int i = 0; i < black.length; i++) {
            blackSum += black[i];
            whiteSum += white[i];
        }
        sum = blackSum + whiteSum;

        if (blackSum % 2 == 0 && whiteSum % 2 == 1) { // oba dobrze
            if (sum > 11) {
                substract(white, true, 1);
                substract(white, true, 2);
                substract(black, false, 1);
                substract(black, false, 2);
            } else if (sum < 11) {
                add(white, true, 1);
                add(white, true, 2);
                add(black, false, 1);
                add(black, false, 2);
            }
        } else if (blackSum % 2 == 0 && whiteSum % 2 == 0) { // czarne dobrze białe źle - powinny być nieparzyste
            if (sum > 11) {
                int amount = sum - 11;
                if (amount % 2 == 1) { // musi być nieparzyste
                    simpleSubstract(white, true, amount);
                    substract(black, false, amount / 2);
                    simpleSubstract(white, true, amount % 2);
                }
            } else if (sum < 11) {
                int amount = 11 - sum;
                if (amount % 2 == 1) { // musi być nieparzyste
                    simpleAdd(white, true, amount);
                    simpleAdd(white, true, amount % 2);
                    add(black, false, amount / 2);
                }
            }
        } else if (whiteSum % 2 == 1 && blackSum % 2 == 1) { // białe dobrze czarne źle - powinny być parzyste
            if (sum > 11) {
                int amount = sum - 11;
                if (amount % 2 == 1) { // musi być nieparzyste
                    simpleSubstract(black, false, amount);
                    substract(white, true, amount / 2);
                    simpleSubstract(black, false, amount % 2);
                }
            } else if (sum < 11) {
                int amount = 11 - sum;
                if (amount % 2 == 1) {
                    simpleAdd(black, false, amount);
                    simpleAdd(black, false, amount % 2);
                    add(white, true, amount / 2);
                }
            }
        } else { // oba źle
            if (sum > 11) {
                int amount = sum - 11;
                if (amount == 2) { // musi być nieparzyste
                    simpleSubstract(black, false, 1);
                    simpleSubstract(white, true, 1);
                } else if (amount == 4) {
                    simpleSubstract(black, false, 1);
                    simpleSubstract(white, true, 3);
                    simpleSubstract(black, false, 3);
                    simpleSubstract(white, true, 1);
                }
            } else if (sum < 11) {
                int amount = 11 - sum;
                if (amount == 2) { // musi być nieparzyste
                    simpleAdd(black, false, 1);
                    simpleAdd(white, true, 1);
                } else if (amount == 4) {
                    simpleAdd(black, false, 1);
                    simpleAdd(white, true, 3);
                    simpleAdd(black, false, 3);
                    simpleAdd(white, true, 1);
                }
            } else {
                simpleAdd(black, false, 1);
                simpleAdd(white, true, 1);
                simpleSubstract(black, false, 1);
                simpleSubstract(white, true, 1);
            }
        }

        HashSet<Item> items = new HashSet<>();
        StringBuilder sb;
        for (String w : whitePos) {
            for (String b : blackPos) {
                String ww[] = w.split("");
                String bb[] = b.split("");
                sb = new StringBuilder();
                sum = 0;
                for (int i = 0; i < ww.length; i++) {
                    if(bb[i].length()>0){
                        sum += Integer.valueOf(bb[i]);
                        sb.append(bb[i]);
                    }
                    if(ww[i].length()>0){
                        sum += Integer.valueOf(ww[i]);
                        sb.append(ww[i]);
                    }
                }
                if (sum == 11 && set.get(sb.toString()) != null) {
                    items.add(set.get(sb.toString()));
                }
            }
        }
        if (items.size() <= MAX_ALTER) {
            return new ArrayList<Item>(items);
        }
        return new ArrayList<Item>();
    }

    private void add(int[] values, boolean white, int amount) {
        int[] copy = new int[values.length];// 3 12 13 23 6 sposobów
        int sum = 0;
        for (int i = 0; i < values.length; i++) { // 0 1 2
            for (int j = i; j < values.length; j++) { //00 01 02 | 11 12 | 22
                for (int k = 0; k < values.length; k++) {
                    copy[k] = values[k];
                }
                sum = 0;

                if (i == j && copy[i] < 3) {
                    copy[i] += 2;
                } else if (copy[i] < 4 && copy[j] < 4) {
                    copy[i]++;
                    copy[j]++;
                }

                for (int k = 0; k < values.length; k++) {
                    sum += copy[k];
                }

                if (white && sum < 8 || !white && sum < 9) {
                    if (amount > 1) {
                        add(copy, white, amount - 1);
                    } else if (white) {
                        whitePos.add(getString(copy));
                    } else {
                        blackPos.add(getString(copy));
                    }
                }
            }
        }
    }

    private void substract(int[] values, boolean white, int amount) {
        int[] copy = new int[values.length];// 3 12 13 23 6 sposobów
        int sum = 0;

        for (int i = 0; i < values.length; i++) { // 00 01 02 11 12 22
            for (int j = i; j < values.length; j++) {
                for (int k = 0; k < values.length; k++) {
                    copy[k] = values[k];
                }
                sum = 0;

                if (i == j && copy[i] > 2) {
                    copy[i] -= 2;
                } else if (copy[i] > 1 && copy[j] > 1) {
                    copy[i]--;
                    copy[j]--;
                }

                for (int k = 0; k < values.length; k++) {
                    sum += copy[k];
                }

                if (white && sum > 2 || !white && sum > 3) {
                    if (amount > 1) {
                        substract(copy, white, amount - 1);
                    } else if (white) {
                        whitePos.add(getString(copy));
                    } else {
                        blackPos.add(getString(copy));
                    }
                }
            }
        }
    }

    private void simpleAdd(int[] values, boolean white, int amount) {
        int[] copy = new int[values.length];// 3 12 13 23 6 sposobów
        int sum = 0;
        for (int i = 0; i < values.length; i++) { // 00 01 02 11 12 22
            for (int k = 0; k < values.length; k++) {
                copy[k] = values[k];
            }
            sum = 0;

            if (copy[i] < 4) {
                copy[i]++;

                for (int k = 0; k < values.length; k++) {
                    sum += copy[k];
                }

                if (white && sum < 8 || !white && sum < 9) {
                    if (amount > 1) {
                        simpleAdd(copy, white, amount - 1);
                    } else if (white) {
                        whitePos.add(getString(copy));
                    } else {
                        blackPos.add(getString(copy));
                    }
                }
            }
        }
    }

    private void simpleSubstract(int[] values, boolean white, int amount) {
        int[] copy = new int[values.length];// 3 12 13 23 6 sposobów
        int sum = 0;

        for (int i = 0; i < values.length; i++) { // 00 01 02 11 12 22
            for (int k = 0; k < values.length; k++) {
                copy[k] = values[k];
            }
            sum = 0;

            if (copy[i] > 1) {
                copy[i]--;

                for (int k = 0; k < values.length; k++) {
                    sum += copy[k];
                }

                if (white && sum > 2 || !white && sum > 3) {
                    if (amount > 1) {
                        simpleSubstract(copy, white, amount - 1);
                    } else if (white) {
                        whitePos.add(getString(copy));
                    } else {
                        blackPos.add(getString(copy));
                    }
                }
            }
        }
    }

    private String getString(int[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
        }
        return sb.toString();
    }
}
