package jenu.ui.viewmodel;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;

import jenu.model.Link;
import jenu.model.Message;
import jenu.model.Page;
import jenu.model.PageState;
import jenu.utils.Statics;
import jenu.worker.PageEvent;
import jenu.worker.PageEventType;
import jenu.worker.PageListener;

/** View model of target view, i.e. list all link targets and consider any dead URL as bad.
 * Targets that have unresolved HTML anchors are shown as erroneous too. */
public final class TargetView extends StateObjectView implements PageListener
{
	/** Column names */
	public enum Column
	{
		Address, Status, Events, Messages, Title, Links_out, Links_in, Anchors, Server, Type, Size, Lines, Date, Level, Seconds;

		private final static Column[] values = values();

		public static Column fromOrdinal(int ordinal)
		{	if (ordinal < 0 || ordinal >= values.length)
				throw new IllegalArgumentException("Invalid column index passed: " + ordinal);
			return values[ordinal];
		}
	}

	@Override public int getRowCount()
	{	return data.size();
	}

	/** get entire PageRow */
	public PageRow getRow(int rowIndex)
	{	return data.get(rowIndex);
	}

	@Override public String getColumnName(int columnIndex)
	{	return Column.fromOrdinal(columnIndex).name().replace('_', ' ');
	}

	@Override public int getColumnCount()
	{	return Column.values.length;
	}

	@Override public Object getValueAt(int rowIndex, int columnIndex)
	{
		PageRow row = data.get(rowIndex);

		switch (Column.fromOrdinal(columnIndex))
		{case Address:
			return row.page.sUrl;
		 case Status:
			return row.getState();
		 case Events:
			return row.getEventTypes();
		 case Messages:
			return row.getEffectiveEvents();
		 case Links_out:
			{	int ret = row.page.getLinksOut().size();
				return ret == 0 ? null : ret;
			}
		 case Links_in:
			{	long ret = row.page.getLinksIn().size();
				return ret == 0 ? null : ret;
			}
		 case Anchors:
			{	int ret = row.page.getAnchors().size();
				return ret == 0 ? null : ret;
			}
		 case Server:
			return row.page.url != null ? row.page.url.getHost() : null;
		 case Type:
			return row.page.getContentType();
		 case Size:
			{	long ret = row.page.getSize();
				return ret == -1L ? null : ret;
			}
		 case Lines:
			{	long ret = row.page.getLines();
				return ret <= 0 ? null : ret;
			}
		 case Title:
			{	String ret = row.page.getTitle();
				return ret != null ? ret : null;
			}
		 case Date:
			return row.page.getDate();
		 case Level:
			{	int ret = row.page.getLevel();
				return ret == Integer.MAX_VALUE ? null : ret;
			}
		 case Seconds:
			{	long millis = row.page.getDuration();
				return millis < 0 ? null : millis / 1000.F;
			}
		}
		return null; // Keep compiler happy
	}

	@Override public Class<?> getColumnClass(int columnIndex)
	{
		switch (Column.fromOrdinal(columnIndex))
		{case Address:
			return URL.class;
		 case Status:
			return RowState.class;
		 case Server:
		 case Type:
		 case Title:
			return String.class;
		 case Links_out:
		 case Links_in:
		 case Anchors:
		 case Lines:
		 case Level:
			return Integer.class;
		 case Size:
			return Long.class;
		 case Date:
			return Date.class;
		 case Seconds:
			return Float.class;
		 case Events:
			return EnumSet.class;
		 case Messages:
			return Message[].class;
		}
		return null; // Keep compiler happy
	}

	private final ArrayList<PageRow> data = new ArrayList<>();
	private final HashMap<Page,PageRow> map = new HashMap<>();

	@Override public void pageChanged(PageEvent e)
	{	PageRow row;
		if (e.type == PageEventType.NEW)
		{	row = new PageRow(e.page, data.size());
			data.add(row);
			map.put(e.page, row);
			fireTableRowsInserted(row.index, row.index);
			fireStateObjectEvent(row, EventType.INSERT);
			return;
		}
		row = map.get(e.page);
		if (row == null)
			return;
		if (e.type == PageEventType.DELETE)
		{	fireTableRowsDeleted(row.index, row.index);
			fireStateObjectEvent(row, EventType.DELETE);
		} else
		{	row.eventCache = null;
			fireTableRowsUpdated(row.index, row.index);
			fireStateObjectEvent(row, EventType.UPDATE);
		}
	}

	public static final class PageRow implements StateObject
	{
		public final Page page;
		final int index;

		PageRow(Page page, int index)
		{	this.page = page;
			this.index = index;
		}

		@Override public RowState getState()
		{	PageState ps = page.getState();
			if (ps != PageState.DONE)
				return RowState.valueOf(ps.ordinal());
			int o = 0;
			for (Message m : getEffectiveEvents())
				if (m.level.ordinal() > o)
					o = m.level.ordinal() + 1;
			return RowState.valueOf(RowState.OK.ordinal() + o);
		}

		@Override public Message[] getEffectiveEvents()
		{	Message[] ret = eventCache;
			if (ret == null)
			{	// Collect anchor messages of links to this page.
				ArrayList<Message> am = null;
				int size = 0;
				if (page.getLinksIn().size() > 0)
				{	for (Link l : page.getLinksIn().toArray(new Link[0])) // thread-safe enumeration
					{	Message m = l.getEvent();
						if (m == null)
							continue;
						if (am == null)
							am = new ArrayList<Message>();
						am.add(m);
					}
					if (am != null)
					{	am.sort(Comparator.<Message>naturalOrder());
						Statics.removeAdjacentDuplicates(am);
						size = am.size();
					}
				}
				// Start with page events.
				Collection<Message> pm = page.getEvents();
				ret = pm.toArray(new Message[pm.size() + size]);
				int i = 0;
				for (int j = ret.length - size; j < ret.length; ++j)
					ret[j] = am.get(i++);
				eventCache = ret;
			}
			return ret;
		}

		private volatile Message[] eventCache = null;
	}
}
