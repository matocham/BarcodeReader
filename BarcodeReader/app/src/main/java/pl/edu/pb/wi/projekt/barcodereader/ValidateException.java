package pl.edu.pb.wi.projekt.barcodereader;

/**
 * Created by Mateusz on 29.11.2017.
 */

public class ValidateException extends Exception {
    int id;

    public ValidateException(int messageId) {
        id = messageId;
    }

    public int getId() {
        return id;
    }
}
