package com.cookandroid.login.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserMemos extends ImagePredict {
    private String dateTime;
    private String eatTime;
    private String imageName;
    private String realTime;
    private String time;

    // 기본 생성자
    public UserMemos() {
        // 현재 날짜와 시간 가져오기
        this.dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // 추가된 생성자
    public UserMemos(String dateTime, String bloodSugar, String memo, String realTime) {
        // 현재 날짜와 시간 가져오기
        this.dateTime = realTime;
        this.eatTime = bloodSugar;
        this.imageName = memo;
        this.realTime = realTime;
        // 시간은 현재 시간으로 설정
        this.time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // getter 및 setter 메서드
    public String getEatTime() {
        return eatTime+"mg/dLs";
    }

    public void getEatTime(String bloodSugar) {
        this.eatTime = bloodSugar;
    }

    public String getImageName() {
        return imageName;
    }

    public void getImageName(String memo) {
        this.imageName = memo;
    }

    public String getRealTime() {
        return realTime;
    }

    public void setRealTime(String realTime) {
        this.realTime = realTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
