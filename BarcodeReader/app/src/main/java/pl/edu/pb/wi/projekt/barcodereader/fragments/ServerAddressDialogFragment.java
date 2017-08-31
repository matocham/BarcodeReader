package pl.edu.pb.wi.projekt.barcodereader.fragments;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.ServerConst;
import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.asyncTasks.CheckAddressAsyncTask;
import pl.edu.pb.wi.projekt.barcodereader.interfaces.OnServerAddressInput;

/**
 * Created by Mateusz on 05.11.2016.
 * handle user logging
 */
public class ServerAddressDialogFragment extends DialogFragment {

    public static final int ADDRESS_OK = 2;
    public static final int ADDRESS_FAIL = 3;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADDRESS_OK:
                    listener.serverAddressSet(addressEditText.getText().toString().trim());
                    errorMessage.setVisibility(View.GONE);
                    ServerAddressDialogFragment.this.dismiss();
                    break;
                case ADDRESS_FAIL:
                    errorMessage.setText(R.string.bad_address_error_message);
                    errorMessage.setVisibility(View.VISIBLE);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private EditText addressEditText;
    TextView errorMessage;
    Button ok, cancel;
    OnServerAddressInput listener;

    public static ServerAddressDialogFragment getInstance(OnServerAddressInput listener) {
        ServerAddressDialogFragment instance = new ServerAddressDialogFragment();
        instance.listener = listener;
        instance.setCancelable(false);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_server, container);

        findWidgets(view);
        configureOkButton();
        configureCancelButton();
        return view;
    }

    private void findWidgets(View view) {
        ok = (Button) view.findViewById(R.id.okButton);
        cancel = (Button) view.findViewById(R.id.cancelButton);
        addressEditText = (EditText) view.findViewById(R.id.addressEditText);
        errorMessage = (TextView) view.findViewById(R.id.error_message);
    }

    private void configureOkButton() {
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addressEditText.length() == 0) {
                    errorMessage.setText(R.string.provide_address);
                    errorMessage.setVisibility(View.VISIBLE);
                } else {
                    new CheckAddressAsyncTask(getContext(), handler)
                            .execute(addressEditText.getText().toString().trim());
                }
            }
        });
    }

    private void configureCancelButton() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.serverAddressSet(null);
                dismiss();
            }
        });
    }

    /**
     * used to provide custom animations
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.server_address_dialog_title);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_Window;
        return dialog;
    }
}
