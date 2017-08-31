package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Mateusz on 10.06.2016.
 */
public class ThreadExecutor {
    public static final int THREAD_NUMBER = Runtime.getRuntime().availableProcessors();

    private static ExecutorService executor;

    static {
        executor = Executors.newFixedThreadPool(THREAD_NUMBER);
    }

    public static ExecutorService getExecutor() {
        return executor;
    }
}
