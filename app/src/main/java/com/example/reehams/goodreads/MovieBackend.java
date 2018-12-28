package com.example.reehams.goodreads;

/**
 * Created by rahulkooverjee on 2/20/17.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.json.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;

public class MovieBackend extends AsyncTask<String, Void, JSONObject> {

    // Returns the JSON Object as a result of search query
    @Override
    protected JSONObject doInBackground(String... strings) {
        try {
            String query = strings[0];
            URL url = new URL(query);
            Scanner scanner = new Scanner(url.openConnection().getInputStream());
            StringBuilder responseBuilder = new StringBuilder();
            while(scanner.hasNextLine()) {
                responseBuilder.append(scanner.nextLine());
            }
            String response = responseBuilder.toString();
            JSONTokener tokener = new JSONTokener(response);
            JSONObject json = new JSONObject(tokener);
            return json;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getPoster(String URL) {
        String[] queryArr = new String[1];
        queryArr[0] = URL;
        AsyncTask search = new URLThread().execute(queryArr);
        try {
            return (Bitmap) search.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject getMovieDetails(String movieId) {
        String[] queryArr = new String[1];
        queryArr[0] = movieId;
        AsyncTask search = new URLThread().execute(queryArr);
        try {
            return (JSONObject) search.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class URLThread extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                String query = strings[0];
                if (query.equals("n/a")) {
                    return null;
                }
                URL url = new URL(query);
                return BitmapFactory.decodeStream(url.openConnection().getInputStream());
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
