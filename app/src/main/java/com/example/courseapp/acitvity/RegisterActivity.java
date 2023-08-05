package com.example.courseapp.acitvity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.courseapp.R;
import com.example.courseapp.models.UsersModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {


    private TextInputLayout nameTill,emailTill,passwordTill,confirmPassTill,mobileTill,addressTill;
    private EditText edtName,edtEmail,edtPassword,edtConfirmPass,edtMobile,edtAddress;
    private Button btnRegister;
    private ImageButton back,gps;
    private String name,email,pass,confirmPass,mobile,address;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
    private FirebaseAuth firebaseAuth;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[@#$%^&+=])" +     // at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{4,}" +                // at least 4 characters
                    "$");
    private ProgressDialog progressDialog;
    private static final int LOCATION_REQUEST_CODE = 100;

    private String[] locationPermission;
    private double latitude=0.0,longitude=0.0;
    FusedLocationProviderClient fusedLocationProviderClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //init views
        initViews();

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission array
        locationPermission =new String[]
        {
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        firebaseAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateName() && validateEmail() && validatePass()
                        && validateConfirmPass() && validateAddress()
                        && validateMobile() && checkLatitudeLongitude()){
                    registerUser();
                }
            }
        });

        back.setOnClickListener((v)->{
            onBackPressed();
        });

        gps.setOnClickListener((v)->{
            if (checkLocationPermission()){
                detectLocation();
            }
            else {
                requestLocationPermission();
            }
        });

    }

    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(RegisterActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        return result;
    }
    private boolean checkLatitudeLongitude(){
        if (latitude == 0.0 && longitude == 0.0){
            Toast.makeText(this, "Pick location using gps button..", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(RegisterActivity.this,locationPermission,LOCATION_REQUEST_CODE);
    }
    private void registerUser() {
        progressDialog.setMessage("cheking user...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email,pass)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                       saveUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void saveUserInfo() {
        progressDialog.setMessage("saving user info...");
        UsersModel model = new UsersModel(
                name,email,"",mobile,address,""+latitude,""+longitude
        );
        databaseReference.child(firebaseAuth.getUid()).setValue(model)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration successfully..", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
    private boolean validatePass(){
        pass = edtPassword.getText().toString().trim();
        if (pass.isEmpty()){
            passwordTill.setError(" password is required!.");
            return false;
        }
        else if (!PASSWORD_PATTERN.matcher(pass).matches()){
            passwordTill.setError(" Password is too weak ");
            return false;
        }
        else if (pass.length()<8){
            passwordTill.setError("password must be minimum 8 or more characters!..");
            return false;
        }
        else {
            passwordTill.setError(null);
            passwordTill.setErrorEnabled(false);
            return true;
        }
    }
    private boolean validateAddress(){
        address = edtAddress.getText().toString().trim();
        if (address.isEmpty()){
            addressTill.setError(" address is required!.");
            return false;
        }
        else {
            addressTill.setError(null);
            addressTill.setErrorEnabled(false);
            return true;
        }
    }
    private boolean validateMobile(){
        mobile = edtMobile.getText().toString().trim();
        if (mobile.isEmpty()){
            mobileTill.setError(" Mobile no is required!.");
            return false;
        }
        else if (mobile.length()>10 || mobile.length()<10){
            mobileTill.setError("Mobile no must be 10 digit");
            return false;
        }
        else {
            mobileTill.setError(null);
            mobileTill.setErrorEnabled(false);
            return true;
        }
    }
    private boolean validateName(){
        name = edtName.getText().toString().trim();
        if (name.isEmpty()){
            nameTill.setError(" Name is required!.");
            return false;
        }
        else {
            nameTill.setError(null);
            nameTill.setErrorEnabled(false);
            return true;
        }
    }
    private boolean validateConfirmPass(){
        confirmPass = edtConfirmPass.getText().toString().trim();
        if (confirmPass.isEmpty()){
            confirmPassTill.setError(" confirm password is required!.");
            return false;
        }
        else if (!pass.equals(confirmPass)){
            confirmPassTill.setError("Password does not match!!");
            return false;
        }
        else {
            confirmPassTill.setError(null);
            confirmPassTill.setErrorEnabled(false);
            return true;
        }
    }

    private void initViews() {

        nameTill = findViewById(R.id.nameTill);
        emailTill = findViewById(R.id.emailTill);
        passwordTill = findViewById(R.id.passTill);
        confirmPassTill = findViewById(R.id.confirmPassTill);
        mobileTill = findViewById(R.id.mobileTill);
        addressTill = findViewById(R.id.addressTill);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPass);
        edtConfirmPass = findViewById(R.id.edtConfirmPass);
        edtMobile = findViewById(R.id.edtMobile);
        edtAddress = findViewById(R.id.edtAddress);
        btnRegister = findViewById(R.id.btnRegister);
        back = findViewById(R.id.backBtn);
        gps = findViewById(R.id.gpsBtn);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        detectLocation();
                    }
                    else {
                        requestLocationPermission();
                    }
                }
                break;
        }
    }

    private void detectLocation() {
        progressDialog.setMessage("checking location...");
        progressDialog.show();
        fusedLocationProviderClient  = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission()){
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location !=null){
                                progressDialog.dismiss();
                                Geocoder geocoder = new Geocoder(RegisterActivity.this, Locale.getDefault());
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
                                    edtAddress.setText(addresses.get(0).getAddressLine(0));

                                }catch (Exception e){
                                    Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        else {
            requestLocationPermission();
        }
    }
}