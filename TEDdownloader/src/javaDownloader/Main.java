package javaDownloader;

import java.io.*;
import java.net.*;

import javax.swing.JFileChooser;

import javaDownloader.Talk;

/**
 * 
 * @author Jonas Jensen
 *
 * This program downloads the first 100 talks from http://www.ted.com/talks/quick-list to the specified folder.
 * Monitors and displays progress.
 * The quality of the videos (and thus file size) can be chosen by altering the 
 * downloadQuality.QUALITY_HIGH constant @ line 181.
 *
 * To-do:
 *  - User-selected options for formatting file names
 *  - Download more than the first 100 videos; go beyond the first page
 */

public class Main {

	public static String getHTMLPageSource(String url) {
		URL myUrl = null;
		try {
			myUrl = new URL(url);
		} catch (MalformedURLException e1) {
			System.out.println("Malformed URL!");
		}
	    BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(myUrl.openStream()));
		} catch (IOException e) {
			System.out.println("getHTMLPageSource -- in: IOException!");
			e.printStackTrace();
			return "";
		}

	    String line = null, 
	    		res = "";
	    try {
			while ((line = in.readLine()) != null) {
			    res += line;
			}
		} catch (IOException e) {
			System.out.println("getHTMLPageSource -- in.readLine(): IOException!");
		}

	    try {
			in.close();
		} catch (IOException e) {
			System.out.println("getHTMLPageSource -- in.close(): IOException!");
		}
		return res;
	}
		
	public static enum downloadQuality { QUALITY_LOW, QUALITY_MED, QUALITY_HIGH };
	public static final String 
		DL_URL_PREFIX 		= "http://download.ted.com/talks/",
		DL_URL_SUFFIX 		= ".mp4?apikey=TEDDOWNLOAD",
		DL_URL_SUFFIX_QLOW	= "-light",
		DL_URL_SUFFIX_QHIGH	= "-480p",
		DL_FILE_SUFFIX		= ".mp4";
	
	public static boolean downloadTalk(Talk talk, downloadQuality q, String savePath, String filePrefix) throws Exception {
		
		String dlQualitySuffix = "";
		if (q.equals(downloadQuality.QUALITY_LOW))
			dlQualitySuffix = DL_URL_SUFFIX_QLOW;
		else
			if (q.equals(downloadQuality.QUALITY_HIGH))
				dlQualitySuffix = DL_URL_SUFFIX_QHIGH;
		
		
		System.out.println("\"" + talk.title + "\" @ \"" + savePath + "\"");
		URL url = new URL(DL_URL_PREFIX + talk.downloadUrl + dlQualitySuffix + DL_URL_SUFFIX);
		URLConnection connection = url.openConnection();
		InputStream in = connection.getInputStream();
				
		FileOutputStream fos = new FileOutputStream(new File(savePath + (filePrefix != null ? filePrefix : "") + 
			talk.title.replaceAll("[^a-zA-Z0-9.-]", "_") + DL_FILE_SUFFIX)); // http://goo.gl/uEeUTK
		byte[] buf = new byte[512];
		
		int fSize = connection.getContentLength();
		long 	dlProgress = 0,
				dlProgressPercent = 0,
				lastPrintedVal = -1;
		
		while (true) {
		    int len = in.read(buf);
		    
		    dlProgress += len;
		    dlProgressPercent = (dlProgress * 100 / fSize);
		    if (dlProgressPercent % 10 == 0 && !(dlProgressPercent == lastPrintedVal)) {
		    	System.out.print(dlProgressPercent + "%" + (dlProgressPercent == 100 ? "\n" : " | "));
		    	lastPrintedVal = dlProgressPercent;
		    }
		    
		    if (len == -1) {
		        break;
		    }
		    fos.write(buf, 0, len);
		}
				
		in.close();
		fos.flush();
		fos.close();
		return true;
	}	
	
	public static boolean batchDlTalks(Talk[] talks, downloadQuality q, String savePath, boolean numPrefix) {
		
		for (int i = 0; i < talks.length; i++) {
			try {
				System.out.print("#" + (i + 1) + "/" + talks.length + ": ");
				downloadTalk(talks[i], q, savePath, (numPrefix ? String.format("%05d", i) : ""));
			} catch (Exception e) {
				System.out.println("Failed to download talk #" + i + ":" + talks[i].title);
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	/**
	 * Currently only fetches first page data. (GET[] VARIABLE ?page=1)
	 * @param pageHTML
	 * @return
	 */
	public static Talk[] fetchTalksData(String pageHTML) {
		// 16 pages in total
		// int numPages = [showing 1-100 of X] X / 100; 

		int pageHTMLi = pageHTML.indexOf("<th class=\"action\">Download</th>") + 43;
		String[] videoData = pageHTML.substring(pageHTMLi, pageHTML.indexOf("</table>")).split("<tr>");
		
		
		Talk[] resTalks = new Talk[videoData.length];
		for (int i = 0; i < videoData.length; i++) {	
			resTalks[i] = new Talk(videoData[i]); 
		}
		
		return resTalks;
	}
		
	/**
	 * Mainly for debugging purposes.
	 * @param talks
	 */
	public static void printTalks(Talk[] talks) {
		System.out.printf("%-3s | %5s | %s%n", "#", "Title", "Duration");
		for (int i = 0; i < talks.length; i++) {
			System.out.printf("%-3d | %5s | %s%n", 
					i, talks[i].duration, talks[i].title);
		}
	}
	
	public static void main(String[] args) {
		final String PAGE_URL  = "http://www.ted.com/talks/quick-list";
		String SAVE_PATH = "";//"C:\\Users\\Jonas\\Desktop\\test\\";
				
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Save downloaded videos to ..");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			SAVE_PATH = chooser.getSelectedFile().getAbsolutePath() + "\\";
			System.out.println("Downloading talks to " + SAVE_PATH);
		} else {
		  System.out.println("No path selected. Terminating.");
		  return;
		}
		
		try {			
			batchDlTalks(fetchTalksData(getHTMLPageSource(PAGE_URL)), downloadQuality.QUALITY_HIGH, SAVE_PATH, true);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
