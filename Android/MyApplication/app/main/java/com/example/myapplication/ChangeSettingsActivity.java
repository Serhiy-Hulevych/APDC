package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.content.Loader;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChangeSettingsActivity extends AppCompatActivity {

    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mEmailView;
    private EditText mProfileView;
    private EditText mAddressView;
    private EditText mMobileView;
    private EditText mPhoneView;
    private JSONObject credentials;
    private UserSaveTask registo;
    private boolean emailInUse = false;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_settings);

        context = this;
        Button mRegisterButton = (Button) findViewById(R.id.save);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    private void save() {
        mPasswordView = (EditText) findViewById(R.id.password2);
        mEmailView = (EditText) findViewById(R.id.email2);
        mProfileView = (EditText) findViewById(R.id.profile2);
        mAddressView = (EditText) findViewById(R.id.address2);
        mMobileView = (EditText) findViewById(R.id.mobile2);
        mPhoneView = (EditText) findViewById(R.id.phone2);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String username = prefs.getString("username","");
        String password = mPasswordView.getText().toString();
        String email = mEmailView.getText().toString();
        String profile = mProfileView.getText().toString();
        String address = mAddressView.getText().toString();
        String mobile = mMobileView.getText().toString();
        String phone = mPhoneView.getText().toString();

        if(TextUtils.isEmpty(password)) {
            mPasswordView.setError("Password required");
            mPasswordView.requestFocus();
        }
        else if(TextUtils.isEmpty(email)) {
            mEmailView.setError("Email required");
            mEmailView.requestFocus();
        }
        else if(TextUtils.isEmpty(profile)) {
            mProfileView.setError("Profile type required");
            mProfileView.requestFocus();
        }
        else if(TextUtils.isEmpty(address)) {
            mAddressView.setError("Address required");
            mAddressView.requestFocus();
        }
        else if(TextUtils.isEmpty(mobile)) {
            mMobileView.setError("Mobile number required");
            mMobileView.requestFocus();
        }
        else if(TextUtils.isEmpty(phone)) {
            mPhoneView.setError("Phone number required");
            mPhoneView.requestFocus();
        }
        else if(!profile.equalsIgnoreCase("public") && !profile.equalsIgnoreCase("private")) {
            mProfileView.setError("Invalid profile type");
            mProfileView.requestFocus();
        }

        else {
            try {
                credentials = new JSONObject();
                credentials.accumulate("username", username);
                credentials.accumulate("password", password);
                credentials.accumulate("email", email);
                credentials.accumulate("perfil", profile);
                credentials.accumulate("morada", address);
                credentials.accumulate("movel", mobile);
                credentials.accumulate("fixo", phone);

                registo = new UserSaveTask(this, credentials);
                registo.execute((Void) null);
            } catch (Exception e) {
                System.out.println("Erro algures a criar os dados do registo\n");
            }
        }
    }

    public class UserSaveTask extends AsyncTask<Void, Void, String> {

        private JSONObject registo;
        private Context context;

        UserSaveTask(Context context, JSONObject registo) {
            this.registo = registo;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                emailInUse = false;
                return RequestsREST.doPOST(new URL("https://light-ratio-234221.appspot.com/rest/users/v4"), credentials);

            } catch (Exception e) {
                String temp = e.getMessage();
                if(temp.equalsIgnoreCase("HTTP error code: 406"))
                    emailInUse = true;
                System.out.println("Erro algures no background\n");
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            try {
                if(emailInUse) {
                    mEmailView.setError("Email already exists");
                    mEmailView.requestFocus();
                    emailInUse = false;
                }
                else
                    redirect(context);
            }
            catch (Exception e) {
                System.out.println("Erro algures no postExecute\n");
            }
        }

        /*private void openMap(Context context, String morada) {
            Intent intent = new Intent(context, HomeMap.class);
            intent.putExtra("Address", morada);
            startActivity(intent);
        }*/

        private void  redirect(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String role = prefs.getString("role","");
            if(role.equalsIgnoreCase("user")) {
                SharedPreferences.Editor ed = prefs.edit();
                ed.putString("address",mAddressView.getText().toString());
                ed.commit();
                Intent intent = new Intent(context, HomeMap.class);
                startActivity(intent);
            }
            else if(role.equalsIgnoreCase("gbo")) {
                Intent intent = new Intent(context, HomeGBO.class);
                startActivity(intent);
            }

        }

    }

}