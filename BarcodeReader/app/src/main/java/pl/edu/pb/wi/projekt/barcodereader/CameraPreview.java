package pl.edu.pb.wi.projekt.barcodereader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.detector.Barcode;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.detector.DetectorThread;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.imageProcessing.StorageConventer;
import pl.edu.pb.wi.projekt.barcodereader.utils.ScreenUtils;

/**
 * Created by Mateusz on 12.05.2016.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    public static final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private ICameraDecoder camActiv; // do informowania acivity o zakończeniu
    private DetectorThread detector;
    private int viewVisibleWidth, viewvisibleHeight;

    /**
     * callback used to retrive image form preview used in processing
     */
    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            int width = camActiv.getPreviewWidth();
            int height = camActiv.getPreviewHeight();
            if (detector != null && detector.isAlive()) {
                if (detector.getCutLine() == 0) {
                    float ratio = ((float) viewvisibleHeight) / getHeight(); // where is the half of the image (red line)
                    detector.setCutLine((int) ((ratio * camActiv.getPreviewHeight()) / 2));
                }
                if (width > 0 && height > 0) {
                    if (detector.hasSpace()) {
                        detector.addImage(StorageConventer.YUVtoRGBGray2D(data, width, height));
                    }
                    String result = detector.getCode();
                    if (result != null) {
                        Toast.makeText((Context) camActiv, result, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, result);
                        detector.finish();
                        camActiv.sendResult(result);
                    }
                    Barcode bar;
                    while ((bar = detector.getBar()) != null) {
                        Log.e(TAG, bar.toString());
                    }
                }
            }
        }
    };

    public CameraPreview(Context context, Camera camera) {
        super(context);
        camActiv = (ICameraDecoder) context;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        setWillNotDraw(false);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // When surface is created is possible to assign preview to camera
        Log.e(TAG, "surface changed fired");
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mCamera.setPreviewCallback(mPreviewCallback);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Fired when orientation changes etc. Fired only once because screen changes are disabled
        if (mHolder.getSurface() == null) {
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.setPreviewCallback(mPreviewCallback);
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void setmCamera(Camera mCamera) {
        this.mCamera = mCamera;
    }

    public void setDetector(DetectorThread detector) {
        this.detector = detector;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // adjust preview widget size to preserve camera preview ratio, in other case image is distorted
        final int viewWidth = resolveSize(getWidth(), widthMeasureSpec);
        final int viewHeight = resolveSize(getHeight(), heightMeasureSpec);
        int navBarWidth = ScreenUtils.getNavigationBarSize((Context) camActiv).x;
        float ratio = ((float) viewWidth + navBarWidth) / (float) camActiv.getPreviewWidth();
        int newHeight, newWidth;
        newHeight = (int) (camActiv.getPreviewHeight() * ratio);
        newWidth = (int) (camActiv.getPreviewWidth() * ratio);
        Log.e(TAG, "widthMeasureSpec: " + widthMeasureSpec + " heightMeasureSpec: " + heightMeasureSpec);
        Log.e(TAG, "viewWidth: " + viewWidth + " viewHeight: " + viewHeight);
        Log.e(TAG, "newWidth: " + newWidth + " newHeight: " + newHeight);
        Log.e(TAG, "camActiv.getPreviewWidth(): " + camActiv.getPreviewWidth() + " camActiv.getPreviewHeight(): " + camActiv.getPreviewHeight());
        setMeasuredDimension(newWidth, newHeight);
    }

    /**
     * loads visible part of the preview
     */
    private void loadVisibleViewDimensions() {
        int viewWidth = 0, viewHeight = 0;
        int viewXCoord, viewYCoord;

        Rect rec = new Rect();
        getLocalVisibleRect(rec); // visible part of the preview
        int[] coords = new int[2];
        getLocationOnScreen(coords); // preview location on device screen
        if (rec != null) {
            viewXCoord = rec.top;
            viewYCoord = rec.left;
            viewWidth = rec.right - viewXCoord;
            viewHeight = rec.bottom - viewYCoord;
            Log.e(TAG, "getLocalVisibleRect " + rec.left + " " + rec.right + " " + rec.top + " " + rec.bottom);
            Log.e(TAG, "getLocationOnScreen " + coords[0] + " " + coords[1]);
            Log.e(TAG, "getWidth " + getWidth() + " getHeight " + getHeight());
        }

        this.viewVisibleWidth = viewWidth;
        this.viewvisibleHeight = viewHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        loadVisibleViewDimensions();

        Paint paint = new Paint();
        paint.setColor(Color.rgb(255, 0, 0));
        paint.setStrokeWidth(3);
        canvas.drawLine(0, viewvisibleHeight / 2, viewVisibleWidth, viewvisibleHeight / 2, paint);
    }
}
