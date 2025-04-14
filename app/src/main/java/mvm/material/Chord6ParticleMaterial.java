package mvm.material;

import java.nio.FloatBuffer;

import r.materials.AMaterial;
import r.math.Number3D;
import android.opengl.GLES20;


public class Chord6ParticleMaterial extends AMaterial {
	protected static final String mVShader = 
			"precision mediump float;\n" +
			"uniform mat4 uMVPMatrix;\n" +
			"uniform float uPointSize;\n" +
			"uniform mat4 uMMatrix;\n" +
			"uniform vec3 uCameraPosition;\n" +
			"uniform vec3 uDistanceAtt;\n" +
			"uniform vec3 uFriction;\n" +
			"uniform float uTime;\n" +
			"uniform bool uMultiParticlesEnabled;\n" +
			
			"#ifdef ANIMATED\n" +
			"uniform float uCurrentFrame;\n" +
			"uniform float uTileSize;\n" +
			"uniform float uNumTileRows;\n" +
			"attribute float aAnimOffset;\n" +
			"#endif\n" +
			
			"attribute vec4 aPosition;\n" +		
			"attribute vec2 aTextureCoord;\n" +
			"attribute vec3 aVelocity;\n" +
			"attribute float aStartTime;\n" +
			"attribute float aSpeed;\n" +
			
			"varying vec2 vTextureCoord;\n" +

			"void main() {\n" +
			"	vec4 position = vec4(aPosition);\n" +
			"	if(uMultiParticlesEnabled){" +
			"		float timed = uTime - aStartTime;\n" +
			"		position.x = position.x;\n" +
			"		position.y = position.y;\n" +
			"		position.z = position.z; }" +
			"	gl_Position = uMVPMatrix * position;\n" +
			"	vec3 cp = vec3(uCameraPosition);\n" +
			"	cp.x *= -1.0;\n" +
			"	float pdist = length(cp - position.xyz);\n" +
			"	gl_PointSize = uPointSize / sqrt(uDistanceAtt.x + uDistanceAtt.y * pdist + uDistanceAtt.z * pdist * pdist);\n" +
			"	#ifdef ANIMATED\n" +
			"		vTextureCoord.s = mod(uCurrentFrame + aStartTime, uNumTileRows) * uTileSize;" +
			"		vTextureCoord.t = uTileSize * floor((uCurrentFrame + aStartTime ) / uNumTileRows);\n" +
			"	#else\n" +
			"		vTextureCoord = aTextureCoord;\n" +
			"	#endif\n" +
			"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"uniform sampler2D uDiffuseTexture;\n" +
		
		"#ifdef ANIMATED\n" +
		"uniform float uTileSize;\n" +
		"uniform float uNumTileRows;\n" +
		"#endif\n" +

		"void main() {\n" +
		"	\n#ifdef ANIMATED\n" +
		"		vec2 realTexCoord = vTextureCoord + (gl_PointCoord / uNumTileRows);" +
		"		gl_FragColor = texture2D(uDiffuseTexture, realTexCoord);\n" +
		"	#else\n" +
		"		gl_FragColor = texture2D(uDiffuseTexture, gl_PointCoord);\n" +
		"	#endif\n" +
		"}\n";
	
	protected float mPointSize = 10.0f;
	
	protected int muPointSizeHandle;
	protected int muDistanceAttHandle;
	protected int muCurrentFrameHandle;
	protected int muTileSizeHandle;
	protected int muNumTileRowsHandle;
	protected int muFrictionHandle;
	protected int muTimeHandle;
	protected int muMultiParticlesEnabledHandle;

	protected int maVelocityHandle;
	protected int maAnimOffsetHandle;
	protected int maSpeedHandle;
	protected int maStartTimeHandle;
	
	protected float[] mDistanceAtt;
	protected boolean mMultiParticlesEnabled;
	protected float[] mFriction;
	protected float mTime;	
	protected int mCurrentFrame;
	protected float mTileSize;
	protected float mNumTileRows;
	
	public Chord6ParticleMaterial() {
		this(false);
	}
	
	public Chord6ParticleMaterial(boolean isAnimated) {
		super(mVShader, mFShader, isAnimated);
		mDistanceAtt = new float[] {1, 1, 1};
		mFriction = new float[3];
		mIsAnimated = isAnimated;
		if(mIsAnimated) {
			mUntouchedVertexShader = "\n#define ANIMATED\n" + mUntouchedVertexShader;
			mUntouchedFragmentShader = "\n#define ANIMATED\n" + mUntouchedFragmentShader;
		}
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public void setPointSize(float pointSize) {
		mPointSize = pointSize;
	}
	
	public void setMultiParticlesEnabled(boolean enabled) {
		mMultiParticlesEnabled = enabled;
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform1f(muPointSizeHandle, mPointSize);
		GLES20.glUniform3fv(muDistanceAttHandle, 1, mDistanceAtt, 0);
		GLES20.glUniform1i(muMultiParticlesEnabledHandle, mMultiParticlesEnabled == true ? GLES20.GL_TRUE : GLES20.GL_FALSE);
		GLES20.glUniform1f(muTimeHandle, mTime);
	}
	
	public void setVelocity(final int velocityBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, velocityBufferHandle);
		GLES20.glEnableVertexAttribArray(maVelocityHandle);
		GLES20.glVertexAttribPointer(maVelocityHandle, 3, GLES20.GL_FLOAT, false,
				0, 0);
    }
	
	public void setStartTime(final int startTimeBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, startTimeBufferHandle);
		GLES20.glEnableVertexAttribArray(maStartTimeHandle);
		GLES20.glVertexAttribPointer(maStartTimeHandle, 1, GLES20.GL_FLOAT, false,
				0, 0);
    }
	
	public void setSpeed(final int speedBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, speedBufferHandle);
		GLES20.glEnableVertexAttribArray(maSpeedHandle);
		GLES20.glVertexAttribPointer(maSpeedHandle, 1, GLES20.GL_FLOAT, false,
				0, 0);
    }

	public void setFriction(Number3D friction) {
		mFriction[0] = friction.x; mFriction[1] = friction.y; mFriction[2] = friction.z;
		GLES20.glUniform3fv(muFrictionHandle, 1, mFriction, 0);
	}
	
	public void setTime(float time) {
		mTime = time;
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muPointSizeHandle = getUniformLocation("uPointSize");
		muDistanceAttHandle = getUniformLocation("uDistanceAtt");
		
		muFrictionHandle = getUniformLocation("uFriction");
		muTimeHandle = getUniformLocation("uTime");
		muMultiParticlesEnabledHandle = getUniformLocation("uMultiParticlesEnabled");
		
		muCurrentFrameHandle = getUniformLocation("uCurrentFrame");
		muTileSizeHandle = getUniformLocation("uTileSize");
		muNumTileRowsHandle = getUniformLocation("uNumTileRows");

		maVelocityHandle = getAttribLocation("aVelocity");
		maAnimOffsetHandle = getAttribLocation("aAnimOffset");
		maSpeedHandle = getAttribLocation("aSpeed");
		maStartTimeHandle = getAttribLocation("aStartTime");
	}
	
	public void setAnimOffsets(FloatBuffer animOffsets) {
		GLES20.glEnableVertexAttribArray(maAnimOffsetHandle);
		GLES20.glVertexAttribPointer(maAnimOffsetHandle, 1, GLES20.GL_FLOAT, false, 0, animOffsets);
	}
	
	public void setCurrentFrame(int currentFrame) {
		mCurrentFrame = currentFrame;
		GLES20.glUniform1f(muCurrentFrameHandle, mCurrentFrame);
	}
	
	public void setTileSize(float tileSize) {
		mTileSize = tileSize;
		GLES20.glUniform1f(muTileSizeHandle, mTileSize);
	}
	
	public void setNumTileRows(int numTileRows) {
		mNumTileRows = numTileRows;
		GLES20.glUniform1f(muNumTileRowsHandle, mNumTileRows);
	}
}
