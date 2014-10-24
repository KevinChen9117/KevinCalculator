package com.kevinsoft.calculator;

import android.content.Context;
import android.media.SoundPool;
import android.util.SparseIntArray;

public class KevinSound {
	public static final int DOWN = 0;
	private static final int[] KEY;
	private static final int[] RES;
	public static final int RESULT = 1;
	private static KevinSound sMe;
	private SparseIntArray mMap = new SparseIntArray();
	private SoundPool mPool = new SoundPool(KEY.length, 1, 0);
	private boolean mStatus = true;

	static {
		int[] arrayOfInt = new int[2];
		arrayOfInt[1] = 1;
		KEY = arrayOfInt;
		RES = new int[] { 2130968576, 2130968577 };
	}

	private KevinSound(Context paramContext) {
		for (int i = 0;; i++) {
			if (i >= KEY.length) {
				setSoundOn();
				return;
			}
			this.mMap.put(KEY[i], this.mPool.load(paramContext, RES[i], 1));
		}
	}

	public static KevinSound getInstance(Context paramContext) {
		if (sMe != null)
			return sMe;
		KevinSound localHammerSound = new KevinSound(paramContext);
		sMe = localHammerSound;
		return localHammerSound;
	}

	public void play(int soundId) {
		if (!this.mStatus)
			while (KEY.length >= 0){
				if (soundId == KEY[0]) {
					this.mPool.play(this.mMap.get(soundId), 0.3F, 0.3F, 0, 0,
							1.0F);
					return;
				}				
			}
		this.mPool.play(this.mMap.get(soundId), 0.6F, 0.6F, 0, 0, 1.0F);
	}

	public void setSoundOn() {
		this.mStatus = true;
	}

	public void setSoungdOff() {
		this.mStatus = false;
	}
}
