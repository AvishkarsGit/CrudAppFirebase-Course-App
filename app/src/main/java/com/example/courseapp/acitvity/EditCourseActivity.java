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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.util.HashMap;
import java.util.Map;

public class EditCourseActivity extends AppCompatActivity {

    private TextInputLayout courseNameTill,coursePriceTill,courseLinkTill,courseDescTill,courseImageLinkTill;
    private TextInputEditText edtCourseName,edtCoursePrice,edtCourseLink,edtCourseDesc,edtCourseImageLink;
    private Button editCourse,deleteCourse;
    private ImageView courseImageView;

    private String courseName,coursePrice,courseLink,courseDesc,courseImageUrl,courseImageLink,courseId;
    private String previousImageUrl;
    private Uri imageUri;
    private static final int STORAGE_REQUEST_CODE= 100;
    private static final int IMAGE_PICK_GALLERY_CODE= 101;
    private String[] storagePermission;

    private ProgressDialog progressDialog;

    private String courseIdPrevious;
    private FirebaseAuth firebaseAuth;

    private StorageReference storageReference = FirebaseStorage.getInstance()
            .getReference("Course_images/");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);



        //init views
        initViews();

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //loading course details
        loadCourseDetails();

        //init permission array
        storagePermission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        };


        courseImageView.setOnClickListener((view)->{
            pickImageDialog();
        });
        editCourse.setOnClickListener((view)->{
            courseName = edtCourseName.getText().toString();
            coursePrice = edtCoursePrice.getText().toString();
            courseLink = edtCourseLink.getText().toString();
            courseDesc = edtCourseDesc.getText().toString();
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
            else if (previousImageUrl == null && imageUri == null){
                Toast.makeText(this, "pick image...", Toast.LENGTH_SHORT).show();
            }
            else if (previousImageUrl!=null && imageUri == null){
                editCourseDataWithPreviousImageUrl(courseName,coursePrice,courseLink,courseDesc,courseId,previousImageUrl);
            }
            else {
                editCourseData(courseName,coursePrice,courseLink,courseDesc,courseId,imageUri);

            }
        });

        deleteCourse.setOnClickListener((view)->{
            deleteCourseData();
        });
    }

    private void deleteCourseData() {
        progressDialog.setMessage("Deleting...");
        progressDialog.show();
        StorageReference storRef = FirebaseStorage.getInstance().getReferenceFromUrl(previousImageUrl);
        storRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Courses");
                dbRef.child(firebaseAuth.getUid())
                        .child(courseId)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(EditCourseActivity.this, "Course Deleted...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(EditCourseActivity.this,MainActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(EditCourseActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(EditCourseActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void editCourseDataWithPreviousImageUrl(String courseName, String coursePrice, String courseLink, String courseDesc, String courseId, String previousImageUrl) {
        progressDialog.setMessage("Updating...");
        progressDialog.show();
        Map<String, Object> map = new HashMap<>();
        map.put("courseName",courseName);
        map.put("coursePrice",coursePrice);
        map.put("courseLink",courseLink);
        map.put("courseImageUrl",previousImageUrl);
        map.put("courseDesc",courseDesc);
        map.put("courseId",courseId);

        DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference("Courses");
        databaseReference.child(firebaseAuth.getUid())
                .child(courseId)
                .updateChildren(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(EditCourseActivity.this, "Course Edited Successfully...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditCourseActivity.this,MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(EditCourseActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private String getFileExtension(Uri muri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(muri));
    }
    private void editCourseData(String courseName, String coursePrice, String courseLink, String courseDesc, String courseId, Uri imageUri) {
        progressDialog.setMessage("uploading data....");
        progressDialog.show();
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(previousImageUrl);
        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
        StorageReference fileRef = storageReference.child(courseName+"."+getFileExtension(imageUri));
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.setMessage("updating data...");
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("courseName",courseName);
                        map.put("coursePrice",coursePrice);
                        map.put("courseLink",courseLink);
                        map.put("courseImageUrl",uri.toString());
                        map.put("courseDesc",courseDesc);
                        map.put("courseId",courseId);

                        DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference("Courses");
                        databaseReference.child(firebaseAuth.getUid())
                                .child(courseId)
                                .updateChildren(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditCourseActivity.this, "Course Edited Successfully...", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(EditCourseActivity.this,MainActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditCourseActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditCourseActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(EditCourseActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        boolean result = ContextCompat.checkSelfPermission(EditCourseActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(EditCourseActivity.this,storagePermission,STORAGE_REQUEST_CODE);
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
            Toast.makeText(EditCourseActivity.this, ""+courseImageUrl, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCourseDetails() {

        Bundle bundle = getIntent().getExtras();
        edtCourseName.setText(bundle.getString("courseName"));
        edtCoursePrice.setText(bundle.getString("coursePrice"));
        edtCourseLink.setText(bundle.getString("courseLink"));
        edtCourseDesc.setText(bundle.getString("courseDescription"));
        courseId = bundle.getString("courseId");
        Glide.with(EditCourseActivity.this).load(bundle.getString("courseImageUrl")).into(courseImageView);
        previousImageUrl = bundle.getString("courseImageUrl");

    }
    private void initViews() {
        courseNameTill = findViewById(R.id.courseNameTillEdit);
        coursePriceTill = findViewById(R.id.coursePriceTillEdit);
        courseLinkTill = findViewById(R.id.courseLinkTillEdit);
        courseDescTill = findViewById(R.id.courseDescTillEdit);
        edtCourseName = findViewById(R.id.edtCourseNameEdit);
        edtCoursePrice = findViewById(R.id.edtCoursePriceEdit);
        edtCourseLink = findViewById(R.id.edtCourseLinkEdit);
        edtCourseDesc = findViewById(R.id.edtCourseDescEdit);
        editCourse = findViewById(R.id.btnEditCourse);
        deleteCourse = findViewById(R.id.btnDeleteCourse);
        courseImageView = findViewById(R.id.courseImageViewEdit);
    }
}