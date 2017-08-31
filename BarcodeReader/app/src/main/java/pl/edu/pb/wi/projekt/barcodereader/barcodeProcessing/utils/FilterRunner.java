package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils;

import java.util.concurrent.CountDownLatch;

/**
 * basic class used in methods that process images in multiple threads
 * Ensure that all instances finished work before main thread go further
 */
public abstract class FilterRunner implements Runnable {
    public int start, end;
    CountDownLatch l;

    public FilterRunner(CountDownLatch latch, int start, int end) {
        this.start = start;
        this.end = end;
        l = latch;
    }

    @Override
    public void run() {
        doCalc();
        l.countDown();
    }

    public abstract void doCalc();
}