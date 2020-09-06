package mvm.flying;

import java.nio.ByteBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import mvm.diplaylist.objroom;
import mvm.diplaylist.objship;
import mvm.diplaylist.objspectrum;
import mvm.diplaylist.objspectrumbase;
import mvm.material.Bonus1ParticleMaterial;
import mvm.material.Bonus2ParticleMaterial;
import mvm.material.Bonus3ParticleMaterial;
import mvm.material.Bonus4ParticleMaterial;
import mvm.material.Bonus5ParticleMaterial;
import mvm.material.Bonus6ParticleMaterial;
import mvm.material.BulletParticleMaterial;
import mvm.material.Chord4ParticleMaterial;
import mvm.material.Chord5ParticleMaterial;
import mvm.material.Chord6ParticleMaterial;
import mvm.particle.Bonus1ParticleSystem;
import mvm.particle.Bonus2ParticleSystem;
import mvm.particle.Bonus3ParticleSystem;
import mvm.particle.Bonus4ParticleSystem;
import mvm.particle.Bonus5ParticleSystem;
import mvm.particle.Bonus6ParticleSystem;
import mvm.particle.BulletParticleSystem;
import mvm.particle.Chord4ParticleSystem;
import mvm.particle.Chord5ParticleSystem;
import mvm.particle.Chord6ParticleSystem;

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

public class FlyingRenderer extends RajawaliRenderer {
	String mStatus = "";
	
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
	
	private objship mObjShip;
	
	public boolean mIsInitialized;

	private Number3D mAccStartValue;
	
	private objroom mObjRoom;
	
	private objspectrum[] mObjSpectrumBin;
	public static Number3D[] mSpectrumBinBaseLocation;
	//private objspectrumbase[] mObjSpectrumBaseBin;
	
	private Number3D mJoystickVelocity = new Number3D();
	private boolean mUseJoystickVelocity = false;
	
	private BulletParticleSystem mBulletParticleSystem;
	
	private Chord4ParticleSystem mChord4ParticleSystem;
	private Chord5ParticleSystem mChord5ParticleSystem;
	private Chord6ParticleSystem mChord6ParticleSystem;
	
	private Bonus1ParticleSystem mBonusWeaponParticleSystem;
	private Bonus2ParticleSystem mBonusShipParticleSystem;
	private Bonus3ParticleSystem mBonus50ParticleSystem;
	private Bonus4ParticleSystem mBonus10ParticleSystem;
	private Bonus5ParticleSystem mBonus25ParticleSystem;
	private Bonus6ParticleSystem mBonus400ParticleSystem;
	
	
	private long startTime;
	
	String mTitle;
	String mFileName;
	boolean bAccelerometer;
	int m_freq;
    int m_chan;				// channel handle
    int[] fx;				// 3 eq bands + reverb
    float m_1024DivFreq;
    ByteBuffer m_bbuf; 		// allocate a buffer for the FFT data
    float[] m_fft; 			// allocate an "int" array for the FFT data
    static float m_FFT_Time;
    static float m_fFFTTime;
    long mBassLen;
    float mBassChannelPositionTime = 1f;
    float mCheckBassChannelPositionTime = mBassChannelPositionTime;
    
    //float mFreeVersionTime = 120f;
    //boolean bFreeVersion = true;
    	    
    static int SPECTRUM_SIZE =	26;//32;
    static int SPECTRUM_SKIP_FREQ = 12;
    public static final int NUM_SPECTRUM_BIN = SPECTRUM_SIZE - SPECTRUM_SKIP_FREQ;
    int SPECTRUM_SCAN_RANGE	= 16000;
    static float SPECTRUM_MAX_VALUE = 2.0f;
    public static float SPECTRUM_TOP = objspectrumbase.SPECTRUM_BIN_SIZE * objspectrumbase.SPECTRUM_BIN_SIZE_Y_FACTOR * SPECTRUM_MAX_VALUE;// - objspectrumbase.SPECTRUM_BIN_SIZE;
    float g_spectrumFreqDelta = SPECTRUM_SCAN_RANGE / SPECTRUM_SIZE;
    float [] mSpectrumVal = new float[SPECTRUM_SIZE];
	
	boolean bSpectrumReadyForDisplay = false;
	
	private float mStatusTime = .1f;
	private float mShowStatusTime = mStatusTime;
	
	long mScoreCount=0;
	
	private float mStartUpDelay = 7f;
	
	private int GAME_STATE_PREV_INIT = -1;
	private int GAME_STATE_INIT = 0;
	private int GAME_STATE_RUNNING = 1;
	private int GAME_STATE_PAUSE = 2;
	private int GAME_STATE_FINISH = 3;
	private int GAME_STATE_EXIT = 4;
	private int mState = GAME_STATE_INIT;
	//int mPrevState = GAME_STATE_PREV_INIT;
	
	private int BASS_STATE_INIT = 0;
	private int BASS_STATE_RUNNING = 1;
	private int mBassState = BASS_STATE_INIT;
	
	boolean bRingIsON = false;
	private float mShipRingTime = 30f;
	private float mShowShipRingTime = mShipRingTime;
	
	private float mAutoFireDuration = 0.0f;
	private float mAutoFireTime = .1f;
	private float mTempAutoFireTime = mAutoFireTime;
	
	private static final float CHORD_VALUE = 100;
	
	static float BONUS_TIME = 2.5f;//5.0f;
	static float fBonusTime = BONUS_TIME;
	public static final int NUM_BONUS_WEAPON = 1;
	public static final int NUM_BONUS_SHIP = 1;
	public static final int NUM_BONUS_SCORE_50 = 1;
	public static final int NUM_BONUS_SCORE_400 = 1;
	public static final int NUM_BONUS_SCORE_10 = 1;
	public static final int NUM_BONUS_SCORE_25 = 1;
	public static final int NUM_BONUS = NUM_BONUS_WEAPON + NUM_BONUS_SCORE_10 + NUM_BONUS_SCORE_25 + NUM_BONUS_SCORE_50 + NUM_BONUS_SCORE_400 + NUM_BONUS_SHIP;

	public static final int MAX_NUM_SHIPS = 5;
	
	public static Number3D[] mBonusPosition = new Number3D[NUM_BONUS];
	final Random mBonusRandom = new Random();
	int[] m_wBonus = new int[NUM_BONUS];
	
	int mShipCount = 3;
	
	boolean bShipHit = false;
	private float mShipHitTime = 6f;
	private float mShowShipHitTime = mShipHitTime;
		
	public FlyingRenderer(Context context, String i_Title, String i_FileName, boolean i_bAccelerometer) { 
		super(context);
		setFrameRate(60);
		
		mState = GAME_STATE_INIT;
		
		
		
		mTitle = i_Title;
		mFileName = i_FileName;
		bAccelerometer = i_bAccelerometer;
		m_freq = 44100;
		fx = new int[4];
		m_1024DivFreq = 1024.0f/(float)m_freq;
	    m_bbuf=ByteBuffer.allocateDirect(512 * 4); 	// allocate a buffer for the FFT data
	    m_bbuf.order(null); 						// little-endian byte order
	    m_fft=new float[512]; 						// allocate an "int" array for the FFT data
	    m_FFT_Time = 0.02f;
	    m_fFFTTime = m_FFT_Time;
	    mBassLen = 0;
	}
	
	protected void initScene() {
		try
		{
			mObjRoom = new objroom();
			
			mObjSpectrumBin = new objspectrum[NUM_SPECTRUM_BIN];
			mSpectrumBinBaseLocation = new Number3D[NUM_SPECTRUM_BIN];
			//mObjSpectrumBaseBin = new objspectrumbase[NUM_SPECTRUM_BIN];
			for (int i = 0; i < NUM_SPECTRUM_BIN; i++)
			{
				mObjSpectrumBin[i] = new objspectrum(new Number3D(0, (ROOM_SIZE * -.5f) + .5f, 0));
				//mObjSpectrumBaseBin[i] = new objspectrumbase(new Number3D(0, (ROOM_SIZE * -.5f) + objspectrum.SPECTRUM_BIN_SIZE*objspectrum.SPECTRUM_BIN_SIZE_Y_FACTOR - 12.0f, 0));
			}
			
			mObjShip = new objship(new Number3D(0, (ROOM_SIZE * -.5f) + 7f, ROOM_SIZE * .5f - 12f));
			
			mObjRoom.init(mContext.getResources(), mTextureManager);
			addChild(mObjRoom.mRoomBox);
			
			TextureInfo objectSpectrumTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.building3));
			//TextureInfo objectSpectrumBaseTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.spectrumbase));
			for (int i = 0; i < NUM_SPECTRUM_BIN; i++) 
			{
				mObjSpectrumBin[i].init(i, objectSpectrumTexture);
				mSpectrumBinBaseLocation[i] = new Number3D(mObjSpectrumBin[i].mSpectrumBin.getPosition());
				//mObjSpectrumBaseBin[i].init(mObjSpectrumBin[i].mSpectrumBin.getPosition(), objectSpectrumBaseTexture);
				
				//addChild(mObjSpectrumBaseBin[i].mSpectrumBaseBin);
				addChild(mObjSpectrumBin[i].mSpectrumBin);
			}
			
			mObjShip.init(mContext.getResources(), mTextureManager);
			addChild(mObjShip.mShip);	
			//addChild(mObjShip.mRedSphere);
			for(int i=0;i<mObjShip.mShipRings.length;i++)
			{
				addChild(mObjShip.mShipRings[i]);	
			}
			//addChild(mObjShip.mSphere);
	
			ChaseCamera cam = new ChaseCamera(new Number3D(0, .6f, 3), 0.05f, mObjShip.mShip);
			cam.setFarPlane(2000);
			mCamera = cam;
			
			
			// Bullet
			Bitmap bulletBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.flare);
			TextureInfo bulletParticleTexture = mTextureManager.addTexture(bulletBitmap);
			mBulletParticleSystem = new BulletParticleSystem(NUM_BULLETS);
			mBulletParticleSystem.setMaterial(new BulletParticleMaterial());
			mBulletParticleSystem.addTexture(bulletParticleTexture);
			mBulletParticleSystem.setPointSize(800);//500);
			addChild(mBulletParticleSystem);
			
			
			TextureInfo chord4ParticleTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.c4));
			mChord4ParticleSystem = new Chord4ParticleSystem(NUM_CHORDS_IN_EACH_GROUP);
			mChord4ParticleSystem.setMaterial(new Chord4ParticleMaterial(true));
			mChord4ParticleSystem.addTexture(chord4ParticleTexture);
			mChord4ParticleSystem.setPointSize(2400);
			addChild(mChord4ParticleSystem);
			
			TextureInfo chord5ParticleTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.c5));
			mChord5ParticleSystem = new Chord5ParticleSystem(NUM_CHORDS_IN_EACH_GROUP);
			mChord5ParticleSystem.setMaterial(new Chord5ParticleMaterial(true));
			mChord5ParticleSystem.addTexture(chord5ParticleTexture);
			mChord5ParticleSystem.setPointSize(2400);
			addChild(mChord5ParticleSystem);
			
			TextureInfo chord6ParticleTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.c6));
			mChord6ParticleSystem = new Chord6ParticleSystem(NUM_CHORDS_IN_EACH_GROUP);
			mChord6ParticleSystem.setMaterial(new Chord6ParticleMaterial(true));
			mChord6ParticleSystem.addTexture(chord6ParticleTexture);
			mChord6ParticleSystem.setPointSize(2400);
			addChild(mChord6ParticleSystem);
			
			
			mBonusPosition[0] = new Number3D(mSpectrumBinBaseLocation[0].x, mSpectrumBinBaseLocation[0].y + ROOM_EDGE, mSpectrumBinBaseLocation[0].z - SPECTRUM_TOP/5f);
			mBonusPosition[1] = new Number3D(mSpectrumBinBaseLocation[12].x, mSpectrumBinBaseLocation[12].y + ROOM_EDGE, mSpectrumBinBaseLocation[12].z + SPECTRUM_TOP/5f);
			mBonusPosition[2] = new Number3D(mSpectrumBinBaseLocation[5].x, mSpectrumBinBaseLocation[5].y + ROOM_EDGE, mSpectrumBinBaseLocation[5].z - SPECTRUM_TOP/5f);
			mBonusPosition[3] = new Number3D(mSpectrumBinBaseLocation[10].x, mSpectrumBinBaseLocation[10].y + ROOM_EDGE, mSpectrumBinBaseLocation[10].z - SPECTRUM_TOP/5f);
			mBonusPosition[4] = new Number3D(mSpectrumBinBaseLocation[2].x, mSpectrumBinBaseLocation[2].y + ROOM_EDGE, mSpectrumBinBaseLocation[2].z + SPECTRUM_TOP/5f);
			mBonusPosition[5] = new Number3D(mSpectrumBinBaseLocation[7].x, mSpectrumBinBaseLocation[5].y + ROOM_EDGE, mSpectrumBinBaseLocation[5].z + SPECTRUM_TOP/5f);
			
			TextureInfo bonusWeaponParticleTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.threerings));
			mBonusWeaponParticleSystem = new Bonus1ParticleSystem(NUM_BONUS_WEAPON);
			mBonusWeaponParticleSystem.setMaterial(new Bonus1ParticleMaterial(true));
			mBonusWeaponParticleSystem.addTexture(bonusWeaponParticleTexture);
			mBonusWeaponParticleSystem.setPointSize(2400);
			addChild(mBonusWeaponParticleSystem);
			
			TextureInfo bonusShipParticleTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bship));
			mBonusShipParticleSystem = new Bonus2ParticleSystem(NUM_BONUS_SHIP);
			mBonusShipParticleSystem.setMaterial(new Bonus2ParticleMaterial(true));
			mBonusShipParticleSystem.addTexture(bonusShipParticleTexture);
			mBonusShipParticleSystem.setPointSize(2400);
			addChild(mBonusShipParticleSystem);
			
			TextureInfo bonus50ParticleTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.b5knew));//.b50));
			mBonus50ParticleSystem = new Bonus3ParticleSystem(NUM_BONUS_SCORE_50);
			mBonus50ParticleSystem.setMaterial(new Bonus3ParticleMaterial(true));
			mBonus50ParticleSystem.addTexture(bonus50ParticleTexture);
			mBonus50ParticleSystem.setPointSize(2400);
			addChild(mBonus50ParticleSystem);
			
			TextureInfo bonus10ParticleTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.b1knew));//.b10));
			mBonus10ParticleSystem = new Bonus4ParticleSystem(NUM_BONUS_SCORE_10);
			mBonus10ParticleSystem.setMaterial(new Bonus4ParticleMaterial(true));
			mBonus10ParticleSystem.addTexture(bonus10ParticleTexture);
			mBonus10ParticleSystem.setPointSize(2400);
			addChild(mBonus10ParticleSystem);
					
			TextureInfo bonus25ParticleTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.b25knew));//.b25));
			mBonus25ParticleSystem = new Bonus5ParticleSystem(NUM_BONUS_SCORE_25);
			mBonus25ParticleSystem.setMaterial(new Bonus5ParticleMaterial(true));
			mBonus25ParticleSystem.addTexture(bonus25ParticleTexture);
			mBonus25ParticleSystem.setPointSize(2400);
			addChild(mBonus25ParticleSystem);
					
			TextureInfo bonus400ParticleTexture = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.b40knew));//.b400));
			mBonus400ParticleSystem = new Bonus6ParticleSystem(NUM_BONUS_SCORE_400);
			mBonus400ParticleSystem.setMaterial(new Bonus6ParticleMaterial(true));
			mBonus400ParticleSystem.addTexture(bonus400ParticleTexture);
			mBonus400ParticleSystem.setPointSize(2400);
			addChild(mBonus400ParticleSystem);
							
			startTime = System.currentTimeMillis();
			
			mIsInitialized = true;
		}
		catch(Exception e)
		{
			
		}
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		//super.onSurfaceCreated(gl, config);
		try
		{
			((FlyingActivity) mContext).showLoader();
			super.onSurfaceCreated(gl, config);
			((FlyingActivity) mContext).hideLoader();
			
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
		((FlyingActivity) mContext).gameOverTime();
	}
	
	//boolean bOneTimeRelease = true;
	public void onDrawFrame(GL10 glUnused) {
		//if(bOneTimeRelease)
		//{
		//	bOneTimeRelease=false;
		//	((FlyingActivity) mContext).hideLoader();
		//}
		
		super.onDrawFrame(glUnused);
		
		if(mState == GAME_STATE_PAUSE)//GAME_STATE_EXIT)
		{
			exit();
			return;
		}
		
		//if(true)
		//	return;
		
		long currentTime = System.currentTimeMillis();
		long elapsed = currentTime - startTime;
		startTime = currentTime;
		float fTimeLapsed = elapsed / 1000f;
		
		if(mState == GAME_STATE_FINISH)
		{
			handleGameOver(fTimeLapsed);
			return;
		}
		
		//if(mState == GAME_STATE_PAUSE)
		//{
		//	return;
		//}
				
		if(mState == GAME_STATE_INIT)
		{
			mStartUpDelay -= fTimeLapsed;
			if(mStartUpDelay <= 0.0f)
			{
				mState = GAME_STATE_RUNNING;
				((FlyingActivity) mContext).removeCountDown();
			}
		}
		
		UpdateDisplayList(fTimeLapsed);
		
        CheckShipHitBonus();
        CheckBulletHitChords();
        CheckChordHitShip();
        
        UpdateStatus(fTimeLapsed);
        
        CheckBassChannelPosition(fTimeLapsed);
        
		// just a test. do not use this. it will kill the OS.
		/*
		// Clear Display List.
		clearChildren();
				
		// Build Display List.
		addChild(mObjRoom.mRoomBox);
		for (int i = 0; i < SPECTRUM_BIN_NUM; i++)
		{
			addChild(mObjSpectrumBin[i].mSpectrumBin);
		}
		addChild(mObjShip.mShip);*/
	}
	
	//boolean bOneTime = true;
	private void UpdateDisplayList(float fTimeLapsed)
	{
		if(bRingIsON)
		{
			UpdateShipRing(fTimeLapsed);
		}
		
		// Set Ship.
		if(mUseJoystickVelocity) {
			mObjShip.vVel.x = mJoystickVelocity.x*2f;
			mObjShip.vVel.y = mJoystickVelocity.y;
			//mObjShip.vVel.add(mJoystickVelocity);
		}
		else
		{
			if(bAccelerometer==false)
			{
				mObjShip.vVel.x *= 0.95f;
				mObjShip.vVel.y *= 0.95f;
			}
		}
		
		if(mState == GAME_STATE_RUNNING)
		{
			//mObjShip.vVel.multiply(0.95f);
			mObjShip.setPosition(fTimeLapsed, bRingIsON, bShipHit);
			if(bShipHit)
			{
				UpdateShipHit(fTimeLapsed);
			}
		}
		
		UpdateAutoFire(fTimeLapsed);
		
		// Set Spectrum.
		UpdateSpectrum(fTimeLapsed);
		
		if (bSpectrumReadyForDisplay)
		{
			for ( int sp = SPECTRUM_SKIP_FREQ; sp < SPECTRUM_SIZE; sp++ )
			{
				//mObjSpectrumBaseBin[sp-SPECTRUM_SKIP_FREQ].setPositionAndScale(SPECTRUM_MAX_VALUE);
				mObjSpectrumBin[sp-SPECTRUM_SKIP_FREQ].setPositionAndScale(mSpectrumVal[sp]);
			}
		}
		
		UpdateBonus(fTimeLapsed);
				
		mBulletParticleSystem.setPosition(fTimeLapsed);
				
		mChord4ParticleSystem.setPosition(fTimeLapsed);
		mChord5ParticleSystem.setPosition(fTimeLapsed);
		mChord6ParticleSystem.setPosition(fTimeLapsed);
				
		mBonusWeaponParticleSystem.setPosition(fTimeLapsed);
		mBonusShipParticleSystem.setPosition(fTimeLapsed);
		mBonus50ParticleSystem.setPosition(fTimeLapsed);
		mBonus10ParticleSystem.setPosition(fTimeLapsed);
		mBonus25ParticleSystem.setPosition(fTimeLapsed);
		mBonus400ParticleSystem.setPosition(fTimeLapsed);
		
	}
	
	
	private void UpdateBonus( float fTimeLapsed )
	{
		fBonusTime -= fTimeLapsed;
	    if( fBonusTime <= 0.0f )
		{
			int rndBonusLocation = mBonusRandom.nextInt(NUM_BONUS);
			
			// Check if there is a bonus in that location.
			for( int i = 0; i < NUM_BONUS; i++ )
			{
				if( m_wBonus[i] == rndBonusLocation )
				{
					fBonusTime = BONUS_TIME;
					return;
				}
			}
			
			InsertBonus( rndBonusLocation );
			
			fBonusTime = BONUS_TIME;
		}
	}


	private void InsertBonus( int rndBonusLocation )
	{
		int rndBonus = mBonusRandom.nextInt(NUM_BONUS);

		if( m_wBonus[rndBonus] != 0 )
			return;

		m_wBonus[rndBonus] = rndBonusLocation;

		switch( rndBonus )
		{
			case 0:
				mBonusWeaponParticleSystem.initPosAndVel(mBonusPosition[rndBonusLocation].x, mBonusPosition[rndBonusLocation].y, mBonusPosition[rndBonusLocation].z);
				break;

			case 1:
				mBonus10ParticleSystem.initPosAndVel(mBonusPosition[rndBonusLocation].x, mBonusPosition[rndBonusLocation].y, mBonusPosition[rndBonusLocation].z);
				break;
			
			case 2:
				mBonus50ParticleSystem.initPosAndVel(mBonusPosition[rndBonusLocation].x, mBonusPosition[rndBonusLocation].y, mBonusPosition[rndBonusLocation].z);
				break;

			case 3:
				mBonus400ParticleSystem.initPosAndVel(mBonusPosition[rndBonusLocation].x, mBonusPosition[rndBonusLocation].y, mBonusPosition[rndBonusLocation].z);
				break;
				
			case 4:
				if(mShipCount < MAX_NUM_SHIPS)
					mBonusShipParticleSystem.initPosAndVel(mBonusPosition[rndBonusLocation].x, mBonusPosition[rndBonusLocation].y, mBonusPosition[rndBonusLocation].z);
				else
					m_wBonus[4] = 0;
				break;

			case 5:
				mBonus25ParticleSystem.initPosAndVel(mBonusPosition[rndBonusLocation].x, mBonusPosition[rndBonusLocation].y, mBonusPosition[rndBonusLocation].z);
				break;
				
			default:
				break;
		}
	}
	
	
	Number3D mTmpBonusPos = new Number3D();
	Number3D mTmpShipPos = new Number3D();
	float mDistanceForBonusCollision = 2.0f;//5.0f;
	private void CheckShipHitBonus()
	{
		mTmpShipPos.x = -1f*mObjShip.mShip.getPosition().x;
		mTmpShipPos.y = mObjShip.mShip.getPosition().y;
		mTmpShipPos.z = mObjShip.mShip.getPosition().z;
		
		for(int i=0; i<NUM_BONUS_WEAPON; i++) 
		{
			if(mBonusWeaponParticleSystem.mAlive[i] == false)
				continue;
			
			mTmpBonusPos.x = mBonusWeaponParticleSystem.mBonusPosition[i].x;
			mTmpBonusPos.y = mBonusWeaponParticleSystem.mBonusPosition[i].y;
			mTmpBonusPos.z = mBonusWeaponParticleSystem.mBonusPosition[i].z;
			
			// Check if Ship hit bonus
			if (Within3DManhattanDistance(mTmpBonusPos, mTmpShipPos, mDistanceForBonusCollision))
			//float distance = mTmpWeaponPos.distanceTo(mObjShip.mShip.getPosition());
			//if(distance < mDistanceForBonusCollision) 
			{
				// Add explosion effects to scene
				
				bRingIsON = true;
				
				//mAutoFireDuration = 0.0f;
				
				// Remove the victim from the scene
				mBonusWeaponParticleSystem.setInactivePosition(i);
				
				m_wBonus[0] = 0;
				
				//break;
			}
		}
		
		for(int i=0; i<NUM_BONUS_SCORE_10; i++) 
		{
			if(mBonus10ParticleSystem.mAlive[i] == false)
				continue;
			
			mTmpBonusPos.x = mBonus10ParticleSystem.mBonusPosition[i].x;
			mTmpBonusPos.y = mBonus10ParticleSystem.mBonusPosition[i].y;
			mTmpBonusPos.z = mBonus10ParticleSystem.mBonusPosition[i].z;
			
			// Check if Ship hit bonus
			if (Within3DManhattanDistance(mTmpBonusPos, mTmpShipPos, mDistanceForBonusCollision))
			//float distance = mTmpWeaponPos.distanceTo(mObjShip.mShip.getPosition());
			//if(distance < mDistanceForBonusCollision) 
			{
				// Add explosion effects to scene
				
				mScoreCount+=1000;
				
				// Remove the victim from the scene
				mBonus10ParticleSystem.setInactivePosition(i);
				
				m_wBonus[1] = 0;
				
				//break;
			}
		}
		
		for(int i=0; i<NUM_BONUS_SCORE_50; i++) 
		{
			if(mBonus50ParticleSystem.mAlive[i] == false)
				continue;
			
			mTmpBonusPos.x = mBonus50ParticleSystem.mBonusPosition[i].x;
			mTmpBonusPos.y = mBonus50ParticleSystem.mBonusPosition[i].y;
			mTmpBonusPos.z = mBonus50ParticleSystem.mBonusPosition[i].z;
			
			// Check if Ship hit bonus
			if (Within3DManhattanDistance(mTmpBonusPos, mTmpShipPos, mDistanceForBonusCollision))
			//float distance = mTmpWeaponPos.distanceTo(mObjShip.mShip.getPosition());
			//if(distance < mDistanceForBonusCollision) 
			{
				// Add explosion effects to scene
				
				mScoreCount+=5000;
				
				// Remove the victim from the scene
				mBonus50ParticleSystem.setInactivePosition(i);
				
				m_wBonus[2] = 0;
				
				//break;
			}
		}
		
		for(int i=0; i<NUM_BONUS_SCORE_400; i++) 
		{
			if(mBonus400ParticleSystem.mAlive[i] == false)
				continue;
			
			mTmpBonusPos.x = mBonus400ParticleSystem.mBonusPosition[i].x;
			mTmpBonusPos.y = mBonus400ParticleSystem.mBonusPosition[i].y;
			mTmpBonusPos.z = mBonus400ParticleSystem.mBonusPosition[i].z;
			
			// Check if Ship hit bonus
			if (Within3DManhattanDistance(mTmpBonusPos, mTmpShipPos, mDistanceForBonusCollision))
			//float distance = mTmpWeaponPos.distanceTo(mObjShip.mShip.getPosition());
			//if(distance < mDistanceForBonusCollision) 
			{
				// Add explosion effects to scene
				
				mScoreCount+=40000;
				
				// Remove the victim from the scene
				mBonus400ParticleSystem.setInactivePosition(i);
				
				m_wBonus[3] = 0;
				
				//break;
			}
		}
		
		for(int i=0; i<NUM_BONUS_SHIP; i++) 
		{
			if(mBonusShipParticleSystem.mAlive[i] == false)
				continue;
			
			mTmpBonusPos.x = mBonusShipParticleSystem.mBonusPosition[i].x;
			mTmpBonusPos.y = mBonusShipParticleSystem.mBonusPosition[i].y;
			mTmpBonusPos.z = mBonusShipParticleSystem.mBonusPosition[i].z;
			
			// Check if Ship hit bonus
			if (Within3DManhattanDistance(mTmpBonusPos, mTmpShipPos, mDistanceForBonusCollision))
			//float distance = mTmpWeaponPos.distanceTo(mObjShip.mShip.getPosition());
			//if(distance < mDistanceForBonusCollision) 
			{
				// Add explosion effects to scene
				
				if(mShipCount < MAX_NUM_SHIPS)
					mShipCount++;
				
				// Remove the victim from the scene
				mBonusShipParticleSystem.setInactivePosition(i);
				
				m_wBonus[4] = 0;
				
				//break;
			}
		}
		
		for(int i=0; i<NUM_BONUS_SCORE_25; i++) 
		{
			if(mBonus25ParticleSystem.mAlive[i] == false)
				continue;
			
			mTmpBonusPos.x = mBonus25ParticleSystem.mBonusPosition[i].x;
			mTmpBonusPos.y = mBonus25ParticleSystem.mBonusPosition[i].y;
			mTmpBonusPos.z = mBonus25ParticleSystem.mBonusPosition[i].z;
			
			// Check if Ship hit bonus
			if (Within3DManhattanDistance(mTmpBonusPos, mTmpShipPos, mDistanceForBonusCollision))
			//float distance = mTmpWeaponPos.distanceTo(mObjShip.mShip.getPosition());
			//if(distance < mDistanceForBonusCollision) 
			{
				// Add explosion effects to scene
				
				mScoreCount+=2500;
				
				// Remove the victim from the scene
				mBonus25ParticleSystem.setInactivePosition(i);
				
				m_wBonus[5] = 0;
				
				//break;
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
			for(int i=0; i<NUM_CHORDS_IN_EACH_GROUP; i++)
			{
				if( mChord4ParticleSystem.mState[i] == Chord4ParticleSystem.STATE_ALIVE && mChord4ParticleSystem.mAlive[i] )
				{
					if (Within3DManhattanDistance(mTmpBulletPos, mChord4ParticleSystem.mChordPosition[i], mDistanceForCollision))
					{
					//float mObjShipDistance = mTmpBulletPos.distanceTo(mChord4ParticleSystem.mChordPosition[i]);
						
					//if(mObjShipDistance < mDistanceForCollision) {
						
						// Remove the victim from the scene
						mChord4ParticleSystem.setInactivePosition(i);
						
						bBulletHit = true;
						mScoreCount+=CHORD_VALUE;
						break;
					}
				}
				
				if( mChord5ParticleSystem.mState[i] == Chord5ParticleSystem.STATE_ALIVE && mChord5ParticleSystem.mAlive[i] )
				{
					if (Within3DManhattanDistance(mTmpBulletPos, mChord5ParticleSystem.mChordPosition[i], mDistanceForCollision))
					{
					//float mObjShipDistance = mTmpBulletPos.distanceTo(mChord5ParticleSystem.mChordPosition[i]);
						
					//if(mObjShipDistance < mDistanceForCollision) {
						
						// Remove the victim from the scene
						mChord5ParticleSystem.setInactivePosition(i);
						
						bBulletHit = true;
						mScoreCount+=CHORD_VALUE;
						break;
					}
				}
				
				if( mChord6ParticleSystem.mState[i] == Chord6ParticleSystem.STATE_ALIVE && mChord6ParticleSystem.mAlive[i] )
				{
					if (Within3DManhattanDistance(mTmpBulletPos, mChord6ParticleSystem.mChordPosition[i], mDistanceForCollision))
					{
					//float mObjShipDistance = mTmpBulletPos.distanceTo(mChord6ParticleSystem.mChordPosition[i]);
						
					//if(mObjShipDistance < mDistanceForCollision) {
						
						// Remove the victim from the scene
						mChord6ParticleSystem.setInactivePosition(i);
						
						bBulletHit = true;
						mScoreCount+=CHORD_VALUE;
						break;
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
			if( mChord4ParticleSystem.mState[i] == Chord4ParticleSystem.STATE_ALIVE && mChord4ParticleSystem.mAlive[i] )
			{
				if (Within3DManhattanDistance(mTmpShipPos, mChord4ParticleSystem.mChordPosition[i], mDistanceForChordCollisionWithShip))
				{
					// Remove the victim from the scene
					mChord4ParticleSystem.setInactivePosition(i);
					if(bShipHit == false)
						mShipCount--;
					bShipHit = true;
					mScoreCount+=CHORD_VALUE;
				}
			}
			
			if( mChord5ParticleSystem.mState[i] == Chord5ParticleSystem.STATE_ALIVE && mChord5ParticleSystem.mAlive[i] )
			{
				if (Within3DManhattanDistance(mTmpShipPos, mChord5ParticleSystem.mChordPosition[i], mDistanceForChordCollisionWithShip))
				{
					// Remove the victim from the scene
					mChord5ParticleSystem.setInactivePosition(i);
					if(bShipHit == false)
						mShipCount--;
					bShipHit = true;
					mScoreCount+=CHORD_VALUE;
				}
			}
			
			if( mChord6ParticleSystem.mState[i] == Chord6ParticleSystem.STATE_ALIVE && mChord6ParticleSystem.mAlive[i] )
			{
				if (Within3DManhattanDistance(mTmpShipPos, mChord6ParticleSystem.mChordPosition[i], mDistanceForChordCollisionWithShip))
				{
					// Remove the victim from the scene
					mChord6ParticleSystem.setInactivePosition(i);
					if(bShipHit == false)
						mShipCount--;
					bShipHit = true;
					mScoreCount+=CHORD_VALUE;
				}
			}
		}
		
		if(mShipCount==0)
		{
			mState = GAME_STATE_FINISH;
		}
	}
     
	private void CheckBassChannelPosition(float fTimeLapsed)
	{
		mCheckBassChannelPositionTime -= fTimeLapsed;
	    if( mCheckBassChannelPositionTime <= 0.0f )
		{
	    	if(BASS.BASS_ChannelGetPosition(m_chan, 0) >= mBassLen)
	    	{
	    		mState = GAME_STATE_FINISH;
	    	}
	    	
	    	mCheckBassChannelPositionTime = mBassChannelPositionTime;
		}
	    
	    //mFreeVersionTime -= fTimeLapsed;
	    //if( mFreeVersionTime <= 0.0f )
		//{
	    //	mState = GAME_STATE_FINISH;
		//}
	}
	
	private void UpdateShipRing(float fTimeLapsed)
	{
		mShowShipRingTime -= fTimeLapsed;
	    if( mShowShipRingTime <= 0.0f )
		{
	    	bRingIsON = false;//!bRingIsON;
	    	
	    	mShowShipRingTime = mShipRingTime;
		}
	}
	
	
	float mShipHitAnimationDuration = .2f;
	float mShipHitAnimation = mShipHitAnimationDuration;
	boolean flashShip = false;
	private void UpdateShipHit(float fTimeLapsed)
	{
		mShowShipHitTime -= fTimeLapsed;
	    if( mShowShipHitTime <= 0.0f )
		{
	    	bShipHit = false;
	    	
	    	if(flashShip)
	    	{
	    		for(int i=0;i<mObjShip.mShip.getNumChildren();i++)
	    		{
	    			mObjShip.mShip.getChildAt(i).setDrawingMode(GLES20.GL_TRIANGLES);
	    		}
	    		//removeChild(mObjShip.mShipExp);
	    		//addChild(mObjShip.mShip);
	    	}
	    	
	    	mShowShipHitTime = mShipHitTime;
		}
	    else
	    {
	    	mShipHitAnimation -= fTimeLapsed;
			if( mShipHitAnimation <= 0.0f )
			{
				flashShip = !flashShip;
				if(flashShip)
				{
					for(int i=0;i<mObjShip.mShip.getNumChildren();i++)
		    		{
		    			mObjShip.mShip.getChildAt(i).setDrawingMode(GLES20.GL_LINES);
		    		}
					//removeChild(mObjShip.mShip);
					//addChild(mObjShip.mShipExp);
				}
				else
				{
					for(int i=0;i<mObjShip.mShip.getNumChildren();i++)
		    		{
		    			mObjShip.mShip.getChildAt(i).setDrawingMode(GLES20.GL_TRIANGLES);
		    		}
					//removeChild(mObjShip.mShipExp);
					//addChild(mObjShip.mShip);
				}
				mShipHitAnimation = mShipHitAnimationDuration;
			}
	    }
	}
	
	private void UpdateAutoFire(float fTimeLapsed)
	{
		mAutoFireDuration -= fTimeLapsed;
		if( mAutoFireDuration > 0.0f )
		{
			mTempAutoFireTime -= fTimeLapsed;
		    if( mTempAutoFireTime <= 0.0f )
			{
		    	if( mState == GAME_STATE_RUNNING)
		    	{
		    		setTouch();//true);//false);
		    	}
	    		mTempAutoFireTime = mAutoFireTime;
			}
		}
	}
	
	int mTempShipCountTracker = 0;
	long mTempScoureTracker = 0;
	private void UpdateStatus(float fTimeLapsed)
	{
		if(mTempShipCountTracker!=mShipCount)
    	{
   			((FlyingActivity) mContext).onShipUpdate(mShipCount);
    		mTempShipCountTracker = mShipCount;
    	}
		
		if(mTempScoureTracker!=mScoreCount)
		{
			mStatus = "  " + mScoreCount + " ";
			displayStatus(mStatus, false);
			mTempScoureTracker = mScoreCount;
		}
		
		mShowStatusTime -= fTimeLapsed;
	    if( mShowStatusTime <= 0.0f )
		{
	    	if( mState == GAME_STATE_INIT)
	    	{
	    		//mStatus = "Game Starts in " + (int)mStartUpDelay + " Seconds";
	    		if((int)mStartUpDelay == 0)
	    		{
	    			mStatus = " ";
	    		}
	    		else
	    		{
	    			mStatus = " " + (int)mStartUpDelay + " ";
	    		}
	    		displayStatus(mStatus, true);
	    	}
	    	/*else if( mState == GAME_STATE_RUNNING)
	    	{
	    		if(mScoreCount > 0)
		    	{
		    		//mStatus = "  Score: " + mScoreCount;
	    			mStatus = "  " + mScoreCount + " ";
		    	}
	    		else
	    		{
	    			mStatus = " " ;
	    			//mStatus = " "  + mObjShip.vVel.toString();
	    			//mStatus = " "  + mObjShip.mShip.getPosition().toString();
	    		}
	    		//mStatus = " "  + mObjShip.vVel.y;
	    		//mStatus = " P:"  + BASS.BASS_ChannelGetPosition(m_chan, 0) + " L:" + mBassLen;
	    		//mStatus = "Ax:" + mAccStartValue.x + " Ay:" + mAccStartValue.y + " Az:" + mAccStartValue.z;
	    		
	    		displayStatus(false);
	    	}*/
	    			
	    	mShowStatusTime = mStatusTime;
		}
	}
	
	private String mTempDisplayStatus = "";
	private void displayStatus(String value, boolean countDown)
	{
		if(mTempDisplayStatus.equals(value))
		{
			return;
		}
		
		if(value!=null && value.length()>0)
		{
			((FlyingActivity) mContext).onStatusUpdate(value, countDown);
			mTempDisplayStatus = value.toString();
		}
	}
	
	private void ExplodeAll()
	{
		for(int i=0; i<NUM_CHORDS_IN_EACH_GROUP; i++)
		{
			if(mChord4ParticleSystem.mState[i] == Chord4ParticleSystem.STATE_ALIVE && mChord4ParticleSystem.mAlive[i])
			{
				mChord4ParticleSystem.setInactivePosition(i);
				mScoreCount+=CHORD_VALUE;
			}
			
			if(mChord5ParticleSystem.mState[i] == Chord5ParticleSystem.STATE_ALIVE && mChord5ParticleSystem.mAlive[i])
			{
				mChord5ParticleSystem.setInactivePosition(i);
				mScoreCount+=CHORD_VALUE;
			}
			
			if(mChord6ParticleSystem.mState[i] == Chord6ParticleSystem.STATE_ALIVE && mChord6ParticleSystem.mAlive[i])
			{
				mChord6ParticleSystem.setInactivePosition(i);
				mScoreCount+=CHORD_VALUE;
			}
			
		}
	}
	
	//int ax,ay,az;
	public void setAccelerometerValues(float x, float y, float z) {
		if(mIsInitialized && mState == GAME_STATE_RUNNING) {
			if(mAccStartValue == null) {
				mAccStartValue = new Number3D(x, y, z);
			}
			if(bAccelerometer && mUseJoystickVelocity == false)
			{
				//mAccStartValue.setAll(-4.523f, -27.457f, -82.369f); // Normal position for holding device.
				mObjShip.vVel.setAll(x*2f, y, z);
				mObjShip.vVel.subtract(mAccStartValue);
				mObjShip.vVel.multiply(.02f);
			}
		}
	}
	
	public void onResume() {
		//startTime = System.currentTimeMillis();
		//if(mPrevState!=GAME_STATE_PREV_INIT)
		//{
		//	mState = mPrevState;
		//}
		//BASS.BASS_Start();
	}
	
	
	public void onPause()
	{
		//mPrevState = mState; 
		mState = GAME_STATE_PAUSE;//GAME_STATE_EXIT;
		//BASS.BASS_Pause();
		//BASS.BASS_Free();
		//stopRendering();
	}
	/*
	public void onResume()
	{
		BASS.BASS_Start();
		startTime = System.currentTimeMillis();
		mAccStartValue = null;
		mState = GAME_STATE_RUNNING;
		
	}*/
	
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
	public void setTouch() //boolean explodeAll) //float x, float y)
	{ 
		if(mIsInitialized && mState == GAME_STATE_RUNNING) {
			if(bRingIsON)
			{
				// Fire bullets to all Chords alive
				for(int i=0; i<NUM_CHORDS_IN_EACH_GROUP; i++)
				{
					if( mChord4ParticleSystem.mState[i] == Chord4ParticleSystem.STATE_ALIVE && mChord4ParticleSystem.mAlive[i] )
					{
						mFireStraightOnly = false;
						mBulletDir.z = mChord4ParticleSystem.mChordPosition[i].z;
						mBulletDir.z -= mObjShip.mShip.getZ();
						if(mObjShip.zDir<0.0f && mBulletDir.z<0.0f)
							mFireStraightOnly = true;
						else if(mObjShip.zDir>0.0f && mBulletDir.z>0.0f)
							mFireStraightOnly = true;
						if(mFireStraightOnly)
						{
							mBulletDir.x = -1f*mChord4ParticleSystem.mChordPosition[i].x;
							mBulletDir.y = mChord4ParticleSystem.mChordPosition[i].y;
						
							mBulletDir.x -= mObjShip.mShip.getX();
							mBulletDir.y -= mObjShip.mShip.getY();
						
							mBulletDir.x *= -1f;
							mBulletDir.normalize();
							mBulletParticleSystem.initPosAndVel(mBulletDir.x, mBulletDir.y, mBulletDir.z, -1f*mObjShip.mShip.getX(), mObjShip.mShip.getY(), mObjShip.mShip.getZ(), true);
						}
					}
					
					if( mChord5ParticleSystem.mState[i] == Chord5ParticleSystem.STATE_ALIVE && mChord5ParticleSystem.mAlive[i] )
					{
						mFireStraightOnly = false;
						mBulletDir.z = mChord5ParticleSystem.mChordPosition[i].z;
						mBulletDir.z -= mObjShip.mShip.getZ();
						if(mObjShip.zDir<0.0f && mBulletDir.z<0.0f)
							mFireStraightOnly = true;
						else if(mObjShip.zDir>0.0f && mBulletDir.z>0.0f)
							mFireStraightOnly = true;
						if(mFireStraightOnly)
						{
							mBulletDir.x = -1f*mChord5ParticleSystem.mChordPosition[i].x;
							mBulletDir.y = mChord5ParticleSystem.mChordPosition[i].y;
												
							mBulletDir.x -= mObjShip.mShip.getX();
							mBulletDir.y -= mObjShip.mShip.getY();
						
							mBulletDir.x *= -1f;
							mBulletDir.normalize();
							mBulletParticleSystem.initPosAndVel(mBulletDir.x, mBulletDir.y, mBulletDir.z, -1f*mObjShip.mShip.getX(), mObjShip.mShip.getY(), mObjShip.mShip.getZ(), true);
						}
					}
					if( mChord6ParticleSystem.mState[i] == Chord6ParticleSystem.STATE_ALIVE && mChord6ParticleSystem.mAlive[i] )
					{
						mFireStraightOnly = false;
						mBulletDir.z = mChord6ParticleSystem.mChordPosition[i].z;
						mBulletDir.z -= mObjShip.mShip.getZ();
						if(mObjShip.zDir<0.0f && mBulletDir.z<0.0f)
							mFireStraightOnly = true;
						else if(mObjShip.zDir>0.0f && mBulletDir.z>0.0f)
							mFireStraightOnly = true;
						if(mFireStraightOnly)
						{
							mBulletDir.x = -1f*mChord6ParticleSystem.mChordPosition[i].x;
							mBulletDir.y = mChord6ParticleSystem.mChordPosition[i].y;
												
							mBulletDir.x -= mObjShip.mShip.getX();
							mBulletDir.y -= mObjShip.mShip.getY();
						
							mBulletDir.x *= -1f;
							mBulletDir.normalize();
							mBulletParticleSystem.initPosAndVel(mBulletDir.x, mBulletDir.y, mBulletDir.z, -1f*mObjShip.mShip.getX(), mObjShip.mShip.getY(), mObjShip.mShip.getZ(), true);
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
		mAutoFireDuration = AutoFireDuration;
	}
	
	private void UpdateSpectrum(float fTimeLapsed)
	{
		// update the Spectrum 
		m_fFFTTime -= fTimeLapsed;
	    if( m_fFFTTime <= 0.0f )
		{
	    	int res = BASS.BASS_ChannelGetData(m_chan, m_bbuf, BASS.BASS_DATA_FFT1024);
			if( res > 0 ) 
			{
				m_bbuf.asFloatBuffer().get(m_fft); // get the data from the buffer into the array
			    for ( int sp = SPECTRUM_SKIP_FREQ; sp < SPECTRUM_SIZE; sp++ )
				{
					float specSize = GetAmp( sp * g_spectrumFreqDelta, (sp + 1) * g_spectrumFreqDelta, m_fft ) * 130.0f;

					if( specSize > mSpectrumVal[sp] )
						mSpectrumVal[sp] = specSize;
					else
						mSpectrumVal[sp] *= 0.85f;
						
					if ( mSpectrumVal[sp] > SPECTRUM_MAX_VALUE )
						mSpectrumVal[sp] = SPECTRUM_MAX_VALUE;
						
					if ( mSpectrumVal[sp] < 0.0f )
						mSpectrumVal[sp] = 0.0f;

					//mSpectrumVal[sp] = 0.1f;

					mObjSpectrumBin[sp-SPECTRUM_SKIP_FREQ].m_fTimeToReleaseChord -= fTimeLapsed;
					
					if ( mSpectrumVal[sp] == SPECTRUM_MAX_VALUE && 
						 mObjSpectrumBin[sp-SPECTRUM_SKIP_FREQ].m_fTimeToReleaseChord <= 0.0f  &&
						 mState == GAME_STATE_RUNNING )
					{
						getRandChord(mSpectrumBinBaseLocation[sp-SPECTRUM_SKIP_FREQ].x, mSpectrumBinBaseLocation[sp-SPECTRUM_SKIP_FREQ].y + SPECTRUM_TOP, mSpectrumBinBaseLocation[sp-SPECTRUM_SKIP_FREQ].z, 
								 	 mObjShip.mShip.getPosition().x, mObjShip.mShip.getPosition().y, mObjShip.mShip.getPosition().z);
						
						mObjSpectrumBin[sp-SPECTRUM_SKIP_FREQ].resetReleaseChordTime();
					}
				}
	        	
	        	bSpectrumReadyForDisplay = true;
			}
	    	m_fFFTTime = m_FFT_Time;
		}
	}
	
	final Random mChordRandom = new Random();
	private boolean getRandChord(float startPosX, float startPosY, float startPosZ, float shipPosX, float shipPosY, float shipPosZ)
	{
		int rand = mChordRandom.nextInt(4);
		switch (rand)
		{
			case 1:
				if(mChord4ParticleSystem.initPosAndVel(startPosX, startPosY, startPosZ, shipPosX, shipPosY, shipPosZ))
				{
					return true;
				}
				else if(mChord5ParticleSystem.initPosAndVel(startPosX, startPosY, startPosZ, shipPosX, shipPosY, shipPosZ))
				{
					return true;
				}
				else if(mChord6ParticleSystem.initPosAndVel(startPosX, startPosY, startPosZ, shipPosX, shipPosY, shipPosZ))
				{
					return true;
				}
				break;
				
			case 2:
				if(mChord5ParticleSystem.initPosAndVel(startPosX, startPosY, startPosZ, shipPosX, shipPosY, shipPosZ))
				{
					return true;
				}
				else if(mChord6ParticleSystem.initPosAndVel(startPosX, startPosY, startPosZ, shipPosX, shipPosY, shipPosZ))
				{
					return true;
				}
				else if(mChord4ParticleSystem.initPosAndVel(startPosX, startPosY, startPosZ, shipPosX, shipPosY, shipPosZ))
				{
					return true;
				}
				break;
				
			case 3:
				if(mChord6ParticleSystem.initPosAndVel(startPosX, startPosY, startPosZ, shipPosX, shipPosY, shipPosZ))
				{
					return true;
				}
				else if(mChord4ParticleSystem.initPosAndVel(startPosX, startPosY, startPosZ, shipPosX, shipPosY, shipPosZ))
				{
					return true;
				}
				else if(mChord5ParticleSystem.initPosAndVel(startPosX, startPosY, startPosZ, shipPosX, shipPosY, shipPosZ))
				{
					return true;
				}
				break;
				
			default:
				break;
		}
		
		return false;
	}
	
	private float GetAmp(float start, float end, float[] fft)
    {
         float amp=0;
    	 int b,bin1,bin2;

    	 // nearest bin to starting freq
         //bin1=(int)(1024*start/(float)freq+0.5);	
    	 bin1=(int)(m_1024DivFreq*start+0.5);	
    	 
    	 // nearest bin to ending freq
         //bin2=(int)(1024*end/(float)freq+0.5);	
    	 bin2=(int)(m_1024DivFreq*end+0.5);	

    	 // check the bins
         for (b=bin1; b<=bin2; b++)
         {
        	 // 4th attempt. Taken from BASS for WinCE
        	 if (fft[b]>amp) 
        	 	amp=fft[b];
        	 
        	 
        		 
        	 /*	 
        	 // 3rd attempt. Taken from BASS for WinCE
        	 //y=sqrt(fft[x+1]/(float)(1<<24))*3*SPECHEIGHT-4; // scale it (sqrt to make low values more visible)
        	 //y=((fft[x+1]>>8)*10*SPECHEIGHT)>>16; // scale it (linearly)
        	 int SPECHEIGHT = 100;
        	 //float val = (float)(Math.sqrt(fft[b]/(float)(1<<24))*3*SPECHEIGHT-4);
        	 float val = ((fft[b]>>8)*10*SPECHEIGHT)>>16;
        	 if (val>amp) 
		     	 amp=val;*/
        	 
        	 // 2nd attempt
        	 //float val = fft[b] >> 16;//14;//
             //if (val>amp) 
		     //	 amp=val;
             
             // 1st attempt
    		 //if (fft[b]>amp) 
    		 //	 amp=fft[b];
         }

         // 4th attempt. Taken from BASS for WinCE
         //int SPECHEIGHT = 50;
    	 //amp=(float)(Math.sqrt(amp/(float)(1<<24))*3*SPECHEIGHT-4);

		// commented out when moved to bass 64bit
         //amp = (((int)amp>>8)*10*SPECHEIGHT)>>16;

         return amp;
    } 
	
	private boolean initBASS()//boolean bUseAsset)
	{
		if (!BASS.BASS_Init(-1, m_freq, 0)) {
			return false;
		}
		
		// first free the current one (try both MOD and stream - it must be one of them)
		BASS.BASS_MusicFree(m_chan);
		BASS.BASS_StreamFree(m_chan);
		if( mFileName.equals(MeVsMusicActivity.DEMO_TRACK1) || 
			mFileName.equals(MeVsMusicActivity.DEMO_TRACK2) ) // ||
			//mFileName.equals(MeVsMusicActivity.DEMO_TRACK3) ||
			//mFileName.equals(MeVsMusicActivity.DEMO_TRACK4) ||
			//mFileName.equals(MeVsMusicActivity.DEMO_TRACK5) ||
			//mFileName.equals(MeVsMusicActivity.DEMO_TRACK6) )
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
		//BASS.BASS_ChannelPlay(m_chan, false);
		
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
	
	// http://www.google.com/search?sugexp=chrome,mod=0&sourceid=chrome&ie=UTF-8&q=fast+3d+distance+approximation+instead+of+sqrt
	// http://stackoverflow.com/questions/3693514/very-fast-3d-distance-check
	boolean Within3DManhattanDistance( Number3D c1, Number3D c2, float distance )
	{	
		float dx = Math.abs(c2.x - c1.x);
	    if (dx > distance) return false; // too far in x direction

	    float dy = Math.abs(c2.y - c1.y);
	    if (dy > distance) return false; // too far in y direction

	    // since x and y distance are likely to be larger than
	    // z distance most of the time we don't need to execute
	    // the code below:

	    float dz = Math.abs(c2.z - c1.z);
	    if (dz > distance) return false; // too far in z direction

	    return true; // we're within the cube
	    
	    /*
	    float dx = Math.abs(c2.x - c1.x);
	    float dy = Math.abs(c2.y - c1.y);
	    float dz = Math.abs(c2.z - c1.z);

	    if (dx > distance) return false; // too far in x direction
	    if (dy > distance) return false; // too far in y direction
	    if (dz > distance) return false; // too far in z direction

	    return true; // we're within the cube
	    */
	}
	
	boolean bGameOverCleanSceneOneTime = true;
	float mGameOverStringTime = 0.5f;
	float mGameOverTitleTime = 2f;
	float mGameOverTop3Time = 4f;
	float mGameOverTop31Time = 6f;
	float mGameOverTop32Time = 8f;
	float mGameOverTop33Time = 10f;
	float mGameOverTime = 20f;
	float mGameOverOneTime = 1000;
	//float mGameOverFreeVerTime = 4f;
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
			
			removeChild(mBonusWeaponParticleSystem);
			removeChild(mBonusShipParticleSystem);
			removeChild(mBonus50ParticleSystem);
			removeChild(mBonus10ParticleSystem);
			removeChild(mBonus25ParticleSystem);
			removeChild(mBonus400ParticleSystem);
			
			((FlyingActivity) mContext).onShipUpdate(mShipCount);
			((FlyingActivity) mContext).gameOverData(mTitle, mScoreCount);
		}

		// Set Spectrum.
		UpdateSpectrum(fTimeLapsed);
		
		if (bSpectrumReadyForDisplay)
		{
			for ( int sp = SPECTRUM_SKIP_FREQ; sp < SPECTRUM_SIZE; sp++ )
			{
				//mObjSpectrumBaseBin[sp-SPECTRUM_SKIP_FREQ].setPositionAndScale(SPECTRUM_MAX_VALUE);
				mObjSpectrumBin[sp-SPECTRUM_SKIP_FREQ].setPositionAndScale(mSpectrumVal[sp]);
			}
		}
		
		mChord4ParticleSystem.setPosition(fTimeLapsed);
		mChord5ParticleSystem.setPosition(fTimeLapsed);
		mChord6ParticleSystem.setPosition(fTimeLapsed);
		
		mGameOverTime -= fTimeLapsed;
		if(mGameOverTime <= 0.0f)
		{
			mGameOverTime = mGameOverOneTime;
			exit();
			return;
		}
		
		/*if(bFreeVersion)
		{
			mGameOverFreeVerTime -= fTimeLapsed;
			if(mGameOverFreeVerTime <= 0.0f)
			{
				mGameOverFreeVerTime = mGameOverOneTime;
				((FlyingActivity) mContext).gameOverFreeVersionTime();
				return;
			}
		}
		else
		{*/
			mGameOverTop33Time -= fTimeLapsed;
			if(mGameOverTop33Time <= 0.0f)
			{
				mGameOverTop33Time = mGameOverOneTime;
				((FlyingActivity) mContext).gameOverTop33Time();
				return;
			}
			
			mGameOverTop32Time -= fTimeLapsed;
			if(mGameOverTop32Time <= 0.0f)
			{
				mGameOverTop32Time = mGameOverOneTime;
				((FlyingActivity) mContext).gameOverTop32Time();
				return;
			}
			
			mGameOverTop31Time -= fTimeLapsed;
			if(mGameOverTop31Time <= 0.0f)
			{
				mGameOverTop31Time = mGameOverOneTime;
				((FlyingActivity) mContext).gameOverTop31Time();
				return;
			}
			
			mGameOverTop3Time -= fTimeLapsed;
			if(mGameOverTop3Time <= 0.0f)
			{
				mGameOverTop3Time = mGameOverOneTime;
				((FlyingActivity) mContext).gameOverTop3Time();
				return;
			}
		//}
		
		mGameOverTitleTime -= fTimeLapsed;
		if(mGameOverTitleTime <= 0.0f)
		{
			mGameOverTitleTime = mGameOverOneTime;
			((FlyingActivity) mContext).gameOverTitleTime();
			return;
		}
		
		mGameOverStringTime -= fTimeLapsed;
		if(mGameOverStringTime <= 0.0f)
		{
			mGameOverStringTime = mGameOverOneTime;
			((FlyingActivity) mContext).gameOverStringTime();
			return;
		}
	}

}
