package jenu.model;

/** Message during check */
public class Message implements Comparable<Message>
{
	/** Message type */
	public final MessageType type;
	/** Severity level */
	public final Severity level;
	/** Message text */
	public final String message;

	/** Create message
	 * @param type Message type
	 * @param level Severity level
	 * @param message Message text */
	public Message(MessageType type, Severity level, String message)
	{	this.type = type;
		this.level = level;
		this.message = message;
	}

	public static final Message[] none = new Message[0];

	@Override public int compareTo(Message o)
	{	if (this == o)
			return 0;
		int cmp = level.compareTo(o.level);
		if (cmp == 0)
		{	cmp = type.compareTo(o.type);
			if (cmp == 0)
				cmp = message.compareTo(o.message);
		}
		return cmp;
	}
}
