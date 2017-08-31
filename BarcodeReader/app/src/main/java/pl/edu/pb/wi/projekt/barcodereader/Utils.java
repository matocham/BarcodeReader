package pl.edu.pb.wi.projekt.barcodereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pl.edu.pb.wi.projekt.barcodereader.database.BcDatabaseHelper;
import pl.edu.pb.wi.projekt.barcodereader.database.ColumnNotFoundException;
import pl.edu.pb.wi.projekt.barcodereader.database.Contract;
import pl.edu.pb.wi.projekt.barcodereader.database.TableNotFoundException;

/**
 * Created by Mateusz on 27.07.2016.
 */
public class Utils {

    public static final String TAG = "Utils";

    static public File getOutputImageFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Barcode Reader");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = getTimestamp();
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
        Log.e(TAG, mediaFile.getAbsolutePath());
        return mediaFile;
    }

    public static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    public static String getFormattedTime() {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(Calendar.getInstance(Locale.getDefault()).getTime());
    }

    /**
     * pack data from cursor into {@link SearchResultRow} objects, witch are easier to manipulate
     * @param queryCursor result of the search
     * @return objects representing search results
     */
    public static List<SearchResultRow> packToObjects(Cursor queryCursor) {
        List<SearchResultRow> resultRows = new ArrayList<>();
        queryCursor.moveToFirst();

        do {
            resultRows.add(new SearchResultRow(queryCursor.getString(0), queryCursor.getString(1), queryCursor.getString(2)));
        } while (queryCursor.moveToNext());


        return resultRows;
    }

    public static boolean checkDatabaseAvailable(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return !sharedPref.getString(context.getResources().getString(R.string.prefs_storage_source_key), context.getResources().
                getString(R.string.prefs_storage_default)).equals(context.getResources().getString(R.string.prefs_storage_default));
    }

    public static boolean checkUserVerified(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getResources().getString(R.string.user_verified), false);
    }

    public static void resetUserData(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putBoolean(context.getResources().getString(R.string.user_verified), false).commit();
    }

    public static AlertDialog showProgressDialog(Context context, String title, String message) {
        ProgressDialog dialog = ProgressDialog.show(context, title, message, true, false);
        return dialog;
    }

    public static void showInfoDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /**
     * shows dialog that close activity after acceptance
     * @param context context where to display dialog
     * @param title title of the dialog
     * @param message message displayed in dialog
     */
    public static void showCloseDialog(final Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((AppCompatActivity) context).finish();
                    }
                });
        alertDialog.show();
    }

    /**
     * create message used in {@link android.os.Handler}
     * @param what code ot the message
     * @param object object appended to message
     * @return message ready to send ussing Handler
     */
    public static Message createMessage(int what, Object object) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = object;
        return msg;
    }


    public static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static boolean checkFlashlightHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public static boolean checkAutofocus(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
    }

    public static Camera getCameraInstance() throws IllegalAccessException{

        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        if(c == null){
            throw new IllegalAccessException("Camera is taken by other application");
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * try to acces camera and finish activity if camera is unavailable
     */
    public static Camera getCameraOrFinish(Activity activity){
        Camera camera = null;
        try {
            camera = getCameraInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Toast.makeText(activity, R.string.cant_load_camera,Toast.LENGTH_LONG).show();
            activity.finish();
        }
        return camera;
    }

    public static void checkDatabaseSchema(SQLiteDatabase database) throws TableNotFoundException, ColumnNotFoundException {
        List<String> tables = BcDatabaseHelper.getTableNames(database);
        Map<String,String[]> requiredTables = new HashMap<>();
        requiredTables.put(Contract.Devices.TABLE_NAME,Contract.Devices.PROJECTION_ALL);
        requiredTables.put(Contract.Persons.TABLE_NAME, Contract.Persons.PROJECTION_ALL);
        requiredTables.put(Contract.Sections.TABLE_NAME, Contract.Sections.PROJECTION_ALL);

        Log.e(TAG,"Loaded tables: "+tables.size() +" "+tables);
        for(String requiredTable : requiredTables.keySet()){
            Log.e(TAG,"Checking table "+requiredTable);
            if(tables.contains(requiredTable)){
                checkDatabaseTable(database,requiredTable,requiredTables.get(requiredTable));
            } else {
                throw new TableNotFoundException(requiredTable);
            }
            Log.e(TAG,"Check table "+requiredTable +" ok");
        }
    }

    private static void checkDatabaseTable(SQLiteDatabase database, String tableName, String[] requiredColumns) throws ColumnNotFoundException {
        Cursor c = database.rawQuery("SELECT * FROM "+tableName+" WHERE 0", null);
        try {
            String[] columnNames = c.getColumnNames();
            for(String requiredColumn : requiredColumns){
                Log.e(TAG,"Checking required column "+requiredColumn);
                if(!tableContains(columnNames, requiredColumn)){
                    throw new ColumnNotFoundException(tableName, requiredColumn);
                }
                Log.e(TAG,"Check required column "+requiredColumn+" ok");
            }
        } finally {
            c.close();
        }
    }

    private static boolean tableContains(String[] table, String value) {
        for(String tableValue : table){
            if(tableValue.equals(value.toUpperCase())){
                return true;
            }
        }
        return false;
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }

    public static void copy(File in, File out) throws IOException {
        InputStream is= new FileInputStream(in);
        OutputStream os = new FileOutputStream(out);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        is.close();
        os.close();
    }
}
