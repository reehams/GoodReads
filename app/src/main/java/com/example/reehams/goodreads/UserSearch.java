package com.example.reehams.goodreads;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.*;

import java.util.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.*;


import static com.example.reehams.goodreads.WelcomeActivity.facebookName;
import static com.example.reehams.goodreads.WelcomeActivity.userId1;

/**
 * Created by rahulkooverjee on 3/26/17.
 */

public class UserSearch extends SideBar {

    EditText editText; // the input textbox
    ListView listView;
    private boolean hasResults = true;
    private ArrayList<User> results = new ArrayList<>();
    private static String[] searchResultsIds = new String[10]; // Options to be shown in list view
    String name;
    String email;
    String id2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_search);
        super.onCreateDrawer();
        listView = (ListView) findViewById(R.id.user_search_list);
        editText = (EditText) findViewById(R.id.editText);
    }

    // Search button - performs search
    protected void onButtonPressed(View view) {
        String searchText = editText.getText().toString();
        if (searchText.equals("")) {
            return;
        }
        search(searchText);
    }

    private void search(final String searchQuery) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        ArrayList<String> noResultsList = new ArrayList<String>();
        noResultsList.add("No Users Found");
        final ArrayAdapter<String> noResultsAdaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, noResultsList);
        final ArrayAdapter<User> arrayAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, results);
        listView.setAdapter(arrayAdapter);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> list = new ArrayList<User>();
                int i = 0;
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (i > 10) {
                        break;
                    }
                   // Toast.makeText(UserSearch.this, childSnapshot.toString(), )
                    String userName = childSnapshot.child("name").getValue(String.class);
                    if (userName != null && userName.toLowerCase().contains(searchQuery.toLowerCase())) {
                        String id = childSnapshot.child("id").getValue(String.class);
                        String email = childSnapshot.child("email").getValue(String.class);
                        User user = new User(userName, email, id);
                        list.add(user);
                        UserSearch.searchResultsIds[i] = childSnapshot.child("id").getValue(String.class);
                        i++;
                    }
                }
                if (list.size() != 0) {
                    hasResults = true;
                    results.clear();
                    results.addAll(list);
                    arrayAdapter.notifyDataSetChanged();
                }
                else {
                    hasResults = false;
                    listView.setAdapter(noResultsAdaptor);
                    noResultsAdaptor.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!hasResults) {
                    return;
                }
                //Toast.makeText(UserSearch.this, "Value: " + results.get(position), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(UserSearch.this, FollowActivity.class);
                name = results.get(position).name;
                email = results.get(position).email;
                id2 = results.get(position).id;
                i.putExtra("name", name);
                i.putExtra("email", email);
                i.putExtra("id", id2);
                i.putExtra("userId1", WelcomeActivity.userId1);
                i.putExtra("userName1", WelcomeActivity.facebookName);
                startActivity(i);
                // Do nothing if there is no result
                if (results.get(position) == null) {
                    Toast.makeText(UserSearch.this, "Null searchresult ID", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
