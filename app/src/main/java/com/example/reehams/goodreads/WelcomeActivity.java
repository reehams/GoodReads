package com.example.reehams.goodreads;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WelcomeActivity extends AppCompatActivity {


    private LoginButton btnLogin;
    private CallbackManager callbackManager;
    static String email;
    static String facebookName;
    static String gender;
    static String profilePicId;
    private DatabaseReference myDatabase;
    static String userId1;
    final boolean[] isInDataBase = new boolean[1];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_welcome);
        btnLogin = (LoginButton)findViewById(R.id.login_button2);
        btnLogin.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));
        callbackManager = CallbackManager.Factory.create();

        myDatabase = FirebaseDatabase.getInstance().getReference();



        if (isLoggedIn()) {
            Intent i = new Intent(WelcomeActivity.this, HomeActivity.class);
            Intent i2 = new Intent(WelcomeActivity.this, FollowActivity.class);
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            userId1 = accessToken.getUserId();

            i.putExtra("user_id", userId1);
            i2.putExtra("user_id", userId1);

            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            Log.v("Main", response.toString());

                            try {
                                WelcomeActivity.this.facebookName = object.getString("name");
                                WelcomeActivity.this.email = object.getString("email");
                                setProfileToView(object);

                                myDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        List<String> watchlist = (ArrayList<String>) dataSnapshot.child(userId1).child("watchlist").getValue();
                                        List<Review> reviews = (ArrayList<Review>) dataSnapshot.child(userId1).child("reviews").getValue();
                                        List<String> following = (ArrayList<String>) dataSnapshot.child(userId1).child("followingIds").getValue();
                                        List<String> followers = (ArrayList<String>) dataSnapshot.child(userId1).child("followerIds").getValue();
                                        if (watchlist == null) {
                                            watchlist = new ArrayList<String>();
                                            watchlist.add("null");
                                        }
                                        if (reviews == null) {
                                            reviews = new ArrayList<Review>();
                                           // reviews.add("null");
                                            reviews.add(new Review(("null")));

                                        }
                                        if (following == null) {
                                            following = new ArrayList<String>();
                                            following.add("null");
                                        }
                                        if (followers == null) {
                                            followers = new ArrayList<String>();
                                            followers.add("null");
                                        }
                                        User user = new User(facebookName, email, userId1, watchlist, reviews, following, followers);
                                        myDatabase.child(userId1).setValue(user);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } catch (Exception e) {
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender, birthday");
            request.setParameters(parameters);
            request.executeAsync();
            startActivity(i);
        }




        btnLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Intent i = new Intent(WelcomeActivity.this, HomeActivity.class);
                //Facebook userId(the numerical one)
                userId1 = loginResult.getAccessToken().getUserId();
                i.putExtra("user_id", userId1);
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("Main", response.toString());
                                setProfileToView(object);
                                try {
                                    WelcomeActivity.this.facebookName = object.getString("name");
                                    WelcomeActivity.this.email = object.getString("email");

                                    myDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            List<String> watchlist = (ArrayList<String>) dataSnapshot.child(userId1).child("watchlist").getValue();
                                            List<Review> reviews = (ArrayList<Review>) dataSnapshot.child(userId1).child("reviews").getValue();
                                            List<String> following = (ArrayList<String>) dataSnapshot.child(userId1).child("followingIds").getValue();
                                            List<String> followers = (ArrayList<String>) dataSnapshot.child(userId1).child("followerIds").getValue();
                                            if (watchlist == null) {
                                                watchlist = new ArrayList<String>();
                                                watchlist.add("null");
                                            }
                                            if (reviews == null) {
                                                reviews = new ArrayList<Review>();
                                                // reviews.add("null");
                                                reviews.add(new Review(("null")));

                                            }
                                            if (following == null) {
                                                following = new ArrayList<String>();
                                                following.add("null");
                                            }
                                            if (followers == null) {
                                                followers = new ArrayList<String>();
                                                followers.add("null");
                                            }
                                            User user = new User(facebookName, email, userId1, watchlist, reviews, following, followers);
                                            myDatabase.child(userId1).setValue(user);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
                startActivity(i);

            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {

            }
        });

    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }





    private void setProfileToView(JSONObject jsonObject) {
        try {
            this.email = (jsonObject.getString("email"));
            this.gender = (jsonObject.getString("gender"));
            this.facebookName = (jsonObject.getString("name"));
            this.profilePicId = (jsonObject.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private List<String> getWatchlist(final String id) {
        final List<String>[] lArr = new List[1];
        myDatabase.child(id).child("watchList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> l = (ArrayList<String>) dataSnapshot.getValue();
                lArr[0]= l;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return lArr[0];
    }

    private boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

}

