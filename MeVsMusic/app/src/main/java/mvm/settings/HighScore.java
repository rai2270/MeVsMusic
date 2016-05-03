package mvm.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class HighScore {
	public static final int TRACKS_NUM = 3;
	public static final int MAX_VIEW_TRACK = 22;
	public static String[] mTrack = new String[TRACKS_NUM];
	public static String[] mViewTrack = new String[TRACKS_NUM];
	public static long[] mScore = new long[TRACKS_NUM];
	public final static String file = ".mvmdata";

	public static void load(GameFileIO files) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(files.readFile(file)));
			for(int i=0; i<TRACKS_NUM; i++)
			{
				mTrack[i] = in.readLine();
				
				mViewTrack[i] = mTrack[i];
				if(mViewTrack[i].length()>MAX_VIEW_TRACK)
				{
					mViewTrack[i] = mViewTrack[i].substring(0, MAX_VIEW_TRACK);
				}
				
				mScore[i] = Long.parseLong(in.readLine());
			}
		} catch (Exception e) {
		} finally {
			try {
				if (in != null)
					in.close();
				/*for(int i=0; i<TRACKS_NUM; i++)
				{
					if(mTrack[i]==null)
					{
						mScore[i] = 0;
					}
				}*/
			} catch (IOException e) {
			}
		}
	}

	public static void save(GameFileIO files) {
		BufferedWriter out = null;
		try {
			for(int i=0; i<TRACKS_NUM; i++)
			{
				mViewTrack[i] = mTrack[i];
				if(mViewTrack[i].length()>MAX_VIEW_TRACK)
				{
					mViewTrack[i] = mViewTrack[i].substring(0, MAX_VIEW_TRACK);
				}
			}
			
			out = new BufferedWriter(new OutputStreamWriter(files.writeFile(file)));
			for(int i=0; i<TRACKS_NUM; i++)
			{
				out.write(mTrack[i]);
				out.write("\n");
				out.write(Long.toString(mScore[i]));
				out.write("\n");
			}
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
