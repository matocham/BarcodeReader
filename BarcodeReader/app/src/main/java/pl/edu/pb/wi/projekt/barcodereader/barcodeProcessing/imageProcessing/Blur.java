package pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.utils.FilterRunner;

public class Blur {

	private static ExecutorService executor = ThreadExecutor.getExecutor();
	/**
	 * rozmycie obrazu na podstawie jądra o podanym wymiarze
	 * @param image obraz do przetworzenia
	 * @param size rozmiar jądra (kwadratowe)
	 * @return obraz po rozmyciu
	 */
	public static int[][] blur(final int[][] image, final int size){

		final int[][] out = new int[image.length][image[0].length];

		CountDownLatch latch = new CountDownLatch(ThreadExecutor.THREAD_NUMBER);
		int areaWidth=(int) Math.ceil(image.length*1.0/ThreadExecutor.THREAD_NUMBER);
		int start,end;
		
		for(int t=0;t<ThreadExecutor.THREAD_NUMBER;t++){
			start= t*areaWidth;
			end = (t+1)*areaWidth;
			if(start==0) start=size/2;
			if(end>image.length - size/2) end=image.length - size/2;
			executor.execute(new FilterRunner(latch,start,end){
				int acc = 0;
				@Override
				public void doCalc() {
					for (int i = start; i < end; i++) {
						for (int j = size/2; j < image[0].length - size/2; j++) {
							acc=0;

							for(int k=-size/2;k<size/2+1;k++){
								for(int l=-size/2;l<size/2+1;l++){
									acc+=image[i+k][j+l];
								}
							}
							acc=acc/(size*size);
							if(acc<0) acc=0;
							else if(acc>255) acc=255;
							out[i][j] = acc;
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
		return out;
	}
}
