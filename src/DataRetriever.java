import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class implements static functions that gets data from the Ile-de-France
 * Mobilités. This is mainly HTML requests with HTTPS and CSRF support, and JSON
 * for query parsing. The API is a web API ; the description can be found on
 * Ile-de-France Mobilités's website. The user has to perform a HTTPS GET query
 * on a given URL, after authenticating through a CSRF cookie and an API key.
 * This program requires you registering to Île-de-France Mobilités website in
 * order to get an API key. This is free, yet you must register to the OpenData
 * program.
 * 
 * @author Quentin
 */
public class DataRetriever {

	/**
	 * Minimal working example
	 * 
	 * @param args
	 *            The first argument must be a valid API key.
	 */
	public static void main(String[] args) {

		// This program requires you registering to Île-de-France Mobilités website in
		// order to get an API key. This is free, yet you must register to the OpenData
		// program.
		// I cannot disclose my key for obvious reasons
		String my_api_key = args[0];

		// Set API Key (static variable that will be used by all methods)
		DataRetriever.setAPIKey(my_api_key);

		List<Departure> testList;

		// List next departures for line M5 at stop Gare du Nord
		System.out.println("Line M5, stop Gare du Nord: ");
		try {
			testList = DataRetriever.getDeparturesLineAtStop("100110005:5", "StopPoint:59270");
			for (Departure d : testList) {
				System.out.println(d);
			}
		} catch (DataRetriever.UnauthorizedException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

		// Typical result:
		// Place d'Italie A quai
		// Place d'Italie 7
		// Place d'Italie 15
		// Place d'Italie 21
		// Bobigny Pablo Picasso A quai
		// Bobigny Pablo Picasso 8
		// Bobigny Pablo Picasso 14
		// Bobigny Pablo Picasso 20

		// List next departures for line 38 at stop Auguste Comte
		System.out.println("\nLine 38, stop Auguste Comte: ");
		try {
			testList = DataRetriever.getDeparturesLineAtStop("100100038:38", "59:3764622");
			for (Departure d : testList) {
				System.out.println(d);
			}
		} catch (DataRetriever.UnauthorizedException e) {
			e.printStackTrace();
			;
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

		// Typical result:
		// Porte d'Orleans 5
		// Gare du Nord 13
		// Porte d'Orleans 15
		// Gare du Nord 29

	}

	/*
	 * Private variables
	 */

	private static String m_apiKey = null;
	private static String m_csrfToken = null;
	private static final String m_csrfURL = "https://api-lab-trone-stif.opendata.stif.info/service";
	private static List<String> m_cookies;

	/**
	 * Private constructor
	 */
	private DataRetriever() {

	}

	/**
	 * Need to provide an API key (access code linked to an Ile-de-France Mobilités
	 * Open Data account).
	 * 
	 * @param _apiKey
	 */
	public static void setAPIKey(String _apiKey) {
		m_apiKey = _apiKey;

		// Fetch CSRF token
		_refreshCSRFToken();
	}

	/**
	 * Given a line ID and a stop ID, get the next times of the line at the stop
	 * according to the data of the API.
	 * 
	 * @param _lineId
	 * @param _stopID
	 * @return
	 * @throws UnauthorizedException
	 * @throws NotFoundException 
	 */
	public static List<Departure> getDeparturesLineAtStop(String _lineId, String _stopID) throws UnauthorizedException, NotFoundException {

		if (m_apiKey == null) {
			throw new UnauthorizedException("You must first set a valid API key using DataRetriever.setAPIKey");
		}

		// Prepare API query
		String _urlString = "https://api-lab-trone-stif.opendata.stif.info/service/tr-vianavigo/departures?" + "apikey="
				+ m_apiKey + "&" + "line_id=" + _lineId + "&" + "stop_point_id=" + _stopID;
		URL url = null;
		try {
			url = new URL(_urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// Get query answer
		String _queryAnswer = _executeGetRequest(url);

		// Debug
		// System.err.println("Raw answer:\n" + _queryAnswer);

		// Build list of departures
		List<Departure> _departures = new ArrayList<Departure>();

		// Format to correct JSON
		String _testString = "{\"item\" : " + _queryAnswer + "}";
		JSONObject obj = new JSONObject(_testString);

		// Parse items of JSON answer
		JSONArray arr = obj.getJSONArray("item");
		for (int i = 0; i < arr.length(); i++) {
			// If duration, call duration constructor
			if (arr.getJSONObject(i).getString("code").equals("duration")) {
				_departures.add(new Departure(arr.getJSONObject(i).getString("lineDirection"),
						Integer.parseInt(arr.getJSONObject(i).getString("time"))));
				// Else if message call message constructor
			} else {
				_departures.add(new Departure(arr.getJSONObject(i).getString("lineDirection"),
						arr.getJSONObject(i).getString("schedule")));
			}
		}
		
		return _departures;
	}

	/*
	 * PRIVATE METHODS
	 * 
	 */

	public static class UnauthorizedException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UnauthorizedException(String _message) {
			super(_message);
		}

	}
	
	
	public static class NotFoundException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NotFoundException(String _message) {
			super(_message);
		}

	}

	
	/**
	 * Execute a HTTP/1.1 GET method using the provided URL, with the current CSRF
	 * token.
	 * 
	 * @param url
	 * @return
	 * @throws UnauthorizedException
	 * @throws NotFoundException 
	 */
	private static String _executeGetRequest(URL url) throws UnauthorizedException, NotFoundException {
		HttpsURLConnection connection = null;
		StringBuffer response = new StringBuffer();

		try {
			// Print query
			System.err.println("Query: " + url);

			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			// Include referrer site
			connection.setRequestProperty("Referer", m_csrfURL);
			
			// Accept gzip encoding
			connection.setRequestProperty("Accept-encoding", "gzip, deflate");
			connection.setRequestProperty("Accept-charset", "utf-8");

			// Include CSRF Token
			connection.setRequestProperty("X-CSRFToken", m_csrfToken);

			// Get answer
			System.err.println("Answer: " + connection.getHeaderFields());

			// Read answer data
			InputStream is = null;
			if (HttpsURLConnection.HTTP_OK == connection.getResponseCode()) {
				is = connection.getInputStream();
			} else if (HttpsURLConnection.HTTP_UNAUTHORIZED == connection.getResponseCode()) {
				throw new UnauthorizedException("The server returned 401 Unauthorized. Maybe your API key is invalid?");
			} else if (HttpsURLConnection.HTTP_NOT_FOUND == connection.getResponseCode()) {
				throw new NotFoundException("The server returned 404 Not Found. The distant server might experience issues.");
			} else {
				is = connection.getErrorStream();
			}
			
			// Handle gzip encoding
			BufferedReader rd = null;
			if (connection.getHeaderField("Content-Encoding")!=null && connection.getHeaderField("Content-Encoding").equals("gzip")) {
				rd = new BufferedReader(new InputStreamReader(new GZIPInputStream(is)));
			} else {
				rd = new BufferedReader(new InputStreamReader(is));
			}
			
			// Read answer line by line
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return response.toString();
	}

	/**
	 * Gets a CSRF Token from Île-de-France Mobilités website.
	 */
	private static void _refreshCSRFToken() {
		HttpsURLConnection connection = null;

		try {
			System.err.println("Fetching CSRF at: " + m_csrfURL);

			connection = (HttpsURLConnection) (new URL(m_csrfURL)).openConnection();
			connection.setRequestMethod("POST");

			// Include referrer site
			connection.setRequestProperty("Referer", m_csrfURL);

			// Accept gzip encoding
			connection.setRequestProperty("Accept-encoding", "gzip, deflate");
			connection.setRequestProperty("Accept-charset", "utf-8");

			// Get Response
			System.err.println("Answer: " + connection.getHeaderFields());

			// Get cookies
			m_cookies = connection.getHeaderFields().get("Set-Cookie");
			
			// Get CSRF token
			m_csrfToken = ((m_cookies.get(0)).split(";")[0]).split("=", 2)[1];
			System.err.println("Detected token: " + m_csrfToken);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
