package com.example.cinemabooking;

import java.util.HashMap;

public class    Movie {
    private String name;
    private int  movieId;
    private String imageUrl;
    private String description;
    private HashMap<String , String> dayHours = new HashMap<String, String>();

    public Movie(String name, int movieId, String imageUrl, String description) {
        this.name = name;
        this.movieId = movieId;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, String> getDayHours() {
        return dayHours;
    }

    public void setDayHours(HashMap<String, String> dayHours) {
        this.dayHours = dayHours;
    }

}
