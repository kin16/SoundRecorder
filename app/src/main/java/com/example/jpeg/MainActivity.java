package com.example.jpeg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final Sound sound = new Sound();
    private final Handler handler = new Handler();
    private double lastAmp = 0;
    private Button record;
    private boolean re = false;
    private String path,A,ripe, size, hit;
    private SoundPool mSoundPool;
    private int mSoundId = 1;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        record = findViewById(R.id.write);

        editText = findViewById(R.id.edit);
        sound.mkdir();


        mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        mSoundPool.load(this, R.raw.sound_1, 1);
        onClickA(editText);
    }


    public void onClickA(View view) {
        editText.setText("");
        path = null;
        ripe = null;
        size = null;
        hit = null;
        A = "A-"+randomName();
    }

    public void onClickHit(View v){
        if(v.getId() == R.id.l){
            hit = "Ладонь";
        }else if(v.getId() == R.id.k) {
            hit = "Кулак";
        }
    }

    public void onClickSize(View v){
       switch (v.getId()){
           case R.id.d:
               size = "0";
               break;
           case R.id.c:
               size = "2";
               break;
           case R.id.b:
               size = "4";
               break;
           case R.id.a:
               size = "10";
               break;
       }
    }

    public void onClickRipe(View v){
        switch (v.getId()){
            case R.id.ba:
                ripe = "5";
                break;
            case R.id.bb:
                ripe = "4";
                break;
            case R.id.bc:
                ripe = "2";
                break;
            case R.id.bd:
                ripe = "1";
                break;
            case R.id.bf:
                ripe = "3";
                break;
            case R.id.be:
                ripe = "0";
                break;
        }
    }

    private void playRaw() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float leftVolume = curVolume / maxVolume;
        float rightVolume = curVolume / maxVolume;
        int priority = 1;
        int no_loop = 0;
        float normal_playback_rate = 1f;
        int mStreamId = mSoundPool.play(mSoundId, leftVolume, rightVolume, priority, no_loop,
                normal_playback_rate);

        Toast.makeText(getApplicationContext(),
                "soundPool.play()",
                Toast.LENGTH_LONG).show();
    }

    //Если произошло нажатие на кнопку
    public void onClick(View v){
        final int PERMISSIONS_RECORD_AUDIO = 1;
        final int PERMISSIONS_WRITE_STORAGE = 2;
        final int PERMISSIONS_READ_STORAGE = 3;
        //Запрос разрешений
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_RECORD_AUDIO);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_WRITE_STORAGE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_READ_STORAGE);
        }

        path = Environment.getExternalStorageDirectory()+ "/records/file-"+ randomName() +".aac";
        if(editText.getText().toString() != null && !editText.getText().toString().equals("")) {
            if (ripe != null && !ripe.equals("")) {
                if (size != null && !size.equals("")) {
                    if (hit != null && !hit.equals("")) {
                        isRecord();
                    } else {
                        Toast.makeText(this, "Чем ударяете?", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "какой размер?", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Какая спелость?", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "Какой сорт?", Toast.LENGTH_SHORT).show();
        }

    }

    private String randomName(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyy-hhmmss.SSS");
        return simpleDateFormat.format(new Date());
    }

    private void isRecord(){
        //Если запись не идет, то сменить вид кнопки,
        // запустить запись аудио, запустить поток чтения амплитуды
        if(!re) {
            re = true;
            setDrawableRecord();
            sound.recordStart();
            handler.post(mPollTask);
        }
        //Если запись идет, то сменить вид кнопки,остановить запись аудио
        else {
            re = false;
            setDrawableRecord();
            sound.recordStop();
            sound.preWrite(path, this);
            playRaw();
            createCsv();
        }
    }

    //Смена вида кнопки
    private void setDrawableRecord(){
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

    //Фоновый поток измерения амплитуды
    private final Runnable mPollTask = new Runnable() {
        public void run() {
            try {
                double amp = sound.getAmplitude();
                if (amp > 0 && (amp - lastAmp) > 2) {
                    synchronized (this) {
                        wait(300);
                    }
                    isRecord();
                    Toast.makeText(MainActivity.this, "Остановлено", Toast.LENGTH_LONG).show();
                } else {
                    lastAmp = amp;
                    handler.postDelayed(mPollTask, 5);
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    };

    //Проверка файла на существование
    private boolean isFile() {
        File f = new File(Environment.getExternalStorageDirectory() + "/records/record.csv"/* what you want to load in SD card */);
        if (f.isFile() && f.canRead()) {
            return true;
        } else {
            return false;
        }
    }

    //Запись в csv файл
    private void createCsv() {
        try {
            boolean b = isFile();
            FileWriter csvWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/records/record.csv", true);
            if(!b){
                csvWriter.append("path");
                csvWriter.append(",");
                csvWriter.append("watermelon");
                csvWriter.append(",");
                csvWriter.append("knock/flat");
                csvWriter.append(",");
                csvWriter.append("ripe");
                csvWriter.append(",");
                csvWriter.append("size");
                csvWriter.append(",");
                csvWriter.append("hit");
                csvWriter.append("\n");
            }
            csvWriter.append(path);
            csvWriter.append(",");
            csvWriter.append(A);
            csvWriter.append(",");
            csvWriter.append(editText.getText().toString());
            csvWriter.append(",");
            csvWriter.append(ripe);
            csvWriter.append(",");
            csvWriter.append(size);
            csvWriter.append(",");
            csvWriter.append(hit);
            csvWriter.append("\n");

            csvWriter.flush();
            csvWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if(mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
        super.onDestroy();
    }

}