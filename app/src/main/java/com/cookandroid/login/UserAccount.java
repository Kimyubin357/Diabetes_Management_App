package com.cookandroid.login;

public class UserAccount{
    private String name; // 이름
    private String idToken;//id 토큰
    private String birthDate;
    private String weight;
    private String height;
    private String gender;


    public UserAccount(){
    }

    public UserAccount(String name){

        this.name = name;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
    }

    public String getIdToken() { return  idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate;}

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }


}