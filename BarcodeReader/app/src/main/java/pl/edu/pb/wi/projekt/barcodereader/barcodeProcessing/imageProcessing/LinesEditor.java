package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.FilterRunner;


public class LinesEditor {

    private static ExecutorService executor = ThreadExecutor.getExecutor();

    /**
     * zastępuje szerokie pionowe linie liniami o szerokości 1 piksela
     *
     * @param image obraz do przetworzenia
     * @return wynikowy obraz
     */
    public static int[][] thinLines(final int[][] image) {
        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end >= image.length) end = image.length;
            executor.execute(new FilterRunner(latch, start, end) {
                int begin = 0;

                @Override
                public void doCalc() {
                    for (int i = start; i < end; i++) {
                        for (int j = 0; j < image[0].length; j++) {
                            if (image[i][j] == 255) {
                                begin = j;
                                while (j < image[0].length && image[i][j] == 255) {
                                    image[i][j] = 0;
                                    j++;
                                }
                                j--; // bo jesteśmy na pierwszym czarnym
                                image[i][(begin + j) / 2] = 255;
                            }
                        }
                    }
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        return image; // raczej nie jest potrzebne
    }

    /**
     * usuwa linie oddalone od siebie o co najmniej x pikseli
     *
     * @param image    obraz do przetworzenia
     * @param distance maksymalna odległość pomiędzy elementami
     * @return obraz po usunięciu linii
     */
    public static int[][] removeSingleLines(final int[][] image, final int distance) {

        CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
        int areaWidth = (int) Math.ceil(image.length * 1.0 / ThreadExecutor.THREAD_NUMBER);
        int start, end;

        for (int t = 0; t < ThreadExecutor.THREAD_NUMBER; t++) {
            start = t * areaWidth;
            end = (t + 1) * areaWidth;
            if (end >= image.length) end = image.length;
            executor.execute(new FilterRunner(latch, start, end) {
                int counter;
                List<Integer> toDelete = new ArrayList<>();

                @Override
                public void doCalc() {
                    for (int i = start; i < end; i++) {
                        toDelete.clear();
                        for (int j = 0; j < image[0].length; j++) {
                            if (j == 0 || image[i][j] == 255) { // gdy trafimy na biały pixel lub właśnie rozpoczęliśmy przeszukiwanie
                                while (j < image[0].length - 1 && image[i][j] == 255)
                                    j++;
                                counter = 0;
                                while (j < image[0].length && image[i][j] == 0) {
                                    j++;
                                    counter++;
                                }
                                if (j >= image[0].length) {
                                    j = image[0].length - 1;
                                }
                                if (counter > distance) { // jesteśmy na białym jeżeli za daleko to usuwamy ten biały
                                    toDelete.add(j);
                                }
                            }
                        }
                        if (!toDelete.isEmpty()) {
                            for (Integer ij : toDelete) {
                                while (ij < image[0].length && image[i][ij] == 255)
                                    image[i][ij++] = 0;
                            }
                        }
                    }
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return image;
    }
}
