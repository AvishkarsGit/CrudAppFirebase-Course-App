package com.example.courseapp.models;

public class CourseModel {
    private String courseName;
    private String coursePrice;
    private String courseLink;
    private String courseDesc;
    private String courseImageUrl;
    private String courseId;

    public CourseModel(String courseName, String coursePrice, String courseLink, String courseDesc, String courseImageUrl,String courseId) {
        this.courseName = courseName;
        this.coursePrice = coursePrice;
        this.courseLink = courseLink;
        this.courseDesc = courseDesc;
        this.courseImageUrl = courseImageUrl;
        this.courseId = courseId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public CourseModel() {
        //empty constructor is required
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCoursePrice() {
        return coursePrice;
    }

    public void setCoursePrice(String coursePrice) {
        this.coursePrice = coursePrice;
    }

    public String getCourseLink() {
        return courseLink;
    }

    public void setCourseLink(String courseLink) {
        this.courseLink = courseLink;
    }

    public String getCourseDesc() {
        return courseDesc;
    }

    public void setCourseDesc(String courseDesc) {
        this.courseDesc = courseDesc;
    }

    public String getCourseImageUrl() {
        return courseImageUrl;
    }

    public void setCourseImageUrl(String courseImageUrl) {
        this.courseImageUrl = courseImageUrl;
    }
}
