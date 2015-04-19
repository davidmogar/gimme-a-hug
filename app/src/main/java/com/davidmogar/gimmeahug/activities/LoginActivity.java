package com.davidmogar.gimmeahug.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.davidmogar.gimmeahug.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


public class LoginActivity extends ActionBarActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_done) {
            handlerCredentials();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handlerCredentials() {
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();

        if (email.trim().length() == 0 || password.trim().length() == 0) {
            Toast.makeText(getApplicationContext(), getText(R.string.empty_form_error), Toast.LENGTH_SHORT).show();
        } else {
            validateCredentials(email, password);
        }
    }

    private void validateCredentials(String email, String password) {
        ValidateUserTask task = new ValidateUserTask();
        task.execute(email, password);
    }

    private void startNextActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void saveUserId(String userId) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.user_id), userId);
        editor.commit();
    }

    private class ValidateUserTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://156.35.95.69:8002/users/auth/" + params[0]);
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(get);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    saveUserId(response.getJSONObject("data").getString("_id"));
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "ValidateUserTask error: " + e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                startNextActivity();
            } else {
                Toast.makeText(getApplicationContext(), getText(R.string.invalid_credentials_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
