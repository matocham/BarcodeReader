package pl.edu.pb.wi.projekt.barcodereader.asyncTasks;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.database.BcDatabaseHelper;
import pl.edu.pb.wi.projekt.barcodereader.database.ColumnNotFoundException;
import pl.edu.pb.wi.projekt.barcodereader.database.TableNotFoundException;
import pl.edu.pb.wi.projekt.barcodereader.fragments.SettingsFragment;

/**
 * Created by Mateusz on 26.07.2016.
 * Copy database from location on device to application internal memory
 */
public class CopyDatabaseAsyncTask extends AsyncTask<Uri, Integer, Boolean> {
    private Context context;
    public static final String LOCATION = "db";
    private static final String TEMP_DB_NAME = "temp_db.db";
    Handler handler;
    AlertDialog dialog;
    private boolean cancelled = false;
    private String uriString;

    public CopyDatabaseAsyncTask(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    protected Boolean doInBackground(Uri... params) {
        Uri path = params[0];
        getDatabaseName(path);

        File databaseDir = context.getDir(LOCATION, Context.MODE_PRIVATE);
        String dbName = "database.db";//TEMP_DB_NAME+"_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".db";
        File dbFile = new File(databaseDir, dbName);
        File tempFile = new File(databaseDir, TEMP_DB_NAME);

        InputStream is;
        OutputStream os;
        try {
            is = context.getContentResolver().openInputStream(path);
            os = new FileOutputStream(tempFile);
            Utils.copy(is, os);
            is.close();
            os.close();

            validateDatabase();
            Utils.copy(tempFile, dbFile);
        } catch (IOException e) {
            e.printStackTrace();
            handler.sendMessage(Utils.createMessage(SettingsFragment.COPY_ERROR, null));
            cancelled = true;
        } catch (TableNotFoundException e) {
            e.printStackTrace();
            cancelled = true;
            handler.sendMessage(Utils.createMessage(SettingsFragment.DB_TABLE_ERROR, e));
        } catch (ColumnNotFoundException e) {
            e.printStackTrace();
            cancelled = true;
            handler.sendMessage(Utils.createMessage(SettingsFragment.DB_COLUMN_ERROR, e));
        } finally {
            tempFile.delete();
        }
        return true;
    }

    private void validateDatabase() throws ColumnNotFoundException, TableNotFoundException {
        try {
            SQLiteDatabase db = BcDatabaseHelper.getInstance(context, TEMP_DB_NAME).getReadableDatabase();
            Utils.checkDatabaseSchema(db);
        } finally {
            BcDatabaseHelper.closeAll();
        }
    }

    private void getDatabaseName(Uri path) {
        uriString = path.toString();
        uriString = Uri.decode(uriString);
        uriString = uriString.substring(uriString.lastIndexOf(":") + 1);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = Utils.showProgressDialog(context, "Loading database", "Please wait ...");
        dialog.show();
        BcDatabaseHelper.closeAll();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        dialog.dismiss();
        if (!cancelled) {
            handler.sendMessage(Utils.createMessage(SettingsFragment.COPY_OK, uriString));
        }
    }

}
