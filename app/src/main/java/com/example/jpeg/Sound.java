package com.example.jpeg;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class Sound {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String fileName = Environment.getExternalStorageDirectory() + "/records/record_pattern.3gpp";
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
        mkdir();
        if(path.equals(null) || path.equals("")){
            Toast.makeText(context,"Введить путь для файла", Toast.LENGTH_LONG).show();
        }else if(new File(path).exists()) {
            Toast.makeText(context,"Такой файл уже существует", Toast.LENGTH_LONG).show();
        }else if(outFile == null){
            Toast.makeText(context,"Запишите аудио", Toast.LENGTH_LONG).show();
        }else {
            File file = new File(path);
            try(FileInputStream fin=new FileInputStream(outFile);
                FileOutputStream fos=new FileOutputStream(file))
            {
                /*byte[] buffer = new byte[fin.available()];
                // считываем буфер
                fin.read(buffer, 0, buffer.length);
                // записываем из буфера в файл
                fos.write(buffer, 0, buffer.length);
                 */
                int min = getDuration() - 350;
                String[] ffmpeg = new String[] {"ffmpeg", "-i", outFile.getPath(),"-ss", "0."+min, "-t", "0."+getDuration(),"-async","1", "-c", "copy", file.getPath()};
                Process p = Runtime.getRuntime().exec(ffmpeg);
                Toast.makeText(context,"Сохранено", Toast.LENGTH_SHORT).show();
            }
            catch(IOException ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    private int getDuration() {
        //узнаем продолжительность записи
        int duration = 0;
        try {
            MediaPlayer mplayer = new MediaPlayer();
            mplayer.setDataSource(fileName);
            mplayer.prepare();
            duration = mplayer.getDuration();
        }catch (Exception e){
            e.printStackTrace();
        }

        return duration;
    }

    public void mkdir(){
        File theDir = new File(Environment.getExternalStorageDirectory() + "/records");
        //создание папки, если отсутсвует
        if (!theDir.exists()) {
            System.out.println("creating directory: " + theDir.getName());
            theDir.mkdir();
        }
    }

    public double getAmplitude() {
        if (mediaRecorder != null)
            //вычисление амплитуды
            return   20 * Math.log10(mediaRecorder.getMaxAmplitude() / 2700.0);
        else
            return 0;
    }
}