package com.example.playsound;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlaySound extends Activity{
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private final int duration = 1; // seconds
    private final int sampleRate = 44100;
    private TextView textF;
//    private final int numSamples = duration * sampleRate;
//    private final double sample[] = new double[numSamples];
//    private final double freqOfTone = 25000; // hz
//    private byte generatedSnd[] = new byte[2 * numSamples];
    //private AudioTrack mAudioTrack = null; 
    private SeekBar bar; // declare seekbar object variable
    private double sliderval;
    boolean isRunning = true;
    Handler handler = new Handler();
    Thread t;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_play_sound);
        textF = (TextView)findViewById(R.id.textViewF);
        bar = (SeekBar)findViewById(R.id.seekbar1); // make seekbar object
        OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) { }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onProgressChanged(SeekBar seekBar, 
                                            int progress,
                                             boolean fromUser) {
                if(fromUser) sliderval = progress / (double)seekBar.getMax();
                textF.setText("The Frequency is: "+String.valueOf(10 + 22000*sliderval)+"Hz");
             }
          };
        bar.setOnSeekBarChangeListener(listener);
        
    }

    public void StartPlay(View view) {
    	isRunning = true;
    	if (t != null) {
            t.interrupt();
        }
/*        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
                handler.post(new Runnable() {

                    public void run() {
                        playSound();
                    }
                });
            }
        });
        thread.start();*/
        // start a new thread to synthesise audio
    	t = new Thread() {
    		public void run() {
    			// set process priority
    			setPriority(Thread.MAX_PRIORITY);
    			// set the buffer size
    			int buffsize = AudioTrack.getMinBufferSize(sampleRate,
    					AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
    			// create an audiotrack object
    			AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
    					sampleRate, AudioFormat.CHANNEL_OUT_MONO,
    					AudioFormat.ENCODING_PCM_16BIT, buffsize,
    					AudioTrack.MODE_STREAM);

    			short samples[] = new short[buffsize];
    			int amp = 10000;
    			double twopi = 2.*Math.PI;
    			double fr = 440.f;
    			double ph = 0.0;

    			// start audio
    			audioTrack.play();

    			// synthesis loop
    			while(isRunning){
    				fr =  10 + 22000*sliderval;
    				for(int i=0; i < buffsize; i++){
    					samples[i] = (short) (amp*Math.sin(ph));
    					ph += twopi*fr/sampleRate;
    				}
    				audioTrack.write(samples, 0, buffsize);
    			}
    			audioTrack.stop();
    			audioTrack.release();
    		}
    	};
    	t.start();  
    }
    
    public void StopPlay(View view) {
    	isRunning = false;
    	try {
    		t.join();
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	}
    }
//    void genTone(){
//        // fill out the array
//        for (int i = 0; i < numSamples; ++i) {
//            sample[i] = Math.cos(2 * Math.PI * i / (sampleRate/freqOfTone));
//        }
//
//        // convert to 16 bit pcm sound array
//        // assumes the sample buffer is normalised.
//        int idx = 0;
//        for (final double dVal : sample) {
//            // scale to maximum amplitude
//            final short val = (short) ((dVal * 32767));
//            // in 16 bit wav PCM, first byte is the low order byte
//            generatedSnd[idx++] = (byte) (val & 0x00ff);
//            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
//
//        }
//    }

//    void playSound(){
//    	mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
//                AudioTrack.MODE_STATIC);
//        mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
//        mAudioTrack.play();
//    }
	public void onDestroy(){
		super.onDestroy();
		isRunning = false;
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t = null;
	}
}
