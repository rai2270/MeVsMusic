package r;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import r.animation.TimerManager;
import r.renderer.RajawaliRenderer;
import r.util.RajLog;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;

public class RActivity extends Activity {
	protected GLSurfaceView mSurfaceView;
	protected FrameLayout mLayout;
	protected boolean mMultisamplingEnabled = false;
	protected boolean mUsesCoverageAa;
	private RajawaliRenderer mRajRenderer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSurfaceView = new GLSurfaceView(this);
        
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        if(info.reqGlEsVersion < 0x20000)
        	throw new Error("OpenGL ES 2.0 is not supported by this device");
        
        // Use OpenGL ES 2.0 with custom context factory for better compatibility
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mLayout = new FrameLayout(this);
        mLayout.addView(mSurfaceView);
        
        if(mMultisamplingEnabled)
        	createMultisampleConfig();
        
        setContentView(mLayout);
    }
    
    protected void createMultisampleConfig() {
    	final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
    	final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;
    	
        mSurfaceView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				int[] configSpec = new int[] { 
						EGL10.EGL_RED_SIZE, 5,
						EGL10.EGL_GREEN_SIZE, 6,
						EGL10.EGL_BLUE_SIZE, 5,
						EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_RENDERABLE_TYPE, 4,
						EGL10.EGL_SAMPLE_BUFFERS, 1,
						EGL10.EGL_SAMPLES, 2,
						EGL10.EGL_NONE
				};

				int[] result = new int[1];
				if(!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
					RajLog.e("Multisampling configuration 1 failed.");
				}
				
				if(result[0] <= 0) {
					// no multisampling, check for coverage multisampling
					configSpec = new int[] {
						EGL10.EGL_RED_SIZE, 5,
						EGL10.EGL_GREEN_SIZE, 6,
						EGL10.EGL_BLUE_SIZE, 5,
						EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_RENDERABLE_TYPE, 4,
						EGL_COVERAGE_BUFFERS_NV, 1,
						EGL_COVERAGE_SAMPLES_NV, 2,
						EGL10.EGL_NONE
					};
					
					if(!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
						RajLog.e("Multisampling configuration 2 failed. Multisampling is not possible on your device.");
					}
					
					if(result[0] <= 0) {
						configSpec = new int[] {
							EGL10.EGL_RED_SIZE, 5,
							EGL10.EGL_GREEN_SIZE, 6, 
							EGL10.EGL_BLUE_SIZE, 5,
							EGL10.EGL_DEPTH_SIZE, 16,
							EGL10.EGL_RENDERABLE_TYPE, 4,
							EGL10.EGL_NONE
						};

						if(!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
							RajLog.e("Multisampling configuration 3 failed. Multisampling is not possible on your device.");
						}

						if(result[0] <= 0) {
							throw new RuntimeException("Couldn't create OpenGL config.");
						}
					} else {
						mUsesCoverageAa = true;
					}
				}
				EGLConfig[] configs = new EGLConfig[result[0]];
				if(!egl.eglChooseConfig(display, configSpec, configs, result[0], result)) {
					throw new RuntimeException("Couldn't create OpenGL config.");
				}
				
				int index = -1;
				int[] value = new int[1];
				for(int i=0; i<configs.length; ++i) {
					egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_RED_SIZE, value);
					if(value[0] == 5) {
						index = i;
						break;
					}
				}

				EGLConfig config = configs.length > 0 ? configs[index] : null;
				if(config == null) {
					throw new RuntimeException("No config chosen");
				}
				
				return config;
			}
		});
    }
    
    protected void setGLBackgroundTransparent(boolean transparent) {
    	if(transparent) {
            mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            mSurfaceView.setZOrderOnTop(true);
    	} else {
            mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            mSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
            mSurfaceView.setZOrderOnTop(false);
    	}
    }
    
    protected void setRenderer(RajawaliRenderer renderer) {
    	mRajRenderer = renderer;
    	mSurfaceView.setRenderer(renderer);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	// Set continuous rendering mode to avoid context issues on some devices
    	mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    	mSurfaceView.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	TimerManager.getInstance().clear();
    	mSurfaceView.onPause();
    }

    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mRajRenderer != null) {
                mRajRenderer.onSurfaceDestroyed();
            }
            unbindDrawables(mLayout);
            System.gc();
        } catch (Exception e) {
            // Prevent crashes during context destruction
        }
    }
    
    protected void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}