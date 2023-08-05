package com.example.courseapp.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courseapp.R;

public class MyViewHolder extends RecyclerView.ViewHolder {

    TextView txtCourseName,txtCoursePrice;
    ImageView courseIv;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        txtCourseName = itemView.findViewById(R.id.txtCourseName);
        txtCoursePrice = itemView.findViewById(R.id.txtCoursePrice);
        courseIv = itemView.findViewById(R.id.IvCourse);
    }
}
