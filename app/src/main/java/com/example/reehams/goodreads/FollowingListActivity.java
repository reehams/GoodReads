package com.example.reehams.goodreads;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static com.example.reehams.goodreads.WelcomeActivity.facebookName;
import static com.example.reehams.goodreads.WelcomeActivity.userId1;

/**
 * Created by reehams on 3/27/17.
 */

public class FollowingListActivity extends SideBar {
    DatabaseReference reference;
    private ListView mListView;
    private ArrayList<String> whoIAmFollowing = new ArrayList<>();
    String[] searchResults; // Options to be shown in list view
    String userId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.following_list);
        super.onCreateDrawer();
        mListView = (ListView) findViewById(R.id.followingListView);
        final ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, whoIAmFollowing);
        mListView.setAdapter(arrayAdapter2);
        userId = getIntent().getStringExtra("user_id");
        reference = FirebaseDatabase.getInstance().getReference();
        reference.child(userId).child("followingIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> set = new TreeSet<String>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String following = childSnapshot.getValue(String.class);
                    if (following.equals("null")) {
                        set.add("You're not following anyone");
                        break;
                    }
                    int idx = following.indexOf(",");
                    following = following.substring(idx + 1, following.length());
                    set.add(following);
                }
                whoIAmFollowing.clear();
                whoIAmFollowing.addAll(set);
                arrayAdapter2.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent i = new Intent(FollowingListActivity.this, FollowActivity.class);
                final String name = whoIAmFollowing.get(position);
                i.putExtra("name", name);
                i.putExtra("userId1", userId1);
                i.putExtra("userName1", WelcomeActivity.facebookName);
                reference.child(WelcomeActivity.userId1).child("followingIds").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Set<String> set = new TreeSet<String>();
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String following = childSnapshot.getValue(String.class);
                            if (following.equals("null")) {
                                set.add("You're not following anyone");
                                break;
                            }
                            final String[] followingDetails = following.split(",");
                            i.putExtra("id", followingDetails[0]);
                            if(followingDetails[1].equals(name)) {
                                reference.child(followingDetails[0]).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String email = dataSnapshot.getValue(String.class);
                                        i.putExtra("email", email);
                                        String id2 = followingDetails[0];
                                        i.putExtra("id", id2);
                                        startActivity(i);
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

        });

    }
}