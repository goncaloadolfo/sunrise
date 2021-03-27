package com.example.sunrise.DocObjs;

public class UserObj {

    private String firstName;
    private String email;
    private String homeTown;
    private String lastName;
    private String profilePicName;

    public UserObj(){}

    public UserObj(String firstName, String email, String homeTown, String lastName, String profilePicName){
        this.firstName = firstName;
        this.email = email;
        this.homeTown = homeTown;
        this.lastName = lastName;
        this.profilePicName = profilePicName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public String getHomeTown() {
        return homeTown;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePicName() {
        return profilePicName;
    }
}
