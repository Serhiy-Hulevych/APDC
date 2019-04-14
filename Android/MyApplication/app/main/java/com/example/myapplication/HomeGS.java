package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.Context;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class HomeGS extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        System.out.println("Entrou aqui-1\n");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_gs);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        EditText value = (EditText) findViewById(R.id.value);
        long guardado = prefs.getLong("wrong",0);
        value.setText(String.valueOf(guardado));
        value.setFocusable(false);

        context = this;
        Button mEmailSignInButton = (Button) findViewById(R.id.logout);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectLogin(context);
            }
        });

        Button mCreateGS = (Button) findViewById(R.id.createGBO);
        mCreateGS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerGBO();
            }
        });

        RelativeLayout parent = (RelativeLayout) findViewById(R.id.parent);
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTime();
            }
        });

        EditText numero = (EditText) findViewById(R.id.value);
        numero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTime();
            }
        });

    }


    private void redirectLogin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        ed.clear().commit();
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
    }

    private void registerGBO() {
        Intent intent = new Intent(this, RegisterGBOActivity.class);
        startActivity(intent);
    }

    private void resetTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        long newLimit = System.currentTimeMillis() + 60*5*1000;
        ed.putLong("validade",newLimit);
        ed.commit();
    }
}
