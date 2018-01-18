/**
 * Simple object class that represents a departure 
 * @author Quentin
 *
 */
public class Departure implements Comparable<Departure> {
	
	
	/**
	 * Minimal working example
	 * @param args
	 */
	public static void main(String args[]) {

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
	}
	
	
	/*
	 * Private parameters
	 */
	
	private static final int CODE_DURATION = 0;
	private static final int CODE_MESSAGE = 1;
	
	private int m_code;
	
	private String m_lineDirection;
	// TODO implement handling direction code when available
	//private int m_directionCode;
	
	private String m_message;
	private int m_time;
	
	/**
	 * Constructor with available numeric time
	 * @param _lineDirection
	 * @param _time
	 */
	public Departure(String _lineDirection, int _time) {
		m_code = CODE_DURATION;
		m_lineDirection = _lineDirection;
		m_time = _time;
	}
	
	/**
	 * Constructor with subjective waiting time (for instance "coming soon")
	 * @param _lineDirection
	 * @param _schedule
	 */
	public Departure(String _lineDirection, String _schedule) {
		m_code = CODE_MESSAGE;
		m_lineDirection = _lineDirection;
		m_message = _schedule;
	}
	
	@Override
	public String toString() {
		return getLineDirection() + "\t" + getWaitingTime();
	}
	
	public String getLineDirection() {
		return m_lineDirection;
	}
	
	public String getWaitingTime() {
		switch(m_code) {
		case CODE_DURATION:
			return "" + m_time;
		case CODE_MESSAGE:
			return m_message;
		}
		return null;
	}

	/**
	 * Implement comparison between departures
	 */
	@Override
	public int compareTo(Departure arg0) {
		if (this.m_code == CODE_DURATION && arg0.m_code == CODE_DURATION) {
			return (this.m_time < arg0.m_time ? 1 : (this.m_time > arg0.m_time ? -1 : 0));
		} else if (this.m_code == CODE_MESSAGE && arg0.m_code == CODE_DURATION) {
			return 1;
		} else if (this.m_code == CODE_DURATION && arg0.m_code == CODE_MESSAGE) {
			return -1;
		} 
		return 0;
	}
}
