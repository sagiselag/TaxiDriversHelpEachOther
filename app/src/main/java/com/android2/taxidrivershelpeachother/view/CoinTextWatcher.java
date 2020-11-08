package com.android2.taxidrivershelpeachother.view;

import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.widget.TextView;

import java.util.Currency;
import java.util.Locale;

public class CoinTextWatcher implements TextWatcher {

    private TextView textView;

    public CoinTextWatcher(TextView textView){
        this.textView = textView;
    }



    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().startsWith(Currency.getInstance(Locale.getDefault()).getSymbol())) {
            textView.setText(String.format("%s%s",
                    Currency.getInstance(Locale.getDefault()).getSymbol(), s.toString().replace(Currency.getInstance(Locale.getDefault()).getSymbol(), "")));
            Selection.setSelection((Spannable) textView.getText(), textView.getText().length());

        }
    }
}
