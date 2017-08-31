package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

/**
 * Created by Mateusz on 12.07.2016.
 */
public class Contrast {
    public static int[][] changeContrast(int[][] image, double contrast) { // contrast -255 : + 255

        double factor = (259 * (contrast + 255)) / (255 * (259 - contrast));
        int color;
        for (int i = 0; i < image.length; i ++) {
            for(int j=0;j<image[0].length;j++){
                color=image[i][j];
                color =(int) (factor * (color - 128) + 128);
                color=truncate(color);
                image[i][j]=color;
            }
        }
        return image;
    }

    public static int truncate(int value) {
        if (value > 255) {
            value = 255;
        }
        if (value < 0) {
            value = 0;
        }
        return value;
    }
}
