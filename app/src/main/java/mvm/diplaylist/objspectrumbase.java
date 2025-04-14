package mvm.diplaylist;

import r.materials.SimpleMaterial;
import r.materials.TextureInfo;
import r.math.Number3D;
import r.primitives.Cube;

public class objspectrumbase extends DisplayObject {
	
	public static final float SPECTRUM_BIN_SIZE = 2.5f;
	public static final float SPECTRUM_BIN_SIZE_Y_FACTOR = 5.0f;
	
	public Cube mSpectrumBaseBin;
	
	private Number3D mSpawnOrigin;
	
	public objspectrumbase(Number3D spawnOrigin)
	{
		super();
		
		dwType = gamecommon.OBJ_SPECTRUM_BASE;
		
		mSpawnOrigin = new Number3D(spawnOrigin.x, spawnOrigin.y, spawnOrigin.z);
	}
	
	public void init(Number3D spawnOrigin, TextureInfo ti)
	{
		mSpectrumBaseBin = new Cube(SPECTRUM_BIN_SIZE);
		mSpectrumBaseBin.setPosition(new Number3D(spawnOrigin));
		mSpectrumBaseBin.setMaterial(new SimpleMaterial());
		mSpectrumBaseBin.addTexture(ti);
		mSpectrumBaseBin.setTransparent(true);
		mSpectrumBaseBin.setScaleY(SPECTRUM_BIN_SIZE_Y_FACTOR);
		
	}
	
	public void setPositionAndScale(float fSpectrumVal)
	{
		float spectrumYScale = fSpectrumVal * SPECTRUM_BIN_SIZE_Y_FACTOR;
		mSpectrumBaseBin.setScaleY(spectrumYScale);
		float spectrumYVal = SPECTRUM_BIN_SIZE * spectrumYScale;
		mSpectrumBaseBin.setPosition(mSpectrumBaseBin.getX(), mSpawnOrigin.y + spectrumYVal / 2.0f, mSpectrumBaseBin.getZ());
	}
	
	

}
