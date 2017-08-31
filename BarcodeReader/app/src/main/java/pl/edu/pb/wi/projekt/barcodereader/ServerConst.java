package pl.edu.pb.wi.projekt.barcodereader;

/**
 * Created by Mateusz on 06.11.2016.
 * Constants used with communication with server
 */
public class ServerConst {
    public static final String LOGIN_URL = "Inwentaryzator2.1/login";
    public static final String SERVER_AUTHORITY = "192.168.1.131:8080";
    public static final String DATABASE_URL = "Inwentaryzator2.1/getDatabase";
    public static final String PROTOCOL = "http";
    public static final int NO_INTERNET = 6;
    public static final int CONNECTION_TIMEOUT = 5;
    public static final int IO_ERROR = 8;
    public static final int LOGIN_RESULT = 7;
    public static final int DOWNLOAD_OK = 2;
    public static final int AUTH_ERROR = 3;
}
