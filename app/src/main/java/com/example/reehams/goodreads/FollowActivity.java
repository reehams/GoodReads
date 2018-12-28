package com.example.reehams.goodreads;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.example.reehams.goodreads.WelcomeActivity.userId1;

/**
 * Created by reehams on 3/26/17.
 */

public class FollowActivity extends SideBar {
    TextView email;
    TextView userName;
    Button followButton;
    ProfilePictureView image;
    private ListView userReviewsView;
    String[] searchResults;
    private ArrayList<String> userReviewsList = new ArrayList<>();
    final Set<Review> set = new TreeSet<Review>();

    private DatabaseReference myDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_activity);
        super.onCreateDrawer();
        FacebookSdk.sdkInitialize(getApplicationContext());
        String imageUsed = getIntent().getStringExtra("id");
        final String userId1 = WelcomeActivity.userId1;
        if (imageUsed.equals(WelcomeActivity.userId1)) {
            Intent i = new Intent(FollowActivity.this, MyAccountActivity.class);
            startActivity(i);
        }
        myDatabase = FirebaseDatabase.getInstance().getReference();
        final String userName2 = getIntent().getStringExtra("name");
        final String userId2 = getIntent().getStringExtra("id");

        String userEmail = getIntent().getStringExtra("email");
        email = (TextView) findViewById(R.id.email2);
        email.setText("Email:" + " " + userEmail);
        userName = (TextView) findViewById(R.id.userName2);
        userName.setText("Name:" + " " + userName2);
        image = (ProfilePictureView) findViewById(R.id.image2);
        image.setPresetSize(ProfilePictureView.NORMAL);
        image.setProfileId(imageUsed);
        myDatabase.child(WelcomeActivity.userId1).child("followingIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> set = new TreeSet<String>();
                followButton = (Button) findViewById(R.id.followbotton);
                followButton.setText("+Follow");
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (childSnapshot.getValue(String.class).equals(userId2 + "," + userName2)) {
                        followButton.setText("-Unfollow");
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        userReviewsView = (ListView) findViewById(R.id.userReviewsList);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userReviewsList);
        userReviewsView.setAdapter(arrayAdapter);
        userReviewsList.clear();
        userReviewsList.add("Loading...");
        arrayAdapter.notifyDataSetChanged();
        DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.child(imageUsed).child("reviews").addListenerForSingleValueEvent(
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
                            userReviewsList.add(userName2 + " has no reviews");
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
                                    Toast.makeText(FollowActivity.this, "Null searchresult ID", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (searchResults[position].equals("empty")) {
                                    return;
                                }
                                // Pass the data of the clicked movie to the movieDetails class
                                Intent i = new Intent(FollowActivity.this,  MovieDetailsActivity.class);
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
                                        Toast.makeText(FollowActivity.this, "More movie details cannot be found in IMBD Database",
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

    protected void followThisUser(View view) {
        userName = (TextView) findViewById(R.id.userName2);
        final String userId1 = getIntent().getStringExtra("userId1");
        final String userName1 = getIntent().getStringExtra("userName1");

        final String userId2 = getIntent().getStringExtra("id");
        final String userName2 = getIntent().getStringExtra("name");

        // Add the person you followed to your list of followers
        myDatabase.child(userId1).child("followingIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                followButton = (Button) findViewById(R.id.followbotton);
                boolean isFollowing = false;
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (childSnapshot.getValue(String.class).equals(userId2 + "," + userName2)) {
                        isFollowing = true;

                    }
                }
                final List<String> l = (ArrayList<String>) dataSnapshot.getValue();
                if (isFollowing) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            FollowActivity.this);

                    // set title
                    alertDialogBuilder.setTitle("Remove from Following List");
                    // set dialog message
                    alertDialogBuilder
                            .setMessage("Are you sure you want to unfollow?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    // if this button is clicked, close
                                    // current activity
                                    String s = userId2 + "," + userName2;
                                    l.remove(s);
                                    if (l.isEmpty()) {
                                        l.add("null");
                                    }
                                    followButton.setText("+Follow");
                                    myDatabase.child(userId1).child("followingIds").setValue(l);
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
                    String s = userId2 + "," + userName2;
                    if (!l.contains(s)) {
                        l.add(s);
                    }
                    followButton.setText("-Unfollow");
                    myDatabase.child(userId1).child("followingIds").setValue(l);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // Add yourself as a follower to the person you just followed
        myDatabase.child(userId2).child("followerIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isFollowing = false;
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (childSnapshot.getValue(String.class).equals(userId1 + "," + userName1)) {
                        isFollowing = true;
                    }
                }
                final List<String> l = (ArrayList<String>) dataSnapshot.getValue();
                if (isFollowing) {
                    String s = userId1 + "," + userName1;
                    l.remove(s);
                    if (l.isEmpty()) {
                        l.add("null");
                    }
                    myDatabase.child(userId2).child("followerIds").setValue(l);
                }
                else {
                   if (l.get(0).equals("null")) {
                        l.remove(0);
                    }
                    String s = userId1 + "," + userName1;
                    if (!l.contains(s)) {
                        l.add(s);
                    }
                    myDatabase.child(userId2).child("followerIds").setValue(l);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void followingOfTheUser(View view) {
        Intent i = new Intent(FollowActivity.this, WhoIsFollowingUser.class);
        i.putExtra("idOfCurrentPage",getIntent().getStringExtra("id"));
        startActivity(i);
    }
    protected void followersOfTheUser(View view) {
        Intent i = new Intent(FollowActivity.this, FollowersofUser.class);
        i.putExtra("idOfCurrentPage",getIntent().getStringExtra("id"));
        startActivity(i);
    }
}
