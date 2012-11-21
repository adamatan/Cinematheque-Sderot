package name.matan.sderot.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.util.Log;

public class DateUtils {
	
	private static HashMap<Integer, String> daysOfWeekHebrew = new HashMap<Integer, String>();
	static {
		daysOfWeekHebrew.put(1, "יום א'");
		daysOfWeekHebrew.put(2, "יום ב'");
		daysOfWeekHebrew.put(3, "יום ג'");
		daysOfWeekHebrew.put(4, "יום ד'");
		daysOfWeekHebrew.put(5, "יום ה'");
		daysOfWeekHebrew.put(6, "יום ו'");
		daysOfWeekHebrew.put(7, "יום שבת");
	}
	
	public static String dateToStringWithDayName(int daysFromNow) {
		Date d = datePlusDays(daysFromNow);
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		return daysOfWeekHebrew.get(dayOfWeek)+", "+dateToString(daysFromNow);
	}
	
	public static Date datePlusDays(int daysFromNow) {
		Date dt = new Date();
		Calendar c = Calendar.getInstance(); 
		c.setTime(dt); 
		c.add(Calendar.DATE, daysFromNow);
		dt = c.getTime();
		return dt;
	}
	
	public static String dateToString(int daysFromNow) {
		Date dt = datePlusDays(daysFromNow);
		SimpleDateFormat s = new SimpleDateFormat("d/MM/yyyy");
		return s.format(dt);
	}
}
