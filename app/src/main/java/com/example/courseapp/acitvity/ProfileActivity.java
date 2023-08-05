package com.example.courseapp.acitvity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.courseapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtEmailProfile;
    private EditText edtPhoneProfile,edtAddressProfile,edtNameProfile;
    private CircleImageView profileImage;

    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
    private FirebaseAuth firebaseAuth;

    private String name,phone,address;

    private Button editProfile;

    private TextInputLayout nameTill,phoneTill,addressTill;
    private Uri imageUri;

    private ImageButton back,gps;
    private ProgressDialog progressDialog;
    private double latitude,longitude;

    private String[] storagePermission;
    private String[] locationPermission;
    private String[] cameraPermission;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final int STORAGE_REQUEST_CODE= 100;
    private static final int CAMERA_REQUEST_CODE= 101;
    private static final int LOCATION_REQUEST_CODE= 111;
    private static final int IMAGE_PICK_GALLERY_CODE= 102;
    private static final int IMAGE_PICK_CAMERA_CODE= 103;

    private StorageReference storageReference = FirebaseStorage.getInstance().getReference("Profile_images/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editProfile= findViewById(R.id.btnEditProfile);
        txtEmailProfile = findViewById(R.id.txtEmailProfile);
        edtPhoneProfile = findViewById(R.id.edtPhoneProfile);
        edtAddressProfile = findViewById(R.id.edtAddressProfile);
        edtNameProfile = findViewById(R.id.edtNameProfile);
        profileImage = findViewById(R.id.profile_image);
        nameTill = findViewById(R.id.nameProfileTill);
        phoneTill = findViewById(R.id.phoneProfileTill);
        addressTill = findViewById(R.id.addressProfileTill);
        back = findViewById(R.id.backBtn);
        gps = findViewById(R.id.gpsBtn);



        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        //init permissions array
        cameraPermission = new String[]{
                Manifest.permission.CAMERA
        };
        storagePermission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        locationPermission = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        };



        firebaseAuth = FirebaseAuth.getInstance();

        loadProfile();

        back.setOnClickListener((view)->{
            onBackPressed();
        });
        gps.setOnClickListener((view)->{
            detectLocation();
        });


        profileImage.setOnClickListener((view)->{
            pickImageDialog();
        });
        editProfile.setOnClickListener((view)->{

            if (validateName() && validatePhone() && validateAddress()){
                if (imageUri == null){
                    //update without image
                    updateProfileWithoutImage();
                }
                else {
                    //update with image
                    updateProfile(imageUri);
                }
            }
        });
    }

    private void pickImageDialog() {
        String[] options = {
                "GALLERY","CAMERA"
        };
        AlertDialog.Builder dialog = new AlertDialog.Builder(ProfileActivity.this);
        dialog.setTitle("Choose image from")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            if (checkStoragePermission()){
                                pickImageFromGallery();
                            }
                            else {
                                requestStoragePermission();
                            }
                        }
                        else {
                            if (checkCameraPermission()){
                                pickImageFromCamera();
                            }
                            else {
                                requestCameraPermission();
                            }
                        }
                    }
                });
        dialog.create().show();
    }

    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(ProfileActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(ProfileActivity.this,locationPermission,LOCATION_REQUEST_CODE);
    }


    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(ProfileActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(ProfileActivity.this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(ProfileActivity.this,Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(ProfileActivity.this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private void updateProfile(Uri uri) {
        progressDialog.setMessage("uploading profile image...");
        progressDialog.show();

        StorageReference fileRef = storageReference.child(name+"."+getFileExtension(uri));
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.setMessage("updating profile data...");
                                Map<String,Object> map = new HashMap<>();
                                map.put("userName",name);
                                map.put("userProfile",uri.toString());
                                map.put("userMobile",phone);
                                map.put("userAddress",address);

                                DatabaseReference ref = FirebaseDatabase
                                        .getInstance()
                                        .getReference("Users");
                                ref.child(firebaseAuth.getUid())
                                        .updateChildren(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileActivity.this, "Profile updatede...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));

    }

    private void updateProfileWithoutImage() {
        progressDialog.setMessage("updating profile...");
        progressDialog.show();
        Map<String,Object> map = new HashMap<>();
        map.put("userName",name);
        map.put("userMobile",phone);
        map.put("userAddress",address);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users");
        dbRef.child(firebaseAuth.getUid())
                .updateChildren(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Profile updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateName(){
        name = edtNameProfile.getText().toString();
        if (name.isEmpty()){
            nameTill.setError("enter name...");
            return false;
        }
        else {
            nameTill.setError(null);
            nameTill.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePhone(){
        phone = edtPhoneProfile.getText().toString();
        if (phone.isEmpty()){
            phoneTill.setError("enter phone no...");
            return false;
        }
        else {
            phoneTill.setError(null);
            phoneTill.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateAddress(){
        address = edtAddressProfile.getText().toString();
        if (address.isEmpty()){
            addressTill.setError("enter address picking location using gps button...");
            return false;
        }
        else {
            addressTill.setError(null);
            addressTill.setErrorEnabled(false);
            return true;
        }
    }

    private void loadProfile() {
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email= ""+snapshot.child("userEmail").getValue();
                        String name= ""+snapshot.child("userName").getValue();
                        String profile= ""+snapshot.child("userProfile").getValue();
                        String address= ""+snapshot.child("userAddress").getValue();
                        String contact= ""+snapshot.child("userMobile").getValue();

                        txtEmailProfile.setText(email);
                        edtAddressProfile.setText(address);
                        edtNameProfile.setText(name);
                        edtPhoneProfile.setText(contact);
                        if (profile.equals("")){
                            profileImage.setImageResource(R.drawable.person);
                        }
                        else {
                            Glide.with(ProfileActivity.this).load(profile).into(profileImage);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted){
                        pickImageFromCamera();
                    }
                    else {
                        requestCameraPermission();
                    }
                }
                break;
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
            case STORAGE_REQUEST_CODE:
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted){
                        pickImageFromGallery();
                    }
                    else {
                        requestStoragePermission();
                    }
                }
                break;

        }
    }

    private void pickImageFromGallery() {

        Intent i =new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickImageFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"title");
        values.put(MediaStore.Images.Media.DESCRIPTION,"desc");
        imageUri =getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void detectLocation() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission()){
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            try {
                                if (location!=null){
                                    Geocoder geocoder = new Geocoder(ProfileActivity.this, Locale.getDefault());
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
                                    String address = addresses.get(0).getAddressLine(0);
                                    edtAddressProfile.setText(address);

                                }
                            }catch (Exception e){
                                Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            requestLocationPermission();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case IMAGE_PICK_CAMERA_CODE:
                if (resultCode == RESULT_OK){
                    profileImage.setImageURI(imageUri);
                }
                break;
            case IMAGE_PICK_GALLERY_CODE:
                if (resultCode == RESULT_OK){
                    imageUri = data.getData();
                    profileImage.setImageURI(imageUri);
                }
                break;
        }
    }
}