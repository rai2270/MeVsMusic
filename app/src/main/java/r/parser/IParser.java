package r.parser;

import r.BaseObject3D;
import r.animation.mesh.AAnimationObject3D;

public interface IParser {
	public void parse();
	public BaseObject3D getParsedObject();
	public AAnimationObject3D getParsedAnimationObject();
}
