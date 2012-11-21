package name.matan.sderot.films;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * Parses the AJAX call that returns the movie list.
 * @author adamatan
 */
public class ScreeningListParser {
	public static List<Screening> parse(String responseString, Context ctx) {
		List<Screening> movies = new ArrayList<Screening>();
		List<String> ajaxResponseFieldsWithTitles = Arrays.asList(responseString.split("<D>"));
		List<String> ajaxResponseFields= ajaxResponseFieldsWithTitles.subList(6, ajaxResponseFieldsWithTitles.size());
		
		int numberOfMovies = ajaxResponseFields.size()/6;
		for (int i=0; i<numberOfMovies; i++) {
			movies.add(new Screening(ajaxResponseFields.subList(i*6, (i+1)*6), ctx));
		}
		
		return movies;
	}
}
