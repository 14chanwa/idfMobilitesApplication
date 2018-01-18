import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
	 */
	@SuppressWarnings("unused")
	public static void main(String args[]) {

		// This program requires you registering to Île-de-France Mobilités website in
		// order to get
		// an API key. This is free, yet you must register to the OpenData program.
		// I cannot disclose my key for obvious reasons
		String my_api_key = null;
		if (my_api_key == null) {
			System.err.println("You must first get a valid API key... sorry!");
			return;
		}

		DataRetriever.setAPIKey(my_api_key);

		List<Departure> testList;

		// List next departures for line M5 at stop Gare du Nord
		System.out.println("Line M5, stop Gare du Nord: ");
		testList = DataRetriever.getDeparturesLineAtStop("100110005:5", "StopPoint:59270");
		for (Departure d : testList) {
			System.out.println(d);
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
		testList = DataRetriever.getDeparturesLineAtStop("100100038:38", "59:3764622");
		for (Departure d : testList) {
			System.out.println(d);
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

	private static String m_apiKey;
	private static String m_csrfToken;
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
	 */
	public static List<Departure> getDeparturesLineAtStop(String _lineId, String _stopID) {

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
		// System.out.println("Raw answer:\n" + _queryAnswer);

		// Build list of departures
		List<Departure> _departures = new ArrayList<Departure>();

		// Format to correct JSON
		String _testString = "{\"item\" : " + _queryAnswer + "}";
		JSONObject obj = new JSONObject(_testString);

		// Parse items of list
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

	/**
	 * Execute a HTTP/1.1 GET method using the provided URL, with the current CSRF
	 * token.
	 * 
	 * @param url
	 * @return
	 */
	private static String _executeGetRequest(URL url) {
		HttpsURLConnection connection = null;
		StringBuffer response = new StringBuffer();

		try {
			// Print query
			System.out.println("Query: " + url);

			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			// Include referrer site
			connection.setRequestProperty("Referer", m_csrfURL);

			// Include CSRF Token
			connection.setRequestProperty("X-CSRFToken", m_csrfToken);

			// Get answer
			System.out.println("Answer: " + connection.getHeaderFields());

			// Read answer data
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

	/**
	 * Gets a CSRF Token from Île-de-France Mobilités website.
	 */
	private static void _refreshCSRFToken() {
		HttpsURLConnection connection = null;
		StringBuffer response = new StringBuffer();

		try {
			System.out.println("Fetching CSRF at: " + m_csrfURL);

			connection = (HttpsURLConnection) (new URL(m_csrfURL)).openConnection();
			connection.setRequestMethod("POST");

			// Include referrer site
			connection.setRequestProperty("Referer", m_csrfURL);

			connection.setRequestProperty("Accept-encoding", "gzip, deflate");
			connection.setRequestProperty("Accept-charset", "utf-8");

			// Get Response
			System.out.println("Answer: " + connection.getHeaderFields());

			// Get cookies
			m_cookies = connection.getHeaderFields().get("Set-Cookie");
			m_csrfToken = ((m_cookies.get(0)).split(";")[0]).split("=", 2)[1];
			System.out.println("Detected token: " + m_csrfToken);

			// Read answer data
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
