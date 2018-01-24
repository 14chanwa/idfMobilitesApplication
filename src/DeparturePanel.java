import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * This class implements an information panel using data from Île-de-France
 * Mobilités's API to display the next departures for a given line at a given
 * stop on a small panel.
 * 
 * @author Quentin
 *
 */
public class DeparturePanel extends JPanel {

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
		JFrame _testFrame = new JFrame();
		_testFrame.setLocationRelativeTo(null);

		int _testId = 1;
		// Get test ID if provided
		if (args.length > 1) {
			_testId = Integer.parseInt(args[1]);
		}

		switch (_testId) {
		case 0:
			// Example of a Metro line
			DeparturePanel.MAX_LINE_NUMBER = 2;
			_testFrame.add(new DeparturePanel("100110005:5", "StopPoint:59270", 5, TimeUnit.SECONDS, "Ligne M 5",
					"Gare du Nord"));
			break;
		case 1:
			// Example of the RER A
			DeparturePanel.MAX_LINE_NUMBER = 10;
			DeparturePanel.MAX_WAITING_TIMES = 3;
			_testFrame.add(new DeparturePanel("810:A", "StopPoint:8775860:810:A", 5, TimeUnit.SECONDS, "Ligne RER A",
					"Châtelet - Les Halles"));
			break;
		case 2:
			// Example of the RER C
			_testFrame.add(new DeparturePanel("800:C", "StopPoint:8754520:800:C", 5, TimeUnit.SECONDS, "Ligne RER C",
					"Saint-Michel - Notre-Dame"));
			break;
		}

		_testFrame.setPreferredSize(new Dimension(500, 250));
		_testFrame.pack();
		_testFrame.setVisible(true);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String m_lineId;
	private String m_stopId;

	private String m_lineName;
	private String m_stopName;

	private List<Departure> m_departureList;

	private JPanel m_interiorPanel;
	private List<List<JLabel>> m_intPanelLabels;

	private static int MAX_LINE_NUMBER = 5;
	private static int MAX_WAITING_TIMES = 2;

	/**
	 * Constructor. Initializes variables and start refresh loop.
	 * 
	 * @param _lineId
	 * @param _stopID
	 * @param _refreshInterval
	 */
	public DeparturePanel(String _lineId, String _stopID, int _refreshInterval, TimeUnit _timeUnit, String _lineName,
			String _stopName) {
		m_lineId = _lineId;
		m_stopId = _stopID;

		m_departureList = new ArrayList<Departure>();

		m_lineName = _lineName;
		m_stopName = _stopName;

		// Build panel
		_buildPanel();

		// Start refresh loop
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					m_departureList = DataRetriever.getDeparturesLineAtStop(m_lineId, m_stopId);
					System.out.println(m_departureList);
					_refreshPanel();
				} catch (DataRetriever.UnauthorizedException e) {
					e.printStackTrace();
					return;
				} catch (DataRetriever.NotFoundException e) {
					e.printStackTrace();
					return;
				}
			}
		}, 0, _refreshInterval, _timeUnit);
	}

	public DeparturePanel(String _lineId, String _stopID, int _refreshInterval, TimeUnit _timeUnit) {
		this(_lineId, _stopID, _refreshInterval, _timeUnit, null, null);
	}

	/**
	 * Constructor. Assumes the TimeUnit is TimeUnit.SECONDS.
	 * 
	 * @param _lineId
	 * @param _stopID
	 * @param _refreshInterval
	 */
	public DeparturePanel(String _lineId, String _stopID, int _refreshInterval) {
		this(_lineId, _stopID, _refreshInterval, TimeUnit.SECONDS);
	}

	/**
	 * Builds panel content
	 */
	private void _buildPanel() {
		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(5, 5, 5, 5));

		// Set name if any
		if (m_lineName != null) {
			JPanel _topPanel = new JPanel(new GridLayout(1, 2));
			_topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
			_topPanel.add(new JLabel(m_lineName));
			_topPanel.add(new JLabel(m_stopName, SwingConstants.RIGHT));
			this.add(_topPanel, BorderLayout.NORTH);
		}

		m_interiorPanel = new JPanel(new GridBagLayout());

		// Direction
		GridBagConstraints _cdir = new GridBagConstraints();
		_cdir.anchor = GridBagConstraints.PAGE_START;
		_cdir.fill = GridBagConstraints.BOTH;
		_cdir.gridx = 0;
		_cdir.weightx = 0.8;
		_cdir.weighty = 1;
		_cdir.insets = new Insets(1, 2, 1, 2);

		// Waiting time
		GridBagConstraints _cwt = new GridBagConstraints();
		_cwt.anchor = GridBagConstraints.PAGE_START;
		_cwt.fill = GridBagConstraints.BOTH;
		_cwt.weightx = 0.3;
		_cwt.weighty = 1;
		_cwt.insets = new Insets(1, 2, 1, 2);

		// Store JLabels
		m_intPanelLabels = new ArrayList<List<JLabel>>();
		for (int i = 0; i < MAX_LINE_NUMBER; i++) {
			m_intPanelLabels.add(new ArrayList<JLabel>());

			// Destination
			_cdir.gridy = i;
			JLabel _destination = new JLabel("");
			m_intPanelLabels.get(i).add(_destination);
			m_interiorPanel.add(_destination, _cdir);
			_destination.setPreferredSize(new Dimension(1, 1));

			// Waiting times
			for (int j = 0; j < MAX_WAITING_TIMES; j++) {
				_cwt.gridy = i;
				_cwt.gridx = j + 1;
				JLabel _waiting_time = new JLabel("", SwingConstants.RIGHT);
				m_intPanelLabels.get(i).add(_waiting_time);
				m_interiorPanel.add(_waiting_time, _cwt);
				_waiting_time.setPreferredSize(new Dimension(1, 1));
			}
		}

		m_departureList.clear();
		m_departureList.add(new Departure("Waiting for informations...", ""));

		this.add(m_interiorPanel, BorderLayout.CENTER);
		_refreshPanel();
	}

	/**
	 * Refreshes panel content
	 */
	private void _refreshPanel() {

		// Clear JLabels
		for (int i = 0; i < MAX_LINE_NUMBER; i++) {
			for (int j = 0; j < MAX_WAITING_TIMES + 1; j++) {
				m_intPanelLabels.get(i).get(j).setText("");
			}
		}

		// Sort by destination
		Map<String, List<String>> _map_destination_waiting_times = new HashMap<String, List<String>>();
		for (Departure _departure : m_departureList) {
			if (!_map_destination_waiting_times.containsKey(_departure.getLineDirection())) {
				_map_destination_waiting_times.put(_departure.getLineDirection(), new ArrayList<String>());
			}
			_map_destination_waiting_times.get(_departure.getLineDirection()).add(_departure.getWaitingTime());
		}

		// Fill JLabels
		int _lineIndex = 0;
		for (String _key : _map_destination_waiting_times.keySet()) {
			if (_lineIndex < MAX_LINE_NUMBER) {
				// Fill destination
				m_intPanelLabels.get(_lineIndex).get(0).setText(_key);

				// Number of waiting times to fill
				int _nb_labels_to_set = Math.min(MAX_WAITING_TIMES, _map_destination_waiting_times.get(_key).size());
				for (int j = 0; j < _nb_labels_to_set; j++) {
					// m_intPanelLabels.get(_lineIndex).get(MAX_WAITING_TIMES - j)
					// .setText(_map_destination_waiting_times.get(_key).get(_nb_labels_to_set - j -
					// 1));
					m_intPanelLabels.get(_lineIndex).get(j + 1)
							.setText(_map_destination_waiting_times.get(_key).get(j));
				}
				_lineIndex++;
			}
		}
	}

}
