package mvm.flying;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.util.Log;

/**
 * Compatibility class for handling OpenGL differences across Android versions
 */
public class OpenGLCompat {
    private static final String TAG = "OpenGLCompat";
    
    /**
     * Initialize OpenGL compatibility settings based on device capabilities
     * 
     * @param context Application context
     * @return true if initialized successfully
     */
    public static boolean init(Context context) {
        try {
            // Force GC to clean up any lingering OpenGL resources
            System.gc();
            
            // Configure JNI OpenGL error handling for newer Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Reduce JNI OpenGL errors on Android 9+ devices
                System.setProperty("debug.hwui.renderer", "skiagl");
            }

            Log.d(TAG, "OpenGL compatibility layer initialized successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing OpenGL compatibility: " + e.getMessage());
            return false;
        }
    }
}
