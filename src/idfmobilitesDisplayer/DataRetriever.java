package idfmobilitesDisplayer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * This class implements a function that gets data from the Ile-de-France
 * Mobilit�s API
 * 
 * @author Quentin
 *
 */
public class DataRetriever {
	
	private String m_apiKey;
	private String m_csrfToken;
	private final String m_csrfURL = "https://api-lab-trone-stif.opendata.stif.info/service";
	private List<String> m_cookies;
	
	public DataRetriever(String _apiKey) {
		m_apiKey = _apiKey;
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		
		// Fetch CSRF token
		RefreshCSRFToken();
	}
	
	public String GetNextLineAtStop(String _lineId, String _stopID) {
		
		String _urlString = "https://api-lab-trone-stif.opendata.stif.info/service/tr-vianavigo/departures?"
				+ "apikey=" + m_apiKey + "&"
				+ "line_id=" + _lineId + "&" 
				+ "stop_point_id=" + _stopID;
		URL url = null;
		try {
			url = new URL(_urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ExecuteGetRequest(url);
	}
	
	private String ExecuteGetRequest(URL url) {
		HttpsURLConnection connection = null;
		StringBuffer response = new StringBuffer();

		try {
			// Print query
			System.out.println("Query: " + url);
			
		    connection = (HttpsURLConnection) url.openConnection();
		    connection.setRequestMethod("GET");
		    
		    // Include Referer
		    connection.setRequestProperty("Referer", "https://api-lab-trone-stif.opendata.stif.info/departures");
		    
		    // Include CSRF Token
		    connection.setRequestProperty("X-CSRFToken", m_csrfToken);

			// Get Response
		    System.out.println("Answer: " + connection.getHeaderFields());
		    
		    InputStream is = null;
		    if (HttpsURLConnection.HTTP_OK == connection.getResponseCode()) {
		    	is = connection.getInputStream();
		    } else {
		    	is = connection.getErrorStream();
		    }
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return response.toString();
	}
	
	
	private void RefreshCSRFToken() {
		HttpsURLConnection connection = null;
		StringBuffer response = new StringBuffer();

		try {
			System.out.println("Fetching CSRF at: " + m_csrfURL);
			
		    connection = (HttpsURLConnection) (new URL(m_csrfURL)).openConnection();
		    connection.setRequestMethod("POST");
		    connection.setRequestProperty("Referer", "https://api-lab-trone-stif.opendata.stif.info/");
		    connection.setRequestProperty("Accept-encoding", "gzip, deflate");
		    connection.setRequestProperty("Accept-charset", "utf-8");
		    
			// Get Response
		    System.out.println("Answer: " + connection.getHeaderFields());
		    
		    // Get cookies
		    m_cookies = connection.getHeaderFields().get("Set-Cookie");
		    m_csrfToken = ((m_cookies.get(0)).split(";")[0]).split("=", 2)[1];
		    System.out.println("Detected token: " + m_csrfToken);
		    
		    
		    InputStream is = null;
		    if (HttpsURLConnection.HTTP_OK == connection.getResponseCode()) {
		    	is = connection.getInputStream();
		    } else {
		    	is = connection.getErrorStream();
		    }
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}	
}
