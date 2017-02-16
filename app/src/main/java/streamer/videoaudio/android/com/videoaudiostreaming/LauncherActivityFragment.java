package streamer.videoaudio.android.com.videoaudiostreaming;

import android.content.Context;
import android.media.AudioDeviceCallback;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A placeholder fragment containing a simple view.
 */
public class LauncherActivityFragment extends Fragment implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,SeekBar.OnSeekBarChangeListener,
        View.OnClickListener,AudioManager.OnAudioFocusChangeListener {

    private SurfaceView mVideoView;
    private SeekBar mBuffer;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder mHolder;
    private Handler mHandler;
    private TextView mCurrentDur,mTotalDur;
    private ProgressBar mProgressBar;
    private Button mClick;
    private boolean isPrepared = false;
    AudioManager am = null;



    public LauncherActivityFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            mediaPlayer = new MediaPlayer();
            mHandler = new Handler();
            am = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
            int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_launcher, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVideoView = (SurfaceView)view.findViewById(R.id.videoView);
        mCurrentDur = (TextView)view.findViewById(R.id.cur_duration);
        mTotalDur = (TextView)view.findViewById(R.id.total_duration);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        mClick = (Button)view.findViewById(R.id.click);
        mClick.setOnClickListener(this);
        mBuffer = (SeekBar)view.findViewById(R.id.buffer);
        mBuffer.setOnSeekBarChangeListener(this);
        mBuffer.setMax(100);
        mBuffer.setProgress(0);
        mHolder = mVideoView.getHolder();
        mHolder.addCallback(this);

    }

    private void initMediaPlayer(){
            mediaPlayer.setDisplay(mHolder);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnCompletionListener(this);
    }

    public static String getDurationString(long durationMs, boolean negativePrefix) {
        return String.format(Locale.getDefault(), "%s%02d:%02d",
                negativePrefix ? "-" : "",
                TimeUnit.MILLISECONDS.toMinutes(durationMs),
                TimeUnit.MILLISECONDS.toSeconds(durationMs) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMs))
        );
    }



    private final Runnable mUpdateCounters = new Runnable() {
        @Override
        public void run() {
            int pos = mediaPlayer.getCurrentPosition();
            final int dur = mediaPlayer.getDuration();
            if (pos > dur) pos = dur;
            mCurrentDur.setText(getDurationString(pos, false));
            mTotalDur.setText(getDurationString(dur, false));
            mBuffer.setProgress(pos);
            mBuffer.setMax(dur);
            if (mHandler != null)
                mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        int duration = mp.getDuration();
        mBuffer.setSecondaryProgress((duration * percent) / 100);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mHandler.removeCallbacks(mUpdateCounters);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        mProgressBar.setVisibility(View.INVISIBLE);
        mHandler.post(mUpdateCounters);
        mp.start();
    }

    @Override
    public void onClick(View v) {
        try {
            if(mediaPlayer.isPlaying()){
                mClick.setText("Play");
                mediaPlayer.pause();
            }else {
                mClick.setText("Pause");
                if(!isPrepared) {
                    mediaPlayer.setDataSource(getActivity() , Uri.parse("http://www.stephaniequinn.com/Music/Allegro%20from%20Duet%20in%20C%20Major.mp3"));
                    mProgressBar.setVisibility(View.VISIBLE);
                    mediaPlayer.prepare();
                }else{
                    mediaPlayer.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initMediaPlayer();



    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mediaPlayer.setDisplay(null);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            mHandler.removeCallbacks(mUpdateCounters);
            mediaPlayer.seekTo(progress);
            mHandler.post(mUpdateCounters);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        Log.d("anurag"," focus change" + focusChange);

        if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        {
            mediaPlayer.pause();
        }
        else if(focusChange == AudioManager.AUDIOFOCUS_GAIN)
        {
            mediaPlayer.setVolume(0.5f, 0.5f);
            mediaPlayer.start();
        }
        else if(focusChange == AudioManager.AUDIOFOCUS_LOSS)
        {
            mediaPlayer.pause();
        }
    }
}
