package mvm.game;

import java.util.Random;

// Platform-independent game rules: state machine, scoring, lives, bonus slots,
// spectrum smoothing and chord-release decisions, and the game-over sequence.
// No Android/Rajawali/BASS imports — the renderer feeds inputs (time, FFT data,
// collisions) and applies outputs (pending spawns, flags) to the scene.
public final class GameLogic {

	public static final int STATE_INIT = 0;
	public static final int STATE_RUNNING = 1;
	public static final int STATE_PAUSE = 2;
	public static final int STATE_FINISH = 3;

	// Bonus slots: index into m_wBonus / BONUS_SCORE / the renderer's bonus systems.
	public static final int BONUS_WEAPON = 0;
	public static final int BONUS_SCORE_10 = 1;
	public static final int BONUS_SCORE_50 = 2;
	public static final int BONUS_SCORE_400 = 3;
	public static final int BONUS_SHIP = 4;
	public static final int BONUS_SCORE_25 = 5;
	public static final int NUM_BONUS = 6;
	private static final long[] BONUS_SCORE = {0, 1000, 5000, 40000, 0, 2500};

	public static final int MAX_NUM_SHIPS = 5;
	private static final float CHORD_VALUE = 100;
	private static final float BONUS_TIME = 2.5f;
	private static final float SHIP_RING_TIME = 30f;
	private static final float SHIP_HIT_TIME = 6f;
	private static final float SHIP_HIT_FLASH_TIME = .2f;
	private static final float AUTO_FIRE_INTERVAL = .1f;
	private static final float STATUS_TIME = .1f;
	private static final float STARTUP_DELAY = 7f;
	private static final float SONG_POSITION_CHECK_TIME = 1f;

	public static final int SPECTRUM_SIZE = 26;
	public static final int SPECTRUM_SKIP_FREQ = 12;
	public static final int NUM_SPECTRUM_BIN = SPECTRUM_SIZE - SPECTRUM_SKIP_FREQ;
	public static final float SPECTRUM_MAX_VALUE = 2.0f;
	private static final int SPECTRUM_SCAN_RANGE = 16000;
	private static final float FFT_INTERVAL = 0.02f;
	private static final float RELEASE_CHORD_MIN_TIME = 1f;
	private static final float RELEASE_CHORD_MAX_TIME = 5f;
	// Original computed this with int division (16000 / 26 = 615), keep it.
	private static final float SPECTRUM_FREQ_DELTA = SPECTRUM_SCAN_RANGE / SPECTRUM_SIZE;

	private final GameEvents mEvents;
	private final String mTitle;
	private final float m1024DivFreq;

	private int mState = STATE_INIT;
	private long mScore = 0;
	private int mShipCount = 3;
	private float mStartUpDelay = STARTUP_DELAY;

	private boolean bRingOn = false;
	private float mShowShipRingTime = SHIP_RING_TIME;

	private boolean bShipHit = false;
	private float mShowShipHitTime = SHIP_HIT_TIME;
	private float mShipHitAnimation = SHIP_HIT_FLASH_TIME;
	private boolean mFlashShip = false;

	private float mAutoFireDuration = 0.0f;
	private float mTempAutoFireTime = AUTO_FIRE_INTERVAL;
	private boolean mAutoFirePending = false;

	private float fBonusTime = BONUS_TIME;
	private final int[] m_wBonus = new int[NUM_BONUS];
	private final Random mBonusRandom = new Random();
	private int mPendingBonusSlot = -1;

	private final Random mChordRandom = new Random();

	private final float[] mSpectrumVal = new float[SPECTRUM_SIZE];
	private final float[] mTimeToReleaseChord = new float[NUM_SPECTRUM_BIN];
	private final boolean[] mChordReleasePending = new boolean[NUM_SPECTRUM_BIN];
	private float m_fFFTTime = FFT_INTERVAL;
	private boolean bSpectrumReady = false;

	private float mShowStatusTime = STATUS_TIME;
	private int mTempShipCountTracker = 0;
	private long mTempScoreTracker = 0;
	private String mTempDisplayStatus = "";

	private float mCheckSongPositionTime = SONG_POSITION_CHECK_TIME;

	private float mGameOverStringTime = 0.5f;
	private float mGameOverTitleTime = 2f;
	private float mGameOverTop3Time = 4f;
	private float mGameOverTop31Time = 6f;
	private float mGameOverTop32Time = 8f;
	private float mGameOverTop33Time = 10f;
	private float mGameOverTime = 20f;
	private static final float GAME_OVER_ONE_TIME = 1000;
	private boolean mGameOverReported = false;
	private boolean mExitPending = false;

	public GameLogic(GameEvents events, String title, int sampleRate) {
		mEvents = events;
		mTitle = title;
		m1024DivFreq = 1024.0f / (float)sampleRate;
		for(int i=0; i<NUM_SPECTRUM_BIN; i++)
			mTimeToReleaseChord[i] = randomReleaseTime();
	}

	private float randomReleaseTime() {
		return RELEASE_CHORD_MIN_TIME + (float)Math.random() * (RELEASE_CHORD_MAX_TIME - RELEASE_CHORD_MIN_TIME);
	}

	// ---- per-frame ----

	// Advances startup countdown, ring/hit/auto-fire timers and the bonus spawner.
	public void tick(float fTimeLapsed) {
		if(mState == STATE_INIT) {
			mStartUpDelay -= fTimeLapsed;
			if(mStartUpDelay <= 0.0f) {
				mState = STATE_RUNNING;
				mEvents.removeCountDown();
			}
		}

		if(bRingOn) {
			mShowShipRingTime -= fTimeLapsed;
			if(mShowShipRingTime <= 0.0f) {
				bRingOn = false;
				mShowShipRingTime = SHIP_RING_TIME;
			}
		}

		if(mState == STATE_RUNNING && bShipHit)
			updateShipHit(fTimeLapsed);

		updateAutoFire(fTimeLapsed);

		updateBonus(fTimeLapsed);
	}

	private void updateShipHit(float fTimeLapsed) {
		mShowShipHitTime -= fTimeLapsed;
		if(mShowShipHitTime <= 0.0f) {
			bShipHit = false;
			mShowShipHitTime = SHIP_HIT_TIME;
		}
		else {
			mShipHitAnimation -= fTimeLapsed;
			if(mShipHitAnimation <= 0.0f) {
				mFlashShip = !mFlashShip;
				mShipHitAnimation = SHIP_HIT_FLASH_TIME;
			}
		}
	}

	private void updateAutoFire(float fTimeLapsed) {
		mAutoFireDuration -= fTimeLapsed;
		if(mAutoFireDuration > 0.0f) {
			mTempAutoFireTime -= fTimeLapsed;
			if(mTempAutoFireTime <= 0.0f) {
				if(mState == STATE_RUNNING)
					mAutoFirePending = true;
				mTempAutoFireTime = AUTO_FIRE_INTERVAL;
			}
		}
	}

	private void updateBonus(float fTimeLapsed) {
		fBonusTime -= fTimeLapsed;
		if(fBonusTime <= 0.0f) {
			int rndBonusLocation = mBonusRandom.nextInt(NUM_BONUS);

			// Check if there is a bonus in that location.
			for(int i = 0; i < NUM_BONUS; i++) {
				if(m_wBonus[i] == rndBonusLocation) {
					fBonusTime = BONUS_TIME;
					return;
				}
			}

			insertBonus(rndBonusLocation);

			fBonusTime = BONUS_TIME;
		}
	}

	private void insertBonus(int rndBonusLocation) {
		int rndBonus = mBonusRandom.nextInt(NUM_BONUS);

		if(m_wBonus[rndBonus] != 0)
			return;

		m_wBonus[rndBonus] = rndBonusLocation;

		if(rndBonus == BONUS_SHIP && mShipCount >= MAX_NUM_SHIPS) {
			m_wBonus[BONUS_SHIP] = 0;
			return;
		}

		mPendingBonusSlot = rndBonus;
	}

	// Publishes score/ship/countdown updates to the UI at the original cadence.
	public void publishStatus(float fTimeLapsed) {
		if(mTempShipCountTracker != mShipCount) {
			mEvents.onShipUpdate(mShipCount);
			mTempShipCountTracker = mShipCount;
		}

		if(mTempScoreTracker != mScore) {
			displayStatus("  " + mScore + " ", false);
			mTempScoreTracker = mScore;
		}

		mShowStatusTime -= fTimeLapsed;
		if(mShowStatusTime <= 0.0f) {
			if(mState == STATE_INIT) {
				String status = ((int)mStartUpDelay == 0) ? " " : " " + (int)mStartUpDelay + " ";
				displayStatus(status, true);
			}
			mShowStatusTime = STATUS_TIME;
		}
	}

	private void displayStatus(String value, boolean countDown) {
		if(mTempDisplayStatus.equals(value))
			return;

		if(value != null && value.length() > 0) {
			mEvents.onStatusUpdate(value, countDown);
			mTempDisplayStatus = value;
		}
	}

	// Drives the staged game-over UI sequence; ends with an exit request after 20s.
	public void tickGameOver(float fTimeLapsed) {
		if(!mGameOverReported) {
			mGameOverReported = true;
			mEvents.onShipUpdate(mShipCount);
			mEvents.gameOverData(mTitle, mScore);
		}

		mGameOverTime -= fTimeLapsed;
		if(mGameOverTime <= 0.0f) {
			mGameOverTime = GAME_OVER_ONE_TIME;
			mExitPending = true;
			return;
		}

		mGameOverTop33Time -= fTimeLapsed;
		if(mGameOverTop33Time <= 0.0f) {
			mGameOverTop33Time = GAME_OVER_ONE_TIME;
			mEvents.gameOverTop33Time();
			return;
		}

		mGameOverTop32Time -= fTimeLapsed;
		if(mGameOverTop32Time <= 0.0f) {
			mGameOverTop32Time = GAME_OVER_ONE_TIME;
			mEvents.gameOverTop32Time();
			return;
		}

		mGameOverTop31Time -= fTimeLapsed;
		if(mGameOverTop31Time <= 0.0f) {
			mGameOverTop31Time = GAME_OVER_ONE_TIME;
			mEvents.gameOverTop31Time();
			return;
		}

		mGameOverTop3Time -= fTimeLapsed;
		if(mGameOverTop3Time <= 0.0f) {
			mGameOverTop3Time = GAME_OVER_ONE_TIME;
			mEvents.gameOverTop3Time();
			return;
		}

		mGameOverTitleTime -= fTimeLapsed;
		if(mGameOverTitleTime <= 0.0f) {
			mGameOverTitleTime = GAME_OVER_ONE_TIME;
			mEvents.gameOverTitleTime();
			return;
		}

		mGameOverStringTime -= fTimeLapsed;
		if(mGameOverStringTime <= 0.0f) {
			mGameOverStringTime = GAME_OVER_ONE_TIME;
			mEvents.gameOverStringTime();
		}
	}

	// ---- spectrum ----

	public boolean spectrumUpdateDue(float fTimeLapsed) {
		m_fFFTTime -= fTimeLapsed;
		if(m_fFFTTime <= 0.0f) {
			m_fFFTTime = FFT_INTERVAL;
			return true;
		}
		return false;
	}

	// Smooths the raw FFT into spectrum bar values and decides which bins release a chord.
	public void onSpectrumData(float[] fft, float fTimeLapsed) {
		for(int sp = SPECTRUM_SKIP_FREQ; sp < SPECTRUM_SIZE; sp++) {
			float specSize = getAmp(sp * SPECTRUM_FREQ_DELTA, (sp + 1) * SPECTRUM_FREQ_DELTA, fft) * 130.0f;

			if(specSize > mSpectrumVal[sp])
				mSpectrumVal[sp] = specSize;
			else
				mSpectrumVal[sp] *= 0.85f;

			if(mSpectrumVal[sp] > SPECTRUM_MAX_VALUE)
				mSpectrumVal[sp] = SPECTRUM_MAX_VALUE;

			if(mSpectrumVal[sp] < 0.0f)
				mSpectrumVal[sp] = 0.0f;

			int bin = sp - SPECTRUM_SKIP_FREQ;
			mTimeToReleaseChord[bin] -= fTimeLapsed;

			if(mSpectrumVal[sp] == SPECTRUM_MAX_VALUE &&
			   mTimeToReleaseChord[bin] <= 0.0f &&
			   mState == STATE_RUNNING) {
				mChordReleasePending[bin] = true;
				mTimeToReleaseChord[bin] = randomReleaseTime();
			}
		}

		bSpectrumReady = true;
	}

	private float getAmp(float start, float end, float[] fft) {
		float amp = 0;
		// nearest bins to the start/end frequencies
		int bin1 = (int)(m1024DivFreq * start + 0.5);
		int bin2 = (int)(m1024DivFreq * end + 0.5);

		for(int b = bin1; b <= bin2; b++) {
			if(fft[b] > amp)
				amp = fft[b];
		}

		return amp;
	}

	public boolean consumeChordRelease(int bin) {
		if(mChordReleasePending[bin]) {
			mChordReleasePending[bin] = false;
			return true;
		}
		return false;
	}

	// Returns the chord group to try first (0..NUM-1), or -1 for no spawn:
	// ~25% of release events are skipped on purpose (original tuning).
	public int rollChordGroup() {
		return mChordRandom.nextInt(4) - 1;
	}

	// ---- inputs from the platform layer ----

	public void bulletHitChord() {
		mScore += CHORD_VALUE;
	}

	public void chordHitShip() {
		if(bShipHit == false)
			mShipCount--;
		bShipHit = true;
		mScore += CHORD_VALUE;

		if(mShipCount == 0)
			mState = STATE_FINISH;
	}

	public void bonusCollected(int slot) {
		if(slot == BONUS_WEAPON)
			bRingOn = true;
		else if(slot == BONUS_SHIP) {
			if(mShipCount < MAX_NUM_SHIPS)
				mShipCount++;
		}
		else
			mScore += BONUS_SCORE[slot];

		m_wBonus[slot] = 0;
	}

	public void autoFire(float duration) {
		mAutoFireDuration = duration;
	}

	public void pause() {
		mState = STATE_PAUSE;
	}

	public void songEnded() {
		mState = STATE_FINISH;
	}

	public boolean songCheckDue(float fTimeLapsed) {
		mCheckSongPositionTime -= fTimeLapsed;
		if(mCheckSongPositionTime <= 0.0f) {
			mCheckSongPositionTime = SONG_POSITION_CHECK_TIME;
			return true;
		}
		return false;
	}

	// ---- queries ----

	public int getState() { return mState; }
	public long getScore() { return mScore; }
	public int getShipCount() { return mShipCount; }
	public boolean isRingOn() { return bRingOn; }
	public boolean isShipHit() { return bShipHit; }
	public boolean isShipFlashOn() { return mFlashShip; }
	public boolean isSpectrumReady() { return bSpectrumReady; }
	public float getSpectrumValue(int sp) { return mSpectrumVal[sp]; }

	public boolean consumeAutoFire() {
		if(mAutoFirePending) {
			mAutoFirePending = false;
			return true;
		}
		return false;
	}

	public int consumePendingBonusSlot() {
		int slot = mPendingBonusSlot;
		mPendingBonusSlot = -1;
		return slot;
	}

	public int bonusLocation(int slot) {
		return m_wBonus[slot];
	}

	public boolean consumeExitRequest() {
		if(mExitPending) {
			mExitPending = false;
			return true;
		}
		return false;
	}
}
