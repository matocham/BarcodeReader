package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.decoder;

/**
 * Klasa opakowująca zakodowany znak i jego numer do obliczania znaku kontrolnego
 *
 * @author Mateusz
 */
class Item {
    int number;
    String value;

    public Item(int number, String value) {
        this.number = number;
        this.value = value;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value + " : " + number;
    }

    @Override
    public boolean equals(Object o) {
        if(!o.getClass().equals(Item.class)){
            return false;
        }
        Item temp = (Item) o;
        return temp.getValue().equals(this.getValue()) && this.getNumber() == temp.getNumber();
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result=1;
        if(getValue()!=null){
            result=result * prime + getValue().hashCode();
        }
        result = result*prime + getNumber();
        return result;
    }
}
