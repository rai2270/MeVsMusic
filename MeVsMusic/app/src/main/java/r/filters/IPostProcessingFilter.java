package r.filters;

import r.materials.TextureInfo;


public interface IPostProcessingFilter {
	public void addTexture(TextureInfo textureInfo);
	public boolean usesDepthBuffer();
}
