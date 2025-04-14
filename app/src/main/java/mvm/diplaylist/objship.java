package mvm.diplaylist;

import mvm.flying.FlyingRenderer;
import android.content.res.Resources;

import mvm.flying.R;

import r.BaseObject3D;
import r.lights.DirectionalLight;
import r.materials.TextureManager;
import r.math.Number3D;
import r.parser.ObjParser;

public class objship extends DisplayObject {
	
	public BaseObject3D mShip;
	//public BaseObject3D mShipExp;
	//public Sphere mSphere;
	//public BaseObject3D mRedSphere;
	private static final int RING_NUM = 3;
	public BaseObject3D[] mShipRings;
	int[] ring_rot;
	int[] ringInterval;
	//int[] ringColors;
	
	//int sphere_ring_rot = 0;
	//int sphere_ringInterval = 1;
		
	private Number3D mSpawnOrigin;
	
	public Number3D mDirection;
	public Number3D mSpeed;
	private static final float MAX_SPEED_Z = -12.0f;
	private float[] mRotMatrix;
	
	//static float m_fIncZSpeedTime;
    //static float m_fTempIncZSpeedTime;
	
	private static final float GAME_WORLD_X_SPACE = FlyingRenderer.GAME_WORLD_X_SPACE - FlyingRenderer.ROOM_EDGE * 5f;
	private static final float GAME_WORLD_Y_SPACE = FlyingRenderer.GAME_WORLD_Y_SPACE;
	private static final float GAME_WORLD_Y_SPACE_MAX = -50f - FlyingRenderer.ROOM_EDGE;
	private static final float GAME_WORLD_Z_SPACE = FlyingRenderer.GAME_WORLD_Z_SPACE - FlyingRenderer.ROOM_EDGE * 5f;
	
	private static final Number3D INACTIVE_POS = new Number3D(FlyingRenderer.INACTIVE_POS, FlyingRenderer.INACTIVE_POS, FlyingRenderer.INACTIVE_POS);
	
	public float zDir = 0f;
	private float prevZ = 0f;
	
	public objship(Number3D spawnOrigin)
	{
		super();
		
		dwType = gamecommon.OBJ_SHIP;
		
		mSpawnOrigin = new Number3D(spawnOrigin.x, spawnOrigin.y, spawnOrigin.z);
		
		mDirection = new Number3D();
		mSpeed = new Number3D(0, 0, MAX_SPEED_Z);
		mRotMatrix = new float[16];
		
		//m_fIncZSpeedTime = 0.1f; 
		//m_fTempIncZSpeedTime = m_fIncZSpeedTime;
		
	}
	
	public void init(Resources r, TextureManager tm)
	{
		/*DirectionalLight mLightRedSphere = new DirectionalLight(0, 1, 1); 
		mLightRedSphere.setPower(1);
		mRedSphere = new Sphere(.6f, 24, 24);
		mRedSphere.addLight(mLightRedSphere);
		mRedSphere.setColor(0x20ff0000);
		PhongMaterial phong = new PhongMaterial();//new float[] { 0.2f, 0.2f, 0.2f, 0.2f }, new float[] { 0.2f, 0.2f, 0.2f, 0.2f }, 0.2f);
		phong.setUseColor(true);
		//phong.setSpecularColor(new float[] { 0.2f, 0.2f, 0.2f, 0.2f }, new float[] { 0.2f, 0.2f, 0.2f, 0.2f }, 0.2f);
		mRedSphere.setMaterial(phong);
		mRedSphere.setDrawingMode(GLES20.GL_LINES);*/
		
		DirectionalLight mLight = new DirectionalLight(1f, 0.2f, 1.0f); // set the direction
		mLight.setColor(1.0f, 1.0f, 1.0f);
		mLight.setPower(2);

		DirectionalLight light1 = new DirectionalLight(.1f, 1f, .1f);
		light1.setColor(1, 1, 1);
		light1.setPower(2);
		
		DirectionalLight light2 = new DirectionalLight(-.1f, -.1f, -.1f);
		light2.setColor(1, 1, 1);
		light2.setPower(2);
		
		ObjParser parser = new ObjParser(r, tm, R.raw.ship_obj);
		parser.parse();
		
		//ObjParser parserExp = new ObjParser(r, tm, R.raw.ship_obj_exp);
		//parserExp.parse();
		
		mShip = parser.getParsedObject();
		//for(int i=0;i<mShip.getNumChildren();i++)
		//{
		//	mShip.getChildAt(i).setDrawingMode(GLES20.GL_LINES);
		//}
		mShip.addLight(light1);
		mShip.addLight(light2);
		
		//mShipExp = parserExp.getParsedObject();
		//mShipExp.addLight(light1);
		//mShipExp.addLight(light2);
		
		mShip.setPosition(mSpawnOrigin);
		//mShipExp.setPosition(mSpawnOrigin);
		prevZ = mShip.getZ();
		
		
		mShipRings = new BaseObject3D[RING_NUM];
		ring_rot = new int [RING_NUM];
		ringInterval = new int [RING_NUM];
		//ringColors = new int [RING_NUM];
		//ringColors[0] = Color.RED;
		//ringColors[1] = Color.GREEN;
		//ringColors[2] = Color.BLUE;
		
		for(int i=0;i<mShipRings.length;i++)
		{
			DirectionalLight light3 = new DirectionalLight(.1f, 1f, .1f);
			//light3.setColor(127, 127, 127);
			light3.setColor(1, 1, 1);
			light3.setPower(2);
			
			DirectionalLight light4 = new DirectionalLight(-.1f, -.1f, -.1f);
			//light4.setColor(127, 127, 127);
			light4.setColor(1, 1, 1);
			light4.setPower(2);
			
			ObjParser parserRing = new ObjParser(r, tm, R.raw.ring005);//.ring003);//.ship_ring);//.ship_three_rings2);
			parserRing.parse();
			mShipRings[i] = parserRing.getParsedObject();
			
			// for ship_ring use:
			//mShipRings[i].setScaleX(.008f);
			//mShipRings[i].setScaleY(.008f);
			//mShipRings[i].setScaleZ(.008f);
			
			// for ring003 use:
			mShipRings[i].setScaleX(.02f);
			mShipRings[i].setScaleY(.02f);
			mShipRings[i].setScaleZ(.02f);
			mShipRings[i].addLight(light3);
			mShipRings[i].addLight(light4);
			
			//mShipRings[i].setColor(ringColors[i]);
			
			mShipRings[i].setPosition(INACTIVE_POS);
			
			ring_rot[i] = 0;
			ringInterval[i] = i+1;
		}
		
		//mRedSphere.setPosition(INACTIVE_POS);
	}
	
	
	public void setPosition(float fTimeLapsed, boolean ringIsON, boolean shieldIsON)
	{
		/*m_fTempIncZSpeedTime -= fTimeLapsed;
	    if( m_fTempIncZSpeedTime <= 0.0f )
	    {
	    	if(mSpeed.z < MAX_SPEED_Z)
	    	{
	    		mSpeed.add(0, 0, 0.1f);
	    	}
	    	m_fTempIncZSpeedTime = m_fIncZSpeedTime;
	    }*/
		
		/*if(mSpeed.z < MAX_SPEED_Z)
    	{
    		mSpeed.add(0, 0, 0.05f);
    	}*/
	    
		//ringIsON = true;
		
		prevZ = mShip.getZ();
		
		//mShip.setRotX(mShip.getRotX() + -vVel.y);
		mShip.setRotY(mShip.getRotY() + vVel.x);
		//mShipExp.setRotY(mShip.getRotY() + vVel.x);
		
		if(ringIsON)
		{
			for(int i=0;i<mShipRings.length;i++)
			{
				mShipRings[i].setPosition(mShip.getPosition());
				ring_rot[i] += ringInterval[i] * ringInterval[0];
				mShipRings[i].setRotX(ring_rot[i] % 360);
				ring_rot[i] += ringInterval[i] * ringInterval[0];
				mShipRings[i].setRotY(ring_rot[i] % 360);
				
				//ring_rot[i] += ringInterval[i] * ringInterval[0];
				//mShipRings[i].setRotZ(ring_rot[i] % 360);
				
				/*mShipRings[1].setPosition(mShip.getPosition());
				ring2_rot += ring1Interval * ring2Interval;
				mShipRings[1].setRotX(ring2_rot % 360);
				ring2_rot += ring1Interval * ring2Interval;
				mShipRings[1].setRotY(ring2_rot % 360);
				
				mShipRings[2].setPosition(mShip.getPosition());
				ring3_rot += ring1Interval * ring3Interval;
				mShipRings[2].setRotX(ring3_rot % 360);
				ring3_rot += ring1Interval * ring3Interval;
				mShipRings[2].setRotY(ring3_rot % 360);*/
			}
		}
		else
		{
			for(int i=0;i<mShipRings.length;i++)
			{
				mShipRings[i].setPosition(INACTIVE_POS);
			}
		}
		
		/*if (shieldIsON)
		{
			mRedSphere.setPosition(mShip.getPosition());
			sphere_ring_rot += sphere_ringInterval * sphere_ringInterval;
			mRedSphere.setRotX(sphere_ring_rot % 360);
			sphere_ring_rot += sphere_ringInterval * sphere_ringInterval;
			mRedSphere.setRotY(sphere_ring_rot % 360);
		}
		else
		{
			mRedSphere.setPosition(INACTIVE_POS);
		}*/
		
		mShip.getOrientation().toRotationMatrix(mRotMatrix);
				
		mDirection.setAllFrom(mSpeed);
		mDirection.y += -vVel.y * 15f;
		mDirection.multiply(mRotMatrix);
		mDirection.x *= -1;
		mDirection.multiply(fTimeLapsed);
		mShip.getPosition().add(mDirection);
		//mShipExp.getPosition().add(mDirection);
		
		if(ringIsON)
		{
			for(int i=0;i<mShipRings.length;i++)
			{
				mShipRings[i].getPosition().add(mDirection);
			}
		}
		
		//if (shieldIsON)
		//	mRedSphere.getPosition().add(mDirection);
		
		if( mShip.getX() < -GAME_WORLD_X_SPACE )
		{
			mShip.setX(-GAME_WORLD_X_SPACE);
			//mShipExp.setX(-GAME_WORLD_X_SPACE);
			if(ringIsON)
			{
				for(int i=0;i<mShipRings.length;i++)
				{
					mShipRings[i].setX(-GAME_WORLD_X_SPACE);
				}
			}
			//if (shieldIsON)
				//mRedSphere.setX(-GAME_WORLD_X_SPACE);
		}
		if( mShip.getX() > GAME_WORLD_X_SPACE )
		{
			mShip.setX(GAME_WORLD_X_SPACE);
			//mShipExp.setX(GAME_WORLD_X_SPACE);
			if(ringIsON)
			{
				for(int i=0;i<mShipRings.length;i++)
				{
					mShipRings[i].setX(GAME_WORLD_X_SPACE);
				}
			}
			//if (shieldIsON)
			//	mRedSphere.setX(GAME_WORLD_X_SPACE);
		}
		
		if( mShip.getY() < -GAME_WORLD_Y_SPACE )
		{
			mShip.setY(-GAME_WORLD_Y_SPACE);
			//mShipExp.setY(-GAME_WORLD_Y_SPACE);
			if(ringIsON)
			{
				for(int i=0;i<mShipRings.length;i++)
				{
					mShipRings[i].setY(-GAME_WORLD_Y_SPACE);
				}
			}
			//if (shieldIsON)
			//	mRedSphere.setY(-GAME_WORLD_Y_SPACE);
		}
		if( mShip.getY() > GAME_WORLD_Y_SPACE_MAX )
		{
			mShip.setY(GAME_WORLD_Y_SPACE_MAX);
			//mShipExp.setY(GAME_WORLD_Y_SPACE_MAX);
			if(ringIsON)
			{
				for(int i=0;i<mShipRings.length;i++)
				{
					mShipRings[i].setY(GAME_WORLD_Y_SPACE_MAX);
				}
			}
			//if (shieldIsON)
			//	mRedSphere.setY(GAME_WORLD_Y_SPACE_MAX);
		}
		
		if( mShip.getZ() < -GAME_WORLD_Z_SPACE )
		{
			mShip.setZ(-GAME_WORLD_Z_SPACE);
			//mShipExp.setZ(-GAME_WORLD_Z_SPACE);
			if(ringIsON)
			{
				for(int i=0;i<mShipRings.length;i++)
				{
					mShipRings[i].setZ(-GAME_WORLD_Z_SPACE);
				}
			}
			//if (shieldIsON)
			//	mRedSphere.setZ(-GAME_WORLD_Z_SPACE);
		}
		if( mShip.getZ() > GAME_WORLD_Z_SPACE )
		{
			mShip.setZ(GAME_WORLD_Z_SPACE);
			//mShipExp.setZ(GAME_WORLD_Z_SPACE);
			if(ringIsON)
			{
				for(int i=0;i<mShipRings.length;i++)
				{
					mShipRings[i].setZ(GAME_WORLD_Z_SPACE);
				}
			}
			//if (shieldIsON)
			//	mRedSphere.setZ(GAME_WORLD_Z_SPACE);
		}
		
		zDir = mShip.getZ() - prevZ;
		
		
	}

}
