package mvm.diplaylist;


import mvm.flying.FlyingRenderer;
import r.materials.SimpleMaterial;
import r.materials.TextureInfo;
import r.math.Number3D;
import r.primitives.Cube;

public class objspectrum extends DisplayObject {
	
	public static final float SPECTRUM_BIN_SIZE = 2.5f;
	public static final float SPECTRUM_BIN_SIZE_Y_FACTOR = 5.0f;
	//public static final float SPECTRUM_BIN_SIZE_Z_FACTOR = 1f;//0.3f;
	
	public Cube mSpectrumBin;
	
	private Number3D mSpawnOrigin;
	
	public static final float RELEASE_CHORD_MIN_TIME = 1f;//3f; // Sec
	public static final float RELEASE_CHORD_MAX_TIME = 5f;//8f; // Sec
	public float m_fTimeToReleaseChord = RELEASE_CHORD_MIN_TIME;
	
	public objspectrum(Number3D spawnOrigin)
	{
		super();
		
		dwType = gamecommon.OBJ_SPECTRUM;
		
		mSpawnOrigin = new Number3D(spawnOrigin.x, spawnOrigin.y, spawnOrigin.z);
		
		resetReleaseChordTime();
	}
	
	public void init(int i, TextureInfo ti)
	{
		mSpectrumBin = new Cube(SPECTRUM_BIN_SIZE);
		float distanceBetweenBin = 1.35f;
		mSpectrumBin.setPosition(mSpawnOrigin.x + (FlyingRenderer.NUM_SPECTRUM_BIN/2f)*(SPECTRUM_BIN_SIZE*distanceBetweenBin) - ((float)i)*(SPECTRUM_BIN_SIZE*distanceBetweenBin), mSpawnOrigin.y, mSpawnOrigin.z);
		mSpectrumBin.setMaterial(new SimpleMaterial());
		mSpectrumBin.addTexture(ti);
		//mSpectrumBin.setTransparent(true);
		mSpectrumBin.setScaleY(0.2f);//SPECTRUM_BIN_SIZE_Y_FACTOR);
		//mSpectrumBin.setScaleZ(SPECTRUM_BIN_SIZE_Z_FACTOR);
	}
	
	public void setPositionAndScale(float fSpectrumVal)
	{
		float spectrumYScale =  fSpectrumVal * SPECTRUM_BIN_SIZE_Y_FACTOR;
		mSpectrumBin.setScaleY(spectrumYScale);
		float spectrumYVal = SPECTRUM_BIN_SIZE * spectrumYScale;
		mSpectrumBin.setPosition(mSpectrumBin.getX(), mSpawnOrigin.y + spectrumYVal / 2.0f, mSpectrumBin.getZ());
	}
	
	public void resetReleaseChordTime()
	{
		m_fTimeToReleaseChord = RELEASE_CHORD_MIN_TIME + (float)Math.random() * (RELEASE_CHORD_MAX_TIME - RELEASE_CHORD_MIN_TIME);
	}

}
