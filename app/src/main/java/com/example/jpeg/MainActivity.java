package com.example.jpeg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private final int PERMISSIONS_RECORD_AUDIO = 1;
    private final int PERMISSIONS_WRITE_STORAGE = 2;
    private final int PERMISSIONS_READ_STORAGE = 3;
    private Sound sound = new Sound();
    private EditText mEditText;
    private Handler handler = new Handler();
    private MainActivity activity = this;
    private double lastAmp = 0;
    private Button record;
    private boolean re = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = findViewById(R.id.input);
        mEditText.setText(Environment.getExternalStorageDirectory() + "/records/record.aac");
        sound.mkdir();
        record = findViewById(R.id.record);
    }


    public void onClick(View v){
        switch (v.getId()){
            case R.id.write:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_READ_STORAGE);
                }
                sound.preWrite(mEditText.getText().toString(), this);
                break;
            case R.id.record:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_RECORD_AUDIO);
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_WRITE_STORAGE);
                }
                isRecord();
                break;
            case R.id.play:
                sound.playStart(this);
                break;
        }
    }

    public void isRecord(){
        if(!re) {
            re = true;
            setDrawableRecord();
            sound.recordStart();
            handler.post(mPollTask);
        }else {
            re = false;
            setDrawableRecord();
            sound.recordStop();
        }
    }

    public void setDrawableRecord(){
        if (re){
            Drawable drawable = this.getResources().getDrawable(R.drawable.button_main_shape_pres);
            record.setBackground(drawable);
            record.setText(R.string.stop_record);
        } else if(!re){
            Drawable drawable = this.getResources().getDrawable(R.drawable.button_main_shape);
            record.setBackground(drawable);
            record.setText(R.string.start_record);
        }
    }

    private Runnable mPollTask = new Runnable() {
        public void run() {
            try {
                double amp = sound.getAmplitude();
                if (amp > 0 && (amp - lastAmp) > 2) {
                    synchronized (this) {
                        wait(300);
                    }
                    isRecord();
                    Toast.makeText(activity, "Остановлено", Toast.LENGTH_LONG).show();
                } else {
                    lastAmp = amp;
                    handler.postDelayed(mPollTask, 5);
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    };
}