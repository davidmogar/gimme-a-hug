package com.davidmogar.gimmeahug.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.adapters.CommentAdapter;
import com.davidmogar.gimmeahug.helpers.DividerItemDecoration;
import com.davidmogar.gimmeahug.models.Comment;
import com.davidmogar.gimmeahug.models.User;
import com.melnykov.fab.FloatingActionButton;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends Fragment {

    private User user;
    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public static CommentsFragment newInstance(User user) {
        CommentsFragment fragment = new CommentsFragment();
        fragment.user = user;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CommentAdapter(user.getComments());
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    private void showDialog() {
        Context context = getActivity();

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Write a comment");

        final EditText input = new EditText(context);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                storeComment(input.getText().toString());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void storeComment(String comment) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(getString(R.string.user_id), "");

        if (userId.trim().length() > 0) {
            new CreateCommentTask().execute(comment, userId);
        }
    }

    private void refreshComments() {
        new GetUserCommentsTask(user).execute();
    }

    private class CreateCommentTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://156.35.95.69:8002/comments");
            post.setHeader("content-type", "application/json");

            try {
                JSONObject comment = new JSONObject();
                comment.put("text", params[0]);
                comment.put("author", params[1]);
                comment.put("target", user.getId());

                post.setEntity(new StringEntity(comment.toString()));

                HttpResponse httpResponse = httpClient.execute(post);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "CreateCommentTask error: " + e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                refreshComments();
            }
        }
    }

    private void parseComments(User user, JSONArray data) {
        List<Comment> comments = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            try {
                Comment comment = new Comment();

                JSONObject commentObject = data.getJSONObject(i);
                comment.setComment(commentObject.getString("text"));
                comment.setUser(new User(commentObject.getString("author")));

                comments.add(comment);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        user.setComments(comments);

        updateUI();
    }

    private void updateUI() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new CommentAdapter(user.getComments());
                recyclerView.swapAdapter(adapter, false);
            }
        });
    }

    private class GetUserCommentsTask extends AsyncTask<Void, Void, Boolean> {

        private User user;

        public GetUserCommentsTask(User user) {
            this.user = user;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://156.35.95.69:8002/users/" + user.getId());
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(get);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    parseComments(user, response.getJSONObject("data").getJSONArray("comments"));
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "GetUserHugsTask error: " + e);
            }

            return result;
        }
    }

}
