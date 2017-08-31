package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.decoder;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mateusz
 */
class Decoder implements IDecoder {

    private static final int MAX_CODE_RESULTS = 1;
    public static final String TAG = Decoder.class.getSimpleName();
    protected static int MAX_ALTER = 2;
    private static int MIN_DISTANCE = 4;
    //zbiory znaków dla wszystkich kodowań
    private Map<String, Item> setA, setB, setC;
    // aktualnie przetwarzany zbiór
    private StringBuilder result;
    private List<Result> results = new ArrayList<>();

    public Decoder() {
        setA = new HashMap<>();
        setB = new HashMap<>();
        setC = new HashMap<>();
        //************ INICJOWANIE STRUKTUR ***********************

        setA.put("212222", new Item(0, " "));        setB.put("212222", new Item(0, " "));         setC.put("212222", new Item(0, "00"));
        setA.put("222122", new Item(1, "!"));        setB.put("222122", new Item(1, "!"));         setC.put("222122", new Item(1, "01"));
        setA.put("222221", new Item(2, "\""));       setB.put("222221", new Item(2, "\""));        setC.put("222221", new Item(2, "02"));
        setA.put("121223", new Item(3, "#"));        setB.put("121223", new Item(3, "#"));         setC.put("121223", new Item(3, "03"));
        setA.put("121322", new Item(4, "$"));        setB.put("121322", new Item(4, "$"));         setC.put("121322", new Item(4, "04"));
        setA.put("131222", new Item(5, "%"));        setB.put("131222", new Item(5, "%"));         setC.put("131222", new Item(5, "05"));
        setA.put("122213", new Item(6, "&"));        setB.put("122213", new Item(6, "&"));         setC.put("122213", new Item(6, "06"));
        setA.put("122312", new Item(7, "'"));        setB.put("122312", new Item(7, "'"));         setC.put("122312", new Item(7, "07"));
        setA.put("132212", new Item(8, "("));        setB.put("132212", new Item(8, "("));         setC.put("132212", new Item(8, "08"));
        setA.put("221213", new Item(9, ")"));        setB.put("221213", new Item(9, ")"));         setC.put("221213", new Item(9, "09"));
        setA.put("221312", new Item(10, "*"));       setB.put("221312", new Item(10, "*"));        setC.put("221312", new Item(10, "10"));
        setA.put("231212", new Item(11, "+"));       setB.put("231212", new Item(11, "+"));        setC.put("231212", new Item(11, "11"));
        setA.put("112232", new Item(12, ","));       setB.put("112232", new Item(12, ","));        setC.put("112232", new Item(12, "12"));
        setA.put("122132", new Item(13, "-"));       setB.put("122132", new Item(13, "-"));        setC.put("122132", new Item(13, "13"));
        setA.put("122231", new Item(14, "."));       setB.put("122231", new Item(14, "."));        setC.put("122231", new Item(14, "14"));
        setA.put("113222", new Item(15, "/"));       setB.put("113222", new Item(15, "/"));        setC.put("113222", new Item(15, "15"));
        setA.put("123122", new Item(16, "0"));       setB.put("123122", new Item(16, "0"));        setC.put("123122", new Item(16, "16"));
        setA.put("123221", new Item(17, "1"));       setB.put("123221", new Item(17, "1"));        setC.put("123221", new Item(17, "17"));
        setA.put("223211", new Item(18, "2"));       setB.put("223211", new Item(18, "2"));        setC.put("223211", new Item(18, "18"));
        setA.put("221132", new Item(19, "3"));       setB.put("221132", new Item(19, "3"));        setC.put("221132", new Item(19, "19"));
        setA.put("221231", new Item(20, "4"));       setB.put("221231", new Item(20, "4"));        setC.put("221231", new Item(20, "20"));
        setA.put("213212", new Item(21, "5"));       setB.put("213212", new Item(21, "5"));        setC.put("213212", new Item(21, "21"));
        setA.put("223112", new Item(22, "6"));       setB.put("223112", new Item(22, "6"));        setC.put("223112", new Item(22, "22"));
        setA.put("312131", new Item(23, "7"));       setB.put("312131", new Item(23, "7"));        setC.put("312131", new Item(23, "23"));
        setA.put("311222", new Item(24, "8"));       setB.put("311222", new Item(24, "8"));        setC.put("311222", new Item(24, "24"));
        setA.put("321122", new Item(25, "9"));       setB.put("321122", new Item(25, "9"));        setC.put("321122", new Item(25, "25"));
        setA.put("321221", new Item(26, ":"));       setB.put("321221", new Item(26, ":"));        setC.put("321221", new Item(26, "26"));
        setA.put("312212", new Item(27, ";"));       setB.put("312212", new Item(27, ";"));        setC.put("312212", new Item(27, "27"));
        setA.put("322112", new Item(28, "<"));       setB.put("322112", new Item(28, "<"));        setC.put("322112", new Item(28, "28"));
        setA.put("322211", new Item(29, "="));       setB.put("322211", new Item(29, "="));        setC.put("322211", new Item(29, "29"));
        setA.put("212123", new Item(30, ">"));       setB.put("212123", new Item(30, ">"));        setC.put("212123", new Item(30, "30"));
        setA.put("212321", new Item(31, "?"));       setB.put("212321", new Item(31, "?"));        setC.put("212321", new Item(31, "31"));
        setA.put("232121", new Item(32, "@"));       setB.put("232121", new Item(32, "@"));        setC.put("232121", new Item(32, "32"));
        setA.put("111323", new Item(33, "A"));       setB.put("111323", new Item(33, "A"));        setC.put("111323", new Item(33, "33"));
        setA.put("131123", new Item(34, "B"));       setB.put("131123", new Item(34, "B"));        setC.put("131123", new Item(34, "34"));
        setA.put("131321", new Item(35, "C"));       setB.put("131321", new Item(35, "C"));        setC.put("131321", new Item(35, "35"));
        setA.put("112313", new Item(36, "D"));       setB.put("112313", new Item(36, "D"));        setC.put("112313", new Item(36, "36"));
        setA.put("132113", new Item(37, "E"));       setB.put("132113", new Item(37, "E"));        setC.put("132113", new Item(37, "37"));
        setA.put("132311", new Item(38, "F"));       setB.put("132311", new Item(38, "F"));        setC.put("132311", new Item(38, "38"));
        setA.put("211313", new Item(39, "G"));       setB.put("211313", new Item(39, "G"));        setC.put("211313", new Item(39, "39"));
        setA.put("231113", new Item(40, "H"));       setB.put("231113", new Item(40, "H"));        setC.put("231113", new Item(40, "40"));
        setA.put("231311", new Item(41, "I"));       setB.put("231311", new Item(41, "I"));        setC.put("231311", new Item(41, "41"));
        setA.put("112133", new Item(42, "J"));       setB.put("112133", new Item(42, "J"));        setC.put("112133", new Item(42, "42"));
        setA.put("112331", new Item(43, "K"));       setB.put("112331", new Item(43, "K"));        setC.put("112331", new Item(43, "43"));
        setA.put("132131", new Item(44, "L"));       setB.put("132131", new Item(44, "L"));        setC.put("132131", new Item(44, "44"));
        setA.put("113123", new Item(45, "M"));       setB.put("113123", new Item(45, "M"));        setC.put("113123", new Item(45, "45"));
        setA.put("113321", new Item(46, "N"));       setB.put("113321", new Item(46, "N"));        setC.put("113321", new Item(46, "46"));
        setA.put("133121", new Item(47, "O"));       setB.put("133121", new Item(47, "O"));        setC.put("133121", new Item(47, "47"));
        setA.put("313121", new Item(48, "P"));       setB.put("313121", new Item(48, "P"));        setC.put("313121", new Item(48, "48"));
        setA.put("211331", new Item(49, "Q"));       setB.put("211331", new Item(49, "Q"));        setC.put("211331", new Item(49, "49"));
        setA.put("231131", new Item(50, "R"));       setB.put("231131", new Item(50, "R"));        setC.put("231131", new Item(50, "50"));
        setA.put("213113", new Item(51, "S"));       setB.put("213113", new Item(51, "S"));        setC.put("213113", new Item(51, "51"));
        setA.put("213311", new Item(52, "T"));       setB.put("213311", new Item(52, "T"));        setC.put("213311", new Item(52, "52"));
        setA.put("213131", new Item(53, "U"));       setB.put("213131", new Item(53, "U"));        setC.put("213131", new Item(53, "53"));
        setA.put("311123", new Item(54, "V"));       setB.put("311123", new Item(54, "V"));        setC.put("311123", new Item(54, "54"));
        setA.put("311321", new Item(55, "W"));       setB.put("311321", new Item(55, "W"));        setC.put("311321", new Item(55, "55"));
        setA.put("331121", new Item(56, "X"));       setB.put("331121", new Item(56, "X"));        setC.put("331121", new Item(56, "56"));
        setA.put("312113", new Item(57, "Y"));       setB.put("312113", new Item(57, "Y"));        setC.put("312113", new Item(57, "57"));
        setA.put("312311", new Item(58, "Z"));       setB.put("312311", new Item(58, "Z"));        setC.put("312311", new Item(58, "58"));
        setA.put("332111", new Item(59, "["));       setB.put("332111", new Item(59, "["));        setC.put("332111", new Item(59, "59"));
        setA.put("314111", new Item(60, "\\"));      setB.put("314111", new Item(60, "\\"));       setC.put("314111", new Item(60, "60"));
        setA.put("221411", new Item(61, "]"));       setB.put("221411", new Item(61, "]"));        setC.put("221411", new Item(61, "61"));
        setA.put("431111", new Item(62, "^"));       setB.put("431111", new Item(62, "^"));        setC.put("431111", new Item(62, "62"));
        setA.put("111224", new Item(63, "_"));       setB.put("111224", new Item(63, "_"));        setC.put("111224", new Item(63, "63"));
        setA.put("111422", new Item(64, "NUL"));     setB.put("111422", new Item(64, "`"));        setC.put("111422", new Item(64, "64"));
        setA.put("121124", new Item(65, "SOH"));     setB.put("121124", new Item(65, "a"));        setC.put("121124", new Item(65, "65"));
        setA.put("121421", new Item(66, "STX"));     setB.put("121421", new Item(66, "b"));        setC.put("121421", new Item(66, "66"));
        setA.put("141122", new Item(67, "ETX"));     setB.put("141122", new Item(67, "c"));        setC.put("141122", new Item(67, "67"));
        setA.put("141221", new Item(68, "EOT"));     setB.put("141221", new Item(68, "d"));        setC.put("141221", new Item(68, "68"));
        setA.put("112214", new Item(69, "ENQ"));     setB.put("112214", new Item(69, "e"));        setC.put("112214", new Item(69, "69"));
        setA.put("112412", new Item(70, "ACK"));     setB.put("112412", new Item(70, "f"));        setC.put("112412", new Item(70, "70"));
        setA.put("122114", new Item(71, "BEL"));     setB.put("122114", new Item(71, "g"));        setC.put("122114", new Item(71, "71"));
        setA.put("122411", new Item(72, "BS"));      setB.put("122411", new Item(72, "h"));        setC.put("122411", new Item(72, "72"));
        setA.put("142112", new Item(73, "HT"));      setB.put("142112", new Item(73, "i"));        setC.put("142112", new Item(73, "73"));
        setA.put("142211", new Item(74, "LF"));      setB.put("142211", new Item(74, "j"));        setC.put("142211", new Item(74, "74"));
        setA.put("241211", new Item(75, "VT"));      setB.put("241211", new Item(75, "k"));        setC.put("241211", new Item(75, "75"));
        setA.put("221114", new Item(76, "FF"));      setB.put("221114", new Item(76, "l"));        setC.put("221114", new Item(76, "76"));
        setA.put("413111", new Item(77, "CR"));      setB.put("413111", new Item(77, "m"));        setC.put("413111", new Item(77, "77"));
        setA.put("241112", new Item(78, "SO"));      setB.put("241112", new Item(78, "n"));        setC.put("241112", new Item(78, "78"));
        setA.put("134111", new Item(79, "SI"));      setB.put("134111", new Item(79, "o"));        setC.put("134111", new Item(79, "79"));
        setA.put("111242", new Item(80, "DLE"));     setB.put("111242", new Item(80, "p"));        setC.put("111242", new Item(80, "80"));
        setA.put("121142", new Item(81, "DC1"));     setB.put("121142", new Item(81, "q"));        setC.put("121142", new Item(81, "81"));
        setA.put("121241", new Item(82, "DC2"));     setB.put("121241", new Item(82, "r"));        setC.put("121241", new Item(82, "82"));
        setA.put("114212", new Item(83, "DC3"));     setB.put("114212", new Item(83, "s"));        setC.put("114212", new Item(83, "83"));
        setA.put("124112", new Item(84, "DC4"));     setB.put("124112", new Item(84, "t"));        setC.put("124112", new Item(84, "84"));
        setA.put("124211", new Item(85, "NAK"));     setB.put("124211", new Item(85, "u"));        setC.put("124211", new Item(85, "85"));
        setA.put("411212", new Item(86, "SYN"));     setB.put("411212", new Item(86, "v"));        setC.put("411212", new Item(86, "86"));
        setA.put("421112", new Item(87, "ETB"));     setB.put("421112", new Item(87, "w"));        setC.put("421112", new Item(87, "87"));
        setA.put("421211", new Item(88, "CAN"));     setB.put("421211", new Item(88, "x"));        setC.put("421211", new Item(88, "88"));
        setA.put("212141", new Item(89, "EM"));      setB.put("212141", new Item(89, "y"));        setC.put("212141", new Item(89, "89"));
        setA.put("214121", new Item(90, "SUB"));     setB.put("214121", new Item(90, "z"));        setC.put("214121", new Item(90, "90"));
        setA.put("412121", new Item(91, "ESC"));     setB.put("412121", new Item(91, "{"));        setC.put("412121", new Item(91, "91"));
        setA.put("111143", new Item(92, "FS"));      setB.put("111143", new Item(92, "|"));        setC.put("111143", new Item(92, "92"));
        setA.put("111341", new Item(93, "GS"));      setB.put("111341", new Item(93, "}"));        setC.put("111341", new Item(93, "93"));
        setA.put("131141", new Item(94, "RS"));      setB.put("131141", new Item(94, "~"));        setC.put("131141", new Item(94, "94"));
        setA.put("114113", new Item(95, "US"));      setB.put("114113", new Item(95, "DEL"));      setC.put("114113", new Item(95, "95"));
        setA.put("114311", new Item(96, "FNC3"));    setB.put("114311", new Item(96, "FNC3"));     setC.put("114311", new Item(96, "96"));
        setA.put("411113", new Item(97, "FNC2"));    setB.put("411113", new Item(97, "FNC2"));     setC.put("411113", new Item(97, "97"));
        setA.put("411311", new Item(98, "SHIFT"));   setB.put("411311", new Item(98, "SHIFT"));    setC.put("411311", new Item(98, "98"));
        setA.put("113141", new Item(99, "CODE_C"));  setB.put("113141", new Item(99, "CODE_C"));   setC.put("113141", new Item(99, "99"));
        setA.put("114131", new Item(100, "CODE_B")); setB.put("114131", new Item(100, "FNC4"));    setC.put("114131", new Item(100, "CODE_B"));
        setA.put("311141", new Item(101, "FNC4"));   setB.put("311141", new Item(101, "CODE_A"));  setC.put("311141", new Item(101, "CODE_A"));
        setA.put("411131", new Item(102, "FNC1"));   setB.put("411131", new Item(102, "FNC1"));    setC.put("411131", new Item(102, "FNC1"));
        setA.put("211412", new Item(103, "Start_A")); setB.put("211412", new Item(103, "Start_A")); setC.put("211412", new Item(103, "Start_A"));
        setA.put("211214", new Item(104, "Start_B")); setB.put("211214", new Item(104, "Start_B")); setC.put("211214", new Item(104, "Start_B"));
        setA.put("211232", new Item(105, "Start_C")); setB.put("211232", new Item(105, "Start_C")); setC.put("211232", new Item(105, "Start_C"));
        setA.put("2331112", new Item(-1, "Stop"));    setB.put("2331112", new Item(-1, "Stop"));    setC.put("2331112", new Item(-1, "Stop"));

        //*********************************************************
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
        List<Item> items;
        List<Result> tempRes = new ArrayList<>();
        results.clear();

        //*********** odczytanie elementu start ************************
        element = barcode.get(0);
        items = getItems(element, setA);
        if (items.isEmpty()) {
            return null;
        } else {
            List<Item> temp = new ArrayList<>();
            for (Item i : items) {
                if (i.value.equals("Start_A") || i.value.equals("Start_B") || i.value.equals("Start_C")) {
                    temp.add(i);
                }
            }

            if (temp.isEmpty()) { // pomimo próby nie udało się znaleźć przekłamania
                return null;
            } else {
                items = temp;
            }
        }
        for (Item item : items) {
            switch (item.value) {
                case "Start_A":
                    results.add(new Result("", item.number, setA));
                    break;
                case "Start_B":
                    results.add(new Result("", item.number, setB));
                    break;
                case "Start_C":
                    results.add(new Result("", item.number, setC));
                    break;
            }
        }

        //**************** dekodowanie zawartości ************************
        for (int i = 1; i < barcode.size() - 2; i++) {
            element = barcode.get(i);
            tempRes.clear();
            for (Result res : results) {
                items = getItems(element, res.workingSet);
                if (!items.isEmpty()) {
                    if (res.afterShift) { // gdy był odczyt po shift to pomijamy ten krok
                        res.afterShift = false;
                    } else {
                        for (Item item : items) {
                            Result newRes = new Result(res.resultString, res.controlSum, res.workingSet); // nowy ciąg wynikowy powstały po rozszerzeniu analizowanego ciągu o kolejny element
                            if (isSpecial(item, res.workingSet)) { // znaków specjalnych nie dodajemy do resultString, tylko dodajemy do sumy kontrolnej

                                switch (item.value) {
                                    case "SHIFT":
                                        List<Item> shiftItems;
                                        newRes.controlSum += item.number * i;
                                        if (newRes.workingSet.equals(setA)) {
                                            shiftItems = shiftRead(setB, barcode.get(i + 1));
                                        } else {
                                            shiftItems = shiftRead(setA, barcode.get(i + 1));
                                        }
                                        if (items.isEmpty()) { // gdy nie udało się znaleźć pasującyh elementów
                                            return null;
                                        } else {
                                            newRes.afterShift = true;
                                            Result tempResult;
                                            for (Item ii : shiftItems) { // dodanie nowego elementu na każdy wynik shiftRead + ustawienie newRes na null
                                                if (!isSpecial(ii, res.workingSet)) { // zabezpieczenie, jeśli w zestawie jest znak specjalny to jest to na pewno błąd
                                                    tempResult = new Result(newRes.resultString, newRes.controlSum, newRes.workingSet);
                                                    tempResult.afterShift = true;
                                                    tempResult.resultString += ii.value;
                                                    tempResult.controlSum += ii.number * (i + 1); // zakładamy, że kod kreskowy jest poprawny i po SHIFT jest sensowna wartość
                                                    tempRes.add(tempResult);
                                                }
                                            }
                                            newRes = null; // dzięki temu nie dodamy niepełnego rozwiązania do możliwych rozwiązań
                                        }
                                        break;
                                    case "CODE_A":
                                        newRes.workingSet = setA;
                                        break;
                                    case "CODE_B":
                                        newRes.workingSet = setB;
                                        break;
                                    case "CODE_C":
                                        newRes.workingSet = setC;
                                        break;
                                    default:
                                        newRes = null;
                                }
                            } else {
                                newRes.resultString += item.value;
                            }
                            if (newRes != null) { // fałszywe tylko gdy czytaliśmy SHIFT
                                newRes.controlSum += item.number * i;
                                tempRes.add(newRes);
                            }
                        }
                    }
                }
            }
            results = new ArrayList<>(tempRes);
        }

        if(results.isEmpty()){
            Log.e(TAG,"No maches found");
            return null;
        }
        //**************** pobranie i sprawdzenie sumy kontrolnej ***********************
        element = barcode.get(barcode.size() - 2); // suma kontrolna
        tempRes.clear(); // w tempRes przechowujemy elementy, które mają poprawną sumę kontrolną
        for (Result res : results) {
           // items = getItems(element, res.workingSet);
            Item control = res.workingSet.get(element);
            if (control!=null) { // dla każdego dobranego elementu trzeba sprawdzić sumę kontrolną
                if (res.controlSum % 103 == control.number) {
                    tempRes.add(res); // suma się zgadza - można dodać do rozwiązań
                    break; // tylko jedna suma będzie prawidłowa
                } else {
                    Log.e(TAG, "invalid control sum");
                }
            } else{
                Log.e(TAG,"null control");
            }
        }

        results = tempRes; // zostały tylko wyniki, dla których suma kontrolna się zgadza
        Log.e("Decoder results",results.toString());
        //****************** sprawdzenie poprawności znaku końca ciągu(element niepotrzebny) ********************

        element = barcode.get(barcode.size() - 1);
        items = getItems(element, setA); // znak końca jest taki sam dla każdego zestawu kodów
        if (items.isEmpty()) { // błędnie odczytany koniec ciągu kodowego
            return null;
        }

        if (results.isEmpty() || results.size()>MAX_CODE_RESULTS) {
            return null;
        } else {
            for (Result res : results) {
                result.append(res.resultString).append(" ");
            }
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    /**
     * przeprowadza parsowanie ciągu znaków na liste Int-ów
     *
     * @param barcode kod kreskowy jako String
     * @return zakodowana wartość
     */
    public String decode(String barcode) {
        if (barcode.length() % 6 != 1 || barcode.length() < 43) { // co najmniej 5 zakodowanych znaków
            return null; // zabezpiecznie przed ciągami, którch nie da się odczytać Start+znak+sumaKontrolna+Stop
        }
        List<String> res = new ArrayList<>();
        for (int i = 0; i < barcode.length(); i += 6) {
            if (i < barcode.length() - 7) {
                res.add(barcode.substring(i, i + 6));
            } else {
                res.add(barcode.substring(i));
                i += 7;
            }
        }
        return decode(res);
    }

    /**
     * sprawdza, czy dany element jest znakiem sterującym
     *
     * @param i element do sprawdzenia
     * @return true jeśli element jest znakiem sterującym
     */
    private boolean isSpecial(Item i, Map<String,Item> set) {
        if ((i.number > 95 && i.number < 106 || i.number == -1) && !set.equals(setC)) return true;
        if ((i.number > 99 && i.number < 106 || i.number == -1)) return true;
        return false;
    }

    /**
     * odczyt znaku po SHIFT
     *
     * @param map  mapa, z której czytamy znak
     * @param part klucz do mapy
     * @return odczytany element
     */
    private List<Item> shiftRead(Map<String, Item> map, String part) {
        return getItems(part, map);
    }

    /**
     * zwraca klucze najbliżej odpowiadające przekazanamu kluczowi
     *
     * @param base błędny klucz
     * @return możliwe poprawne wartości elementów
     */
    protected List<Item> getAlternatives(String base, Map<String, Item> set) {
        int minDistance = MIN_DISTANCE; // wartość większa niż możliwa
        int min;
        List<Item> alternatives = new ArrayList<>();
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
        return alternatives.size() > 0 && alternatives.size() <= MAX_ALTER ? alternatives : new ArrayList<Item>(); // gdy nie ma wyników zwracamy null lub gdy wartości jest za dużo
    }

    private List<Item> getItems(String code, Map<String, Item> set) {
        List<Item> items;
        Item i = set.get(code);
        if (i != null) {
            items = new ArrayList<>();
            items.add(i);
        } else {
            items = getAlternatives(code, set);
        }
        Log.e("items",items.toString());
        return items;
    }

    private static class Result {

        String resultString;
        int controlSum;
        Map<String, Item> workingSet;
        boolean afterShift = false;

        public Result(String resultString, int controlSum, Map<String, Item> workingSet) {
            this.resultString = resultString;
            this.controlSum = controlSum;
            this.workingSet = workingSet;
        }

        @Override
        public String toString() {
            return "'" + resultString + "' suma:" + controlSum; //To change body of generated methods, choose Tools | Templates.
        }
    }
}
