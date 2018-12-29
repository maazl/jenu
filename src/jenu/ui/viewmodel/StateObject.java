package jenu.ui.viewmodel;

import java.util.EnumSet;

import jenu.model.Message;
import jenu.model.MessageType;

/** Common Interface of model objects. */
public interface StateObject
{
	/** What is the task status of this object?
	 * @threadsafety This function is <em>thread-safe</em>,
	 * but the result may already be out-dated at time of return. */
	public RowState getState();

	/** Return all events related to this object.
	 * @threadsafety This function is <em>thread-safe</em>,
	 * but the result may already be out-dated at time of return. */
	public Message[] getEffectiveEvents();

	/** Get Summary of all relevant message types related to this object.
	 * @threadsafety This function is <em>thread-safe</em>,
	 * but the result may already be out-dated at time of return. */
	default EnumSet<MessageType> getEventTypes()
	{	EnumSet<MessageType> ret = EnumSet.noneOf(MessageType.class);
		for (Message m : getEffectiveEvents())
			ret.add(m.type);
		return ret;
	}
}
