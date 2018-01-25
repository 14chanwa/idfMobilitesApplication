import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Comparator;
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * This class implements an brick information panel using data from
 * Île-de-France Mobilités's API to display the next departures for a given line
 * at a given stop. It has to be incorporated in a suitable frame.
 * 
 * @author Quentin
 *
 */
public class InformationPanel extends JPanel {

	/**
	 * Minimal working example
	 * 
	 * @param args
	 *            The first argument must be a valid API key. The second argument
	 *            (optional) selects a test case.
	 */
	public static void main(String[] args) {
		// This program requires you registering to Île-de-France Mobilités website in
		// order to get an API key. This is free, yet you must register to the OpenData
		// program.
		// I cannot disclose my key for obvious reasons
		DataRetriever.setAPIKey(args[0]);

		// Create a new DepartureViewer with some line and stop and a refresh time of 5
		// seconds.

		int _testId = -1;
		// Get test ID if provided
		if (args.length > 1) {
			_testId = Integer.parseInt(args[1]);
		}

		/*
		 * Test cases selection
		 */
		String _lineId = null;
		String _stopId = null;
		InformationPanel _informationPanel = null;

		switch (_testId) {
		case 0:
			// Example of a Metro line
			_lineId = "100110005:5";
			_stopId = "StopPoint:59270";
			_informationPanel = new InformationPanel("Ligne M 5", "Gare du Nord", 5, 2);
			break;
		case 1:
			// Example of the RER C
			_lineId = "800:C";
			_stopId = "StopPoint:8754520:800:C";
			_informationPanel = new InformationPanel("Ligne RER C", "Saint-Michel - Notre-Dame", 5, 2);
			break;
		default:
			// Example of the RER A
			_lineId = "810:A";
			_stopId = "StopPoint:8775860:810:A";
			_informationPanel = new InformationPanel("Ligne RER A", "Châtelet - Les Halles", 10, 3);

			// If we do not want the top line to be displayed
			// _informationPanel = new InformationPanel(null, null, 10, 3);

			break;
		}

		// Build frame
		JFrame _testFrame = new JFrame();
		_testFrame.setLocationRelativeTo(null);
		JPanel _mainContainer = new JPanel(new BorderLayout());
		_mainContainer.setBorder(new EmptyBorder(5, 5, 5, 5));
		_mainContainer.add(_informationPanel);
		_testFrame.add(_mainContainer, BorderLayout.CENTER);

		// Display frame
		_testFrame.setPreferredSize(new Dimension(500, 250));
		_testFrame.pack();
		_testFrame.setVisible(true);

		/*
		 * Start refresh loop
		 */

		// Final versions of parameters
		final String _finalLineId = _lineId;
		final String _finalStopId = _stopId;
		final InformationPanel _finalInformationPanel = _informationPanel;

		// Refresh each 5 seconds
		int _refreshInterval = 5;
		TimeUnit _timeUnit = TimeUnit.SECONDS;
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

		// Execute loop
		exec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					// Get information from
					List<Departure> _departureList = DataRetriever.getDeparturesLineAtStop(_finalLineId, _finalStopId);
					System.out.println(_departureList);

					// Refresh panel information
					_finalInformationPanel.refreshPanel(_departureList);
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String m_lineName;
	private String m_stopName;

	// private List<Departure> m_departureList;

	private JPanel m_interiorPanel;
	private List<List<JLabel>> m_intPanelLabels;

	private int m_max_line_number = 5;
	private int m_max_waiting_times = 2;

	private final int DEFAULT_MAX_LINE_NUMBER = 5;
	private final int DEFAULT_MAX_WAITING_TIMES = 2;

	/**
	 * Constructor. Initializes variables and start refresh loop.
	 * 
	 * @param _lineName
	 * @param _stopName
	 * @param _max_line_number
	 * @param _max_waiting_time
	 */
	public InformationPanel(String _lineName, String _stopName, int _max_line_number, int _max_waiting_time) {
		m_lineName = _lineName;
		m_stopName = _stopName;

		// Set number of lines and waiting times if parameters provided
		if (_max_line_number != -1 && _max_waiting_time != -1) {
			m_max_line_number = _max_line_number;
			m_max_waiting_times = _max_waiting_time;
		} else {
			m_max_line_number = DEFAULT_MAX_LINE_NUMBER;
			m_max_waiting_times = DEFAULT_MAX_WAITING_TIMES;
		}

		// Build panel
		_buildPanel();
	}

	/**
	 * Constructor. No name for line and station.
	 */
	public InformationPanel() {
		this(null, null, -1, -1);
	}

	/**
	 * Refreshes panel content
	 * 
	 * @param _departureList
	 */
	public void refreshPanel(List<Departure> _departureList) {

		// Clear JLabels
		for (int i = 0; i < m_max_line_number; i++) {
			for (int j = 0; j < m_max_waiting_times + 1; j++) {
				m_intPanelLabels.get(i).get(j).setText("");
			}
		}

		/*
		 * If list empty, display message
		 */
		if (_departureList.size() == 0) {
			m_intPanelLabels.get(0).get(0).setText("No information available");
			return;
		}

		/*
		 * Map destination and waiting times
		 */
		Map<String, List<String>> _map_destination_waiting_times = new HashMap<String, List<String>>();
		for (Departure _departure : _departureList) {
			if (!_map_destination_waiting_times.containsKey(_departure.getLineDestination())) {
				_map_destination_waiting_times.put(_departure.getLineDestination(), new ArrayList<String>());
			}
			_map_destination_waiting_times.get(_departure.getLineDestination()).add(_departure.getWaitingTime());
		}

		/*
		 * Sort waiting times
		 */
		// Assume that the departure times are given ordered (since we can not process
		// for instance "Sans arrêt" (no stop) that do not depict incoming train)

		// Else, could have proceeded that way:

		// // For each destination, sort the waiting times (by ascending values, or
		// // alphabetical order if non-numerical)
		// for (String _key : _map_destination_waiting_times.keySet()) {
		// _map_destination_waiting_times.get(_key).sort(new Comparator<String>() {
		//
		// @Override
		// public int compare(String o1, String o2) {
		//
		// int _num_wt1 = -1;
		// int _num_wt2 = -1;
		//
		// try {
		// _num_wt1 = Integer.parseInt(o1);
		// } catch (NumberFormatException e) {
		// // Nothing to do
		// }
		//
		// try {
		// _num_wt2 = Integer.parseInt(o2);
		// } catch (NumberFormatException e) {
		// // Nothing to do
		// }
		//
		// if (_num_wt1 < 0) {
		// if (_num_wt2 < 0) {
		// return o1.compareTo(o2);
		// }
		// return -1;
		// }
		// if (_num_wt2 < 0) {
		// return 1;
		// }
		// return _num_wt1 - _num_wt2;
		// }
		//
		// });
		// }

		/*
		 * Sort destinations
		 */
		// Sort keys by increasing waiting times (or key alphabetical order if equality)
		List<String> _sortedKeys = new ArrayList<String>(_map_destination_waiting_times.keySet());
		_sortedKeys.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {

				// Get nearest waiting time
				String _wt1 = _map_destination_waiting_times.get(o1).get(0);
				String _wt2 = _map_destination_waiting_times.get(o2).get(0);

				// -1 corresponds to non-numerical waiting time
				int _num_wt1 = -1;
				int _num_wt2 = -1;

				try {
					_num_wt1 = Integer.parseInt(_wt1);
				} catch (NumberFormatException e) {
					// Nothing to do
				}

				try {
					_num_wt2 = Integer.parseInt(_wt2);
				} catch (NumberFormatException e) {
					// Nothing to do
				}

				if (_num_wt1 < 0) {
					if (_num_wt2 < 0) {
						return o1.compareTo(o2);
					}
					return -1;
				}

				if (_num_wt2 < 0) {
					return 1;
				}
				return (_num_wt1 < _num_wt2 ? -1 : _num_wt1 > _num_wt2 ? 1 : o1.compareTo(o2));

			}
		});

		/*
		 * Fill labels
		 */
		int _lineIndex = 0;
		for (String _key : _sortedKeys) {
			if (_lineIndex < m_max_line_number) {
				// Fill destination
				m_intPanelLabels.get(_lineIndex).get(0).setText(_key);

				// Number of waiting times to fill
				int _nb_labels_to_set = Math.min(m_max_waiting_times, _map_destination_waiting_times.get(_key).size());
				for (int j = 0; j < _nb_labels_to_set; j++) {
					m_intPanelLabels.get(_lineIndex).get(j + 1)
							.setText(_map_destination_waiting_times.get(_key).get(j));
				}
				_lineIndex++;
			}
		}
	}

	/*
	 * PRIVATE METHODS
	 */

	/**
	 * Builds panel content
	 */
	private void _buildPanel() {
		this.setLayout(new BorderLayout());

		// Set name and stop if any
		if (m_lineName != null || m_stopName != null) {
			JPanel _topPanel = new JPanel(new GridLayout(1, 2));
			_topPanel.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 2, 0),
					BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black)));
			if (m_lineName != null)
				_topPanel.add(new JLabel(m_lineName));
			if (m_stopName != null)
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
		for (int i = 0; i < m_max_line_number; i++) {
			m_intPanelLabels.add(new ArrayList<JLabel>());

			// Destination
			_cdir.gridy = i;
			JLabel _destination = new JLabel("");
			m_intPanelLabels.get(i).add(_destination);
			m_interiorPanel.add(_destination, _cdir);
			_destination.setPreferredSize(new Dimension(1, 1));

			// Waiting times
			for (int j = 0; j < m_max_waiting_times; j++) {
				_cwt.gridy = i;
				_cwt.gridx = j + 1;
				JLabel _waiting_time = new JLabel("", SwingConstants.RIGHT);
				m_intPanelLabels.get(i).add(_waiting_time);
				m_interiorPanel.add(_waiting_time, _cwt);
				_waiting_time.setPreferredSize(new Dimension(1, 1));
			}
		}

		List<Departure> _departureList = new ArrayList<Departure>();
		_departureList.add(new Departure("Waiting for informations...", ""));

		this.add(m_interiorPanel, BorderLayout.CENTER);
		refreshPanel(_departureList);
	}

}
