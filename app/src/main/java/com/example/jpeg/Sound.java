package com.example.jpeg;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class Sound {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String fileName = Environment.getExternalStorageDirectory() + "/record.3gpp";
    private File outFile;

    public void recordStart() {
        try {
            releaseRecorder();

            outFile = new File(fileName);
            if (outFile.exists()) {
                outFile.delete();
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(fileName);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void recordStop() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
        }
    }

    public void playStart() {
        try {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void write(String path, Context context){
        File file = new File(path);
        if(path == null || path.equals("")){
            Toast.makeText(context,"Введить путь для файла", Toast.LENGTH_LONG).show();
        }else if(outFile == null){
            Toast.makeText(context,"Запишите аудио", Toast.LENGTH_LONG).show();
        }else {

            try(FileInputStream fin=new FileInputStream(outFile);
                FileOutputStream fos=new FileOutputStream(file))
            {
                byte[] buffer = new byte[fin.available()];
                // считываем буфер
                fin.read(buffer, 0, buffer.length);
                // записываем из буфера в файл
                fos.write(buffer, 0, buffer.length);
                Toast.makeText(context,"Сохранено", Toast.LENGTH_SHORT).show();
            }
            catch(IOException ex){
                System.out.println(ex.getMessage());
            }
        }
    }
}