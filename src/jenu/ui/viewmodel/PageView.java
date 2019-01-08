package jenu.ui.viewmodel;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Date;
import java.util.EnumSet;

import jenu.model.Message;
import jenu.model.Page;
import jenu.model.PageState;
import jenu.ui.viewmodel.TargetView.PageRow;
import jenu.worker.PageListener;
import jenu.worker.ThreadManager;

/** Common base class of source view and target view. */
public abstract class PageView<R extends PageView.PageRow> extends StateObjectView<R> implements PageListener
{
	/** Column names */
	public enum Column
	{
		Address, Status, Events, Messages, Title, Links_out, Links_in, Anchors, Server, Type, Size, Lines, Date, Level, Seconds;

		public final static Column[] values = values();

		public static Column fromOrdinal(int ordinal)
		{	if (ordinal < 0 || ordinal >= values.length)
				throw new IllegalArgumentException("Invalid column index passed: " + ordinal);
			return values[ordinal];
		}
	}

	protected WeakReference<ThreadManager> tm = new WeakReference<>(null);

	/** Attach this instance to a backend model. */
	public final void attach(ThreadManager tm)
	{	detach();
		this.tm = new WeakReference<>(tm);
		tm.addPageListener(this);
		refresh();
	}

	/** Detach this instance to a backend model.
	 * Required to prevent the model from staying alive indefinitely. */
	public final void detach()
	{	ThreadManager tm = this.tm.get();
		if (tm != null)
			tm.removePageListener(this);
	}

	@Override protected void fetchData()
	{	ThreadManager tm = this.tm.get();
		if (tm == null)
			return;
		Page[] data = tm.getData();
		for (Page page : data)
			addNewRow(page);
	}

	/** Construct and append new row to {@link #data} */
	protected abstract R addNewRow(Page page);

	@Override public final String getColumnName(int columnIndex)
	{	return Column.fromOrdinal(columnIndex).name().replace('_', ' ');
	}

	@Override public final int getColumnCount()
	{	return Column.values.length;
	}

	@Override public Object getValueAt(int rowIndex, int columnIndex)
	{
		PageRow row = getRow(rowIndex);

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
	{	switch (Column.fromOrdinal(columnIndex))
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

	public static abstract class PageRow implements StateObject
	{
		public final Page page;
		protected final int index;

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
	}
}
