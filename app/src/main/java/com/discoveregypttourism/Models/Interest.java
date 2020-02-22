package com.discoveregypttourism.Models;

public class Interest {
    public String name;
    public int image ;

    public Interest (String name , int image){
        this.name = name;
        this.image = image ;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getImage() {
        return image;
    }
}
