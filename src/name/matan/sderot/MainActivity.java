package name.matan.sderot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import name.matan.sderot.films.Film;
import name.matan.sderot.films.Screening;
import name.matan.sderot.films.ScreeningListParser;
import name.matan.sderot.films.Utils;
import name.matan.sderot.network.URIGetter;
import name.matan.sderot.util.DateUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	private int NUMBER_OF_DAYS_TO_VIEW = 5;
	private final BlockingQueue<Screening> screeningsToQueryForFullData=new LinkedBlockingQueue<Screening>();
	private final List<Screening> screenings = Collections.synchronizedList(new ArrayList<Screening>());
	private final Map<String, Film> films = Collections.synchronizedMap(new HashMap<String, Film>());
	private Handler handler;
	TextView ids;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LinearLayout entirePage = (LinearLayout) findViewById(R.id.entire_page_container);
		//ids = new TextView(this);
		//ids.setText("Ids: ");
		//entirePage.addView(ids, 1);

		this.handler=new Handler();

		LinearLayout daysOfWeek = (LinearLayout) findViewById(R.id.DaysOfWeekLAyout);
		for (int i=0; i<NUMBER_OF_DAYS_TO_VIEW; i++) {
			LinearLayout day = createDayLayoutWithSpinner(i);
			daysOfWeek.addView(day);
		}
		for (int i=0; i<NUMBER_OF_DAYS_TO_VIEW; i++){
			getDailyMoviesConcurrently(i);
		}
		updateFilmsWithFullData();
	}

	private class GetFilmTask extends AsyncTask<String, Void, Film> {

		@Override
		protected Film doInBackground(String... params) {
			String uri = params[0];
			Log.i("Getting film info", uri);
			if (films.containsKey(uri)) {
				return films.get(uri);
			}
			else {
				films.put(uri, null);
				String response = URIGetter.getURI(uri);
				Film film = new Film(response);
				films.put(uri, film);
				return film;
			}
		}

		@Override
		protected void onPostExecute(Film f) {
			if (f!=null) {
				//ids.setText(ids.getText()+" "+f.getDuration());
				for (Screening s:screenings) {
					String uri = s.getFilmUri();
					if (films.containsKey(uri) && f==films.get(uri)) {
						//s.getDuration().setText(f.getLength());
						s.setFilm(f);
					}
				}
			}
		}
	}


	private void updateFilmsWithFullData() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				final List<Screening> newScreenings = new ArrayList<Screening>();
				while (true) {
					Log.i("updateFilmsWithFullData", "Starting loop");
					try {
						Thread.sleep(200);
						newScreenings.add(screeningsToQueryForFullData.take());
						screeningsToQueryForFullData.drainTo(newScreenings);

						for (Screening s:newScreenings) {
							if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
								new GetFilmTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s.getFilmUri());
							}
							else {
								new GetFilmTask().execute(s.getFilmUri());
							}
						}
						newScreenings.clear();

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					handler.post(new Runnable() {
						@Override
						public void run() {
						}
					});
				}
			}
		};
		new Thread(runnable).start();
	}

	private LinearLayout createDayLayoutWithSpinner(int i) {
		LinearLayout day = new LinearLayout(MainActivity.this);
		day.setOrientation(LinearLayout.VERTICAL);
		day.setGravity(Gravity.CENTER_VERTICAL);
		TextView title = new TextView(MainActivity.this);
		title.setBackgroundColor(Color.rgb(204, 230, 255));
		title.setGravity(Gravity.CENTER);
		title.setText(DateUtils.dateToStringWithDayName(i));
		title.setTextSize(16);
		day.addView(title);
		ProgressBar pb = new ProgressBar(this);
		day.addView(pb);
		return day;
	}

	/**
	 * http://stackoverflow.com/questions/13329537/android-http-requests-in-asynctask-are-not-concurrent
	 * @param daysFromNow How many days forward to query (0 is today)
	 */
	private void getDailyMoviesConcurrently(int daysFromNow) {
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
			new GetMoviesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, daysFromNow);
		}
		else {
			new GetMoviesTask().execute(daysFromNow);
		}
	}

	private class GetMoviesTask extends AsyncTask<Integer, Void, List<Screening>> {
		private int dayId;
		@Override
		protected List<Screening> doInBackground(Integer... i) {
			this.dayId=i[0];
			String uri = Utils.getUriByDate(dayId);
			Log.i("GetMoviesTask", String.format("Calling HTTP for day %d", dayId));
			List<Screening> movies = ScreeningListParser.parse(URIGetter.getURI(uri), MainActivity.this);
			Log.i("GetMoviesTask", String.format("Finished calling HTTP for day %d", dayId));
			return movies;
		}

		@Override
		protected void onPostExecute(List<Screening> screenings) {
			LinearLayout linearLayout = (LinearLayout) ((LinearLayout) (findViewById(R.id.DaysOfWeekLAyout))).getChildAt(this.dayId);
			linearLayout.removeViewAt(1);
			for (Screening film: screenings) {
				linearLayout.addView(film.getFilmLayout());
				addFilmIdToQueue(film);
			}
		}

		private void addFilmIdToQueue(Screening screening) {
			try {
				screenings.add(screening);
				screeningsToQueryForFullData.put(screening);
				String msg = "";
				for (Object o : screeningsToQueryForFullData.toArray()) {
					msg+=o;
					msg+=", ";
				}
				Log.i("Movie Ids queue", msg);
			} catch (InterruptedException e) {
				Log.e("Can not add movie id to queue", 
						String.format("Name:\"%s\",  id:%d", screening.getName(), screening.getFilmId()));
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
