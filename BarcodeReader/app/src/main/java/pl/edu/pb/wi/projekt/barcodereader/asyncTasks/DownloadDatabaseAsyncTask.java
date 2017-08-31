package pl.edu.pb.wi.projekt.barcodereader.asyncTasks;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.journeyapps.barcodescanner.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.ServerConst;
import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.database.BcDatabaseHelper;
import pl.edu.pb.wi.projekt.barcodereader.database.ColumnNotFoundException;
import pl.edu.pb.wi.projekt.barcodereader.database.TableNotFoundException;
import pl.edu.pb.wi.projekt.barcodereader.fragments.SettingsFragment;

/**
 * Created by Mateusz on 06.11.2016.
 * Downloads database form remote server using user credentials form login form
 */
public class DownloadDatabaseAsyncTask extends AsyncTask<Void, Integer, Boolean> {
    private Context context;
    public static final String LOCATION = "db";
    private static final String TEMP_DB_NAME = "temp_db.db";
    AlertDialog dialog;

    private Handler handler;
    private boolean cancelled = false;

    public DownloadDatabaseAsyncTask(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        File databaseDir = context.getDir(LOCATION, Context.MODE_PRIVATE);
        String dbName = "database.db";//TEMP_DB_NAME+"_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".db";
        File dbFile = new File(databaseDir, dbName);
        File tempFile = new File(databaseDir, TEMP_DB_NAME);
        InputStream is;
        OutputStream os;

        if (!Utils.getNetworkStatus(context)) {
            handler.sendMessage(Utils.createMessage(ServerConst.NO_INTERNET, null));
            return false;
        }

        //make request
        try {
            URL url = getUrl();
            HttpURLConnection conn = connect(url);
            int response = conn.getResponseCode();
            Log.d("Download async", "The response is: " + response);
            if (response == 403){
                handler.sendMessage(Utils.createMessage(ServerConst.AUTH_ERROR, null));
                cancelled = true;
                return false;
            }
            is = new BufferedInputStream(conn.getInputStream());
            os = new FileOutputStream(tempFile);

            Utils.copy(is,os);

            conn.disconnect();
            is.close();
            os.close();
            validateDatabase();
            dbFile.delete();
            Utils.copy(tempFile,dbFile);
        } catch (MalformedURLException | SocketTimeoutException e) {
            e.printStackTrace();
            handler.sendMessage(Utils.createMessage(ServerConst.CONNECTION_TIMEOUT, null));
            cancelled = true;
        } catch (IOException e) {
            e.printStackTrace();
            handler.sendMessage(Utils.createMessage(ServerConst.IO_ERROR, null));
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

    @NonNull
    private HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("POST");
        conn.setChunkedStreamingMode(1024); // 1KB
        conn.setDoInput(true); // TODO test if required
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type","application/json");
        Utils.fillSendData(context, conn.getOutputStream());
        // Starts the query
        conn.connect();
        return conn;
    }

    @NonNull
    private URL getUrl() throws MalformedURLException {
        String authority = Utils.getServerAuthority(context);
        if(authority.isEmpty()){
            authority = ServerConst.SERVER_AUTHORITY;
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(ServerConst.PROTOCOL)
                .encodedAuthority(authority)
                .appendPath(ServerConst.DATABASE_URL);
        String stringUrl = builder.build().toString();

        return new URL(stringUrl);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = Utils.showProgressDialog(context, context.getString(R.string.downloading_database)
                , context.getString(R.string.please_wait));
        dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean v) {
        super.onPostExecute(v);
        dialog.dismiss();
        if (!cancelled) {
            handler.sendMessage(Utils.createMessage(ServerConst.DOWNLOAD_OK, null));
        }
    }

    private void validateDatabase() throws ColumnNotFoundException, TableNotFoundException {
        try{
            SQLiteDatabase db = BcDatabaseHelper.getInstance(context,TEMP_DB_NAME).getReadableDatabase();
            Utils.checkDatabaseSchema(db);
        } finally {
            BcDatabaseHelper.closeAll();
        }
    }
}
