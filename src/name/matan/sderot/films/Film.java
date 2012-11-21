package name.matan.sderot.films;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import android.util.Log;

/**
 * Film data (shared by multiple screening events). 
 * @author adamatan
 */

public class Film {
	private String httpResponse;
	private String duration; 
	private String year;
	private String language;
	private String country;
	private String description="";
	private String imageUrl="";
	
	public String getImageUrl() {
		return imageUrl;
	}

	private Map<String, String> details = new TreeMap<String, String>();
	
	public Map<String, String> getDetails() {
		return details;
	}

	public String getCountry() {
		return country;
	}

	public Film(String httpResponse) {
		this.httpResponse=httpResponse;
		this.duration 	= getField(".*<td id=\"f31\">(.*?)</td>.*", 	httpResponse);
		this.year		= getField(".*<td id=\"f17\">(.*?)</td>.*", 	httpResponse);
		this.language	= getField(".*<td id=\"f15\">(.*?)</td>.*", 	httpResponse);
		this.country	= getField(".*<td id=\"f32\">(.*?)</td>.*", 	httpResponse);
		this.imageUrl	= Jsoup.parse(httpResponse).select("div#show_imgDiv0_f18").select("img").attr("src");
		
		String regex = "<b>(.*?):</b></td>\\s*<td id=\"(f\\d+)\">(.*?)</td>";
		
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE|Pattern.DOTALL);
		Matcher matcher = pattern.matcher(httpResponse);
		
		while (matcher.find()) {
			this.details.put(matcher.group(1), matcher.group(3));
		}
		
		//String regex2 = "lang=\"HE\">(.*?)</span>";
		String descriptionRegex = "<div id=\"f2\">(.*?)</div>";
		Pattern pattern2 = Pattern.compile(descriptionRegex, Pattern.MULTILINE|Pattern.DOTALL);
		Matcher matcher2 = pattern2.matcher(httpResponse);
		if (matcher2.find()) {
			System.out.println("Description found:");
			String match = matcher2.group(1);
			//System.out.println(match);
			this.description+=(match.replace("\n", " "));
			this.description = Jsoup.parse(this.description).text();
		}
	}
	
	public String getDescription() {
		return description;
	}

	public String getLanguage() {
		return language;
	}

	private String getField(String regex, String httpResponse) {
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(httpResponse);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public String getDuration() {
		return this.duration;
	}
	
	public String getYear() {
		return this.year;
	}
}
