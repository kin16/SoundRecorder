package com.example.jpeg;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import java.io.File;



public class Sound {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private final String fileName = Environment.getExternalStorageDirectory() + "/records/record_pattern.aac";
    private File outFile;
    private File file;
    private static final String TAG = "Sound";

    //Начало записи
    public void recordStart() {
        try {
            releaseRecorder();

            outFile = new File(fileName);
            if (outFile.exists()) {
                outFile.delete();
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(96000);
            mediaRecorder.setOutputFile(fileName);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Остановка записи
    public void recordStop() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    //Начало воспроизведения
    public void playStart(Context context) {
        try {
            if (file == null) {
                Toast.makeText(context, "Сохраните аудио", Toast.LENGTH_LONG).show();
            } else {
                releasePlayer();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(file.getAbsolutePath());
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    //Подготовка к обрезанию
    public void preWrite(String path,Context context){
        if(getDuration() > 350) {
            file = new File(path);
            new MyTask().execute(context);
        } else {
            Toast.makeText(context,"Error", Toast.LENGTH_SHORT).show();
        }
    }

    //Обрезание, используя библиотеку ffmpeg
    private void write(Context context) {
            FFmpeg fFmpeg = FFmpeg.getInstance(context);
            try {
                fFmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {

                    @Override
                    public void onFailure() {
                        Log.i("Load", "on failure");
                    }

                    @Override
                    public void onSuccess() {
                        Log.i("Load", "success");
                    }

                    @Override
                    public void onStart() {
                        Log.i("Load", "on start");
                    }
                    @Override
                    public void onFinish() {
                        Log.i("Load", "on finish");
                    }
                });

                int duration = getDuration();
                if(duration > 350) {
                    int mduration = duration - 350;
                    int min = mduration / 1000;
                    int max = duration / 1000;
                    duration = duration - (max * 1000);
                    mduration = mduration - (min * 1000);
                    int mx = 0;
                    if (max > 10) {
                        mx = max / 10;
                        max = max - (mx * 10);
                    }
                    int mn = 0;
                    if (min > 10) {
                        mn = min / 10;
                        min = min - (mn * 10);
                    }
                    fFmpeg.execute(new String[]{"-y", "-i", outFile.getAbsolutePath(),
                                    "-ss", "00:00:" + mn + "" + min + "." + mduration,
                                    "-t", "00:00:" + mx + "" + max + "." + duration,
                                    "-async", "1", "-c", "copy", file.getAbsolutePath()},
                            new FFmpegExecuteResponseHandler() {
                                @Override
                                public void onSuccess(String message) {
                                    Log.i("FFmpeg", message);
                                }

                                @Override
                                public void onProgress(String message) {
                                    String[] strings = message.split("\n");
                                    for (String string : strings) {
                                        Log.i("FFmpeg", string);
                                    }
                                }

                                @Override
                                public void onFailure(String message) {
                                    String[] strings = message.split("\n");
                                    for (String string : strings) {
                                        Log.e("FFmpeg", string);
                                    }
                                }

                                @Override
                                public void onStart() {
                                    Log.i("FFmpeg", "on start");
                                }

                                @Override
                                public void onFinish() {
                                    Log.i("FFmpeg", "on finish");
                                }
                            });
                }
            } catch (FFmpegNotSupportedException e) {
                e.printStackTrace();
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return duration;
    }

    public void mkdir() {
        File theDir = new File(Environment.getExternalStorageDirectory() + "/records");
        //создание папки, если отсутсвует
        if (!theDir.exists()) {
            Log.d(TAG, "creating directory: " + theDir.getName());
            theDir.mkdir();
        }
    }

    public double getAmplitude() {
        if (mediaRecorder != null)
            //вычисление амплитуды
            return 20 * Math.log10(mediaRecorder.getMaxAmplitude() / 2700.0);
        else
            return 0;
    }

    //Перевод в фоновый поток
    private class MyTask extends AsyncTask<Context, Void, Void> {
        @Override
        protected Void doInBackground(Context... voids) {
            write(voids[0]);
            return null;
        }
    }
}