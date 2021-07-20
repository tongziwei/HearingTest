package com.example.hearingtestlibrary;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * PlaySound is used to generate tones at specific frequencies for the screening.
 */
public class PlaySound {

    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>. Also modified by Peter Oien and Andrew Lundgren
    private final int duration = 2; // seconds
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private double freqOfTone = 1000; // hz
    private static final double MAX_AMPLITUDE = 32768;//16位的采样率，2的16次方就应该是65536，所以正弦波的波峰就应该是32768
    private double volume = (MAX_AMPLITUDE/ Math.pow(Math.sqrt(10),7)); //30 decibels
    private double increase = Math.pow(Math.sqrt(10), (.5)); //increase by 5 decibels
    private double decrease = Math.sqrt(10); //Decrease by 10 decibels
    private int decibel = 30;
    private final String TAG = "PlaySound";
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int DOUBLE = 3;


    private final byte generatedSnd[] = new byte[2 * numSamples];

    /**
     * Set the frequency that the tone should be played at.
     * @param newFreq
     */
    public void setFrequency(double newFreq) {
        freqOfTone = newFreq;
    }

    /**
     * Get the current decibel level of the tone that may be produced.
     * @return decibel
     */
    public int getDecibel() {
        return decibel;
    }


    /**
     * Used to increase the volume of the tone by 5dB at a time.
     */
    public void increaseVolume() {

        if((volume * increase) <= MAX_AMPLITUDE) {
            volume *= increase;
            decibel += 5;
        } else {
            volume = MAX_AMPLITUDE;
        }
    }


    /**
     * Used to decrease the volume of a tone by 10dB at a time.
     */
    public void decreaseVolume() {

        volume /= decrease;
        decibel -= 10;
    }

    public void setDecibel(int db){
        decibel = db;
        double n =(db/5)*0.5;
        Log.d(TAG, "setDecibel: n:"+n);
        volume = (MAX_AMPLITUDE/ Math.pow(Math.sqrt(10),10))*Math.pow(Math.sqrt(10),n);
        Log.d(TAG, "setDecibel: volume"+volume);
    }

    public void setDecibel(int db,int f){
        double temp =0.0;
        switch (f){
            case 125:
                temp = 17.0;
                break;
            case 250:
                temp = 18.0;
                break;
            case 500:
                temp = 12.5;
                break;
            case 1000:
                temp = 11.0;
                break;
            case 2000:
                temp = 8.0;
                break;
            case 4000:
                temp = 2.5;
                break;
            case 6000:
                temp = -7.8;
                break;
            case 8000:
                temp =-2.6;
                break;
            default:
                break;
        }
        decibel = db;
        double n =((db-temp)/5)*0.5;
        Log.d(TAG, "setDecibel: n:"+n);
        volume = (MAX_AMPLITUDE/ Math.pow(Math.sqrt(10),10))*Math.pow(Math.sqrt(10),n);
        Log.d(TAG, "setDecibel: volume"+volume);
    }

    public void genTone(){
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * volume));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    public void playSound(int channel){
        genTone();
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_STEREO, // AudioFormat.CHANNEL_OUT_MONO
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        // 生成正弦波
        switch (channel) {
            case LEFT:
                // audioTrack.setStereoVolume(volume, 0f);
                audioTrack.setStereoVolume(1f, 0f);
                break;
            case RIGHT:
                Log.d(TAG, "playSound: maxvolume:"+AudioTrack.getMaxVolume());//1.0
                audioTrack.setStereoVolume(0f,1f); //0.6f时，90db测量90.3,1.0f时，90DB测量得94db
                break;
            case DOUBLE:
                audioTrack.setStereoVolume(0.6f, 0.6f);
                break;
        }
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(TAG, "Error line 113, Thread interrupted");
        }
        audioTrack.release();
    }



}
