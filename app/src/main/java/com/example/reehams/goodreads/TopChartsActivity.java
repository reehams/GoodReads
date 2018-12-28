package com.example.reehams.goodreads;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.widget.AdapterView;
import android.os.AsyncTask;

import android.view.View;

import android.widget.ListView;
import android.widget.Toast;

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

/**
 * Created by rahulkooverjee on 4/11/17.
 */

public class TopChartsActivity extends SideBar {

    private ListView topChartListView;
    private final ArrayList<String> topChartList = new ArrayList<>();
    final String userId = WelcomeActivity.userId1;
    String[] searchResults;
    final TreeSet<MovieAvgRating> set = new TreeSet<MovieAvgRating>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topcharts_layout);
        super.onCreateDrawer();
        topChartListView = (ListView) findViewById(R.id.topChartList);
        final ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, topChartList);
        topChartListView.setAdapter(arrayAdapter2);
        topChartList.clear();
        topChartList.add("Loading...");
        arrayAdapter2.notifyDataSetChanged();
        getTopChart();
    }

    public void getTopChart() {
        final long currentTimeInMS = System.currentTimeMillis();
        final long oneWeekInMS = 604800000;
        topChartList.clear();
        final DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Loop through all database entries
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String movieId = snapshot.getKey();
                    // Now we have all the movie Id children
                    if (isMovieId(movieId)) {
                        myDatabase.child(movieId).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // Get user value
                                        double sumRating = 0;
                                        double numRating = 0;
                                        String movieTitle = "";
                                        List<HashMap<String, String>> l = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
                                        for (HashMap<String, String> s : l) {
                                            String movieId = s.get("movieId");
                                            if (movieId.equals("null")) continue;
                                            movieTitle = s.get("movieTitle");
                                            double rating = Double.parseDouble(s.get("rating"));
                                            long time = Long.parseLong(s.get("time"));
                                            // Only show reviews within a week
                                            if (currentTimeInMS - oneWeekInMS < time) {
                                                sumRating += rating;
                                                numRating++;
                                            }
                                        }
                                        if (numRating != 0) {
                                           // Toast.makeText(TopChartsActivity.this, "hi", Toast.LENGTH_SHORT).show();
                                            double avgRating = sumRating / numRating;
                                            MovieAvgRating ratingObject = new MovieAvgRating(movieTitle, movieId, avgRating);
                                            set.add(ratingObject);
                                        }
                                        displaySet();

                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void displaySet() {
        boolean isEmpty = set.isEmpty();
        final ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, topChartList);
        topChartListView.setAdapter(arrayAdapter2);
        topChartList.add("Loading...");
        arrayAdapter2.notifyDataSetChanged();
        topChartList.clear();
        if (isEmpty) {
            topChartList.add("No reviews yet");
            searchResults = new String[1];
            searchResults[0] = "empty";
        }
        else {
            int numDisplayedResults = Math.max(set.size(), 10);
            int i = 0;
            searchResults = new String[set.size()];
            for (MovieAvgRating s : set) {
                if (i >= numDisplayedResults) {
                    break;
                }
                searchResults[i] = s.id;
                topChartList.add(s.name + "\n -Average rating in the past week: " + s.avgRating);
                i++;
            }
        }
        arrayAdapter2.notifyDataSetChanged();
        topChartListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(WatchlistActivity.this, "Position: " + position, Toast.LENGTH_SHORT).show();
                // Do nothing if there is no result
                if (searchResults[position] == null) {
                    Toast.makeText(TopChartsActivity.this, "Null searchresult ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (searchResults[position].equals("empty")) {
                    return;
                }
                // Pass the data of the clicked movie to the movieDetails class
                Intent i = new Intent(TopChartsActivity.this,  MovieDetailsActivity.class);
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
                        Toast.makeText(TopChartsActivity.this, "More movie details cannot be found in IMBD Database",
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

    protected boolean isMovieId(String s) {
        if (s == null || s.length() < 2) {
            return false;
        }
        return s.substring(0, 2).equals("tt");
    }


}
