package name.matan.sderot.films;

import name.matan.sderot.util.DateUtils;

public class Utils {
	public static String getUriByDate(int daysFromNow) {
		//			 "http://www.sderot-cin.org.il/cgi-webaxy/sal/sal.pl?ID=839468_cinematheque&act=search2&dbResOptions=hideResTabTitle=0&getValueAJAX=dataid,f1,f2,f3,f4&dbid=shows2009&query=sort%3CD%3Ef3%3CD%3Esort2%3CD%3Ef4%3CD%3Ef2%3CD%3E8/11/2012%3CD%3Ef2%3CD%3E8/11/2012";
		String uri = "http://www.sderot-cin.org.il/cgi-webaxy/sal/sal.pl?ID=839468_cinematheque&act=search2&dbResOptions=hideResTabTitle=0&getValueAJAX=dataid,f1,f2,f3,f4&dbid=shows2009&query=sort%3CD%3Ef3%3CD%3Esort2%3CD%3Ef4%3CD%3Ef2%3CD%3E"+DateUtils.dateToString(daysFromNow)+"%3CD%3Ef2%3CD%3E"+DateUtils.dateToString(daysFromNow);
		return uri;
	}
}
