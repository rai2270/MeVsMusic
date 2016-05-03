package mvm.particle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import mvm.diplaylist.gamecommon;
import mvm.flying.FlyingRenderer;
import mvm.material.Chord4ParticleMaterial;

import r.BaseObject3D;
import r.Camera;
import r.Geometry3D;
import r.materials.AMaterial;
import r.math.Number3D;
import android.opengl.GLES20;

public final class Chord4ParticleSystem extends BaseObject3D {
	protected Number3D mFriction;
	protected FloatBuffer mVelocityBuffer;
	protected FloatBuffer mStartTimeBuffer;
	protected FloatBuffer mSpeedBuffer;
	protected FloatBuffer mNewVelocityBuffer;
	protected FloatBuffer mNewTimeBuffer;
	protected FloatBuffer mNewSpeedBuffer;
	protected FloatBuffer mNewPositionBuffer;
	protected int mCurrentFrame;
	protected int mVelocityBufferHandle;
	protected int mTimeBufferHandle;
	protected int mSpeedBufferHandle;
	protected float mTime;
	protected float mPointSize = 10.0f;
	private Chord4ParticleMaterial mChordMat;
	private int mNumParticles;
	
	public Number3D[] mChordPosition;
	private Number3D[] mVel;
	private int[] mChordFrameCount;
	private int[] mChordFrameCountStateBorn;
	private int[] mChordFrameCountStateAlive;
	private int[] mChordFrameCountStateExplode;
	private float[] mChordSpeed;
	
	public boolean[] mAlive;
	public int[] mState;
	
	private Number3D mTempPos = new Number3D();
    private Number3D mTempVel = new Number3D();
    
    float mFrameCountTimeTotal;
    float mFrameCountTime;
    
    //public int mType;
    
    private static final int ONE_ROTATE_TAKES = 2;// 2 Seconds
	private static final int RAND_INTERVAL = 0;
	
	private static final int NUM_PIC_IN_ROW_AND_COL = 8;
	private static final float TILE_SIZE = 1 / ((float)NUM_PIC_IN_ROW_AND_COL);
	
	private static final int TOTAL_NUM_PIC_BORN = 8;
	private static final int TOTAL_NUM_PIC_ALIVE = 40;
	private static final int TOTAL_NUM_PIC_EXPLODE = 16;
	
	
	private static final float CHORD_SPEED_MIN = 5.0f;
	private static final float CHORD_SPEED_MAX = 11.0f;
	
	public static final int ROOM_SIZE = FlyingRenderer.ROOM_SIZE;
    public static final float ROOM_SIZE_HALF = ROOM_SIZE * .5f;
    public static final float INACTIVE_POS = ROOM_SIZE * 1.5f;
	
	private static final float GAME_WORLD_X_SPACE = FlyingRenderer.GAME_WORLD_X_SPACE;
	private static final float GAME_WORLD_Y_SPACE_MAX = -50f - FlyingRenderer.ROOM_EDGE;
	
	private static final float GAME_WORLD_SPACE = GAME_WORLD_X_SPACE;
	
	public static final int STATE_INACTIVE = 0;
	public static final int STATE_BORN = 1;
	public static final int STATE_ALIVE = 2;
	public static final int STATE_EXPLODE = 3;
	
	
	
	public Chord4ParticleSystem(int numParticles) {
		super();
		mNumParticles = numParticles;
		
		//mType = gamecommon.OBJ_CHORD4;
		
		float randTime = ONE_ROTATE_TAKES + (float)Math.random() * RAND_INTERVAL;
		mFrameCountTimeTotal = randTime / ((float)TOTAL_NUM_PIC_ALIVE);  //  ( (ONE_ROTATE_TAKES * 1000) / 64f ) / 1000f
	    mFrameCountTime = mFrameCountTimeTotal;
		
		mAlive = new boolean[mNumParticles];
		mState = new int[mNumParticles];
		mChordPosition = new Number3D[mNumParticles];
		mVel = new Number3D[mNumParticles];
		mChordFrameCount = new int[mNumParticles];
		mChordFrameCountStateBorn = new int[mNumParticles];
		mChordFrameCountStateAlive = new int[mNumParticles];
		mChordFrameCountStateExplode = new int[mNumParticles];
		mChordSpeed = new float[mNumParticles];
		
		for(int i=0; i<mNumParticles; i++)
	    {
			mAlive[i] = false;
			mState[i] = STATE_INACTIVE;
	    	mChordPosition[i] = new Number3D(INACTIVE_POS, INACTIVE_POS, INACTIVE_POS);
	    	mVel[i] = new Number3D(0, 0, 0);
	    	mChordFrameCount[i] = 0;
	    	mChordFrameCountStateBorn[i] = 0;
			mChordFrameCountStateAlive[i] = (int)((float)Math.random() * (TOTAL_NUM_PIC_ALIVE));
			mChordFrameCountStateExplode[i] = 0;
	    	mChordSpeed[i] = CHORD_SPEED_MIN + ((float)Math.random() * (CHORD_SPEED_MAX - CHORD_SPEED_MIN));
	    }
		
		init();
	}
	
	public void setPointSize(float pointSize) {
		mPointSize = pointSize;
	}
	
	public float getPointSize() {
		return mPointSize;
	}
	
	public FloatBuffer getVelocityBuffer() {
		return mVelocityBuffer;
	}
	
	public FloatBuffer getStartTimeBuffer() {
		return mStartTimeBuffer;
	}
	
	protected void init() {
		setDrawingMode(GLES20.GL_POINTS);
		setTransparent(true);
		
		float[] startTime = new float[mNumParticles];
		float[] speed = new float[mNumParticles];
		float[] vertices = new float[mNumParticles * 3];
		float[] velocity = new float[mNumParticles * 3];
		float[] textureCoords = new float[mNumParticles * 2];
		float[] normals = new float[mNumParticles * 3];
		float[] colors = new float[mNumParticles * 4];
		int[] indices = new int[mNumParticles];
		
		int index = 0;
		
		for(int i=0; i<mNumParticles; ++i) {
			index = i * 3;
			// -- put them in a place where we can't see them
			vertices[index] = mChordPosition[i].x;
			vertices[index + 1] = mChordPosition[i].y;
			vertices[index + 2] = mChordPosition[i].z;
			
			velocity[index] = 0;
			velocity[index + 1] = 0;
			velocity[index + 2] = 0;
			
			normals[index] = 0;
			normals[index + 1] = 0;
			normals[index + 2] = 1;
			
			index = i * 2;
			textureCoords[i] = 0;
			textureCoords[i + 1] = 0;
			
			index = i * 4;
			colors[i] = 1;
			colors[i + 1] = i;
			colors[i + 2] = i;
			colors[i + 3] = i;
			
			indices[i] = i;
		}
		
		mNewVelocityBuffer = ByteBuffer
				.allocateDirect(3 * Geometry3D.FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mNewPositionBuffer = ByteBuffer
				.allocateDirect(3 * Geometry3D.FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mNewTimeBuffer = ByteBuffer
				.allocateDirect(Geometry3D.FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mNewSpeedBuffer = ByteBuffer
				.allocateDirect(3 * Geometry3D.FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		mVelocityBuffer = ByteBuffer
				.allocateDirect(velocity.length * Geometry3D.FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mVelocityBuffer.put(velocity).position(0);
		
		mSpeedBuffer = ByteBuffer
				.allocateDirect(speed.length * Geometry3D.FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mSpeedBuffer.put(speed).position(0);
		
		mStartTimeBuffer = ByteBuffer
				.allocateDirect(startTime.length * Geometry3D.FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		int buff[] = new int[3];
		GLES20.glGenBuffers(3, buff, 0);
		mVelocityBufferHandle = buff[0];
		mTimeBufferHandle = buff[1];
		mSpeedBufferHandle = buff[2];
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVelocityBufferHandle);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVelocityBuffer.limit() * Geometry3D.FLOAT_SIZE_BYTES, mVelocityBuffer, GLES20.GL_DYNAMIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTimeBufferHandle);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mStartTimeBuffer.limit() * Geometry3D.FLOAT_SIZE_BYTES, mStartTimeBuffer, GLES20.GL_DYNAMIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mSpeedBufferHandle);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mSpeedBuffer.limit() * Geometry3D.FLOAT_SIZE_BYTES, mSpeedBuffer, GLES20.GL_DYNAMIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		
		mFriction = new Number3D(.95f, .95f, .95f);
		
		setData(vertices, GLES20.GL_DYNAMIC_DRAW, normals, GLES20.GL_STATIC_DRAW, textureCoords, GLES20.GL_STATIC_DRAW,
				colors, GLES20.GL_STATIC_DRAW,indices, GLES20.GL_STATIC_DRAW);
	}
	
	public void reload() {
		super.reload();
		init();
	}
	
	public void setMaterial(AMaterial material, boolean copyTextures) {
		super.setMaterial(material, copyTextures);
		mChordMat = (Chord4ParticleMaterial)material;
	}
	
	public void setTime(float time) {
		mTime = time;
	}
	
	public float getTime() {
		return mTime;
	}
	
	public void setCurrentFrame(int currentFrame) {
		mCurrentFrame = currentFrame;
	}
	
	private int GetNextAvailableChord()
	{
		for(int i=0; i<mNumParticles; i++)
		{
			if( mAlive[i] == false )
			{
				mAlive[i] = true;
				mState[i] = STATE_BORN;
				return i;
			}
		}
		
		return -1;
	}
	
	public boolean initPosAndVel(float startPosX, float startPosY, float startPosZ, float shipPosX, float shipPosY, float shipPosZ)
	{
		int nextChordIndex = GetNextAvailableChord();
		
		if(nextChordIndex != -1)
		{
			mChordPosition[nextChordIndex].setAll(-1f*startPosX, startPosY, startPosZ);
			
			mVel[nextChordIndex].setAll(shipPosX, shipPosY, shipPosZ);
			mVel[nextChordIndex].x -= startPosX;
			mVel[nextChordIndex].y -= startPosY;
			mVel[nextChordIndex].z -= startPosZ;
			//mVel[nextBulletIndex].multiply(.2f);
			mVel[nextChordIndex].x *= -1f;
			mVel[nextChordIndex].normalize();
			
			return true;
		}
		
		return false;
	}
	
	/*
	public void initPosAndVel(float velX, float velY, float velZ, float posX, float posY, float posZ)
	{
		int nextBulletIndex = GetNextAvailableBullet();
		
		if(nextBulletIndex != -1)
		{
			mChordPosition[nextBulletIndex].setAll(posX, posY, posZ);
			
			mVel[nextBulletIndex].setAll(velX, velY, velZ);
			
		}
	}
	*/
	
	public void setPosition(float fTimeLapsed)
	{
		mFrameCountTime -= fTimeLapsed;
	    if( mFrameCountTime <= 0.0f )
	    {
	    	for(int i=0; i<mNumParticles; i++)
		    {
	    		if( mAlive[i] == false )
					continue;
	    		
	    		if(mState[i] == STATE_BORN)
	    		{
	    			if(mChordFrameCountStateBorn[i]++ >= TOTAL_NUM_PIC_BORN)
					{
	    				mState[i] = STATE_ALIVE;
	    				mChordFrameCountStateBorn[i] = 0;
	    				//mChordFrameCount[i] = TOTAL_NUM_PIC_BORN - 1;
					}
	    			else
	    			{
	    				mChordFrameCount[i] = mChordFrameCountStateBorn[i] - 1;
	    				if(mChordFrameCount[i]<0)
	    					mChordFrameCount[i]=0;
	    				continue;
	    			}
	    		}
	    		if(mState[i] == STATE_ALIVE)
	    		{
	    			if(mChordFrameCountStateAlive[i]++ >= TOTAL_NUM_PIC_ALIVE)
					{
	    				mChordFrameCountStateAlive[i] = 0;
	    				mChordFrameCount[i] = TOTAL_NUM_PIC_BORN;
					}
	    			else
	    			{
	    				mChordFrameCount[i] = TOTAL_NUM_PIC_BORN + mChordFrameCountStateAlive[i] - 1;
	    			}
	    			continue;
	    		}
	    		
	    		if(mState[i] == STATE_EXPLODE)
	    		{
	    			if(mChordFrameCountStateExplode[i]++ >= TOTAL_NUM_PIC_EXPLODE)
					{
	    				mAlive[i] = false;
	    				mState[i] = STATE_INACTIVE;
	    				mChordPosition[i].setAll(INACTIVE_POS, INACTIVE_POS, INACTIVE_POS);
	    				mChordFrameCountStateBorn[i] = 0;
	    				mChordFrameCountStateAlive[i] = (int)((float)Math.random() * (TOTAL_NUM_PIC_ALIVE));
	    				mChordFrameCountStateExplode[i] = 0;
	    				mChordFrameCount[i] = 0;
					}
	    			else
	    			{
	    				mChordFrameCount[i] = TOTAL_NUM_PIC_BORN + TOTAL_NUM_PIC_ALIVE + mChordFrameCountStateExplode[i] - 1;
	    			}
	    		}
	    		
				//if(mChordFrameCount[i]++ >= TOTAL_NUM_PIC)
				//{
				//	mChordFrameCount[i] = 0;
				//}
		    }
			
			mFrameCountTime = mFrameCountTimeTotal;
	    }
	    
	    for(int i=0; i<mNumParticles; i++)
	    {
	    	if( mAlive[i] == false )
				continue;
	    	
	    	mTempPos.setAll(mChordPosition[i].x, mChordPosition[i].y, mChordPosition[i].z);
		    mTempVel.setAll(mVel[i].x, mVel[i].y, mVel[i].z);
			mTempVel.multiply(fTimeLapsed);
			mTempVel.multiply(mChordSpeed[i]);
			mTempPos.add(mTempVel);
			
			mChordPosition[i].setAll(mTempPos.x, mTempPos.y, mTempPos.z);
			
			// Bounce of the wall.
			if(mTempPos.x > GAME_WORLD_SPACE)
			{
				mChordPosition[i].x = GAME_WORLD_SPACE;
				mVel[i].x *= -1f;
			}
			else if(mTempPos.x < -GAME_WORLD_SPACE)
			{
				mChordPosition[i].x = -GAME_WORLD_SPACE;
				mVel[i].x *= -1f;
			}
			
			if(mTempPos.y > GAME_WORLD_Y_SPACE_MAX)
			{
				mChordPosition[i].y = GAME_WORLD_Y_SPACE_MAX;
				mVel[i].y *= -1f;
			}
			else if(mTempPos.y < -GAME_WORLD_SPACE)
			{
				mChordPosition[i].y = -GAME_WORLD_SPACE;
				mVel[i].y *= -1f;
			}
			
			if(mTempPos.z > GAME_WORLD_SPACE)
			{
				mChordPosition[i].z = GAME_WORLD_SPACE;
				mVel[i].z *= -1f;
			}
			else if(mTempPos.z < -GAME_WORLD_SPACE)
			{
				mChordPosition[i].z = -GAME_WORLD_SPACE;
				mVel[i].z *= -1f;
			}
	    }
	}
	
	public void setInactivePosition(int i)
	{
		//mAlive[i] = false;
		mState[i] = STATE_EXPLODE;
		//mChordPosition[i].setAll(INACTIVE_POS, INACTIVE_POS, INACTIVE_POS);
	}
	
	public void setPosAndFrameData()
	{
		for(int i=0; i<mNumParticles; i++)
		{
			setPosAndFrameData(i);
		}
	}
	
	public void setPosAndFrameData(int i)
	{
		mNewPositionBuffer.put(0, mChordPosition[i].x);
		mNewPositionBuffer.put(1, mChordPosition[i].y);
		mNewPositionBuffer.put(2, mChordPosition[i].z);
		mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), mNewPositionBuffer, i);
		
		//mAnimOffsets.put(i, (float)mChordFrameCount[i]);
		
		mNewTimeBuffer.put(0, (float)mChordFrameCount[i]);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTimeBufferHandle);
		GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, i * Geometry3D.FLOAT_SIZE_BYTES, Geometry3D.FLOAT_SIZE_BYTES, mNewTimeBuffer);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	protected void setShaderParams(Camera camera) {
		super.setShaderParams(camera);
		mChordMat.setCamera(camera);
		mChordMat.setPointSize(mPointSize);
		
		setPosAndFrameData();
		
		/*
		if(mVelocityDirty) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVelocityBufferHandle);
			GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mNewVelocityIndex * 12, 12, mNewVelocityBuffer);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTimeBufferHandle);
			GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mNewVelocityIndex * Geometry3D.FLOAT_SIZE_BYTES, Geometry3D.FLOAT_SIZE_BYTES, mNewTimeBuffer);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mSpeedBufferHandle);
			GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mNewVelocityIndex * Geometry3D.FLOAT_SIZE_BYTES, Geometry3D.FLOAT_SIZE_BYTES, mNewSpeedBuffer);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
			mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), mNewPositionBuffer, mNewVelocityIndex);
			mVelocityDirty = false;
		}*/
		
		mChordMat.setTileSize(TILE_SIZE);
		mChordMat.setNumTileRows(NUM_PIC_IN_ROW_AND_COL);
		mChordMat.setFriction(mFriction);
		mChordMat.setVelocity(mVelocityBufferHandle);
		mChordMat.setSpeed(mSpeedBufferHandle);
		mChordMat.setStartTime(mTimeBufferHandle);
		mChordMat.setMultiParticlesEnabled(true);
		mChordMat.setCurrentFrame(mCurrentFrame);
		mChordMat.setTime(mTime);
		
		//mChordMat.setAnimOffsets(mAnimOffsets);
	}
	
	/*
	public void setVelocity(int index, float velX, float velY, float velZ) {
		int ind = mNewVelocityIndex * 3;
		mVelocityBuffer.put(ind, velX);
		mVelocityBuffer.put(ind+1, velY);
		mVelocityBuffer.put(ind+2, velZ);
		mNewVelocityBuffer.put(0, velX);
		mNewVelocityBuffer.put(1, velY);
		mNewVelocityBuffer.put(2, velZ);
		mVelocityDirty = true;
	}
	
	public void setVelocityAndPosition(int index, float startTime, float speed, float velX, float velY, float velZ, float posX, float posY, float posZ) {
		mNewVelocityIndex = index % mNumParticles;
		int ind = mNewVelocityIndex * 3;
		mVelocityBuffer.put(ind, velX);
		mVelocityBuffer.put(ind+1, velY);
		mVelocityBuffer.put(ind+2, velZ);
		mNewVelocityBuffer.put(0, velX);
		mNewVelocityBuffer.put(1, velY);
		mNewVelocityBuffer.put(2, velZ);
		mGeometry.getVertices().put(ind, posX);
		mGeometry.getVertices().put(ind+1, posY);
		mGeometry.getVertices().put(ind+2, posZ);
		mNewPositionBuffer.put(0, posX);
		mNewPositionBuffer.put(1, posY);
		mNewPositionBuffer.put(2, posZ);
		mStartTimeBuffer.put(mNewVelocityIndex, startTime);
		mNewTimeBuffer.put(0, startTime);
		mSpeedBuffer.put(mNewVelocityIndex, speed);
		mNewSpeedBuffer.put(0, speed);
		mVelocityDirty = true;
	}*/
}
