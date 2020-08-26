package com.android2.taxidrivershelpeachother.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.model.FireBaseHandler;
import com.android2.taxidrivershelpeachother.model.MyPhoneTextWatcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FragmentManager fragmentManager;
    private int PHONE_CALL_REQUEST_CODE = MainActivity.PHONE_CALL_REQUEST_CODE;
    private final int WRITE_PERMISSION_REQUEST = MainActivity.WRITE_PERMISSION_REQUEST;
    private final int READ_PERMISSION_REQUEST = MainActivity.READ_PERMISSION_REQUEST;
    private EditText phoneNumberET;
    private Button loginButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    @Override
    public void onStart() {
        super.onStart();
        //        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.root_layout, new MenuFragment(), null).addToBackStack(null);
//        fragmentTransaction.commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        phoneNumberET = view.findViewById(R.id.phoneNumberEt);
        final MyPhoneTextWatcher phoneTextWatcher = new MyPhoneTextWatcher(getContext(), phoneNumberET);
        final String countryCode = phoneTextWatcher.getCountryCode();

        permissionsInit();
        phoneNumberET.setText(countryCode);
        phoneNumberET.addTextChangedListener(phoneTextWatcher);

        loginButton = view.findViewById(R.id.login_activity_login_btn);
        Button registerButton = view.findViewById(R.id.login_activity_register_btn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumberET.getText().length() == countryCode.length() + 10) {
                    FireBaseHandler.getInstance().sendVerificationCode(getActivity(), fragmentManager, phoneNumberET.getText().toString());
                }
                else {
                    phoneNumberET.setError(getString(R.string.please_enter_ten_digits_phone_number));
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.root_layout, new RegisterDriverInfoFragment(), null).addToBackStack(null);
                fragmentTransaction.commit();
//                Intent callIntent = new Intent(Intent.ACTION_CALL);
//                callIntent.setData(Uri.parse("tel:0526868684"));
//                startActivity(callIntent);
            }
        });

        return view;
    }

    private void permissionsInit() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PHONE_CALL_REQUEST_CODE);
            }
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            }
            if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PHONE_CALL_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), getString(R.string.cantWorkProperly), Toast.LENGTH_LONG).show();
            }
        }
    }
}
