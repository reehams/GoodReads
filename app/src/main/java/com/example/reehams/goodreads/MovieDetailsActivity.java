package com.example.reehams.goodreads;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by rahulkooverjee on 3/9/17.
 */

public class MovieDetailsActivity extends SideBar {

    String userId;
    String movieId;
    String nameId;

    String movieName;

    private DatabaseReference myDatabase;

    String[] searchResults = new String[5]; // Options to be shown in list view
    private ArrayList<String> movieReviewsList = new ArrayList<>();

    ListView movieReviewsListView;
    final Set<Review> set = new TreeSet<Review>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details_activity);
        super.onCreateDrawer();
        userId = WelcomeActivity.userId1;
        myDatabase = FirebaseDatabase.getInstance().getReference();
        // Variables to save movie data
        String name = "n/a";
        String releaseDate = "n/a";
        String posterURL = "n/a";
        String description = "n/a";
        String runtime = "n/a";
        String genres  = "n/a";
        String director = "n/a";
        String rating = "n/a";
        String actors = "n/a";

        try {
            // Get movie ID from intent
            String id = getIntent().getStringExtra("JSON_Data");
            movieId = id;
            // Get movie details in JSON object
            String[] queryArr = new String[1];
            queryArr[0] = "http://www.omdbapi.com/?i=" + id;
            AsyncTask search = new MovieBackend().execute(queryArr);
            JSONObject json = null;
            json = (JSONObject) search.get();
            // Set movie info
            name = json.get("Title").toString();
            movieName = name;
            nameId = name;
            releaseDate = json.get("Released").toString();
            runtime = json.get("Runtime").toString();
            posterURL = json.get("Poster").toString();
            if (posterURL.equalsIgnoreCase("n/a")) {
                posterURL = "http://s3.amazonaws.com/static.betaeasy.com/screenshot/456/456-25984-14192637741419263774.42.jpeg";
            }
            description = json.get("Plot").toString();
            director = getDirectorString(json.get("Director").toString());
            rating = json.get("imdbRating").toString();
            actors = getActorString(json.get("Actors").toString());
            genres = json.get("Genre").toString();
        } catch (Exception e) {
            Toast.makeText(MovieDetailsActivity.this, e + "", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        // Set movie info to view
        ((TextView) findViewById(R.id.movie_name)).setText(name);
        ((TextView) findViewById(R.id.movie_description)).setText(description);
        ((ImageView) findViewById(R.id.imageView2)).setImageBitmap(MovieBackend.getPoster(posterURL));
        ((TextView) findViewById(R.id.movie_genre)).setText(genres);
        ((TextView) findViewById(R.id.movie_genre)).bringToFront();
        ((TextView) findViewById(R.id.movie_release)).setText(releaseDate);
        ((TextView) findViewById(R.id.movie_runtime)).setText(runtime);
        ((TextView) findViewById(R.id.movie_director)).setText(director);
        ((TextView) findViewById(R.id.movie_rating)).setText(rating + " / 10");
        ((TextView) findViewById(R.id.movie_actors)).setText(actors);
        myDatabase.child(userId).child("watchlist").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> l = (ArrayList<String>) dataSnapshot.getValue();
                        Button watchButton = (Button) findViewById(R.id.watchlist_button);
                        if (l.contains(movieId + "," + movieName)) {
                            watchButton.setText("Remove from Watchlist");
                        }
                        else {
                            watchButton.setText("Add to Watchlist");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });



        /****************/
        movieReviewsListView = (ListView) findViewById(R.id.movieReviewsList);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, movieReviewsList);
        movieReviewsListView.setAdapter(arrayAdapter);
        movieReviewsList.clear();
//        movieReviewsList.add("Loading...");
        arrayAdapter.notifyDataSetChanged();
        DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();


            myDatabase.child(movieId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
//                        List l = dataSnapshot.getValue(List.class);
                                List<HashMap<String, String>> l = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
                            if (l != null) {
                                for (HashMap<String, String> s : l) {
                                    String movieId = s.get("movieId");
                                    if (movieId.equals("null")) continue;
                                    String movieTitle = s.get("movieTitle");
                                    String rating = s.get("rating");
                                    String reviewText = s.get("reviewText");
                                    if (reviewText.length() > 175) {
                                        reviewText = reviewText.substring(0, 175) + "...";
                                    }
                                    String time = s.get("time");
                                    Review r = new Review(movieId, rating, reviewText, movieTitle, time);
                                    if(set.size() < 6) {
                                        set.add(r);
                                    }
                                }

                            } else {
                                searchResults = new String[1];
                                searchResults[0] = "empty";
                                movieReviewsList.clear();
                                movieReviewsList.add("Movie has not been reviewed yet");
                                return;
                            }
                            movieReviewsList.clear();
                            if (set.isEmpty()) {
                                searchResults = new String[1];
                                searchResults[0] = "empty";
                                movieReviewsList.clear();
                                movieReviewsList.add("Movie has no reviews");
                                return;
                            } else {
                                searchResults = new String[set.size()];
                                int i = 0;
                                for (Review rev : set) {
                                    searchResults[i] = rev.movieId;
                                    String displayText = rev.movieTitle + "\n" + rev.getStars() + "\n\"" + rev.reviewText + "\"";
                                    movieReviewsList.add(displayText);
                                    i++;
                                }
                            }
                            arrayAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


    }


    protected void watchlistOnButtonPressed(View view) {
        myDatabase.child(userId).child("watchlist").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final List<String> l = (ArrayList<String>) dataSnapshot.getValue();
                        Button watchButton = (Button) findViewById(R.id.watchlist_button);
                        if (l.contains(movieId + "," + movieName)) {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                        MovieDetailsActivity.this);

                                // set title
                                alertDialogBuilder.setTitle("Remove from Watchlist");

                                // set dialog message
                                alertDialogBuilder
                                        .setMessage("Are you sure you want to remove the movie from your watchlist?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int id) {
                                                // if this button is clicked, close
                                                // current activity
                                                String s = movieId + "," + movieName;
                                                l.remove(s);
                                                if (l.isEmpty()) {
                                                    l.add("null");
                                                }
                                                Button watchButton = (Button) findViewById(R.id.watchlist_button);
                                                watchButton.setText("Add to Watchlist");
                                                myDatabase.child(userId).child("watchlist").setValue(l);

                                            }
                                        })
                                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int id) {
                                                // if this button is clicked, just close
                                                // the dialog box and do nothing
                                                dialog.cancel();
                                            }
                                        });
                                // create alert dialog
                                AlertDialog alertDialog = alertDialogBuilder.create();

                                // show it
                                alertDialog.show();
                        }
                        else {
                            if (l.get(0).equals("null")) {
                                l.remove(0);
                            }
                            String s = movieId + "," + movieName;
                            if (!l.contains(s)) {
                                l.add(s);
                            }
                            watchButton.setText("Remove from Watchlist");
                            myDatabase.child(userId).child("watchlist").setValue(l);
                        }
                     }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }



    protected void reviewOnButtonPressed(View view) {
        Intent i = new Intent(MovieDetailsActivity.this, ReviewFormActivity.class);
        Bundle extras = new Bundle();
        extras.putString("user_id", userId);
        extras.putString("movie_id", movieId);
        extras.putString("movie_name", movieName);
        i.putExtras(extras);
        startActivity(i);
    }

    private String getActorString(String actors) {
        StringBuilder builder = new StringBuilder();
        int firstComma = actors.indexOf(',');
        if (firstComma == -1) {
            return actors;
        }
        builder.append(actors.substring(0, firstComma));
        int secondComma = actors.indexOf(',', firstComma + 1);
        if (secondComma == -1) {
            return builder.toString();
        }
        builder.append("\n");
        builder.append(actors.substring(firstComma + 2, secondComma));
        return builder.toString();
    }

    private String getDirectorString(String directors) {
        StringBuilder builder = new StringBuilder();
        int firstComma = directors.indexOf(',');
        if (firstComma == -1) {
            return directors;
        }
        builder.append(directors.substring(0, firstComma));
        return builder.toString();
    }

}