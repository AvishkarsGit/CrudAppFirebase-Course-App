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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.courseapp.R;
import com.example.courseapp.models.CourseModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddCourseActivity extends AppCompatActivity {

    private TextInputLayout courseNameTill,coursePriceTill,courseLinkTill,courseDescTill,courseImageLinkTill;
    private TextInputEditText edtCourseName,edtCoursePrice,edtCourseLink,edtCourseDesc,edtCourseImageLink;
    private Button addCourse;
    private ImageView courseImageView;

    private String courseName,coursePrice,courseLink,courseDesc,courseImageUrl,courseImageLink,courseId;

    private ProgressDialog progressDialog;


    private FirebaseAuth firebaseAuth;
    private Uri imageUri;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Courses");

    private String[] storagePermission;

    private static final int STORAGE_REQUEST_CODE= 100;
    private static final int IMAGE_PICK_GALLERY_CODE= 101;


    private StorageReference storageReference =
             FirebaseStorage.getInstance()
            .getReference("Course_images/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        //init widgets
        initViews();

        //init progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission array
        storagePermission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        courseImageView.setOnClickListener((view)->{
            pickImageDialog();
        });

        addCourse.setOnClickListener((view)->{
            courseName = edtCourseName.getText().toString();
            coursePrice = edtCoursePrice.getText().toString();
            courseLink = edtCourseLink.getText().toString();
            courseDesc = edtCourseDesc.getText().toString();
            courseId = courseName;
            if (courseName.isEmpty()){
                courseNameTill.setError("Enter Course Name...");
            }
            else if (coursePrice.isEmpty()){
                coursePriceTill.setError("Enter course price..");
            }
            else if (courseLink.isEmpty()){
                courseLinkTill.setError("Enter course Link...");
            }
            else if (courseDesc.isEmpty()){
                courseDescTill.setError("Enter course Description...");
            }
            else if (imageUri == null){
                Toast.makeText(this, "pick image...", Toast.LENGTH_SHORT).show();
            }
            else {
                addCourseData(courseName,coursePrice,courseLink,courseDesc,courseId,imageUri);
            }

        });


    }

    private void addCourseData(String courseName, String coursePrice, String courseLink, String courseDesc,String courseId,Uri imageUri) {
        progressDialog.setMessage("uploading image...");
        progressDialog.show();
        StorageReference fileRef =storageReference.child(courseId+"."+getFileExtention(imageUri));
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();
                        CourseModel courseModel = new CourseModel(
                                courseName,coursePrice,courseLink,courseDesc,imageUrl,courseId
                        );
                        databaseReference.child(firebaseAuth.getUid())
                                .child(courseId)
                                .setValue(courseModel)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddCourseActivity.this, "Course Added Successfully..", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(AddCourseActivity.this, MainActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddCourseActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddCourseActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private String getFileExtention(Uri muri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(muri));
    }

    private void pickImageDialog() {
        if (checkStoragePermission()){
            pickImageFromGallery();
        }
        else {
            requestStoragePermission();
        }
    }

    private void pickImageFromGallery() {
        Intent gallerIntent = new Intent(Intent.ACTION_PICK);
        gallerIntent.setType("image/*");
        startActivityForResult(gallerIntent,IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(AddCourseActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(AddCourseActivity.this,storagePermission,STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK)
        {
            imageUri =data.getData();
            courseImageView.setImageURI(imageUri);
            courseImageUrl = imageUri.toString();
            Toast.makeText(AddCourseActivity.this, ""+courseImageUrl, Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        courseNameTill = findViewById(R.id.courseNameTill);
        coursePriceTill = findViewById(R.id.coursePriceTill);
        courseLinkTill = findViewById(R.id.courseLinkTill);
        courseDescTill = findViewById(R.id.courseDescTill);
        courseImageLinkTill = findViewById(R.id.courseImageLinkTill);
        edtCourseName = findViewById(R.id.edtCourseName);
        edtCoursePrice = findViewById(R.id.edtCoursePrice);
        edtCourseLink = findViewById(R.id.edtCourseLink);
        edtCourseDesc = findViewById(R.id.edtCourseDesc);
        edtCourseImageLink = findViewById(R.id.edtCourseImageLink);
        addCourse = findViewById(R.id.btnAddCourse);
        courseImageView = findViewById(R.id.courseImageView);
    }




}