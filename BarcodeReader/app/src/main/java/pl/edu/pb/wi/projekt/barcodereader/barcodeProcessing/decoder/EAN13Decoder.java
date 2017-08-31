/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.decoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mateusz
 */
class EAN13Decoder implements IDecoder {
    private static int MAX_ALTER = 6;
    private static int MIN_DISTANCE = 3;
    //zbiory znaków dla wszystkich kodowań
    private Map<String, Integer> setA, setB, setC, parity, sentinel;
    // aktualnie przetwarzany zbiór
    private StringBuilder result;
    private List<Result> results = new ArrayList<>();

    public EAN13Decoder() {
        setA = new HashMap<>();
        setB = new HashMap<>();
        setC = new HashMap<>();
        parity = new HashMap<>();
        sentinel = new HashMap<>();

        //************ INICJOWANIE STRUKTUR ***********************

        setA.put("3211", 0);
        setB.put("1123", 0);
        setC.put("3211", 0);
        setA.put("2221", 1);
        setB.put("1222", 1);
        setC.put("2221", 1);
        setA.put("2122", 2);
        setB.put("2212", 2);
        setC.put("2122", 2);
        setA.put("1411", 3);
        setB.put("1141", 3);
        setC.put("1411", 3);
        setA.put("1132", 4);
        setB.put("2311", 4);
        setC.put("1132", 4);
        setA.put("1231", 5);
        setB.put("1321", 5);
        setC.put("1231", 5);
        setA.put("1114", 6);
        setB.put("4111", 6);
        setC.put("1114", 6);
        setA.put("1312", 7);
        setB.put("2131", 7);
        setC.put("1312", 7);
        setA.put("1213", 8);
        setB.put("3121", 8);
        setC.put("1213", 8);
        setA.put("3112", 9);
        setB.put("2113", 9);
        setC.put("3112", 9);
        parity.put("NNNNNN", 0);
        parity.put("NNPNPP", 1);
        parity.put("NNPPNP", 2);
        parity.put("NNPPPN", 3);
        parity.put("NPNNPP", 4);
        parity.put("NPPNNP", 5);
        parity.put("NPPPNN", 6);
        parity.put("NPNPNP", 7);
        parity.put("NPNPPN", 8);
        parity.put("NPPNPN", 9);
        sentinel.put("111", 1); // prawy i lewy
        sentinel.put("11111", 2); // srodkowy
    }

    /**
     * przeprowadza dekodowanie ciągu (dla kazdego elementu parzysta liczba
     * czarnych - wziac pod uwage podczas odczytu ze zdjecia
     *
     * @param barcode szerokości kolejnych pasków z kodu kreskowego
     * @return zdekodowana wartość lub null gdy nie udało się zdekodować
     */
    private String decode(List<String> barcode) {

        //************** deklaracja i inicjowanie zmiennych *******************

        String element;
        result = new StringBuilder();
        List<Integer> items;
        List<Result> tempRes = new ArrayList<>();
        int control;
        results.clear();

        //*********** odczytanie pierwszego elementu ************************
        element = barcode.get(0); // pierwszy element to sentinel
        items = getItems(element, sentinel);
        if (items.isEmpty()) {
            return null;
        } else { // znaleziono strażnika - można kontynuować
            results.add(new Result("", ""));
            //**************** dekodowanie lewej strony ************************
            for (int i = 1; i < 7; i++) {
                element = barcode.get(i);
                tempRes.clear();
                for (Result res : results) {
                    // analiza dla nieparzystych
                    items = getItems(element, setA);
                    if (items.isEmpty()) {
                    } else {
                        for (Integer item : items) {
                            Result newRes = new Result(res.resultString, res.parity + "N"); // nowy ciąg wynikowy powstały po rozszerzeniu analizowanego ciągu o kolejny element
                            newRes.resultString += item;
                            tempRes.add(newRes);
                        }
                    }

                    // analiza dla parzystych
                    if (i != 1) { // dla 1 zawsze jest nieparzyste
                        items = getItems(element, setB);

                        if (!items.isEmpty()) {
                            for (Integer item : items) {
                                Result newRes = new Result(res.resultString, res.parity + "P"); // nowy ciąg wynikowy powstały po rozszerzeniu analizowanego ciągu o kolejny element
                                newRes.resultString += item;
                                tempRes.add(newRes);
                            }
                        }
                    }
                    results = new ArrayList<>(tempRes);
                }
            }

            // *************** dodanie brakującego elementu na podstawie parzystości *****************
            tempRes.clear();
            for (Result res : results) {
                items = getItems(res.parity, parity);
                if (!items.isEmpty()) { // jeżeli mamy jakieś wyniki to dodajemy do wyników
                    for (Integer i : items) {
                        Result newRes = new Result(i + res.resultString, res.parity);
                        tempRes.add(newRes);
                    }
                }
            }
            results = new ArrayList<>(tempRes);

            //**************** środkowy sentinel ************************
            // zawsze zadziała dla prawidłowo odczytanego ciągu
            element = barcode.get(7);
            if (!getItems(element, sentinel).contains(2)) {
                return null;
            }
            //**************** dekodowanie prawej strony ************************

            for (int i = 8; i < barcode.size() - 2; i++) {// lub <13
                element = barcode.get(i);
                tempRes.clear();
                for (Result res : results) {
                    items = getItems(element, setC);
                    if (!items.isEmpty()) {
                        for (Integer item : items) {
                            Result newRes = new Result(res.resultString, res.parity); // nowy ciąg wynikowy powstały po rozszerzeniu analizowanego ciągu o kolejny element
                            newRes.resultString += item;
                            tempRes.add(newRes);
                        }
                    }
                }
                results = new ArrayList<>(tempRes);
            }


            //**************** pobranie i sprawdzenie sumy kontrolnej ***********************
            element = barcode.get(barcode.size() - 2); // cyfra kontrolna kontrolna
            tempRes.clear(); // w tempRes przechowujemy elementy, które mają poprawną sumę kontrolną
            for (Result res : results) {
                // obliczenie sumy kontrolnej wyniku
                control = 0;
                for (int i = res.resultString.length() - 1; i >= 0; i -= 2) {
                    control += res.resultString.charAt(i) - 48;
                }
                control *= 3;
                for (int i = res.resultString.length() - 2; i >= 0; i -= 2) {
                    control += res.resultString.charAt(i) - 48;
                }
                // sprawdzenie poprawności sumy kontrolnej
                items = getItems(element, setC);
                if (!items.isEmpty()) { // dla każdego dobranego elementu trzeba sprawdzić sumę kontrolną
                    for (Integer item : items) { // dla każdego znaku kontrolnego
                        if ((control + item) % 10 == 0) {
                            res.resultString += item;
                            tempRes.add(res); // suma się zgadza - można dodać do rozwiązań
                            break; // tylko jedna suma będzie prawidłowa
                        }
                    }
                }
            }
            results = tempRes; // zostały tylko wyniki, dla których suma kontrolna się zgadza

            //****************** sprawdzenie poprawności znaku końca ciągu(element niepotrzebny) ********************

            element = barcode.get(barcode.size() - 1);
            items = getItems(element, sentinel); // znak końca jest taki sam dla każdego zestawu kodów
            if (items.isEmpty()) { // błędnie odczytany koniec ciągu kodowego
                return null;
            } else {
                if (items.get(0) == 1) {
                }
            }

            if (results.isEmpty()) {
                return null;
            } else {
                for (Result res : results) {
                    result.append(res.resultString).append(" ");
                }
                result.deleteCharAt(result.length() - 1);
            }
            if (result.toString().split(" ").length > 8) return null;
            return result.toString();
        }
    }

    /**
     * przeprowadza parsowanie ciągu znaków na listę stringów
     *
     * @param barcode kod kreskowy jako String
     * @return zakodowana wartość
     */
    public String decode(String barcode) {
        if (barcode.length() != 59) { // 4*12+2*3+5 = 48+6+5=59
            return null; // zabezpiecznie przed ciągami, którch nie da się odczytać Start+znak+sumaKontrolna+Stop
        }
        List<String> res = new ArrayList<>();
        res.add(barcode.substring(0, 3)); // lewy sentinel
        int position;
        for (position = 3; position < 27; position += 4) { // 6 cyfr
            res.add(barcode.substring(position, position + 4));
        }
        res.add(barcode.substring(position, position + 5)); // środkowy sentinel
        position += 5;

        for (; position < barcode.length() - 3; position += 4) { //  6 cyfr
            res.add(barcode.substring(position, position + 4));
        }
        res.add(barcode.substring(barcode.length() - 3)); // prawy sentinel
        return decode(res);
    }

    /**
     * zwraca klucze najbliżej odpowiadające przekazanamu kluczowi
     *
     * @param base błędny klucz
     * @return możliwe poprawne wartości elementów
     */
    private List<Integer> getAlternatives(String base, Map<String, Integer> set) {
        int minDistance = MIN_DISTANCE; // wartość większa niż możliwa
        int min;
        List<Integer> alternatives = new ArrayList<>();
        for (String comp : set.keySet()) {
            min = 0;
            if (comp.length() == base.length()) {
                for (int j = 0; j < comp.length(); j++) {
                    min += Math.abs(comp.charAt(j) - base.charAt(j)); // dodajemy odległość pomiędzy elementami
                }
                if (min < minDistance) {
                    alternatives.clear();
                    alternatives.add(set.get(comp));
                    minDistance = min;
                } else if (min == minDistance) {
                    alternatives.add(set.get(comp));
                }
            }
        }
        return alternatives.size() > 0 && alternatives.size() <= MAX_ALTER ? alternatives : new ArrayList<Integer>(); // gdy nie ma wyników zwracamy null lub gdy wartości jest za dużo
    }

    private List<Integer> getItems(String code, Map<String, Integer> set) {
        List<Integer> items;
        Integer i = set.get(code);
        if (i != null) {
            items = new ArrayList<>();
            items.add(i);
        } else {
            items = getAlternatives(code, set);
        }
        return items;
    }

    private static class Result {

        String resultString;
        String parity;

        public Result(String resultString, String par) {
            this.resultString = resultString;
            this.parity = par;
        }

        @Override
        public String toString() {
            return "'" + resultString + "' parzystość:" + parity; //To change body of generated methods, choose Tools | Templates.
        }


    }
}
