package com.discoveregypttourism.Models;

public class User {

    public String image , gender , name , country , birthday , phone , id ;

    public User (String image , String gender  , String name , String country , String birthday , String phone){
        this.birthday = birthday ;
        this.country = country;
        this.gender = gender;
        this.image = image ;
        this.name = name ;
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getCountry() {
        return country;
    }

    public String getImage() {
        return image;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
