package com.example.reehams.goodreads;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;

import com.facebook.login.widget.ProfilePictureView;
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
 * Created by reehams on 2/17/17.
 */

public class MyAccountActivity extends SideBar {
    TextView email;
    TextView userName;
    ProfilePictureView image;
    private ListView userReviewsView;
    String[] searchResults;
    private ArrayList<String> userReviewsList = new ArrayList<>();
    final Set<Review> set = new TreeSet<Review>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.my_account);
        super.onCreateDrawer();
        email = (TextView) findViewById(R.id.email);
        email.setText("Email:" + " " + WelcomeActivity.email);
        userName = (TextView) findViewById(R.id.userName);
        userName.setText("Name:" + " " + WelcomeActivity.facebookName);
        image = (ProfilePictureView) findViewById(R.id.image);
        image.setPresetSize(ProfilePictureView.NORMAL);
        image.setProfileId(WelcomeActivity.profilePicId);
        userReviewsView = (ListView) findViewById(R.id.userReviewsList);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userReviewsList);
        userReviewsView.setAdapter(arrayAdapter);
        userReviewsList.clear();
        userReviewsList.add("Loading...");
        arrayAdapter.notifyDataSetChanged();
        DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.child(WelcomeActivity.userId1).child("reviews").addListenerForSingleValueEvent(
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
                        userReviewsList.clear();
                        if (set.isEmpty()) {
                            searchResults = new String[1];
                            searchResults[0] = "empty";
                            userReviewsList.add("You have no reviews");
                        }
                        else {
                            searchResults = new String[set.size()];
                            int i = 0;
                            for (Review rev : set) {
                                searchResults[i] = rev.movieId;
                                String displayText = rev.movieTitle + "\n" + rev.getStars() + "\n\"" + rev.reviewText + "\"";
                                userReviewsList.add(displayText);
                                i++;
                            }
                        }
                        arrayAdapter.notifyDataSetChanged();
                        userReviewsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                //Toast.makeText(WatchlistActivity.this, "Position: " + position, Toast.LENGTH_SHORT).show();
                                // Do nothing if there is no result
                                if (searchResults[position] == null) {
                                    Toast.makeText(MyAccountActivity.this, "Null searchresult ID", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (searchResults[position].equals("empty")) {
                                    return;
                                }
                                // Pass the data of the clicked movie to the movieDetails class
                                Intent i = new Intent(MyAccountActivity.this,  MovieDetailsActivity.class);
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
                                        Toast.makeText(MyAccountActivity.this, "More movie details cannot be found in IMBD Database",
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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        //waitForFirebase();
    }

    protected void watchlistOnButtonPressed(View view) {
        Intent i = new Intent(this,WatchlistActivity.class);
        startActivity(i);
    }

    protected void whoIamFollowing(View view) {
        Intent i = new Intent(this, FollowingListActivity.class);
        i.putExtra("user_id", WelcomeActivity.userId1);
        startActivity(i);
    }
    protected void myFollowers(View view) {
        Intent i = new Intent(this, FollowerListActivity.class);
        i.putExtra("user_id", WelcomeActivity.userId1);
        startActivity(i);
    }
}
