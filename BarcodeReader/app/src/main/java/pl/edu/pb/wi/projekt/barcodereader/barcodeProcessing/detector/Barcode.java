package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.detector;

public class Barcode implements Comparable<Barcode> {
    public int x;
    public int y;
    public int width;
    public int height;
    // może się przydać w późniejszym etapie
    int base; // wiersz, na podstawie którego dokonano wyboru takiego obszaru

    public Barcode() {
        x = y = width = height = base = 0;
    }

    boolean overlaps(Barcode b) {
        boolean over = false;
        if (x >= b.x && x <= b.x + b.width && y >= b.y && y <= b.y + b.height) over = true; // x,y
        else if (x + width >= b.x && x + width <= b.x + b.width && y + height >= b.y && y + height <= b.y + b.height)
            over = true;//x2,y2
        else if (x + width >= b.x && x + width <= b.x + b.width && y >= b.y && y <= b.y + b.height)
            over = true;//x2,y
        else if (x >= b.x && x <= b.x + b.width && y + height >= b.y && y + height <= b.y + b.height)
            over = true;//x,y2
        else if (x >= b.x && x <= b.x + b.width && y <= b.y && x + width <= b.x + b.width && y + height >= b.y + b.height)
            over = true;
            //DLA PARAMETRU
        else if (b.x >= x && b.x <= x + width && b.y >= y && b.y <= y + height) over = true; // x,y
        else if (b.x + b.width >= x && b.x + b.width <= x + width && b.y + b.height >= y && b.y + b.height <= y + height)
            over = true;//x2,y2
        else if (b.x + b.width >= x && b.x + b.width <= x + width && b.y >= y && b.y <= y + height)
            over = true;//x2,y
        else if (b.x >= x && b.x <= x + width && b.y + b.height >= y && b.y + b.height <= y + height)
            over = true;//x,y2
        else if (b.x >= x && b.x <= x + width && b.y <= y && b.x + b.width <= x + width && b.y + b.height >= y + height)
            over = true;
        return over;
    }

    @Override
    public int compareTo(Barcode o) { // najpierw sortowanie po osi y potem po x
        int result = this.y - o.y;
        if (result == 0) {
            result = this.x - o.x;
        }
        return result;
    }

    @Override
    public String toString() {
        return "x=" + x + " y=" + y + " width=" + width + " height=" + height + " row=" + base;
    }
}
