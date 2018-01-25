import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
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
 * This class implements an information frame using data from Île-de-France
 * Mobilités's API to display the next departures for a given line at a given
 * stop on a small panel. It separates the departures in one direction or an
 * other, when this attribute is provided by the API (i.e. the "sens" attribute
 * is available)
 * 
 * @author Quentin
 *
 */
public class TrainInformationFrame extends JFrame {

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

		TrainInformationFrame _trainInformationFrame = new TrainInformationFrame("Ligne RER A", "Châtelet - Les Halles",
				6, 2, ORIENTATION.HORIZONTAL, "Direction Est", "Direction Ouest");
		_trainInformationFrame.setPreferredSize(new Dimension(800, 250));
		_trainInformationFrame.pack();
		_trainInformationFrame.setVisible(true);

		// Refresh Line A information
		final String _finalLineId = "810:A";
		final String _finalStopId = "StopPoint:8775860:810:A";
		final TrainInformationFrame _finalTrainInformationFrame = _trainInformationFrame;

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
					_finalTrainInformationFrame.refreshFrame(_departureList);
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

	public static enum ORIENTATION {
		VERTICAL, HORIZONTAL
	}

	InformationPanel m_left_informationPanel;
	InformationPanel m_right_informationPanel;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TrainInformationFrame(String _lineName, String _stopName, int _max_line_number, int _max_waiting_time,
			ORIENTATION _orientation, String _rightName, String _leftName) {
		JPanel _mainContainer = new JPanel(new BorderLayout());
		_mainContainer.setBorder(new EmptyBorder(5, 5, 5, 5));

		/*
		 * Top messages
		 */
		JPanel _topPanel = new JPanel(new GridLayout(1, 2));
		_topPanel.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 2, 0),
				BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black)));
		_topPanel.add(new JLabel(_lineName));
		_topPanel.add(new JLabel(_stopName, SwingConstants.RIGHT));
		_mainContainer.add(_topPanel, BorderLayout.NORTH);

		/*
		 * Information panels
		 */
		JPanel _interiorPanel = new JPanel(new GridBagLayout());

		// Direction 1
		m_left_informationPanel = new InformationPanel(_rightName, null, _max_line_number, _max_waiting_time);

		GridBagConstraints _cstr = new GridBagConstraints();
		_cstr.anchor = GridBagConstraints.PAGE_START;
		_cstr.fill = GridBagConstraints.BOTH;
		_cstr.gridx = 0;
		_cstr.gridy = 0;
		_cstr.weightx = 0.5;
		_cstr.weighty = 1;

		_interiorPanel.add(m_left_informationPanel, _cstr);

		// Direction -1
		m_right_informationPanel = new InformationPanel(_leftName, null, _max_line_number, _max_waiting_time);

		// Put the frame horizontally or vertically
		switch (_orientation) {
		case VERTICAL:
			_cstr.gridy = 1;
			_cstr.insets = new Insets(5, 0, 0, 0);
			break;
		case HORIZONTAL:
			_cstr.gridx = 1;
			_cstr.insets = new Insets(0, 5, 0, 0);
			break;
		}

		_interiorPanel.add(m_right_informationPanel, _cstr);
		_mainContainer.add(_interiorPanel, BorderLayout.CENTER);

		this.add(_mainContainer);
	}

	public TrainInformationFrame(String _lineName, String _stopName, int _max_line_number, int _max_waiting_time) {
		this(_lineName, _stopName, _max_line_number, _max_waiting_time, ORIENTATION.HORIZONTAL, null, null);
	}

	public void refreshFrame(List<Departure> _departureList) {
		// Distinguish between direction codes
		List<Departure> _leftList = new ArrayList<Departure>();
		List<Departure> _rightList = new ArrayList<Departure>();

		for (Departure d : _departureList) {
			if (d.getDirectionCode() == Departure.DEFAULT_DIRECTION_CODE) {
				_leftList.add(d);
			} else {
				_rightList.add(d);
			}
		}
		m_left_informationPanel.refreshPanel(_leftList);
		m_right_informationPanel.refreshPanel(_rightList);
	}

}
