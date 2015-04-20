package com.davidmogar.gimmeahug.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.models.Comment;
import com.davidmogar.gimmeahug.models.User;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> implements View.OnClickListener {

    private List<Comment> comments;
    private View.OnClickListener listener;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_view, parent, false);

        view.setOnClickListener(this);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Comment comment = comments.get(i);

        viewHolder.tvComment.setText(comment.getComment());

        new ImageLoadTask("http://156.35.95.69/gimmeahug/" + comment.getUser().getId(), viewHolder.profileImage).execute();
        new GetUserTask(comment.getUser(), viewHolder.tvName, viewHolder.tvEmail).execute();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public List<Comment> getComments() {
        return comments;
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onClick(view);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profileImage;
        public TextView tvName;
        public TextView tvEmail;
        public TextView tvComment;

        public ViewHolder(View view) {
            super(view);

            profileImage = (CircleImageView) view.findViewById(R.id.profile_image);
            tvName = (TextView) view.findViewById(R.id.name);
            tvEmail = (TextView) view.findViewById(R.id.email);
            tvComment = (TextView) view.findViewById(R.id.comment);
        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }

    private class GetUserTask extends AsyncTask<Void, Void, Boolean> {

        private User user;
        private TextView tvName;
        private TextView tvEmail;

        public GetUserTask(User user, TextView tvName, TextView tvEmail) {
            this.user = user;
            this.tvName = tvName;
            this.tvEmail = tvEmail;
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
                    user.setName(response.getJSONObject("data").getString("username"));
                    user.setEmail(response.getJSONObject("data").getString("email"));
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "GetUserTask error: " + e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());
        }
    }
}
