package r.animation;

import r.ATransformable3D;
import r.math.Number3D;
import r.math.Quaternion;
import r.math.Number3D.Axis;

public class RotateAnimation3D extends Animation3D {
	protected float mDegreesToRotate;
	protected float mRotateFrom;
	protected float mRotationAngle;
	protected Number3D mRotationAxis;
	protected Quaternion mQuat;
	protected Quaternion mQuatFrom;
	protected boolean mCopyCurrentOrientation;
	
	public RotateAnimation3D(Axis axis, float degreesToRotate) {
		this(axis, 0, degreesToRotate);
		mCopyCurrentOrientation = true;
	}
	
	public RotateAnimation3D(Axis axis, float rotateFrom, float degreesToRotate ) {
		this(Number3D.getAxisVector(axis), rotateFrom, degreesToRotate);
	}
	
	public RotateAnimation3D(Number3D axis, float degreesToRotate ) {
		this(axis, 0, degreesToRotate);
		mCopyCurrentOrientation = true;
	}

	public RotateAnimation3D(Number3D axis, float rotateFrom, float degreesToRotate ) {
		super();
		mQuat = new Quaternion();
		mQuatFrom = new Quaternion();
		mQuatFrom.fromAngleAxis(rotateFrom, axis);
		mRotationAxis = axis;
		mRotateFrom = rotateFrom;
		mDegreesToRotate = degreesToRotate;
	}
	
	@Override
	public void start() {
		if(mCopyCurrentOrientation)
			mQuatFrom.setAllFrom(mTransformable3D.getOrientation());
		super.start();
	}
	
	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if(mCopyCurrentOrientation)
			mQuatFrom.setAllFrom(transformable3D.getOrientation());
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime) {
		mRotationAngle = mRotateFrom + (interpolatedTime * mDegreesToRotate);
		//Log.d("Rajawali", angle);
		mQuat.fromAngleAxis(mRotationAngle, mRotationAxis);
		mQuat.multiply(mQuatFrom);
		mTransformable3D.setOrientation(mQuat);
	}

	/**
	 * @deprecated use RotateAnimation3D(axis, degreesToRotate) or RotateAnimation(axis, rotateFrom, degreesToRotate)
	 * @param toRotate
	 */
	public RotateAnimation3D(Number3D toRotate) {
	}
	
	/**
	 * @deprecated use RotateAnimation3D(axis, degreesToRotate) or RotateAnimation(axis, rotateFrom, degreesToRotate) 
	 * @param fromRotate
	 * @param toRotate
	 */
	public RotateAnimation3D(Number3D fromRotate, Number3D toRotate) {
	}
}

