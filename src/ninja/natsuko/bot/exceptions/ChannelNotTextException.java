package ninja.natsuko.bot.exceptions;

public class ChannelNotTextException extends Throwable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1846726817687278973L;

	public ChannelNotTextException() {
		super("Channel was not a Text Channel. Please provide a Text Channel ID or verify that the channel exists.");
	}
}
