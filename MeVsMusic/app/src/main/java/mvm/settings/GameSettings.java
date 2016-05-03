package mvm.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class GameSettings {
	public static boolean bAccelerometer = false;
	public final static String file = ".mvmset";

	public static void load(GameFileIO files) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(files.readFile(file)));
			bAccelerometer = Boolean.parseBoolean(in.readLine());
		} catch (Exception e) {
		} finally {
			try {
				if (in != null)
					in.close();
				
			} catch (IOException e) {
			}
		}
	}

	public static void save(GameFileIO files) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(files.writeFile(file)));
			out.write(Boolean.toString(bAccelerometer));
		} catch (Exception e) {
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
			}
		}
	}
}
