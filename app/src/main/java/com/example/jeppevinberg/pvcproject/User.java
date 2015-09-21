package com.example.jeppevinberg.pvcproject;

/**
 * Created by Jeppe Vinberg on 20-09-2015.
 */
public class User {
    private String name;
    private String password;
    private String lat;
    private String lng;

    public User(){}

    public User(String name, String password, String lat, String lng){
        this.name = name.toLowerCase();
        this.password = password.toLowerCase();
        this.lat = lat;
        this.lng = lng;
    }

    public String getName(){
        return name;
    }

    public String getPassword(){
        return password;
    }

    public String getLat(){
        return lat;
    }

    public String getLng(){
        return lng;
    }

    @Override
    public boolean equals(Object o){
        if(o != null){
            if(o.getClass() == User.class){
                User u = (User)o;
                if(u.getName().equals(name) && u.getPassword().equals(password)){
                    return true;
                }
            }
        }
        return false;
    }



}
