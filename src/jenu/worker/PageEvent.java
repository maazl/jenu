package jenu.worker;

import java.util.EventObject;

import jenu.model.Page;

public class PageEvent extends EventObject
{
	/** Page where the event refers to */
	public final Page page;
	/** What kind of event happened.
	 * Note that there is a small chance that an another event type happens before the NEW event.
	 * You should ignore this events in doubt. */
	public final PageEventType type;

	public PageEvent(Object source, Page page, PageEventType type)
	{	super(source);
		this.page = page;
		this.type = type;
	}
}
