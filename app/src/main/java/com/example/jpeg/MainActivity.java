package com.example.jpeg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;


public class MainActivity extends AppCompatActivity {
    private Sound sound = new Sound();
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mEditText = findViewById(R.id.input);
        mEditText.setText(Environment.getExternalStorageDirectory() + "/record.3gpp");
    }


    public void onClick(View v){
        switch (v.getId()){
            case R.id.write:
                sound.write(mEditText.getText().toString(), this);
            case R.id.start_record:
                sound.recordStart();
                break;
            case R.id.stop_record:
                sound.recordStop();
                break;
            case R.id.start_play:
                sound.playStart();
                break;
            case R.id.stop_play:
                sound.playStop();
        }
    }
}