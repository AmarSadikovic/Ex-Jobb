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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isPlaying = false;
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

    public void stopSound(){
        player.stop();
        player.release();
        isPlaying = false;
        System.out.println("STOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOP");
    }

    public class LocalBinder extends Binder {
        SoundPlayer getService() {
            return SoundPlayer.this;
        }
    }
}
