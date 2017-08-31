package pl.edu.pb.wi.projekt.barcodereader.asyncTasks;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.ServerConst;
import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.fragments.ServerAddressDialogFragment;

/**
 * Created by Mateusz on 04.01.2017.
 */

public class CheckAddressAsyncTask extends AsyncTask<String, Integer, Void> {
    private Context context;
    AlertDialog dialog;

    private Handler handler;

    public CheckAddressAsyncTask(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    protected Void doInBackground(String... params) {
        String address = params[0];
        try {
            connect(getUrl(address)).disconnect();
            handler.sendMessage(Utils.createMessage(ServerAddressDialogFragment.ADDRESS_OK, null));
        } catch (IOException e) {
            e.printStackTrace();
            handler.sendMessage(Utils.createMessage(ServerAddressDialogFragment.ADDRESS_FAIL, null));
        }
        return null;
    }

    @NonNull
    private HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000 /* milliseconds */);
        conn.setConnectTimeout(5500 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setChunkedStreamingMode(1024); // 1KB
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type","application/json");
        conn.connect();
        return conn;
    }

    @NonNull
    private URL getUrl(String address) throws MalformedURLException {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(ServerConst.PROTOCOL)
                .encodedAuthority(address)
                .appendPath(ServerConst.LOGIN_URL);
        String stringUrl = builder.build().toString();

        return new URL(stringUrl);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = Utils.showProgressDialog(context, context.getString(R.string.address_check_dialog_title)
                , context.getString(R.string.please_wait));
        dialog.show();
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        dialog.dismiss();
    }

}
