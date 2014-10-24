package com.kevinsoft.calculator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;

public class KevinSoundManager {
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
	
	public static KevinSoundManager getInstance() {
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
		
		addSound("1", R.raw.one, 320);
        addSound("2", R.raw.two, 274);
        addSound("3", R.raw.three, 304);
        addSound("4", R.raw.four, 215);
        addSound("5", R.raw.five, 388);
        addSound("6", R.raw.six, 277);
        addSound("7", R.raw.seven, 447);
        addSound("8", R.raw.eight, 274);
        addSound("9", R.raw.nine, 451);
        addSound("0", R.raw.zero, 404);
        addSound("CLR", R.raw.ac, 696);
        addSound("DELETE", R.raw.del, 442);
        addSound("+", R.raw.plus, 399);
        addSound("\u00d7", R.raw.mul, 399);
        addSound("\u2212", R.raw.minus, 399);
        addSound("\u00f7", R.raw.div, 399);
        addSound("=", R.raw.equal, 480);
        addSound(".", R.raw.dot, 454);
		
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
		Log.d("TCL", "soundsToPlay :" + soundsToPlay.toString());
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

	public void playSound(String text) {
		stopSound();
		this.mSoundQueue.add(text);
		playNextSound();
	}

	public void stopSound() {
		this.mHandler.removeCallbacks(this.mPlayNext);
		this.mSoundQueue.clear();
		this.mSoundPool.stop(this.mCurrentStreamId);
		this.mPlaying = false;
	}

	public void unloadAll() {
		stopSound();
		if (this.mSoundPoolMap.size() > 0) {
			Iterator localIterator = this.mSoundPoolMap.keySet().iterator();
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