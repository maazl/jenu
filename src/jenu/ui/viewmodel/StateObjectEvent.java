package jenu.ui.viewmodel;

import java.util.EventObject;

public class StateObjectEvent extends EventObject
{
	/** Page where the event refers to. Might be null to signal that the entire content changed. */
	public final StateObject item;
	/** What kind of event happened, i.e. INSERT, UPDATE or DELETE. */
	public final EventType type;

	public StateObjectEvent(Object source, StateObject item, EventType type)
	{	super(source);
		this.item = item;
		this.type = type;
	}
}
