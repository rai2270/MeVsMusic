package r.animation.mesh;

import r.Geometry3D;

public interface IAnimationFrame {
	public Geometry3D getGeometry();
	public void setGeometry(Geometry3D geometry);
	public String getName();
	public void setName(String name);
}
