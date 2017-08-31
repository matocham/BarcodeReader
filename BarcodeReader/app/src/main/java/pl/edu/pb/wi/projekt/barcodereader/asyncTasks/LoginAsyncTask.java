package pl.edu.pb.wi.projekt.barcodereader.asyncTasks;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.ServerConst;
import pl.edu.pb.wi.projekt.barcodereader.Utils;

/**
 * Created by Mateusz on 05.11.2016.
 * Performs http call to login user on remote server
 */
//TODO test https
public class LoginAsyncTask extends AsyncTask<String, Integer, Boolean> {
    private AlertDialog waitDialog;
    private Context context;
    private Handler handler;
    private boolean connectionError = false;

    public LoginAsyncTask(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        waitDialog = Utils.showProgressDialog(context, context.getString(R.string.login_dialog_title)
                , context.getString(R.string.please_wait));
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String username = params[0];
        String password = params[1];
        boolean loginStatus = false;
        //backdoor

        /*if (username.equals("admin") && password.equals("admin")) {
            return true;
        }*/
        // no internet
        if (!Utils.getNetworkStatus(context)) {
            connectionError = true;
            handler.sendMessage(Utils.createMessage(ServerConst.NO_INTERNET, null));
            return false;
        }

        //make request
        HttpURLConnection conn;
        BufferedReader reader;
        try {
            URL url = getUrl();
            conn = connect(url, username, password);
            int response = conn.getResponseCode();
            Log.d("Login async", "The response is: " + response);

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String result = reader.readLine();
            Log.e("LoginAsync", result);
            if (result.equals("success")) {
                loginStatus = true;
            }
            reader.close();
            conn.disconnect();
        } catch (MalformedURLException | SocketTimeoutException e) {
            e.printStackTrace();
            connectionError = true;
            handler.sendMessage(Utils.createMessage(ServerConst.CONNECTION_TIMEOUT, null));
        } catch (IOException e) {
            e.printStackTrace();
            connectionError = true;
            handler.sendMessage(Utils.createMessage(ServerConst.IO_ERROR, null));
        }

        return loginStatus;
    }

    @NonNull
    private HttpURLConnection connect(URL url, String username, String password) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        OutputStream output= conn.getOutputStream();
        JSONObject obj = new JSONObject();

        try {
            obj.put("login", username);
            obj.put("password", Utils.encrypt(password));
            output.write(obj.toString().getBytes(Charset.forName("UTF-8")));
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        conn.connect();
        return conn;
    }

    @NonNull
    private URL getUrl() throws MalformedURLException {
        String authority = Utils.getServerAuthority(context);
        if (authority.isEmpty()) {
            authority = ServerConst.SERVER_AUTHORITY;
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(ServerConst.PROTOCOL)
                .encodedAuthority(authority)
                .appendPath(ServerConst.LOGIN_URL);
        String stringUrl = builder.build().toString();

        return new URL(stringUrl);
    }

    @Override
    protected void onPostExecute(Boolean status) {
        super.onPostExecute(status);
        waitDialog.dismiss();
        if (!connectionError) {
            handler.sendMessage(Utils.createMessage(ServerConst.LOGIN_RESULT, status));
        }
    }
}
