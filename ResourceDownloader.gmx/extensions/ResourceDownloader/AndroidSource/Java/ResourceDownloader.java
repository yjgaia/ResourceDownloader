package ${YYAndroidPackageName};

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.UUID;

import com.yoyogames.runner.RunnerJNILib;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Dialog;
import android.view.MotionEvent;

import android.Manifest;
import android.os.Build;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.media.MediaMetadataRetriever;

public class ResourceDownloader {
	private static final int EVENT_OTHER_SOCIAL = 70;

	private String folderPath;
	private String path;

	private String tag;
	private String url;

	public double rd_init(String tag, String url) {
		this.tag = tag;
		this.url = url;

		if (Build.VERSION.SDK_INT >= 23 && RunnerActivity.CurrentActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(RunnerActivity.CurrentActivity, new String[]{
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			}, 1);
			return 0;
		}

		return 1;
	}

	public double rd_download(String filename) {
		
		String folderPath = Environment.getExternalStorageDirectory() + "/" + tag + "/resource/";
		String path = folderPath + filename;

		boolean isValid = true;

		if (new File(path).exists() != true) {
			isValid = false;
		} else {
			try {
				MediaMetadataRetriever retriever = new MediaMetadataRetriever();
				retriever.setDataSource(path);
				retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			} catch (Exception e) {
				isValid = false;
			}
		}

		if (isValid == true) {
			int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
			RunnerJNILib.DsMapAddString(dsMapIndex, "type", "__RESOURCE_READY");
			RunnerJNILib.DsMapAddString(dsMapIndex, "filename", filename);
			RunnerJNILib.DsMapAddString(dsMapIndex, "path", path);
			RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
		}

		else {
			File folder = new File(folderPath);
			if (folder.exists() != true) {
				folder.mkdirs();
			}
			new DownloadTask().execute(url + "/resource/" + filename, filename);
		}

		return -1;
	}

	private class DownloadTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... args) {
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {
				connection = (HttpURLConnection) new URL(args[0]).openConnection();
				connection.connect();

				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

					String path = Environment.getExternalStorageDirectory() + "/" + tag + "/resource/" + args[1];

					input = connection.getInputStream();
					output = new FileOutputStream(path);

					byte data[] = new byte[4096];
					int count;
					while ((count = input.read(data)) != -1) {
						if (isCancelled() == true) {
							input.close();
							return null;
						}
						output.write(data, 0, count);
					}

					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "__RESOURCE_READY");
					RunnerJNILib.DsMapAddString(dsMapIndex, "filename", args[1]);
					RunnerJNILib.DsMapAddString(dsMapIndex, "path", path);
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (output != null) {
						output.close();
					}
					if (input != null) {
						input.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (connection != null) {
					connection.disconnect();
				}
			}
			return null;
		}
	}
}
