package pl.edu.pb.wi.projekt.barcodereader;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Mateusz on 05.11.2016.
 * Disables view that should be disabled when text length in EditText is insufficient and displays error message
 */
public class TextValidator implements TextWatcher {
    private EditText field;
    String errorMessage;
    private View dependantView;
    private int constraint;

    public TextValidator(EditText field, int constraint, View dependantView, String errorMessage) {
        this.field = field;
        this.constraint = constraint;
        this.dependantView = dependantView;
        this.errorMessage = errorMessage;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(field.getText().length()<constraint){
            dependantView.setEnabled(false);
            field.setError(errorMessage);
        }
    }
}
