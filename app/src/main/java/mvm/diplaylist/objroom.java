package mvm.diplaylist;

import mvm.flying.FlyingRenderer;
import mvm.flying.R;

import r.BaseObject3D;
import r.materials.SimpleMaterial;
import r.materials.TextureManager;
import r.parser.ObjParser;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

public class objroom extends DisplayObject {
	
	public BaseObject3D mRoomBox;
	//public Cube mRoomBox;
	
	public objroom()
	{
		super();
		
		dwType = gamecommon.OBJ_ROOM;
	}
	
	public void init(Resources r, TextureManager tm)
	{
		/*
		mRoomBox = new Cube(2f, true);
		Bitmap[] textures = new Bitmap[6];
		textures[0] = BitmapFactory.decodeResource(r, R.drawable.wall5156);//.wood_left);
		textures[1] = BitmapFactory.decodeResource(r, R.drawable.wall5156);//wood_right);
		textures[2] = BitmapFactory.decodeResource(r, R.drawable.blue_sky2);//.ceiling);
		textures[3] = BitmapFactory.decodeResource(r, R.drawable.floor);
		textures[4] = BitmapFactory.decodeResource(r, R.drawable.wall5156);//wood_back);
		textures[5] = BitmapFactory.decodeResource(r, R.drawable.wall5156);//wood_back);
		TextureInfo tInfo = tm.addCubemapTextures(textures);
		SkyboxMaterial mat = new SkyboxMaterial();
		mat.addTexture(tInfo);
		mRoomBox.setMaterial(mat);
		mRoomBox.setScaleX(FlyingRenderer.ROOM_SIZE * .5f);
		mRoomBox.setScaleY(FlyingRenderer.ROOM_SIZE * .125f);
		mRoomBox.setScaleZ(FlyingRenderer.ROOM_SIZE * .5f);
		// All is standing on -100f in Y. So we need to take it down to that location (.5 - ScaleY). 
		mRoomBox.setPosition(mRoomBox.getX(),mRoomBox.getY()-(FlyingRenderer.ROOM_SIZE * (.5f - .125f)),mRoomBox.getZ());
		*/
		
		ObjParser parser = new ObjParser(r, tm, R.raw.room);
		parser.parse();
		
		mRoomBox = parser.getParsedObject();
		mRoomBox.setMaterial(new SimpleMaterial());
		mRoomBox.addTexture(tm.addTexture(BitmapFactory.decodeResource(r, R.drawable.room7)));
		mRoomBox.setScaleX(FlyingRenderer.ROOM_SIZE * .5f);
		mRoomBox.setScaleY(FlyingRenderer.ROOM_SIZE * .125f);
		mRoomBox.setScaleZ(FlyingRenderer.ROOM_SIZE * .5f);
		// All is standing on -100f in Y. So we need to take it down to that location (.5 - ScaleY). 
		mRoomBox.setPosition(mRoomBox.getX(),mRoomBox.getY()-(FlyingRenderer.ROOM_SIZE * (.5f - .125f)),mRoomBox.getZ());
		
	}

}
