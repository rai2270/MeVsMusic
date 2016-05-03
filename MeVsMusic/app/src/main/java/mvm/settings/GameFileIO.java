package mvm.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GameFileIO {
    //String externalStoragePath;
	
	File externalFilesDir;

    public GameFileIO(File i_externalFilesDir) {
    	externalFilesDir = i_externalFilesDir;
        //this.externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        //this.externalStoragePath = Environment.get.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    public InputStream readFile(String fileName) throws IOException {
        return new FileInputStream(new File(externalFilesDir, fileName));
    }

    public OutputStream writeFile(String fileName) throws IOException {
        return new FileOutputStream(new File(externalFilesDir, fileName));
    }
}
