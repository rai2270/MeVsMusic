package mvm.flying;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import mvm.diplaylist.objroom;
import mvm.diplaylist.objship;
import mvm.diplaylist.objspectrum;
import mvm.diplaylist.objspectrumbase;
import mvm.game.GameEvents;
import mvm.game.GameLogic;
import mvm.material.GameParticleMaterial;
import mvm.particle.BonusParticleSystem;
import mvm.particle.BulletParticleSystem;
import mvm.particle.ChordParticleSystem;

import r.ChaseCamera;
import r.materials.TextureInfo;
import r.math.Number3D;
import r.renderer.RajawaliRenderer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import mvm.flying.R;
import com.un4seen.bass.BASS;

// Scene + audio layer of the game: owns the Rajawali objects, the BASS channel and
// the frame loop. All game rules live in GameLogic; this class feeds it inputs
// (time, FFT data, collisions) and applies its outputs to the scene.
public class FlyingRenderer extends RajawaliRenderer {

	public static final int ROOM_SIZE = 200;

	private static final int NUM_CHORDS_IN_EACH_GROUP = 40;
	private static final int NUM_GROUPS = 3;
	private static final int NUM_TOTAL_CHORDS = NUM_CHORDS_IN_EACH_GROUP * NUM_GROUPS;

	private static final int NUM_BULLETS = NUM_TOTAL_CHORDS;
	public static final float INACTIVE_POS = ROOM_SIZE * 1.5f;
	public static final float ROOM_SIZE_HALF = ROOM_SIZE * .5f;

	public static final float ROOM_EDGE = 1.0f;
	public static final float GAME_WORLD_X_SPACE = ROOM_SIZE_HALF - ROOM_EDGE;
	public static final float GAME_WORLD_Y_SPACE = ROOM_SIZE_HALF - ROOM_EDGE;
	public static final float GAME_WORLD_Z_SPACE = ROOM_SIZE_HALF - ROOM_EDGE;

	// Kept public for scene classes (objspectrum, FlyingActivity).
	public static final int NUM_SPECTRUM_BIN = GameLogic.NUM_SPECTRUM_BIN;
	public static final int MAX_NUM_SHIPS = GameLogic.MAX_NUM_SHIPS;
	public static float SPECTRUM_TOP = objspectrumbase.SPECTRUM_BIN_SIZE * objspectrumbase.SPECTRUM_BIN_SIZE_Y_FACTOR * GameLogic.SPECTRUM_MAX_VALUE;

	private objship mObjShip;

	public boolean mIsInitialized;

	private Number3D mAccStartValue;

	private objroom mObjRoom;

	private objspectrum[] mObjSpectrumBin;
	private Number3D[] mSpectrumBinBaseLocation;

	private Number3D mJoystickVelocity = new Number3D();
	private boolean mUseJoystickVelocity = false;

	private BulletParticleSystem mBulletParticleSystem;

	private ChordParticleSystem[] mChordParticleSystems;

	private BonusParticleSystem[] mBonusParticleSystems;

	private Number3D[] mBonusPosition = new Number3D[GameLogic.NUM_BONUS];

	private long startTime;

	String mFileName;
	boolean bAccelerometer;
	int m_freq;
	int m_chan;				// channel handle
	int[] fx;				// 3 eq bands + reverb
	ByteBuffer m_bbuf; 		// allocate a buffer for the FFT data
	float[] m_fft; 			// allocate an "int" array for the FFT data
	long mBassLen;

	private int BASS_STATE_INIT = 0;
	private int BASS_STATE_RUNNING = 1;
	private int mBassState = BASS_STATE_INIT;

	private final GameEvents mEvents;
	private final GameLogic mGameLogic;

	public FlyingRenderer(Context context, GameEvents events, String i_Title, String i_FileName, boolean i_bAccelerometer) {
		super(context);
		mEvents = events;
		setFrameRate(60);

		mFileName = i_FileName;
		bAccelerometer = i_bAccelerometer;
		m_freq = 44100;
		fx = new int[4];
	    m_bbuf=ByteBuffer.allocateDirect(512 * 4); 	// allocate a buffer for the FFT data
	    m_bbuf.order(null); 						// little-endian byte order
	    m_fft=new float[512]; 						// allocate an "int" array for the FFT data
	    mBassLen = 0;

	    mGameLogic = new GameLogic(events, i_Title, m_freq);
	}

	protected void initScene() {
		try
		{
			mObjRoom = new objroom();

			mObjSpectrumBin = new objspectrum[NUM_SPECTRUM_BIN];
			mSpectrumBinBaseLocation = new Number3D[NUM_SPECTRUM_BIN];
			for (int i = 0; i < NUM_SPECTRUM_BIN; i++)
			{
				mObjSpectrumBin[i] = new objspectrum(new Number3D(0, (ROOM_SIZE * -.5f) + .5f, 0));
			}

			mObjShip = new objship(new Number3D(0, (ROOM_SIZE * -.5f) + 7f, ROOM_SIZE * .5f - 12f));

			mObjRoom.init(mContext.getResources(), mTextureManager);
			addChild(mObjRoom.mRoomBox);

			TextureInfo objectSpectrumTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.building3));
			for (int i = 0; i < NUM_SPECTRUM_BIN; i++)
			{
				mObjSpectrumBin[i].init(i, objectSpectrumTexture);
				mSpectrumBinBaseLocation[i] = new Number3D(mObjSpectrumBin[i].mSpectrumBin.getPosition());

				addChild(mObjSpectrumBin[i].mSpectrumBin);
			}

			mObjShip.init(mContext.getResources(), mTextureManager);
			addChild(mObjShip.mShip);
			for(int i=0;i<mObjShip.mShipRings.length;i++)
			{
				addChild(mObjShip.mShipRings[i]);
			}

			ChaseCamera cam = new ChaseCamera(new Number3D(0, .6f, 3), 0.05f, mObjShip.mShip);
			cam.setFarPlane(2000);
			mCamera = cam;


			// Bullet
			Bitmap bulletBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.flare);
			TextureInfo bulletParticleTexture = mTextureManager.addTexture(bulletBitmap);
			mBulletParticleSystem = new BulletParticleSystem(NUM_BULLETS);
			mBulletParticleSystem.setMaterial(new GameParticleMaterial());
			mBulletParticleSystem.addTexture(bulletParticleTexture);
			mBulletParticleSystem.setPointSize(800);//500);
			addChild(mBulletParticleSystem);


			int[] chordTextures = {R.drawable.c4, R.drawable.c5, R.drawable.c6};
			mChordParticleSystems = new ChordParticleSystem[NUM_GROUPS];
			for (int i = 0; i < NUM_GROUPS; i++)
			{
				mChordParticleSystems[i] = new ChordParticleSystem(NUM_CHORDS_IN_EACH_GROUP);
				mChordParticleSystems[i].setMaterial(new GameParticleMaterial(true));
				mChordParticleSystems[i].addTexture(mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), chordTextures[i])));
				mChordParticleSystems[i].setPointSize(2400);
				addChild(mChordParticleSystems[i]);
			}


			mBonusPosition[0] = new Number3D(mSpectrumBinBaseLocation[0].x, mSpectrumBinBaseLocation[0].y + ROOM_EDGE, mSpectrumBinBaseLocation[0].z - SPECTRUM_TOP/5f);
			mBonusPosition[1] = new Number3D(mSpectrumBinBaseLocation[12].x, mSpectrumBinBaseLocation[12].y + ROOM_EDGE, mSpectrumBinBaseLocation[12].z + SPECTRUM_TOP/5f);
			mBonusPosition[2] = new Number3D(mSpectrumBinBaseLocation[5].x, mSpectrumBinBaseLocation[5].y + ROOM_EDGE, mSpectrumBinBaseLocation[5].z - SPECTRUM_TOP/5f);
			mBonusPosition[3] = new Number3D(mSpectrumBinBaseLocation[10].x, mSpectrumBinBaseLocation[10].y + ROOM_EDGE, mSpectrumBinBaseLocation[10].z - SPECTRUM_TOP/5f);
			mBonusPosition[4] = new Number3D(mSpectrumBinBaseLocation[2].x, mSpectrumBinBaseLocation[2].y + ROOM_EDGE, mSpectrumBinBaseLocation[2].z + SPECTRUM_TOP/5f);
			mBonusPosition[5] = new Number3D(mSpectrumBinBaseLocation[7].x, mSpectrumBinBaseLocation[5].y + ROOM_EDGE, mSpectrumBinBaseLocation[5].z + SPECTRUM_TOP/5f);

			int[] bonusTextures = {R.drawable.threerings, R.drawable.b1knew, R.drawable.b5knew, R.drawable.b40knew, R.drawable.bship, R.drawable.b25knew};
			// addChild() in the original scene order (transparent objects render in add order).
			int[] bonusSceneOrder = {GameLogic.BONUS_WEAPON, GameLogic.BONUS_SHIP, GameLogic.BONUS_SCORE_50, GameLogic.BONUS_SCORE_10, GameLogic.BONUS_SCORE_25, GameLogic.BONUS_SCORE_400};
			mBonusParticleSystems = new BonusParticleSystem[GameLogic.NUM_BONUS];
			for (int k = 0; k < GameLogic.NUM_BONUS; k++)
			{
				int i = bonusSceneOrder[k];
				mBonusParticleSystems[i] = new BonusParticleSystem(1);
				mBonusParticleSystems[i].setMaterial(new GameParticleMaterial(true));
				mBonusParticleSystems[i].addTexture(mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), bonusTextures[i])));
				mBonusParticleSystems[i].setPointSize(2400);
				addChild(mBonusParticleSystems[i]);
			}

			startTime = System.currentTimeMillis();

			mIsInitialized = true;
		}
		catch(Exception e)
		{

		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		try
		{
			mEvents.showLoader();
			super.onSurfaceCreated(gl, config);
			mEvents.hideLoader();

			// Make sure Bass will try to run one time only.
			if(mBassState == BASS_STATE_INIT)
			{
				mBassState = BASS_STATE_RUNNING;
				if(initBASS() == false)
				{
					exit();
					return;
				}
				mBassLen = BASS.BASS_ChannelGetLength(m_chan, 0);
				if(playBASS() == false)
				{
					exit();
					return;
				}
			}
		}
		catch(Exception e)
		{

		}
	}

	private void exit()
	{
		BASS.BASS_Stop();
		BASS.BASS_Free();
		mEvents.gameOverTime();
	}

	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);

		if(mGameLogic.getState() == GameLogic.STATE_PAUSE)
		{
			exit();
			return;
		}

		long currentTime = System.currentTimeMillis();
		long elapsed = currentTime - startTime;
		startTime = currentTime;
		float fTimeLapsed = elapsed / 1000f;

		if(mGameLogic.getState() == GameLogic.STATE_FINISH)
		{
			handleGameOver(fTimeLapsed);
			return;
		}

		mGameLogic.tick(fTimeLapsed);

		UpdateDisplayList(fTimeLapsed);

        CheckShipHitBonus();
        CheckBulletHitChords();
        CheckChordHitShip();

        mGameLogic.publishStatus(fTimeLapsed);

        if(mGameLogic.songCheckDue(fTimeLapsed))
        {
        	if(BASS.BASS_ChannelGetPosition(m_chan, 0) >= mBassLen)
        	{
        		mGameLogic.songEnded();
        	}
        }
	}

	private void UpdateDisplayList(float fTimeLapsed)
	{
		// Set Ship.
		if(mUseJoystickVelocity) {
			mObjShip.vVel.x = mJoystickVelocity.x*2f;
			mObjShip.vVel.y = mJoystickVelocity.y;
		}
		else
		{
			if(bAccelerometer==false)
			{
				mObjShip.vVel.x *= 0.95f;
				mObjShip.vVel.y *= 0.95f;
			}
		}

		if(mGameLogic.getState() == GameLogic.STATE_RUNNING)
		{
			boolean shipHit = mGameLogic.isShipHit();
			mObjShip.setPosition(fTimeLapsed, mGameLogic.isRingOn(), shipHit);
			applyShipFlash(shipHit && mGameLogic.isShipFlashOn());
		}

		if(mGameLogic.consumeAutoFire())
		{
			setTouch();
		}

		// Set Spectrum.
		UpdateSpectrum(fTimeLapsed);

		if (mGameLogic.isSpectrumReady())
		{
			for ( int sp = GameLogic.SPECTRUM_SKIP_FREQ; sp < GameLogic.SPECTRUM_SIZE; sp++ )
			{
				mObjSpectrumBin[sp-GameLogic.SPECTRUM_SKIP_FREQ].setPositionAndScale(mGameLogic.getSpectrumValue(sp));
			}
		}

		int bonusSlot = mGameLogic.consumePendingBonusSlot();
		if(bonusSlot != -1)
		{
			Number3D pos = mBonusPosition[mGameLogic.bonusLocation(bonusSlot)];
			mBonusParticleSystems[bonusSlot].initPosAndVel(pos.x, pos.y, pos.z);
		}

		mBulletParticleSystem.setPosition(fTimeLapsed);

		for (ChordParticleSystem chords : mChordParticleSystems)
			chords.setPosition(fTimeLapsed);

		for (BonusParticleSystem bonus : mBonusParticleSystems)
			bonus.setPosition(fTimeLapsed);

	}

	private boolean mShipFlashApplied = false;
	private void applyShipFlash(boolean flash)
	{
		if(flash == mShipFlashApplied)
			return;
		mShipFlashApplied = flash;

		int drawingMode = flash ? GLES20.GL_LINES : GLES20.GL_TRIANGLES;
		for(int i=0;i<mObjShip.mShip.getNumChildren();i++)
		{
			mObjShip.mShip.getChildAt(i).setDrawingMode(drawingMode);
		}
	}

	Number3D mTmpShipPos = new Number3D();
	float mDistanceForBonusCollision = 2.0f;//5.0f;
	private void CheckShipHitBonus()
	{
		mTmpShipPos.x = -1f*mObjShip.mShip.getPosition().x;
		mTmpShipPos.y = mObjShip.mShip.getPosition().y;
		mTmpShipPos.z = mObjShip.mShip.getPosition().z;

		for(int b=0; b<GameLogic.NUM_BONUS; b++)
		{
			BonusParticleSystem bonus = mBonusParticleSystems[b];
			for(int i=0; i<bonus.mAlive.length; i++)
			{
				if(bonus.mAlive[i] == false)
					continue;

				// Check if Ship hit bonus
				if (Within3DManhattanDistance(bonus.mBonusPosition[i], mTmpShipPos, mDistanceForBonusCollision))
				{
					mGameLogic.bonusCollected(b);

					// Remove the victim from the scene
					bonus.setInactivePosition(i);
				}
			}
		}
	}

	Number3D mTmpBulletPos = new Number3D();
	float mDistanceForCollision = 2.0f;//4.0f;
	private void CheckBulletHitChords()
	{
		boolean bBulletHit;

		for(int bi=0; bi<NUM_BULLETS; bi++) {
			bBulletHit = false;

			if(mBulletParticleSystem.mAlive[bi] == false)
				continue;

			mTmpBulletPos.x = mBulletParticleSystem.mBulletPosition[bi].x;
			mTmpBulletPos.y = mBulletParticleSystem.mBulletPosition[bi].y;
			mTmpBulletPos.z = mBulletParticleSystem.mBulletPosition[bi].z;

			// Check if bullet hit a Chord
			chordSearch:
			for(int i=0; i<NUM_CHORDS_IN_EACH_GROUP; i++)
			{
				for(ChordParticleSystem chords : mChordParticleSystems)
				{
					if( chords.mState[i] == ChordParticleSystem.STATE_ALIVE && chords.mAlive[i] )
					{
						if (Within3DManhattanDistance(mTmpBulletPos, chords.mChordPosition[i], mDistanceForCollision))
						{
							// Remove the victim from the scene
							chords.setInactivePosition(i);

							bBulletHit = true;
							mGameLogic.bulletHitChord();
							break chordSearch;
						}
					}
				}
			}

			if(bBulletHit)
			{
				mBulletParticleSystem.setInactivePosition(bi);
			}
		}
	}

	float mDistanceForChordCollisionWithShip = 2.0f;
	private void CheckChordHitShip()
	{
		mTmpShipPos.x = -1f*mObjShip.mShip.getPosition().x;
		mTmpShipPos.y = mObjShip.mShip.getPosition().y;
		mTmpShipPos.z = mObjShip.mShip.getPosition().z;

		// Check if Chord hit the Ship
		for(int i=0; i<NUM_CHORDS_IN_EACH_GROUP; i++)
		{
			for(ChordParticleSystem chords : mChordParticleSystems)
			{
				if( chords.mState[i] == ChordParticleSystem.STATE_ALIVE && chords.mAlive[i] )
				{
					if (Within3DManhattanDistance(mTmpShipPos, chords.mChordPosition[i], mDistanceForChordCollisionWithShip))
					{
						// Remove the victim from the scene
						chords.setInactivePosition(i);
						mGameLogic.chordHitShip();
					}
				}
			}
		}
	}

	public void setAccelerometerValues(float x, float y, float z) {
		if(mIsInitialized && mGameLogic.getState() == GameLogic.STATE_RUNNING) {
			if(mAccStartValue == null) {
				mAccStartValue = new Number3D(x, y, z);
			}
			if(bAccelerometer && mUseJoystickVelocity == false)
			{
				mObjShip.vVel.setAll(x*2f, y, z);
				mObjShip.vVel.subtract(mAccStartValue);
				mObjShip.vVel.multiply(.02f);
			}
		}
	}

	public void onResume() {
	}

	public void onPause()
	{
		mGameLogic.pause();
	}

	public void setShipVelocity(float x, float y) {
		mJoystickVelocity.x = x; //-x;
		mJoystickVelocity.y = y; //-y;
		mUseJoystickVelocity = true;
	}

	public void addJoystickVelocity(boolean value) {
		mUseJoystickVelocity = value;
	}

	Number3D mBulletDir = new Number3D();
	boolean mFireStraightOnly = false;
	public void setTouch()
	{
		if(mIsInitialized && mGameLogic.getState() == GameLogic.STATE_RUNNING) {
			if(mGameLogic.isRingOn())
			{
				// Fire bullets to all Chords alive
				for(int i=0; i<NUM_CHORDS_IN_EACH_GROUP; i++)
				{
					for(ChordParticleSystem chords : mChordParticleSystems)
					{
						if( chords.mState[i] == ChordParticleSystem.STATE_ALIVE && chords.mAlive[i] )
						{
							mFireStraightOnly = false;
							mBulletDir.z = chords.mChordPosition[i].z;
							mBulletDir.z -= mObjShip.mShip.getZ();
							if(mObjShip.zDir<0.0f && mBulletDir.z<0.0f)
								mFireStraightOnly = true;
							else if(mObjShip.zDir>0.0f && mBulletDir.z>0.0f)
								mFireStraightOnly = true;
							if(mFireStraightOnly)
							{
								mBulletDir.x = -1f*chords.mChordPosition[i].x;
								mBulletDir.y = chords.mChordPosition[i].y;

								mBulletDir.x -= mObjShip.mShip.getX();
								mBulletDir.y -= mObjShip.mShip.getY();

								mBulletDir.x *= -1f;
								mBulletDir.normalize();
								mBulletParticleSystem.initPosAndVel(mBulletDir.x, mBulletDir.y, mBulletDir.z, -1f*mObjShip.mShip.getX(), mObjShip.mShip.getY(), mObjShip.mShip.getZ(), true);
							}
						}
					}
				}
			}
			else
			{
				// Fire bullet from the ship forward.
				mBulletDir.setAllFrom(mObjShip.mDirection);
				mBulletDir.y = 0.0f;
				mBulletDir.x *= -1f;
				mBulletDir.normalize();
				mBulletParticleSystem.initPosAndVel(mBulletDir.x, mBulletDir.y, mBulletDir.z, -1f*mObjShip.mShip.getX(), mObjShip.mShip.getY(), mObjShip.mShip.getZ(), false);
			}
		}
	}

	public void setTouch(float AutoFireDuration) {
		mGameLogic.autoFire(AutoFireDuration);
	}

	private void UpdateSpectrum(float fTimeLapsed)
	{
		if(mGameLogic.spectrumUpdateDue(fTimeLapsed) == false)
			return;

		int res = BASS.BASS_ChannelGetData(m_chan, m_bbuf, BASS.BASS_DATA_FFT1024);
		if( res > 0 )
		{
			m_bbuf.asFloatBuffer().get(m_fft); // get the data from the buffer into the array
			mGameLogic.onSpectrumData(m_fft, fTimeLapsed);

			for (int bin = 0; bin < NUM_SPECTRUM_BIN; bin++)
			{
				if(mGameLogic.consumeChordRelease(bin))
				{
					spawnChord(bin);
				}
			}
		}
	}

	private void spawnChord(int bin)
	{
		int group = mGameLogic.rollChordGroup();
		if(group < 0)
			return;

		// Try the rolled chord type first, then the others in rotation.
		for(int j=0; j<NUM_GROUPS; j++)
		{
			if(mChordParticleSystems[(group + j) % NUM_GROUPS].initPosAndVel(
					mSpectrumBinBaseLocation[bin].x, mSpectrumBinBaseLocation[bin].y + SPECTRUM_TOP, mSpectrumBinBaseLocation[bin].z,
					mObjShip.mShip.getPosition().x, mObjShip.mShip.getPosition().y, mObjShip.mShip.getPosition().z))
			{
				return;
			}
		}
	}

	private boolean initBASS()
	{
		if (!BASS.BASS_Init(-1, m_freq, 0)) {
			return false;
		}

		// first free the current one (try both MOD and stream - it must be one of them)
		BASS.BASS_MusicFree(m_chan);
		BASS.BASS_StreamFree(m_chan);
		if( mFileName.equals(MeVsMusicActivity.DEMO_TRACK1) ||
			mFileName.equals(MeVsMusicActivity.DEMO_TRACK2) )
		{
			if ((m_chan=BASS.BASS_StreamCreateFile(new BASS.Asset(mContext.getAssets(), mFileName), 0, 0, 0))==0
				&& (m_chan=BASS.BASS_MusicLoad(new BASS.Asset(mContext.getAssets(), mFileName), 0, 0, BASS.BASS_MUSIC_RAMP, 1))==0) {
				// whatever it is, it ain't playable
				return false;
			}
		}
		else
		{
			if ((m_chan=BASS.BASS_StreamCreateFile(mFileName, 0, 0, 0))==0
				&& (m_chan=BASS.BASS_MusicLoad(mFileName, 0, 0, BASS.BASS_MUSIC_RAMP, 1))==0) {
				// whatever it is, it ain't playable
				return false;
			}
		}
		// setup the effects and start playing
		fx[0]=BASS.BASS_ChannelSetFX(m_chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
		fx[1]=BASS.BASS_ChannelSetFX(m_chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
		fx[2]=BASS.BASS_ChannelSetFX(m_chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
		fx[3]=BASS.BASS_ChannelSetFX(m_chan, BASS.BASS_FX_DX8_REVERB, 0);
		BASS.BASS_DX8_PARAMEQ p=new BASS.BASS_DX8_PARAMEQ();
		p.fGain=0;
		p.fBandwidth=18;
		p.fCenter=125;
		BASS.BASS_FXSetParameters(fx[0], p);
		p.fCenter=1000;
		BASS.BASS_FXSetParameters(fx[1], p);
		p.fCenter=8000;
		BASS.BASS_FXSetParameters(fx[2], p);
		UpdateFX(10,0);
		UpdateFX(10,1);
		UpdateFX(10,2);
		UpdateFX(0,3);

		return true;
	}

	private void UpdateFX(int v, int n) {
		if (n<3) {
			BASS.BASS_DX8_PARAMEQ p=new BASS.BASS_DX8_PARAMEQ();
			BASS.BASS_FXGetParameters(fx[n], p);
			p.fGain=v-10;
			BASS.BASS_FXSetParameters(fx[n], p);
		} else {
			BASS.BASS_DX8_REVERB p=new BASS.BASS_DX8_REVERB();
			BASS.BASS_FXGetParameters(fx[n], p);
			p.fReverbMix=(float)(v!=0?Math.log(v/20.0)*20:-96);
			BASS.BASS_FXSetParameters(fx[n], p);
		}
	}

	private boolean playBASS()
	{
		return BASS.BASS_ChannelPlay(m_chan, false);
	}

	// http://stackoverflow.com/questions/3693514/very-fast-3d-distance-check
	boolean Within3DManhattanDistance( Number3D c1, Number3D c2, float distance )
	{
		float dx = Math.abs(c2.x - c1.x);
	    if (dx > distance) return false; // too far in x direction

	    float dy = Math.abs(c2.y - c1.y);
	    if (dy > distance) return false; // too far in y direction

	    float dz = Math.abs(c2.z - c1.z);
	    if (dz > distance) return false; // too far in z direction

	    return true; // we're within the cube
	}

	boolean bGameOverCleanSceneOneTime = true;
	private void handleGameOver(float fTimeLapsed)
	{
		if(bGameOverCleanSceneOneTime)
		{
			bGameOverCleanSceneOneTime = false;

			// Clean scene
			removeChild(mObjShip.mShip);
			for(int i=0;i<mObjShip.mShipRings.length;i++)
			{
				removeChild(mObjShip.mShipRings[i]);
			}

			removeChild(mBulletParticleSystem);

			for (BonusParticleSystem bonus : mBonusParticleSystems)
				removeChild(bonus);
		}

		// Set Spectrum.
		UpdateSpectrum(fTimeLapsed);

		if (mGameLogic.isSpectrumReady())
		{
			for ( int sp = GameLogic.SPECTRUM_SKIP_FREQ; sp < GameLogic.SPECTRUM_SIZE; sp++ )
			{
				mObjSpectrumBin[sp-GameLogic.SPECTRUM_SKIP_FREQ].setPositionAndScale(mGameLogic.getSpectrumValue(sp));
			}
		}

		for (ChordParticleSystem chords : mChordParticleSystems)
			chords.setPosition(fTimeLapsed);

		mGameLogic.tickGameOver(fTimeLapsed);

		if(mGameLogic.consumeExitRequest())
		{
			exit();
		}
	}

}
