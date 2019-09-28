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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private final Sound sound = new Sound();
    private EditText mEditText;
    private final Handler handler = new Handler();
    private double lastAmp = 0;
    private Button record;
    private boolean re = false;
    private Spinner ready;
    private Spinner size;
    private String radioButton;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ready = findViewById(R.id.ready);
        size = findViewById(R.id.size);

        //Записываем в переменную, чем ударяем
        RadioGroup group = findViewById(R.id.radiogr);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.l:
                        radioButton = "Ладонь";
                        break;
                    case R.id.k:
                        radioButton = "Кулак";
                        break;
                        default:
                            break;
                }
            }
        });

        editText = findViewById(R.id.edit);
        mEditText = findViewById(R.id.input);
        mEditText.setText(Environment.getExternalStorageDirectory() + "/records/record.aac");
        sound.mkdir();
        record = findViewById(R.id.record);
    }

    //Если произошло нажатие на кнопку
    public void onClick(View v){
        final int PERMISSIONS_RECORD_AUDIO = 1;
        final int PERMISSIONS_WRITE_STORAGE = 2;
        final int PERMISSIONS_READ_STORAGE = 3;
        switch (v.getId()){
            case R.id.write:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_READ_STORAGE);
                }
                if(!editText.getText().toString().equals("")) {
                    if (radioButton != null) {
                        if (sound.preWrite(mEditText.getText().toString(), this)) {
                            createCsv();
                        }
                    } else {
                        Toast.makeText(this, "Чем вы ударяте?", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "Введите сорт арбуза", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.record:
                //Запрос разрешений
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
                csvWriter.append("type");
                csvWriter.append(",");
                csvWriter.append("ready");
                csvWriter.append(",");
                csvWriter.append("size");
                csvWriter.append(",");
                csvWriter.append("hit");
                csvWriter.append("\n");
            }
            csvWriter.append(mEditText.getText().toString());
            csvWriter.append(",");
            csvWriter.append(editText.getText().toString());
            csvWriter.append(",");
            csvWriter.append(ready.getSelectedItem().toString());
            csvWriter.append(",");
            csvWriter.append(size.getSelectedItem().toString());
            csvWriter.append(",");
            csvWriter.append(radioButton);
            csvWriter.append("\n");

            csvWriter.flush();
            csvWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}