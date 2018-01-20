import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

public class DepartureViewer extends JFrame {

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
		DataRetriever.setAPIKey(args[0]);

		// Create a new DepartureViewer with some line and stop and a refresh time of 5
		// seconds.
		new DepartureViewer("100110005:5", "StopPoint:59270", 5, TimeUnit.SECONDS);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String m_lineId;
	protected String m_stopId;

	/**
	 * Constructor. Initializes variables and start refresh loop.
	 * 
	 * @param _lineId
	 * @param _stopID
	 * @param _refreshInterval
	 */
	public DepartureViewer(String _lineId, String _stopID, int _refreshInterval, TimeUnit _timeUnit) {
		m_lineId = _lineId;
		m_stopId = _stopID;

		// Build frame
		_buildFrame();

		// Start refresh loop
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					List<Departure> _departureList = DataRetriever.getDeparturesLineAtStop(m_lineId, m_stopId);
					System.out.println(_departureList);
					_refreshFrame(_departureList);
				} catch (DataRetriever.UnauthorizedException e) {
					e.printStackTrace();
				}
			}
		}, 0, _refreshInterval, _timeUnit);
	}

	/**
	 * Constructor. Assumes the TimeUnit is TimeUnit.SECONDS.
	 * 
	 * @param _lineId
	 * @param _stopID
	 * @param _refreshInterval
	 */
	public DepartureViewer(String _lineId, String _stopID, int _refreshInterval) {
		this(_lineId, _stopID, _refreshInterval, TimeUnit.SECONDS);
	}

	/**
	 * Builds frame content
	 */
	private void _buildFrame() {
		// TODO _buildFrame
	}

	/**
	 * Refreshes frame content
	 */
	private void _refreshFrame(List<Departure> _departureList) {
		// TODO _refreshFrame
	}
}
