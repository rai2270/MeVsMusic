package mvm.diplaylist;

import r.math.Number3D;

public class DisplayObject {
	
	public boolean		bAlive;

	public int			dwType;				// Type of object
	public boolean		bVisible;			// Whether the object is visible
	//public Number3D		vPos;				// Position
	public Number3D		vVel;				// Velocity
	public float		fSize;				// Size
	public float		fFrame;				// Current animation frame
	public float		fDelay;				// Frame/second
	public int			dwColor;

	public DisplayObject	pNext;				// Link to next object
	public DisplayObject	pPrev;				// Link to previous object
	
	public DisplayObject()
	{
		bAlive	 = false;
		bVisible = true;
		fSize	 = 0.0f;
		fFrame	 = 0.0f;
		fDelay	 = 0.0f;
		dwColor  = 0xffffffff;
		//vPos 	 = new Number3D();
		vVel 	 = new Number3D();

		pNext    = null;
	    pPrev    = null;
	}

}
