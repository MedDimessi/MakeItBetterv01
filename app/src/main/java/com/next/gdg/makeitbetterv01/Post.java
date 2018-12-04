package com.next.gdg.makeitbetterv01;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Med on 09/06/2018.
 */

public class Post implements Parcelable {
    String comment;
    Double latitude;
    Double longitude;
    String picture;
    String postId;
    String username;
    ArrayList<String> whoLiked;
    ArrayList<String> whoVolunteered;



    public  Post(Parcel in){

        comment = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        picture = in.readString();
        postId = in.readString();
        username = in.readString();
        whoLiked = in.readArrayList(null);
        whoVolunteered = in.readArrayList(null);


    }

    public Post(String comment, Double latitude, Double longitude, String picture, String postId, String username, ArrayList<String> whoLiked, ArrayList<String> whoVolunteered) {
        this.comment = comment;
        this.latitude = latitude;
        this.longitude = longitude;
        this.picture = picture;
        this.postId = postId;
        this.username = username;
        this.whoLiked = whoLiked;
        this.whoVolunteered = whoVolunteered;



    }

    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>(){

        @Override
        public Post createFromParcel(Parcel parcel) {

            return new Post(parcel);
        }

        @Override
        public Post[] newArray(int i) {
            return new Post[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(comment);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(picture);
        parcel.writeString(postId);
        parcel.writeString(username);
        parcel.writeList(whoLiked);
        parcel.writeList(whoVolunteered);
    }



    public String getPostId() {
        return postId;
    }

    public String getUsername() {
        return username;
    }

    public String getPicture() {
        return picture;
    }

    public String getComment() {
        return comment;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }


}
