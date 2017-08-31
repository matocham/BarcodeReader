package pl.edu.pb.wi.projekt.barcodereader.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.ServerConst;
import pl.edu.pb.wi.projekt.barcodereader.TextValidator;
import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.asyncTasks.LoginAsyncTask;
import pl.edu.pb.wi.projekt.barcodereader.interfaces.OnLoginSuccessListener;

/**
 * Created by Mateusz on 05.11.2016.
 * handle user logging
 */
public class LoginDialogFragment extends DialogFragment implements TextWatcher {

    private static final int LOGIN_CONSTRAINT = 4;
    private static final int PASSWORD_CONSTRAINT = 4;

    private EditText loginEditText, passwordEditText;
    TextView errorMessage;
    Button login, cancel;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ServerConst.CONNECTION_TIMEOUT:
                    Utils.showInfoDialog(getContext(), getContext().getString(R.string.connection_error)
                            , getContext().getString(R.string.connection_timeout));
                    break;
                case ServerConst.NO_INTERNET:
                    Utils.showInfoDialog(getContext(), getContext().getString(R.string.connection_error)
                            , getContext().getString(R.string.no_internet));
                    break;
                case ServerConst.IO_ERROR:
                    Utils.showInfoDialog(getContext(), getContext().getString(R.string.file_send_error)
                            , getContext().getString(R.string.server_connection_error));
                    break;
                case ServerConst.LOGIN_RESULT:
                    boolean status = (boolean) msg.obj;
                    if (status) {
                        dismiss();
                        Utils.saveUserCredentials(getContext(),loginEditText.getText().toString(),passwordEditText.getText().toString());
                        listener.onLoginEnd(true);
                    } else {
                        errorMessage.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    private OnLoginSuccessListener listener;

    public static LoginDialogFragment getInstance(OnLoginSuccessListener onLoginEndListener) {
        LoginDialogFragment instance = new LoginDialogFragment();
        instance.setListener(onLoginEndListener);
        instance.setCancelable(false);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_login, container);

        findWidgets(view);
        configureLoginEditText();
        configurePasswordEditText();
        configureLoginButton();
        configureCancelButton();

        return view;
    }

    private void findWidgets(View view) {
        login = (Button) view.findViewById(R.id.loginButton);
        cancel = (Button) view.findViewById(R.id.cancelButton);
        loginEditText = (EditText) view.findViewById(R.id.loginEditText);
        passwordEditText = (EditText) view.findViewById(R.id.passwordEditText);
        errorMessage = (TextView) view.findViewById(R.id.error_message);
    }

    private void configureCancelButton() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLoginEnd(false);
                dismiss();
            }
        });
    }

    private void configureLoginButton() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorMessage.setVisibility(View.GONE);
                new LoginAsyncTask(getContext(), handler)
                        .execute(loginEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });
    }

    private void configurePasswordEditText() {
        passwordEditText.addTextChangedListener(new TextValidator(passwordEditText, PASSWORD_CONSTRAINT, login, "Hasło musi składać się co najmniej z 4 znaków"));
        passwordEditText.addTextChangedListener(this);
    }

    private void configureLoginEditText() {
        loginEditText.addTextChangedListener(new TextValidator(loginEditText, LOGIN_CONSTRAINT, login, "Login musi składać się co najmniej z 4 znaków"));
        loginEditText.addTextChangedListener(this);
    }

    public void setListener(OnLoginSuccessListener listener) {
        this.listener = listener;
    }

    /**
     * used to provide custom animations
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.login_dialog_title);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_Window;
        return dialog;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    /**
     * method responsible for enabling Login button when both textView's are filled
     */
    @Override
    public void afterTextChanged(Editable s) {
        if (passwordEditText.getText().length() >= PASSWORD_CONSTRAINT &&
                loginEditText.getText().length() >= LOGIN_CONSTRAINT) {
            login.setEnabled(true);
        }
    }
}
