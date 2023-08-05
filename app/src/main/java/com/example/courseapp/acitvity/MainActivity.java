package com.example.courseapp.acitvity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.courseapp.R;
import com.example.courseapp.adapter.CourseRvAdapter;
import com.example.courseapp.models.CourseModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.PrivateKey;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements CourseRvAdapter.CourseClickInterface {


    private FloatingActionButton fab;
    private ImageButton logout;
    private CircleImageView profile;
    private FirebaseAuth firebaseAuth;
    private TextView txtEmail;
    private ProgressDialog progressDialog;
    private DatabaseReference reference = FirebaseDatabase
            .getInstance().getReference("Users");


    private RelativeLayout relativeLayout;
    private ArrayList<CourseModel> arrayList;
    private CourseRvAdapter adapter;

    private RecyclerView recyclerView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);
        logout = findViewById(R.id.logoutBtn);
        txtEmail = findViewById(R.id.txtEmail);
        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        arrayList = new ArrayList<>();
        relativeLayout = findViewById(R.id.bottomSheetRl);
        profile = findViewById(R.id.profileIconIv);

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();


        profile.setOnClickListener((view)->{
            startActivity(new Intent(MainActivity.this,ProfileActivity.class));
        });
        fab.setOnClickListener((view)->{
            startActivity(new Intent(MainActivity.this, AddCourseActivity.class));
        });

        logout.setOnClickListener((view)->{
            firebaseAuth.signOut();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        });

        loadUserProfileImage();
        loadUserInfo();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Courses");
        databaseReference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrayList.clear();
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                            CourseModel model = dataSnapshot.getValue(CourseModel.class);
                            arrayList.add(model);
                        }
                        adapter = new CourseRvAdapter(MainActivity.this,arrayList,MainActivity.this);
                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadUserProfileImage(){
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileIcon = ""+snapshot.child("userProfile").getValue();
                        Glide.with(MainActivity.this).load(profileIcon).into(profile);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadUserInfo() {
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("userEmail").getValue();
                        txtEmail.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onCourseClick(int position) {
        displayBottomSheet(arrayList.get(position));
    }

    private void displayBottomSheet(CourseModel courseModel)
    {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        View layoutView = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_sheet_dialog,relativeLayout);
        bottomSheetDialog.setContentView(layoutView);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        bottomSheetDialog.show();

        TextView courseNameTv = layoutView.findViewById(R.id.tvCourseName);
        TextView courseDescTv = layoutView.findViewById(R.id.tvCourseDescription);
        TextView coursePriceTv = layoutView.findViewById(R.id.tvCoursePrice);
        ImageView courseImage = layoutView.findViewById(R.id.IvCourseImage);
        Button editButton = layoutView.findViewById(R.id.btnEditCourse);
        Button viewCourseButton = layoutView.findViewById(R.id.btnViewCourse);

        courseNameTv.setText(courseModel.getCourseName());
        courseDescTv.setText(courseModel.getCourseDesc());
        coursePriceTv.setText("Rs."+courseModel.getCoursePrice());
        Glide.with(MainActivity.this).load(courseModel.getCourseImageUrl()).into(courseImage);

        editButton.setOnClickListener((view)->{

            Intent iNext = new Intent(MainActivity.this,EditCourseActivity.class);
            iNext.putExtra("courseName",courseModel.getCourseName());
            iNext.putExtra("coursePrice",courseModel.getCoursePrice());
            iNext.putExtra("courseLink",courseModel.getCourseLink());
            iNext.putExtra("courseDescription",courseModel.getCourseDesc());
            iNext.putExtra("courseImageUrl",courseModel.getCourseImageUrl());
            iNext.putExtra("courseId",courseModel.getCourseId());
            startActivity(iNext);
            bottomSheetDialog.dismiss();
        });
        viewCourseButton.setOnClickListener((view)->{
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            String courseLink = courseModel.getCourseLink();
            Uri courseUri = Uri.parse(courseLink);
            viewIntent.setData(courseUri);
            startActivity(viewIntent);
        });
    }
}