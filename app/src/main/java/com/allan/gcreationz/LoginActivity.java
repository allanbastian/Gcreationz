package com.allan.gcreationz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText phoneEditText, otpEditText;
    private Button submitBtn, verifyBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerificationStateChangedCallbacks;
    private String mobile, mVerificationId, smsCode;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }
        phoneEditText = findViewById(R.id.phone);
        otpEditText = findViewById(R.id.otp);
        submitBtn = findViewById(R.id.submit_btn);
        verifyBtn = findViewById(R.id.verify_btn);
        progressDialog = new ProgressDialog(this);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobile = phoneEditText.getText().toString();
                if (!mobile.equalsIgnoreCase("")) {
                    mobile = "+91" + mobile;
                    verifyMobileNumber(mobile);
                } else {
                    phoneEditText.setError("please enter valid mobile number");
                    phoneEditText.requestFocus();
                }
            }
        });
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setTitle("Please wait....");
                progressDialog.show();
                String otp = otpEditText.getText().toString();
                if (!otp.equalsIgnoreCase("")) {
                    verifyPhoneNumberWithCode(otp);
                } else {
                    otpEditText.setError("Please enter otp");
                    otpEditText.requestFocus();
                }
            }
        });

        onVerificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                progressDialog.dismiss();
                smsCode = phoneAuthCredential.getSmsCode();
                if (smsCode != null) {
                    otpEditText.setText(smsCode);
                } else {
                    Toast.makeText(LoginActivity.this, "Enter The OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.dismiss();
                Log.e("MainActivity", "onVerificationFailed: " + e.getLocalizedMessage());
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("otplogin", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                verifyBtn.setVisibility(View.VISIBLE);
                otpEditText.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
            }
        };
    }

    private void verifyMobileNumber(String mobile) {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(mobile, 60, TimeUnit.SECONDS, this, onVerificationStateChangedCallbacks);
    }

    private void signInWithMobile(PhoneAuthCredential phoneAuthCredential) {

        firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    FirebaseUser currentUser = task.getResult().getUser();
                    String uid = currentUser.getUid();
                    Log.e("otp login", "" + currentUser.getUid());
                    Toast.makeText(LoginActivity.this, "Mobile number is verified succcessfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verifyPhoneNumberWithCode(String otp) {
        if (!otp.equals("")) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
            signInWithMobile(credential);
        } else {
            Toast.makeText(LoginActivity.this, "please enter the otp", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, DashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            finish();
            startActivity(intent);
        }
    }
}
