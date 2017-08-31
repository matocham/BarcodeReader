package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * abstract class used in processing images in multiple threads. It uses template method design pattern
 * Every instance obtains {@link CountDownLatch} that is decremeted after work is finished
 * Main thread wait on latch for all thread to finish their work
 * Returns single nubmer
 */
public abstract class IntegerCaller implements Callable<Integer> {

    public int start, end, value;
    CountDownLatch l;

    public IntegerCaller(CountDownLatch latch, int start, int end) {
        this.start = start;
        this.end = end;
        l = latch;
        value = 0;
    }

    @Override
    public Integer call() {
        doCalc();
        l.countDown();
        return value;
    }

    public abstract void doCalc();
}
