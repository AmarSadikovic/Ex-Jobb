package se.mah.af6260.exjobb;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by oskar on 2018-03-14.
 */

public class SoundPlayer extends Service{
    private MediaPlayer player;
    private MainActivity mainActivity;
    private LocalBinder mBinder;
    private boolean isPlaying;
    private boolean isPause;
    private boolean isFinished;
    private int pauseTime;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isPlaying = false;
        isPause = false;
        isFinished = true;
        mBinder = new LocalBinder();
    }

    public void setListenerActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public void playSound(int res){
        player = MediaPlayer.create(this, res);
        player.setVolume(100,100);
        isPlaying = true;
        isFinished = false;
        player.setLooping(false);
        player.start();
        System.out.println("LJUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUD");
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopSound();
            }
        });
    }

    public void pauseSound(){
        if(!isFinished) {
            pauseTime = player.getCurrentPosition();
            player.pause();
        }
        isPause = true;
    }

    public void resume(){
        if(!isFinished) {
            player.seekTo(pauseTime);
            player.start();
        }
        isPause = false;
    }

    public boolean isPause(){
        return isPause;
    }

    public void stopSound(){
        player.stop();
        player.release();
        isPlaying = false;
        isFinished = true;
        System.out.println("STOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOP");
    }

    public class LocalBinder extends Binder {
        SoundPlayer getService() {
            return SoundPlayer.this;
        }
    }
}
