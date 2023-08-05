package com.example.courseapp.acitvity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.courseapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailTill,passTill;
    private EditText edtEmail,edtPass;
    private TextView forgotIv,registerTv;
    private Button login;
    private String email,pass;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[@#$%^&+=])" +     // at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{4,}" +                // at least 4 characters
                    "$");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //init views
        initViews();
        firebaseAuth = FirebaseAuth.getInstance();

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        registerTv.setOnClickListener((view)->{
            startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
        });

        forgotIv.setOnClickListener((view)->{
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        });

        login.setOnClickListener((view)->{
            if (validateEmail() && validatePass()){
                loginUser();
            }
        });
    }

    private void loginUser() {
        progressDialog.setMessage("Checking user...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email,pass)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Login successful...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initViews() {
        emailTill = findViewById(R.id.emailTill);
        passTill = findViewById(R.id.passTill);
        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        forgotIv = findViewById(R.id.tvForgotPass);
        registerTv = findViewById(R.id.TvClickRegister);
        login = findViewById(R.id.btnLogin);

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
    private boolean validatePass(){
        pass = edtPass.getText().toString().trim();
        if (pass.isEmpty()){
            passTill.setError(" password is required!.");
            return false;
        }
        else if (!PASSWORD_PATTERN.matcher(pass).matches()){
            passTill.setError(" Password is too weak ");
            return false;
        }
        else if (pass.length()<8){
            passTill.setError("password must be minimum 8 or more characters!..");
            return false;
        }
        else {
            passTill.setError(null);
            passTill.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user!=null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            this.finish();
        }
    }
}