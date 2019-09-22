package com.example.jpeg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private final int PERMISSIONS_RECORD_AUDIO = 1;
    private final int PERMISSIONS_WRITE_STORAGE = 2;
    private Sound sound = new Sound();
    private EditText mEditText;
    private Handler handler = new Handler();
    private MainActivity mainActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mEditText = findViewById(R.id.input);
        mEditText.setText(Environment.getExternalStorageDirectory() + "/records/record.3gpp");
        sound.mkdir();
    }


    public void onClick(View v){
        switch (v.getId()){
            case R.id.write:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_WRITE_STORAGE);
                }
                sound.write(mEditText.getText().toString(), this);
            case R.id.start_record:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_RECORD_AUDIO);
                }

                sound.recordStart();
                handler.post(mPollTask);
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

    private Runnable mPollTask = new Runnable() {
        public void run() {
            try {
                double amp = sound.getAmplitude();

                if ((amp > 0)) {
                    synchronized (this) {
                        wait(300);
                    }
                    sound.recordStop();
                    Toast.makeText(mainActivity, "Остановлено", Toast.LENGTH_LONG).show();
                } else {
                    handler.postDelayed(mPollTask, 20);
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    };
}