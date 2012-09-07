package fitnesse.slim;

public class SlimServerBadResponseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SlimServerBadResponseException() {
		super();
	}

	public SlimServerBadResponseException(String message) {
		super("Steam Read Failure. Can't read length of message from the server.  Possibly test aborted.  Last thing read: " + message);
	}

	public SlimServerBadResponseException(Throwable cause) {
		super(cause);
	}

	public SlimServerBadResponseException(String message, Throwable cause) {
		super("Steam Read Failure. Can't read length of message from the server.  Possibly test aborted.  Last thing read: " + message, cause);
	}

}
