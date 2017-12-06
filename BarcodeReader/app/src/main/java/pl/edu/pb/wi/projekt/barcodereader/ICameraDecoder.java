package pl.edu.pb.wi.projekt.barcodereader;

/**
 * Listener that will be informed about scanning results, provides information required to start scanning
 * Implemented by {@link pl.edu.pb.wi.projekt.barcodereader.activities.CameraCaptureActivity}
 */
public interface ICameraDecoder {
    int getPreviewWidth();

    int getPreviewHeight();

    void sendResult(String result);
}
