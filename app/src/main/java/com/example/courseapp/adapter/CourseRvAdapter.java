package com.example.courseapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.courseapp.R;
import com.example.courseapp.models.CourseModel;

import java.util.ArrayList;

public class CourseRvAdapter extends RecyclerView.Adapter<MyViewHolder> {

    int lastPos=-1;
    Context context;
    ArrayList<CourseModel> modelArrayList;

    private CourseClickInterface courseClickInterface;
    public CourseRvAdapter(Context context, ArrayList<CourseModel> modelArrayList,CourseClickInterface courseClickInterface) {
        this.context = context;
        this.modelArrayList = modelArrayList;
        this.courseClickInterface = courseClickInterface;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.course_row_lay,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final CourseModel  model = modelArrayList.get(position);
        holder.txtCoursePrice.setText("Rs."+model.getCoursePrice());
        holder.txtCourseName.setText(model.getCourseName());
        setAnimation(holder.itemView,position);
        Glide.with(context).load(model.getCourseImageUrl()).into(holder.courseIv);

        holder.itemView.setOnClickListener((view)->{
            courseClickInterface.onCourseClick(position);
        });
    }

    private void setAnimation(View itemView, int position){
        if (position>lastPos){
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            itemView.setAnimation(animation);
            lastPos = position;
        }
    }
    @Override
    public int getItemCount() {
        return modelArrayList.size();
    }

    public interface CourseClickInterface{
        void onCourseClick(int position);
    }
}
