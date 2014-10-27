package com.kevinsoft.calculator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;

/**
 * This class manages sound resources and plays the sound.
 * 
 * @author Kevin CHEN
 *
 */
public class KevinSoundManager {
	//Tag of this class for logging purpose
	private static String TAG = KevinSoundManager.class.getSimpleName();
	//Singleton instance
	private static KevinSoundManager sMe;
	private int mCurrentStreamId;
	private AudioManager mAudioManager;
	private Context mContext;
	private Handler mHandler = new Handler();
	
	private static final String[] KEY = {"1","2","3","4","5","6","7","8","9","0","AC","DEL","+","\u00d7","\u2212","\u00f7","=","."};
	private static final int[] RES = {R.raw.one,R.raw.two,R.raw.three,R.raw.four,R.raw.five,
									  R.raw.six,R.raw.seven,R.raw.eight,R.raw.nine,R.raw.zero,
									  R.raw.ac,R.raw.del,R.raw.plus,R.raw.mul,R.raw.minus,
									  R.raw.div,R.raw.equal,R.raw.dot};

	private Runnable mPlayNext = new Runnable() {
		public void run() {
			KevinSoundManager.this.mSoundPool
					.stop(KevinSoundManager.this.mCurrentStreamId);
			KevinSoundManager.this.playNextSound();
		}
	};
	private boolean mPlaying;
	private SoundPool mSoundPool;
	private HashMap<String, Sound> mSoundPoolMap ;
	private Vector<String> mSoundQueue = new Vector<String>();

	private KevinSoundManager(){}
	
	//Singleton 
	public static synchronized KevinSoundManager getInstance() {
		if (sMe == null){
			sMe = new KevinSoundManager();
		}
		return sMe;
	}

	private void playNextSound() {
		if(this.mSoundQueue.isEmpty()){
			return ;
		}
		if (!this.mSoundQueue.isEmpty()) {
			String str = (String) this.mSoundQueue.remove(0);
			Sound sound = (Sound) this.mSoundPoolMap.get(str);
			if (sound != null) {
				this.mCurrentStreamId = this.mSoundPool.play(sound.id, 0.2F,
						0.2F, 1, 0, 1.0F);
				this.mPlaying = true;
				this.mHandler.postDelayed(this.mPlayNext, sound.time);
			}
		}
	}

	public void addSound(String text, int resID, int timeDelayed) {
		Sound localSound = new Sound(this.mSoundPool.load(this.mContext,resID, 1), timeDelayed);
		this.mSoundPoolMap.put(text, localSound);
	}

	/**
	 * Clean up method
	 */
	public void cleanup() {
		unloadAll();
		this.mSoundPool.release();
		this.mSoundPool = null;
		sMe = null;
	}

	
	public void initSounds(Context context) {
		this.mContext = context;
		this.mSoundPool = new SoundPool(KEY.length, 3, 0);
		this.mSoundPoolMap = new HashMap<String, Sound>();
		this.mAudioManager = ((AudioManager) this.mContext
				.getSystemService("audio"));
		this.mPlaying = false;
		
		addSound(mContext.getString(R.string.digit1), R.raw.one, 320);
        addSound(mContext.getString(R.string.digit2), R.raw.two, 274);
        addSound(mContext.getString(R.string.digit3), R.raw.three, 304);
        addSound(mContext.getString(R.string.digit4), R.raw.four, 215);
        addSound(mContext.getString(R.string.digit5), R.raw.five, 388);
        addSound(mContext.getString(R.string.digit6), R.raw.six, 277);
        addSound(mContext.getString(R.string.digit7), R.raw.seven, 447);
        addSound(mContext.getString(R.string.digit8), R.raw.eight, 274);
        addSound(mContext.getString(R.string.digit9), R.raw.nine, 451);
        addSound(mContext.getString(R.string.digit0), R.raw.zero, 404);
        addSound(mContext.getString(R.string.clear), R.raw.ac, 696);
        addSound(mContext.getString(R.string.del), R.raw.del, 442);
        addSound(mContext.getString(R.string.plus), R.raw.plus, 399);
        addSound(mContext.getString(R.string.mul), R.raw.mul, 399);
        addSound(mContext.getString(R.string.minus), R.raw.minus, 399);
        addSound(mContext.getString(R.string.div), R.raw.div, 399);
        addSound(mContext.getString(R.string.equal), R.raw.equal, 480);
        addSound(mContext.getString(R.string.dot), R.raw.dot, 454);
		
	}

	public void playSeqSounds(String[] soundsToPlay) {
		int length = soundsToPlay.length;
//		for(int i = 0; i < length ; i++){
//			String str = soundsToPlay[i];
//			this.mSoundQueue.add(str);
//		}
//		if (!this.mPlaying){
//			playNextSound();
//		}
		Log.d(TAG, "soundsToPlay :" + soundsToPlay.toString());
		for (int j = 0;; j++) {
			if (j >= length) {
				if (!this.mPlaying)
					playNextSound();
				return;
			}
			String str = soundsToPlay[j];
			this.mSoundQueue.add(str);
		}
	}

	/**
	 * Play the sound 
	 * @param text text for the sound
	 */
	public void playSound(String text) {
		stopSound();
		this.mSoundQueue.add(text);
		playNextSound();
	}

	/**
	 * Stop playing the sound .
	 */
	public void stopSound() {
		this.mHandler.removeCallbacks(this.mPlayNext);
		this.mSoundQueue.clear();
		this.mSoundPool.stop(this.mCurrentStreamId);
		this.mPlaying = false;
	}

	/**
	 * Remove all the sound 
	 */
	public void unloadAll() {
		stopSound();
		if (this.mSoundPoolMap.size() > 0) {
			Iterator<String> localIterator = this.mSoundPoolMap.keySet().iterator();
			while (true) {
				if (!localIterator.hasNext()) {
					this.mSoundPoolMap.clear();
					return;
				}
				String str = (String) localIterator.next();
				this.mSoundPool.unload(((Sound) this.mSoundPoolMap.get(str)).id);
			}
		}

	}

	private final class Sound {
		public int id;
		public int time;

		public Sound(int id, int time) {
			this.id = id;
			this.time = time;
		}
	}
}