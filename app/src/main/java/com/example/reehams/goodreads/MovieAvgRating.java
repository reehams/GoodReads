package com.example.reehams.goodreads;

/**
 * Created by rahulkooverjee on 4/12/17.
 */

public class MovieAvgRating implements Comparable<MovieAvgRating> {

    protected String name;
    protected String id;
    double avgRating;

    public MovieAvgRating(String name, String id, double avgRating) {
        this.name = name;
        this.id = id;
        this.avgRating = avgRating;
    }

    @Override
    public int compareTo(MovieAvgRating o) {
        // Compare
        return - Double.compare(avgRating, o.avgRating);
    }
}
