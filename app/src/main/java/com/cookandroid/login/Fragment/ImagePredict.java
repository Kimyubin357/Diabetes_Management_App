package com.cookandroid.login.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePredict {
    private String dateTime;
    private String eatTime;
    private String imageName;

    public ImagePredict() {
        // 현재 날짜와 시간 가져오기
        this.dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public ImagePredict(String meal, String imageName) {
        // 현재 날짜와 시간 가져오기
        this.dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());;
        this.eatTime = meal;
        this.imageName = imageName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getEatTime() {
        return eatTime;
    }

    public void setEatTime(String meal) {
        this.eatTime = meal;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
