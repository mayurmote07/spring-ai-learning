package com.mm.Spring.AI.Model;

public class Movie {

    private String movieName;
    private String leadActor;
    private String year;
    private String genre;
    private String director;

    public String getMovieName() {
        return movieName;
    }

    public void setLeadActor(String leadActor) {
        this.leadActor = leadActor;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getDirector() {
        return director;
    }

    public String getYear() {
        return year;
    }

    public String getGenre() {
        return genre;
    }

    public String getLeadActor() {
        return leadActor;
    }
}
