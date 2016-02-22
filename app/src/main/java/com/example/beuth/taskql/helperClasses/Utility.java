package com.example.beuth.taskql.helperClasses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.beuth.taskql.activities.LoginActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class methods
 * @author Wael Gabsi, Stefan Völkel
 */
public class Utility {
	private static Pattern pattern;
	private static Matcher matcher;
	private static Toast toast;
	//Email Pattern
	private static final String EMAIL_PATTERN =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	/**
	 * Validate Email with regular expression
	 * 
	 * @param email
	 * @return true for Valid Email and false for Invalid Email
	 */
	public static boolean validate(String email) {
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(email);
		return matcher.matches();
	}

	/**
	 * Checks for Null String object
	 * 
	 * @param txt
	 * @return true for not null and false for null String object
	 */
	public static boolean isNotNull(String txt){
		return txt!=null && txt.trim().length()>0 ? true: false;
	}

	/**
	 * Method which navigates to Login Activity
	 */
	public static void navigateToLoginActivity(Context context, Activity currentActivity){
		Intent homeIntent = new Intent(context, LoginActivity.class);
		currentActivity.startActivity(homeIntent);
		currentActivity.finish();
	}

	/**
	 * Display toast message only once
	 */
	public static void displayToast(Context context, String message){
		if(toast != null) {
			toast.cancel();
		}
		toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
