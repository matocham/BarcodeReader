package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.detector;


class Range implements Comparable<Range> {
    int start;
    int end;
    int row;

    public Range(int s, int e, int r) {
        start = s;
        end = e;
        row = r;
    }

    public Range() {
        start = end = row = 0;
    }

    @Override
    public int compareTo(Range o) {
        int result = (o.end - o.start) - (this.end - this.start); //malejace - tylko gdy nie bierzemy pod uwagę kilku kodów w jednym wierszu
        return result;
    }
}
