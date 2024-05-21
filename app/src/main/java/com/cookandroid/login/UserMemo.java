package com.cookandroid.login;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserMemo { // 디비에 올릴 변수 리스트
    private String date;
    private String time;
    private String bloodSugar;
    private String memo;

    private String realTime;

    public UserMemo() {
        this.realTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public UserMemo(String date, String time, String bloodSugar, String memo) {
        this.date = date;
        this.time = time;
        this.bloodSugar = bloodSugar;
        this.memo = memo;
        this.realTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBloodSugar() {
        return bloodSugar;
    }

    public void setBloodSugar(String bloodSugar) {
        this.bloodSugar = bloodSugar;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getRealTime() {
        return realTime;
    }

    public void setRealTime(String realTime) {
        this.realTime = realTime;
    }
}
