package net.elektropribor.vozov.terminal7;

import android.content.Context;
import android.media.MediaPlayer;

public class AudioPlayer {
    private MediaPlayer mPlayer;
    private int mWav;
    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();//уничтожение
            mPlayer = null;
        }
    }
    public void play(Context c) {
        stop();
        mPlayer = MediaPlayer.create(c, mWav);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
        mPlayer.start();
    }
    //задание мелодии для проигрования
    public void setSong(int wav){
        this.mWav = wav;
    }
}
