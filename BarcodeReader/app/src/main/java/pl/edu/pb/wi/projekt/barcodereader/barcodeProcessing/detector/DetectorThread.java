package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.detector;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import pl.edu.pb.wi.projekt.barcodereader.utils.Utils;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.ColorConventer;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.StorageConventer;

/**
 * Created by Mateusz on 10.06.2016.
 */
public class DetectorThread extends Thread {
    public static boolean DEBUG = false;
    private BlockingQueue<int[][]> images;
    private BlockingQueue<String> codes;
    private BlockingQueue<Barcode> bars;

    private Detector detector;
    private boolean working;
    private int cutLine;

    public DetectorThread() {
        images = new ArrayBlockingQueue(2);
        codes = new ArrayBlockingQueue(2);
        bars = new ArrayBlockingQueue(10);
        working = true;
        detector = new Detector();
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted() && working) {
                if (images.isEmpty()) {
                    Thread.currentThread().sleep(100);
                } else {
                    int[][] image = images.poll();

                    ArrayList<Barcode> results = detector.detectFromCut(image, cutLine);
                    String result;

                    for (Barcode bar : results) {
                        // debug pozwala na zapisanie przetworzonych obrazów i nalizę wyników wyszukiwania kodu na obrazie
                        // znacząco spowalnia wykonanie programu
                        if (DEBUG) {
                            saveDebugImageInfo(image, bar);
                        }
                        bars.add(bar);
                        result = detector.rd.read(image, bar.x, bar.y, bar.width, bar.height);
                        if (result != null) {
                            codes.add(result);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            // mozliwe i akceptowalne
        }
    }

    private void saveDebugImageInfo(int[][] image, Barcode bar) {
        FileOutputStream outf = null;
        try {
            outf = new FileOutputStream(Utils.getOutputImageFile().getAbsolutePath() + new Random().nextInt());
            Bitmap bmp = Bitmap.createBitmap(image[0].length, image.length, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < image.length; i++) {
                for (int j = 0; j < image[0].length; j++) {
                    bmp.setPixel(j, i, Color.rgb(image[i][j], image[i][j], image[i][j]));
                }
            }
            for (int i = 0; i < bar.height; i++) {
                for (int j = 0; j < bar.width; j++) {
                    if (i == 0 || j == 0) {
                        bmp.setPixel(j + bar.x, i + bar.y, Color.rgb(0, 255, 0));
                    } else if (j == bar.width || i == bar.height) {
                        bmp.setPixel(j + bar.x, i + bar.y, Color.rgb(0, 255, 0));
                    }
                }
            }
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outf != null) {
                    outf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addImage(Bitmap img) {
        images.add(ColorConventer.to2Dgreyscale(img));
    }

    public void addImage(byte[] img, int width, int height) {
        images.add(StorageConventer.YUVtoRGBGray2D(img, width, height));
    }

    public void addImage(int[][] image) {
        images.add(image);
    }

    public boolean hasSpace() {
        return images.isEmpty();
    }

    public Barcode getBar() {
        return bars.poll();
    }

    public String getCode() {
        return codes.poll();
    }

    public void finish() {
        this.working = false;
    }

    public void setCutLine(int cutLine) {
        this.cutLine = cutLine;
    }

    public int getCutLine() {
        return cutLine;
    }
}
