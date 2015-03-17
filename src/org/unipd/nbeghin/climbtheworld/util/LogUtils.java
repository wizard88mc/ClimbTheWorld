package org.unipd.nbeghin.climbtheworld.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;

public final class LogUtils {

	private LogUtils() {
	}


	public static void initGameLog(Context context, String username) {

		String log_file_name = "";
		int log_file_id = PreferenceManager
				.getDefaultSharedPreferences(context).getInt("log_game_file_id", -1);

		if (log_file_id == -1) {
			log_file_name = "game_log";
		} else {
			log_file_name = "game_log_" + log_file_id;
		}

		final File logFile = new File(context.getDir("climbTheWorld_dir",
				Context.MODE_PRIVATE), log_file_name);

		try {
			if (!logFile.exists()) {
				Log.e(MainActivity.AppName, "Log file not exists");
				logFile.createNewFile();
			}

			// 'true' per aggiungere il testo al file esistente
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));

			buf.append("LOG GAME");
			buf.newLine();
			buf.newLine();
			buf.append("USER " + username);
			buf.newLine();

			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void writeGameUpdate(Context context, String text) {

		String log_file_name = "";
		int log_file_id = PreferenceManager
				.getDefaultSharedPreferences(context).getInt("log_game_file_id", -1);

		if (log_file_id == -1) {
			log_file_name = "game_log";
		} else {
			log_file_name = "game_log" + log_file_id;
		}

		final File logFile = new File(context.getDir("climbTheWorld_dir",
				Context.MODE_PRIVATE), log_file_name);

		try {
			if (!logFile.exists()) {
				Log.e(MainActivity.AppName, "Log file not exists");
				logFile.createNewFile();
			}

			// 'true' per aggiungere il testo al file esistente
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}