package mvm.flying;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;

/**
 * Utility class for OpenGL compatibility handling
 */
public class OpenGLUtils {
    
    /**
     * Check if the device supports OpenGL ES 3.0
     * @param context Application context
     * @return true if the device supports OpenGL ES 3.0
     */
    public static boolean supportsOpenGLES3(Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            return configurationInfo.reqGlEsVersion >= 0x30000;
        }
        return false;
    }
}
