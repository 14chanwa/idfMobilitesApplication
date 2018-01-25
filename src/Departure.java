/**
 * Simple object class that represents a departure for a given line at a given
 * stop. The object implements Comparable since the waiting time is not always
 * numerical: for instance, the waiting time can go from 1 min to "Coming soon".
 * We assume that a message is inferior to a numerical waiting time. One should
 * also seek to parse warning messages like "Delayed".
 * 
 * @author Quentin
 */
public class Departure implements Comparable<Departure> {

	/**
	 * Minimal working example
	 * 
	 * @param args
	 *            No arguments required.
	 */
	public static void main(String[] args) {

		Departure _departure1 = new Departure("Destination paradise", 21);
		Departure _departure2 = new Departure("Destination heaven", 12);
		Departure _departure3 = new Departure("Destination above", "Coming next");
		Departure _departure4 = new Departure("Destination bliss", "Just arriving");

		// Print Departures
		System.out.println(_departure1);
		System.out.println(_departure2);
		System.out.println(_departure3);

		// Comparison between two departures
		// a.compareTo(b) should be 1, 0, -1 if a < b, a == b, a > b
		System.out.println(_departure1.compareTo(_departure2));
		System.out.println(_departure2.compareTo(_departure1));
		System.out.println(_departure1.compareTo(_departure3));
		System.out.println(_departure3.compareTo(_departure1));
		System.out.println(_departure3.compareTo(_departure4));

		// Result:
		// Destination paradise 21
		// Destination heaven 12
		// Destination above Coming next
		// -1 // Time to time comparison
		// 1 // Time to time comparison
		// -1 // Time to message comparison
		// 1 // Message to time comparison
		// 0 // Message to message comparison

	}

	/**
	 * Indicates whether the message is a duration or a message
	 * @author Quentin
	 *
	 */
	private static enum MESSAGE_TYPE {
		CODE_DURATION,
		CODE_MESSAGE
	}
	
	/*
	 * Private parameters
	 */

	// CODE_DURATION if the waiting time is an amount (of minutes f.i.),
	// CODE_MESSAGE else
	private MESSAGE_TYPE m_code;

	// Name of the destination
	private String m_lineDestination;

	// The waiting time (message or amount (of minutes f.i.))
	private String m_message;
	private int m_time;
	
	public static final int DEFAULT_DIRECTION_CODE = 1;

	// 1 or -1 depending on the direction of the line
	private int m_directionCode;

	/**
	 * Constructor with available numeric time and direction
	 * 
	 * @param _lineDestination
	 * @param _time
	 * @param _directionCode
	 */
	public Departure(String _lineDestination, int _time, int _directionCode) {
		m_code = MESSAGE_TYPE.CODE_DURATION;
		m_lineDestination = _lineDestination;
		m_time = _time;
		m_directionCode = _directionCode;
	}

	/**
	 * Constructor with available numeric time
	 * 
	 * @param _lineDestination
	 * @param _time
	 */
	public Departure(String _lineDestination, int _time) {
		this(_lineDestination, _time, DEFAULT_DIRECTION_CODE);
	}

	/**
	 * Constructor with subjective waiting time (for instance "coming soon") and
	 * direction code
	 * 
	 * @param _lineDestination
	 * @param _schedule
	 * @param _directionCode
	 */
	public Departure(String _lineDestination, String _schedule, int _directionCode) {
		m_code = MESSAGE_TYPE.CODE_MESSAGE;
		m_lineDestination = _lineDestination;
		m_message = _schedule;
		m_directionCode = _directionCode;
	}

	/**
	 * Constructor with subjective waiting time (for instance "coming soon")
	 * 
	 * @param _lineDestination
	 * @param _schedule
	 */
	public Departure(String _lineDestination, String _schedule) {
		this(_lineDestination, _schedule, DEFAULT_DIRECTION_CODE);
	}

	@Override
	public String toString() {
		return getLineDestination() + "\t" + getWaitingTime();
	}

	public String getLineDestination() {
		return m_lineDestination;
	}

	public String getWaitingTime() {
		switch (m_code) {
		case CODE_DURATION:
			return "" + m_time;
		case CODE_MESSAGE:
			return m_message;
		}
		return null;
	}

	public int getDirectionCode() {
		return m_directionCode;
	}

	/**
	 * Implement comparison between departures
	 */
	@Override
	public int compareTo(Departure arg0) {
		if (this.m_code == MESSAGE_TYPE.CODE_DURATION && arg0.m_code == MESSAGE_TYPE.CODE_DURATION) {
			return (this.m_time < arg0.m_time ? 1 : (this.m_time > arg0.m_time ? -1 : 0));
		} else if (this.m_code == MESSAGE_TYPE.CODE_MESSAGE && arg0.m_code == MESSAGE_TYPE.CODE_DURATION) {
			return 1;
		} else if (this.m_code == MESSAGE_TYPE.CODE_DURATION && arg0.m_code == MESSAGE_TYPE.CODE_MESSAGE) {
			return -1;
		}
		return 0;
	}
}
