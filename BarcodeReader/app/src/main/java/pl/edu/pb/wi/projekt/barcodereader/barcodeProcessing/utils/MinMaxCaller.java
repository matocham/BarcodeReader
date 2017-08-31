package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * used when two return values are needed. Work the same way as {@link IntegerCaller}
 */
public abstract class MinMaxCaller implements Callable<int[]> {

    public int start, end;
    public int[] value;
    CountDownLatch l;

    public MinMaxCaller(CountDownLatch latch, int start, int end) {
        this.start = start;
        this.end = end;
        l = latch;
        value = new int[2];
    }

    @Override
    public int[] call() {
        doCalc();
        l.countDown();
        return value;
    }

    public abstract void doCalc();
}
