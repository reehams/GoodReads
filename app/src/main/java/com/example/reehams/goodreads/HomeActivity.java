package com.example.reehams.goodreads;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.*;

import org.json.JSONObject;

import java.util.*;


/**
 * Created by rahulkooverjee on 3/30/17.
 */

public class HomeActivity extends SideBar {

    private ListView myReviewList;
    private ArrayList<String> myReviews = new ArrayList<>();
    static boolean followersDone = false;
    static boolean userDone = false;
    String[] searchResults;
    final String userId = WelcomeActivity.userId1;
    final Set<Review> set = new TreeSet<Review>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        followersDone = false;
        userDone = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_layout);
        super.onCreateDrawer();
        myReviewList = (ListView) findViewById(R.id.yourReviewList);
        final ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myReviews);
        myReviewList.setAdapter(arrayAdapter2);
        myReviews.clear();
        myReviews.add("Loading...");
        arrayAdapter2.notifyDataSetChanged();
        waitForFirebase();
    }

    public void helper(long delay) {
        final String userId = WelcomeActivity.userId1;
        final DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();
        myReviewList = (ListView) findViewById(R.id.yourReviewList);
        final ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myReviews);
        myReviewList.setAdapter(arrayAdapter2);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myDatabase.child(userId).child("followingIds").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                List<String> l = (ArrayList<String>) dataSnapshot.getValue();
                                for (String s : l) {
                                    if (s==null) break;
                                    if (s.equals("null")) break;
                                    int commaIdx = s.indexOf(',');
                                    String id = s.substring(0, commaIdx);
                                    myDatabase.child(id).child("reviews").addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    List<HashMap<String, String>> l = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
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
                                                        set.add(r);
                                                    }
                                                    displaySet();
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                }
                                makeFollowersTrue();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                myDatabase.child(userId).child("reviews").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //List l = dataSnapshot.getValue(List.class);
                                List<HashMap<String, String>> l = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
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
                                    set.add(r);
                                }
                                makeUserDoneTrue();
                                displaySet();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

        }, delay); // wait a while basically
    }

    public void displaySet() {
        if (!followersDone) return;
        if (!userDone) return;
        final ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myReviews);
        myReviewList.setAdapter(arrayAdapter2);
        myReviews.clear();
        if (set.isEmpty()) {
            myReviews.add("No reviews yet");
            searchResults = new String[1];
            searchResults[0] = "empty";
        }
        else {
            searchResults = new String[set.size()];
            int i = 0;
            for (Review rev : set) {
                String displayText = rev.movieTitle + "\n" + rev.getStars() + "\n\"" + rev.reviewText + "\"";
                myReviews.add(displayText);
                searchResults[i] = rev.movieId;
                i++;
            }
        }
        arrayAdapter2.notifyDataSetChanged();
        myReviewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(WatchlistActivity.this, "Position: " + position, Toast.LENGTH_SHORT).show();
                // Do nothing if there is no result
                if (searchResults[position] == null) {
                    Toast.makeText(HomeActivity.this, "Null searchresult ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (searchResults[position].equals("empty")) {
                    return;
                }
                // Pass the data of the clicked movie to the movieDetails class
                Intent i = new Intent(HomeActivity.this,  MovieDetailsActivity.class);
                i.putExtra("user_id", userId);
                try {
                    // Pass the IMBD movie id to the details page
                    String movieId = searchResults[position];
                    String[] queryArr = new String[1];
                    queryArr[0] = "https://api.themoviedb.org/3/movie/" + movieId +
                            "?api_key=9f4d052245dda68f14bcbd986787dc7b&language=en-US";
                    AsyncTask search = new MovieBackend().execute(queryArr);
                    JSONObject json = null;
                    json = (JSONObject) search.get();
                    String imbdId = json.get("imdb_id").toString();
                    boolean isInvalid = (imbdId == null);
                    if (!isInvalid) {
                        isInvalid = imbdId.equals("") || imbdId.equals("null");
                    }
                    if (isInvalid) {
                        Toast.makeText(HomeActivity.this, "More movie details cannot be found in IMBD Database",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (imbdId.charAt(imbdId.length() - 1) == '/') {
                        imbdId = imbdId.substring(0, imbdId.length() - 1);
                    }
                    i.putExtra("JSON_Data", imbdId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        });
    }

    private void makeFollowersTrue() {
        followersDone = true;
    }

    private void makeUserDoneTrue() {
        userDone = true;
    }

    private void waitForFirebase() {
        final DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.child(userId).child("followingIds").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> l = (ArrayList<String>) dataSnapshot.getValue();
                        if (l == null) {
                            waitForFirebase();
                        }
                        else {
                            helper(0);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


}

