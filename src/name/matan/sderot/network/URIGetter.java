package name.matan.sderot.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Gets the contents of a URI. Requires "android.permission.INTERNET" user
 * permission on AndroidManifest.xml
 * @author adamatan
 * Credit: http://stackoverflow.com/questions/3505930/make-an-http-request-with-android
 */
public class URIGetter {
	
	public static String ERROR_MESSAGE="No inernet connection";
	
	public static String getURI(String uri) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		String responseString = null;
		try {
			response = httpclient.execute(new HttpGet(uri));
			StatusLine statusLine = response.getStatusLine();
			if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
			} else{
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			responseString=ERROR_MESSAGE;
		} catch (IOException e) {
			responseString=ERROR_MESSAGE;
		}
		return responseString;
	}
}
