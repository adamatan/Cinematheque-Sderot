package name.matan.sderot.films;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import name.matan.sderot.MainActivity;
import name.matan.sderot.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A film screening event.  
 * @author adamatan
 */
public class Screening {
	private int    filmId; 
	private String name;
	private String theater;
	private String screeningDate;
	private String screeningTime;
	
	private Context		 	layoutContext;
	private LinearLayout 	screeningLayout;
	private LinearLayout 	filmDataLayout;
	private LinearLayout 	screeningTitleContainer; 
	private TextView		filmName;
	private TextView 		duration;
	private TextView 		year;
	

	private String filmUri;
	private Film   film;
	private static String filmURITemplate = "http://www.sderot-cin.org.il/cgi-webaxy/sal/sal.pl?lang=en&ID=839468_cinematheque&act=show&dbid=movies2009&dataid=";
	/*
	 * 0	["6767",
	 * 1	"2153",
	 * 2	"סלסט וג'סי לנצח",
	 * 3	"8/11/2012",
	 * 4	"21:30",
	 * 5	"אולם 1"]
	 */
	public Screening(List<String> data, Context context) {
		this.layoutContext	= context;
		this.name			= data.get(2);
		Log.i("Screening", String.format("Initialized screening with film name \"%s\"", name));
		this.screeningDate	= data.get(3);
		this.screeningTime	= data.get(4);
		this.theater		= data.get(5);

		this.filmId			= Integer.parseInt(data.get(1));
		this.filmUri		= filmURITemplate+String.format("%d", this.filmId);
		
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);

		this.screeningLayout = new LinearLayout(this.layoutContext);
		this.screeningLayout.setGravity(Gravity.RIGHT);
		this.screeningLayout.setOrientation(LinearLayout.VERTICAL);

		this.filmName = new TextView(this.layoutContext);
		this.filmName.setBackgroundColor(Color.WHITE);
		this.filmName.setText(this.screeningTime+" "+this.name);
		this.filmName.setTextSize(20);
		this.filmName.setTextColor(Color.rgb(200, 200, 200));
		this.filmName.setLayoutParams(params);

		this.screeningTitleContainer = new LinearLayout(this.layoutContext);
		this.screeningTitleContainer.setLayoutParams(params);
		this.screeningTitleContainer.addView(filmName);
		
		ProgressBar pb = new ProgressBar(this.layoutContext);
		this.screeningTitleContainer.addView(pb);
		
		this.screeningLayout.addView(screeningTitleContainer);

		this.filmDataLayout = new LinearLayout(this.layoutContext);
		this.filmDataLayout.setOrientation(LinearLayout.VERTICAL);
		this.filmDataLayout.setGravity(Gravity.RIGHT);
		this.filmDataLayout.setLayoutParams(params);
		this.filmDataLayout.setVisibility(View.GONE);

		this.screeningTitleContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(name, String.format("%d %d", filmDataLayout.getVisibility(), View.GONE));
				if (filmDataLayout.getVisibility()==View.GONE) {
					Log.i(name, "Setting visible");
					filmDataLayout.setVisibility(View.VISIBLE);
				} else {
					Log.i(name, "Setting invisible");
					filmDataLayout.setVisibility(View.GONE);
				}
			}
		});
		this.screeningLayout.addView(filmDataLayout);
		
		Button shareButton = new Button(this.layoutContext);
		shareButton.setText("שתף סרט");
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				share("שתף סרט", "אני רוצה לשתף אותך בסרט", "בא לך על טינקרבל בשמונה ורבע?");
			}
		});
		this.filmDataLayout.addView(shareButton);
	}
	
	public void setFilm(Film f) {
		if (f==null) {
			Log.i("Screening", "Got null film id");
			return;
		}
		else if  (this.film!=null) {
			Log.i("Screening", "Already associated with a film.");
			return;
		}
		this.film=f;
		this.filmName.setTextColor(Color.BLACK);
		this.screeningTitleContainer.removeViewAt(1);
		
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
			new GetFilmImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, f.getImageUrl());
		}
		else {
			new GetFilmImageTask().execute(f.getImageUrl());
		}

		
		Map<String, String> details = f.getDetails();
		for (String title:details.keySet()) {
			addDetails(title, details.get(title));
		}
		
		TextView descriptionView = new TextView(this.layoutContext);
		descriptionView.setText(f.getDescription());
		filmDataLayout.addView(descriptionView);
	}
	
	private void setFilmImage(Bitmap bmp) {
		ImageView imageView = new ImageView(this.layoutContext);
		this.filmDataLayout.addView(imageView);
		imageView.setImageBitmap(bmp);
	}

	
	public void share(String title, String subject, String body) {
    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        this.layoutContext.startActivity(Intent.createChooser(sharingIntent, title)); 
	}
	
	
	private class GetFilmImageTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... imageUrls) {
			String urlString = imageUrls[0];
			URL url;
			try {
				url = new URL(urlString);
			} catch (MalformedURLException e) {
				Log.w("Screening", "Could not load image url "+urlString+", (MalformedURLException)");
				return null;
			}
			Bitmap bmp=null;
			try {
				bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				return bmp;
			} catch (IOException e) {
				return null;
			}
		}
		
		protected void onPostExecute(Bitmap bmp) {
			setFilmImage(bmp);
		}
		
	}
	
	private void addDetails(String title, String text) {
		LinearLayout detailLayout = new LinearLayout(this.layoutContext);
		detailLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		detailLayout.setOrientation(LinearLayout.HORIZONTAL);
		detailLayout.setGravity(Gravity.RIGHT);
		detailLayout.setPadding(10, 0, 10, 0);
		TextView titleView = new TextView(this.layoutContext);
		titleView.setPadding(0, 0, 20, 0);
		titleView.setText(title);
		titleView.setTypeface(null, Typeface.BOLD);
		detailLayout.addView(titleView);
		TextView tv = new TextView(this.layoutContext);
		if (text!=null) {
			tv.setText(text);
		}
		else {
			tv.setText("אין מידע");
		}
		detailLayout.addView(tv);
		this.filmDataLayout.addView(detailLayout);
	}

	public TextView getDuration() {
		return duration;
	}
	
	public String getFilmUri() {
		return filmUri;
	}

	public LinearLayout getFilmLayout() {
		return this.screeningLayout;
	}
	
	public String getName() {
		return name;
	}

	public int getFilmId() {
		return filmId;
	}

	public String getTheater() {
		return theater;
	}

	public String getScreeningDate() {
		return screeningDate;
	}

	public String getScreeningTime() {
		return screeningTime;
	}
}
