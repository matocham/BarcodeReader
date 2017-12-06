package pl.edu.pb.wi.projekt.barcodereader.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.List;

import pl.edu.pb.wi.projekt.barcodereader.CameraPreview;
import pl.edu.pb.wi.projekt.barcodereader.utils.ScreenUtils;
import pl.edu.pb.wi.projekt.barcodereader.utils.Utils;
import pl.edu.pb.wi.projekt.barcodereader.ICameraDecoder;
import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.barcodeProcessing.detector.DetectorThread;

/**
 * activity responsible for displaying preview from camera
 * sets camera parameters according to those specified in settings
 */
public class CameraCaptureActivity extends AppCompatActivity implements ICameraDecoder {

    public static final String TAG = "CameraCaptureActivity";
    public static int REQUEST_CAMERA_PERMISSION = 100;
    private Camera mCamera;
    private CameraPreview mPreview;
    Camera.Size pictureSize = null;
    Camera.Size previewSize = null;
    FrameLayout preview;
    DetectorThread dt;
    PowerManager.WakeLock wakeLock;

    String focusMode;
    String torchMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);

        dt = new DetectorThread();
        dt.start();
        
        mCamera = Utils.getCameraOrFinish(this);

        getCameraSize(mCamera);
        configCamera(mCamera);

        mPreview = new CameraPreview(this, mCamera);
        mPreview.setDetector(dt);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();
        Log.e(TAG, "OnCreateCalled");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock.isHeld()) wakeLock.release();
        releaseCamera();
        Log.e(TAG, "OnPauseCalled");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "OnResumeCalled");

        if (mCamera == null) {
            mCamera = Utils.getCameraOrFinish(this);
            configCamera(mCamera);
            mPreview.setmCamera(mCamera);
            mPreview.getHolder().addCallback(mPreview);
        }
        if (!wakeLock.isHeld()) wakeLock.acquire();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "OnStopCalled");
        dt.finish();
    }

    private void releaseCamera() {
        Log.e(TAG, "release camera called");
        if (mCamera != null) {
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * reads configuration from shared preferences and configures the camera
     */
    private void configCamera(Camera cam) {
        String defaultValue = getResources().getString(R.string.prefs_default_value);
        Camera.Parameters param;
        param = cam.getParameters();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String preview;
        focusMode = sharedPref.getString(getResources().getString(R.string.prefs_camera_autofocus_key), "");
        torchMode = sharedPref.getString(getResources().getString(R.string.prefs_camera_torch_key), "");
        Log.e("CameraCapture",focusMode+ " "+torchMode);
        preview = sharedPref.getString(getResources().getString(R.string.prefs_camera_picture_size_key), "");

        if (preview.length() < 1 || preview.equals(defaultValue)) { // no value in shared prefs
            pictureSize = getCameraSize(cam);
            previewSize = getCameraPreviewSize(cam);
        } else {
            int width = Integer.parseInt(preview.substring(0, preview.indexOf("x")));
            int height = Integer.parseInt(preview.substring(preview.indexOf("x") + 1));
            pictureSize = getCameraSize(cam, width, height);
            previewSize = getCameraPreviewSize(cam, width); // ustawienia dla zdjęcia odzwierciedlają ustawienia dla ekranu
        }

        if (!focusMode.equals(defaultValue) ) {
            param.setFocusMode(focusMode);
        }
        if (!torchMode.equals(defaultValue)) {
            param.setFlashMode(torchMode);
        }
        param.setPictureSize(pictureSize.width, pictureSize.height);
        param.setPreviewSize(previewSize.width, previewSize.height);
        cam.setParameters(param);
    }

    /**
     * get camera size that is closest to 1024xheight
     * @param cam camera used by activity
     * @return the best size
     */
    private Camera.Size getCameraSize(Camera cam) {
        List<Camera.Size> sizeList = cam.getParameters().getSupportedPictureSizes();
        Camera.Size pictureSize = sizeList.get(0);
        int screenSize = ScreenUtils.getXScreenSize(this);

        int width, distance = 100000; // distance from 1024 px width

        for (int i = 1; i < sizeList.size(); i++) {
            width = sizeList.get(i).width;

            if (screenSize>width && distance > Math.abs(width - 1024)) {
                pictureSize = sizeList.get(i);
                distance = Math.abs(width - 1024);
            }
        }
        return pictureSize;
    }

    /**
     * get camera size that matches given width and height, else return default size
     *
     * @param cam
     * @param w width of the image
     * @param h height of the image
     * @return
     */
    private Camera.Size getCameraSize(Camera cam, int w, int h) {
        for (Camera.Size size : cam.getParameters().getSupportedPictureSizes()) {
            if (size.width == w && size.height == h) return size;
        }
        return getCameraSize(cam);
    }

    /**
     * gets camera preview size that is the closest to screen width
     * @param cam
     * @return
     */
    private Camera.Size getCameraPreviewSize(Camera cam) {
        int screenSize = ScreenUtils.getXScreenSize(this);

        List<Camera.Size> previewSizeList = cam.getParameters().getSupportedPreviewSizes();
        int width, height;

        Camera.Size bestPreviewSize = previewSizeList.get(0);
        for (int i = 1; i < previewSizeList.size(); i++) {
            width = previewSizeList.get(i).width;
            height = previewSizeList.get(i).height;
            if(width>screenSize) continue;

            if (width * height > (bestPreviewSize.width * bestPreviewSize.height)) {
                bestPreviewSize = previewSizeList.get(i);
            }
        }
        return bestPreviewSize;
    }

    /**
     * gets preview size that's width is the closest to specified one
     */
    private Camera.Size getCameraPreviewSize(Camera cam, int w) {
        List<Camera.Size> previewSizeList = cam.getParameters().getSupportedPreviewSizes();
        int width, height;
        int distance = 100000;

        Camera.Size bestPreviewSize = previewSizeList.get(0);
        for (int i = 1; i < previewSizeList.size(); i++) {
            width = previewSizeList.get(i).width;
            height = previewSizeList.get(i).height;
            if (distance > Math.abs(width - w) /*&& (double) width / height > 1.5*/) {
                bestPreviewSize = previewSizeList.get(i);
                distance = Math.abs(width - w);
            }
        }
        return bestPreviewSize;
    }

    @Override
    public int getPreviewWidth() {
        if (previewSize != null) return previewSize.width;
        return 0;
    }

    @Override
    public int getPreviewHeight() {
        if (previewSize != null) return previewSize.height;
        return 0;
    }

    /**
     * handle scanning results
     */
    @Override
    public void sendResult(String result) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(SearchActivity.BARCODE_KEY,result);
        intent.setAction(SearchActivity.SCAN_RESULT_ACTION);
        startActivity(intent);
        finish();
    }

    /**
     * requests camera focus. Flash turns on if set to Auto in settings
     */
    public void getFocus(View view) {
        if(focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO) && mCamera!=null){
            mCamera.autoFocus(null);
        }
    }
}
