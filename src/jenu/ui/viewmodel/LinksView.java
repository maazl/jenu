package jenu.ui.viewmodel;

import static jenu.utils.Statics.strcat;

import java.util.EnumSet;
import jenu.model.Link;
import jenu.model.Message;
import jenu.model.Page;
import jenu.model.PageState;
import jenu.worker.PageListener;


/** Base class for different link views. */
public abstract class LinksView extends StateObjectView<LinksView.LinkRow> implements PageListener
{
	/** Column names provided by this view. */
	public enum Column
	{
		Tag, Target, Raw_target, Status, Events, Messages, Source, Source_line;

		final static Column[] values = values();

		public static Column fromOrdinal(int ordinal)
		{	if (ordinal < 0 || ordinal >= values.length)
				throw new IllegalArgumentException("Invalid column index passed: " + ordinal);
			return values[ordinal];
		}
	}

	/** Construct and append new row to {@link #data} */
	protected LinkRow addNewRow(Link link)
	{	LinkRow row = new LinkRow(link, data.size());
		data.add(row);
		return row;
	}

	@Override public String getColumnName(int columnIndex)
	{	return Column.fromOrdinal(columnIndex).name().replace('_', ' ');
	}

	@Override public int getColumnCount()
	{	return Column.values.length;
	}

	@Override public final Object getValueAt(int rowIndex, int columnIndex)
	{	return getValueAt(rowIndex, Column.fromOrdinal(columnIndex));
	}
	public Object getValueAt(int rowIndex, Column column)
	{
		LinkRow row = getRow(rowIndex);
		switch (column)
		{case Tag:
			return row.link.type;
		 case Target:
			{	Page tp = row.link.getTargetPage();
				if (tp == null)
					return row.link.originalTarget;
				String anchor = row.link.getAnchor();
				return strcat(tp.sUrl, anchor, '#');
			}
		 case Raw_target:
			return row.link.originalTarget;
		 case Source:
			return row.link.source.sUrl;
		 case Source_line:
			return row.link.sourceLine != 0 ? row.link.sourceLine : null;
		 case Status:
			return row.getState();
		 case Events:
			return row.getEventTypes();
		 case Messages:
			return row.getEffectiveEvents();
		}
		return null; // Keep compiler happy
	}

	@Override public Class<?> getColumnClass(int columnIndex)
	{
		switch (Column.fromOrdinal(columnIndex))
		{case Source_line:
			return Integer.class;
		 case Status:
			return RowState.class;
		 case Events:
			return EnumSet.class;
		 case Messages:
			return Message[].class;
		 default:
			return String.class;
		}
	}

	/** Result row of a link view */
	public static final class LinkRow implements StateObject
	{
		public final Link link;
		final int index;

		LinkRow(Link link, int index)
		{	this.link = link;
			this.index = index;
		}

		@Override public RowState getState()
		{	final Page target = link.getTargetPage();
			if (target == null)
				return RowState.PENDING;
			final PageState ps = target .getState();
			if (ps != PageState.DONE)
				return RowState.valueOf(ps.ordinal());
			int o = RowState.OK.ordinal();
			Message m = link.getEvent();
			if (m != null)
					o += m.level.ordinal() + 1;
			return RowState.valueOf(o);
		}

		@Override public Message[] getEffectiveEvents()
		{	Message[] ret = eventCache;
			if (ret != null)
				return ret;
			// Target events excluding parser errors + link event.
			final Page target = link.getTargetPage();
			final Message message = link.getEvent();
			if (target != null)
			{	final Message[] events = target.getEvents().toArray(Message.none); // thread-safe enumeration
				// remove parser errors
				int retlen = 0;
				while (retlen < events.length)
				{	if (!isLinkError(events[retlen]))
					{	// first entry to be discarded
						for (int i = retlen; ++i < events.length; )
							if (isLinkError(events[retlen]))
								events[retlen++] = events[i];
						break;
					}
					retlen++;
				}
				// Anything left?
				if (retlen != 0)
				{	final int totalLen = message != null ? retlen + 1 : retlen;
					if (totalLen != events.length)
					{	ret = new Message[totalLen];
						System.arraycopy(events, 0, ret, 0, retlen);
					} else
						ret = events;
					if (message != null)
						ret[retlen] = message;
					return eventCache = ret;
				}
			}
			// Only link message, if any.
			return eventCache = message == null ? Message.none : new Message[] { message };
		}
		volatile Message[] eventCache = null;

		public static final LinkRow[] none = new LinkRow[0];
	}
}
