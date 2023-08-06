package com.example.courseapp.acitvity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.courseapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout emailTill;
    private EditText edtEmail;
    private Button reset;
    private String email;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailTill = findViewById(R.id.emailTill);
        edtEmail = findViewById(R.id.edtEmail);
        reset = findViewById(R.id.btnReset);
        back = findViewById(R.id.backBtn);

        progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();


        back.setOnClickListener((view)->{
            onBackPressed();
        });



        reset.setOnClickListener((view)->{
            if (validateEmail()){
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        progressDialog.setMessage("sending link...");
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset link sent to your email....", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateEmail(){
        email = edtEmail.getText().toString().trim();
        if (email.isEmpty()){
            emailTill.setError(" Email is required!.");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailTill.setError(" invalid email address!!");
            return false;
        }
        else {
            emailTill.setError(null);
            emailTill.setErrorEnabled(false);
            return true;
        }
    }
}