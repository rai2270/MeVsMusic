package mvm.particle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import mvm.diplaylist.gamecommon;
import mvm.flying.FlyingRenderer;
import mvm.material.BulletParticleMaterial;

import r.BaseObject3D;
import r.Camera;
import r.Geometry3D;
import r.materials.AMaterial;
import r.math.Number3D;
import android.opengl.GLES20;

public final class BulletParticleSystem extends BaseObject3D {
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
	private BulletParticleMaterial mBulletMat;
	private int mNumParticles;
	
	public Number3D[] mBulletPosition;
	private Number3D[] mVel;
	private float[] mSpeed;
	
	public boolean[] mAlive;
	
	private Number3D mTempPos = new Number3D();
    private Number3D mTempVel = new Number3D();
    
    private static final float BULLET_SPEED = 100.0f;
    private static final float BULLET_SPEED_VERY_FAST = 250.0f;
    
    public static final int ROOM_SIZE = FlyingRenderer.ROOM_SIZE;
    public static final float ROOM_SIZE_HALF = ROOM_SIZE * .5f;
    public static final float INACTIVE_POS = ROOM_SIZE * 1.5f;
    
    //public int mType;
    
	public BulletParticleSystem(int numParticles) {
		super();
		
		//mType = gamecommon.OBJ_BULLET;
		
		mNumParticles = numParticles;
		
		mAlive = new boolean[mNumParticles];
		mBulletPosition = new Number3D[mNumParticles];
		mVel = new Number3D[mNumParticles];
		mSpeed = new float[mNumParticles];
		
		for(int i=0; i<mNumParticles; i++)
	    {
			mAlive[i] = false;
	    	mBulletPosition[i] = new Number3D(INACTIVE_POS, INACTIVE_POS, INACTIVE_POS);
	    	mVel[i] = new Number3D(0, 0, 0);
	    	mSpeed[i] = BULLET_SPEED;
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
			vertices[index] = mBulletPosition[i].x;
			vertices[index + 1] = mBulletPosition[i].y;
			vertices[index + 2] = mBulletPosition[i].z;
			
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
		mBulletMat = (BulletParticleMaterial)material;
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
	
	public int GetNextAvailableBullet()
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
	
	/*public void initPosAndVel(float velX, float velY, float velZ, float posX, float posY, float posZ)
	{
		int nextBulletIndex = GetNextAvailableBullet();
		
		if(nextBulletIndex != -1)
		{
			mBulletPosition[nextBulletIndex].setAll(posX, posY, posZ);
			
			mVel[nextBulletIndex].setAll(velX, velY, velZ);
		}
	}*/
	
	public void initPosAndVel(float velX, float velY, float velZ, float posX, float posY, float posZ, boolean bVeryFastSpeed)
	{
		int nextBulletIndex = GetNextAvailableBullet();
		
		if(nextBulletIndex != -1)
		{
			mBulletPosition[nextBulletIndex].setAll(posX, posY, posZ);
			
			mVel[nextBulletIndex].setAll(velX, velY, velZ);
			
			if(bVeryFastSpeed)
			{
				mSpeed[nextBulletIndex] = BULLET_SPEED_VERY_FAST;
			}
		}
	}
	
	/*
	public boolean initPosAndVel(float startPosX, float startPosY, float startPosZ, float targetPosX, float targetPosY, float targetPosZ, float speed)
	{
		int nextBulletIndex = GetNextAvailableBullet();
		
		if(nextBulletIndex != -1)
		{
			mBulletPosition[nextBulletIndex].setAll(-1f*startPosX, startPosY, startPosZ);
			
			mVel[nextBulletIndex].setAll(startPosX, startPosY, startPosZ);
			mVel[nextBulletIndex].x -= targetPosX;
			mVel[nextBulletIndex].y -= targetPosY;
			mVel[nextBulletIndex].z -= targetPosZ;
			mVel[nextBulletIndex].normalize();
			
					
			mSpeed[nextBulletIndex] = speed;
			
			return true;
		}
		
		return false;
	}*/
	
	public void setPosition(float fTimeLapsed)
	{
		for(int i=0; i<mNumParticles; i++)
	    {
			if( mAlive[i] == false )
				continue;
			
	    	mTempPos.setAll(mBulletPosition[i].x, mBulletPosition[i].y, mBulletPosition[i].z);
		    mTempVel.setAll(mVel[i].x, mVel[i].y, mVel[i].z);
			mTempVel.multiply(fTimeLapsed);
			mTempVel.multiply(mSpeed[i]);
			mTempPos.add(mTempVel);
			
			mBulletPosition[i].setAll(mTempPos.x, mTempPos.y, mTempPos.z);
			
			if( Math.abs(mTempPos.x) >= ROOM_SIZE_HALF ||
				Math.abs(mTempPos.y) >= ROOM_SIZE_HALF ||
				Math.abs(mTempPos.z) >= ROOM_SIZE_HALF )
			{
				setInactivePosition(i);
			}
	    }
	}
	
	public void setInactivePosition(int i)
	{
		mAlive[i] = false;
		mSpeed[i] = BULLET_SPEED;
		mBulletPosition[i].setAll(INACTIVE_POS, INACTIVE_POS, INACTIVE_POS);
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
		mNewPositionBuffer.put(0, mBulletPosition[i].x);
		mNewPositionBuffer.put(1, mBulletPosition[i].y);
		mNewPositionBuffer.put(2, mBulletPosition[i].z);
		mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), mNewPositionBuffer, i);
	}
	
	protected void setShaderParams(Camera camera) {
		super.setShaderParams(camera);
		mBulletMat.setCamera(camera);
		mBulletMat.setPointSize(mPointSize);
		
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
		
		mBulletMat.setTileSize(1 / 5f);
		mBulletMat.setNumTileRows(5);
		mBulletMat.setFriction(mFriction);
		mBulletMat.setVelocity(mVelocityBufferHandle);
		mBulletMat.setSpeed(mSpeedBufferHandle);
		mBulletMat.setStartTime(mTimeBufferHandle);
		mBulletMat.setMultiParticlesEnabled(true);
		mBulletMat.setCurrentFrame(mCurrentFrame);
		mBulletMat.setTime(mTime);
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
