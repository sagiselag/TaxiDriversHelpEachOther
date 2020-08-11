package com.android2.taxidrivershelpeachother.view;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.model.FireBaseHandler;

public class AuthenticationFragment extends Fragment {
    private Button next;
    private TextView sendCodeAgainInTv, sendCodeAgainTv;
    private int timerTimeInSec = 30;
    private CountDownTimer countDownTimer;
    private int defaultTextColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.authentication_fragment, container, false);
        final EditText passwordEditText = view.findViewById(R.id.passwordEt);
        FireBaseHandler.getInstance().setPasswordEditText(passwordEditText);
        next = view.findViewById(R.id.authenticationNextButton);
        sendCodeAgainInTv = view.findViewById(R.id.sendCodeAgainInTv);
        sendCodeAgainTv = view.findViewById(R.id.sendCodeAgainTv);
        defaultTextColor = sendCodeAgainInTv.getCurrentTextColor();

        sendCodeAgainTv.setClickable(false);

        sendCodeAgainTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodeAgainInTv.setVisibility(View.VISIBLE);
                countDownTimer.start();
            }
        });

        countDownTimer = new CountDownTimer(timerTimeInSec * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendCodeAgainInTv.setText( String.valueOf((int)millisUntilFinished / 1000));
                sendCodeAgainTv.setTextColor(defaultTextColor);
                sendCodeAgainTv.setClickable(false);
            }

            @Override
            public void onFinish() {
                sendCodeAgainInTv.setVisibility(View.INVISIBLE);
                sendCodeAgainTv.setClickable(true);
                sendCodeAgainTv.setTextColor(Color.BLUE);
            }
        }.start();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passwordEditText.getText().toString().length() == 6) {
                    FireBaseHandler.getInstance().verifyCode(passwordEditText.getText().toString());
                }
                else {
                    passwordEditText.setError(getString(R.string.six_digits_number));
                }
            }
        });

        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(6);
        passwordEditText.setFilters(filterArray);
        Selection.setSelection(passwordEditText.getText(), passwordEditText.getText().length());

        return view;
    }
}