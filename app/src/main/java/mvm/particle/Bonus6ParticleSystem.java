package mvm.particle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import mvm.diplaylist.gamecommon;
import mvm.flying.FlyingRenderer;
import mvm.material.Bonus6ParticleMaterial;

import r.BaseObject3D;
import r.Camera;
import r.Geometry3D;
import r.materials.AMaterial;
import r.math.Number3D;
import android.opengl.GLES20;

public final class Bonus6ParticleSystem extends BaseObject3D {
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
	private Bonus6ParticleMaterial mChordMat;
	private int mNumParticles;
	
	public Number3D[] mBonusPosition;
	private Number3D[] mVel;
	private int[] mChordFrameCount;
	private float[] mChordSpeed;
	
	public boolean[] mAlive;
	
	float mFrameCountTimeTotal;
    float mFrameCountTime;
    
    //public int mType;
    
    private static final int ONE_ROTATE_TAKES = 2;// 2 Seconds
	private static final int RAND_INTERVAL = 0;
	
	private static final int NUM_PIC_IN_ROW_AND_COL = 8;
	private static final float TILE_SIZE = 1 / ((float)NUM_PIC_IN_ROW_AND_COL);
	private static final int TOTAL_NUM_PIC = NUM_PIC_IN_ROW_AND_COL * NUM_PIC_IN_ROW_AND_COL;
	
	private static final float CHORD_SPEED_MIN = 5.0f;
	private static final float CHORD_SPEED_MAX = 11.0f;
	
	public static final int ROOM_SIZE = FlyingRenderer.ROOM_SIZE;
    public static final float ROOM_SIZE_HALF = ROOM_SIZE * .5f;
    public static final float INACTIVE_POS = ROOM_SIZE * 1.5f;
	
	public Bonus6ParticleSystem(int numParticles) {
		super();
		mNumParticles = numParticles;
		
		//mType = gamecommon.OBJ_BONUS_WEAPON2;
		
		float randTime = ONE_ROTATE_TAKES + (float)Math.random() * RAND_INTERVAL;
		mFrameCountTimeTotal = randTime / ((float)TOTAL_NUM_PIC);  //  ( (ONE_ROTATE_TAKES * 1000) / 64f ) / 1000f
	    mFrameCountTime = mFrameCountTimeTotal;
		
		mAlive = new boolean[mNumParticles];
		mBonusPosition = new Number3D[mNumParticles];
		mVel = new Number3D[mNumParticles];
		mChordFrameCount = new int[mNumParticles];
		mChordSpeed = new float[mNumParticles];
		
		for(int i=0; i<mNumParticles; i++)
	    {
			mAlive[i] = false;
			mBonusPosition[i] = new Number3D(INACTIVE_POS, INACTIVE_POS, INACTIVE_POS);
	    	mVel[i] = new Number3D(0, 0, 0);
	    	mChordFrameCount[i] = (int)((float)Math.random() * (TOTAL_NUM_PIC));
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
			vertices[index] = mBonusPosition[i].x;
			vertices[index + 1] = mBonusPosition[i].y;
			vertices[index + 2] = mBonusPosition[i].z;
			
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
		mChordMat = (Bonus6ParticleMaterial)material;
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
	
	private int GetNextAvailableBonus()
	{
		for(int i=0; i<mNumParticles; i++)
		{
			if( mAlive[i] == false )
			{
				mAlive[i] = true;
				return i;
			}
		}
		
		return -1;
	}
	
	public boolean initPosAndVel(float startPosX, float startPosY, float startPosZ)
	{
		int nextBonusIndex = GetNextAvailableBonus();
		
		if(nextBonusIndex != -1)
		{
			mBonusPosition[nextBonusIndex].setAll(-1f*startPosX, startPosY, startPosZ);
			
			mVel[nextBonusIndex].setAll(0f, 0f, 0f);
						
			return true;
		}
		
		return false;
	}
	
	public void setPosition(float fTimeLapsed)
	{
		mFrameCountTime -= fTimeLapsed;
	    if( mFrameCountTime <= 0.0f )
	    {
	    	for(int i=0; i<mNumParticles; i++)
		    {
				if(mChordFrameCount[i]++ >= TOTAL_NUM_PIC)
				{
					mChordFrameCount[i] = 0;
				}
		    }
			
			mFrameCountTime = mFrameCountTimeTotal;
	    }
	    
	    
	}
	
	public void setInactivePosition(int i)
	{
		mAlive[i] = false;
		mBonusPosition[i].setAll(INACTIVE_POS, INACTIVE_POS, INACTIVE_POS);
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
		mNewPositionBuffer.put(0, mBonusPosition[i].x);
		mNewPositionBuffer.put(1, mBonusPosition[i].y);
		mNewPositionBuffer.put(2, mBonusPosition[i].z);
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
		
		mChordMat.setTileSize(TILE_SIZE);
		mChordMat.setNumTileRows(NUM_PIC_IN_ROW_AND_COL);
		mChordMat.setFriction(mFriction);
		mChordMat.setVelocity(mVelocityBufferHandle);
		mChordMat.setSpeed(mSpeedBufferHandle);
		mChordMat.setStartTime(mTimeBufferHandle);
		mChordMat.setMultiParticlesEnabled(true);
		mChordMat.setCurrentFrame(mCurrentFrame);
		mChordMat.setTime(mTime);
				
	}
	
	
}
