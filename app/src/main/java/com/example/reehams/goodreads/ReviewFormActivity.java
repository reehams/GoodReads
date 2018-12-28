package com.example.reehams.goodreads;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReviewFormActivity extends SideBar implements AdapterView.OnItemSelectedListener {


    private String[] ratingSpinner;
    private String rating;
    private EditText review;
    private Button submitBtn;
    private Button cancelBtn;
    private String reviewText;
    private String movieName;
    private TextView reviewHeader;
    private DatabaseReference myDatabase;
    private String movieId;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_form);
        super.onCreateDrawer();
        myDatabase = FirebaseDatabase.getInstance().getReference();
        reviewHeader = (TextView) findViewById(R.id.reviewheader);
        movieName = getIntent().getStringExtra("movie_name");
        movieId = getIntent().getStringExtra("movie_id");
        userId = getIntent().getStringExtra("user_id");
        reviewHeader.setText("Review " + movieName + " below!");

        review = (EditText) findViewById(R.id.reviewEditText);

        // Saving user review text from form to Firebase
        submitBtn = (Button) findViewById(R.id.button2);
        cancelBtn = (Button) findViewById(R.id.button3);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        ReviewFormActivity.this);

                // set title
                alertDialogBuilder.setTitle("Submit Review");

                if (rating.equals("blank")) {
                    Toast.makeText(getApplicationContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
                    return;
                }

                // set dialog message
                alertDialogBuilder
                        .setMessage("Are you sure you want to submit this review?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                // current activity
                                reviewText = review.getText().toString();
                                String name = WelcomeActivity.facebookName;
                                String userId = WelcomeActivity.userId1;
                                final Review review1 = new Review(movieId, rating, reviewText, movieName);
                                myDatabase.child(userId).child("reviews").addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                // Get user value
                                                List<HashMap<String, String>> l = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
                                                if (l.get(0).get("movieId").equals("null")) {
                                                    l.remove(0);
                                                }
                                                l.add(review1.getMapping());
                                                myDatabase.child(ReviewFormActivity.this.userId).child("reviews").setValue(l);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                boolean b = myDatabase.child(movieId).getDatabase() != null;
                                myDatabase.addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.child(movieId).exists()) {
                                                    myDatabase.child(movieId).addListenerForSingleValueEvent(
                                                            new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    // Get user value
                                                                    List<HashMap<String, String>> l = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
                                                                    l.add(review1.getMapping());
                                                                    myDatabase.child(movieId).setValue(l);
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });
                                                } else {
                                                    List<HashMap<String, String>> l = new ArrayList<HashMap<String, String>>();
                                                    l.add(review1.getMapping());
                                                    myDatabase.child(movieId).setValue(l);
                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                Intent i = new Intent(ReviewFormActivity.this, MovieDetailsActivity.class);
                                Bundle extras = new Bundle();
                                extras.putString("user_id", userId);
                                extras.putString("JSON_Data", movieId);
                                i.putExtras(extras);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
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
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        ReviewFormActivity.this);

                // set title
                alertDialogBuilder.setTitle("Cancel Review");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Are you sure you want to cancel your review?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                // current activity
                                Intent i = new Intent(ReviewFormActivity.this, MovieDetailsActivity.class);
                                Bundle extras = new Bundle();
                                extras.putString("user_id", userId);
                                extras.putString("JSON_Data", movieId);
                                i.putExtras(extras);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
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
        });


        this.ratingSpinner = new String[]{"Select Rating", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ratings_array, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        switch (pos) {
            case 0:
                rating = "blank";
                break;
            case 1:
                rating = "0";
                break;
            case 2:
                rating = "1";
                break;
            case 3:
                rating = "2";
                break;
            case 4:
                rating = "3";
                break;
            case 5:
                rating = "4";
                break;
            case 6:
                rating = "5";
                break;
            case 7:
                rating = "6";
                break;
            case 8:
                rating = "7";
                break;
            case 9:
                rating = "8";
                break;
            case 10:
                rating = "9";
                break;
            case 11:
                rating = "10";
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another Interface callback
    }

    @Override
    protected void onCreateDrawer() {
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        userId = getIntent().getStringExtra("user_id");

        addDrawerItems();
        setupDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

   @Override
   protected void addDrawerItems() {
       String[] osArray = { "Home", "My Account", "My Watchlist", "Top Charts", "Movie Search", "User Search", "About Us", "Log Out"};
       mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
       mDrawerList.setAdapter(mAdapter);
       mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
               AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                       ReviewFormActivity.this);

               // set title
               alertDialogBuilder.setTitle("Cancel Review");

               // set dialog message
               alertDialogBuilder
                       .setMessage("Nagivating away will cancel your review! Are you sure you want to proceed?")
                       .setCancelable(false)
                       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                               if (position == 0) {
                                   Intent i = new Intent(ReviewFormActivity.this,  HomeActivity.class);
                                   i.putExtra("user_id", userId);
                                   startActivity(i);
                               }
                               if (position == 1) {
                                   Intent i = new Intent(ReviewFormActivity.this,  MyAccountActivity.class);
                                   i.putExtra("user_id", userId);
                                   startActivity(i);
                               }
                               if (position == 2) {
                                   Intent i = new Intent(ReviewFormActivity.this,  WatchlistActivity.class);
                                   i.putExtra("user_id", userId);
                                   startActivity(i);
                               }
                               if (position == 3) {
                                   Intent i = new Intent(ReviewFormActivity.this,  TopChartsActivity.class);
                                   i.putExtra("user_id", userId);
                                   startActivity(i);
                               }
                               if (position == 4) {
                                   Intent i = new Intent(ReviewFormActivity.this,  MovieActivity.class);
                                   i.putExtra("user_id", userId);
                                   startActivity(i);
                               }
                               if (position == 5) {
                                   Intent i = new Intent(ReviewFormActivity.this,  UserSearch.class);
                                   i.putExtra("user_id", userId);
                                   startActivity(i);
                               }
                               if (position == 6) {
                                   Intent i = new Intent(ReviewFormActivity.this, AboutUs.class);
                                   i.putExtra("user_id", userId);
                                   startActivity(i);
                               }
                               if (position == 7) {
                                   Intent i = new Intent(ReviewFormActivity.this, LogOutActivity.class);
                                   i.putExtra("user_id", userId);
                                   startActivity(i);
                               }
                           }
                       })
                       .setNegativeButton("No", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                               // if this button is clicked, just close
                               // the dialog box and close drawer too
                               dialog.cancel();
                               mDrawerLayout.closeDrawer(Gravity.LEFT);
                           }
                       });
               // create alert dialog
               AlertDialog alertDialog = alertDialogBuilder.create();

               // show it
               alertDialog.show();
           }
       });
   }
}
