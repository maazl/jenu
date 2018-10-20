package jenu.ui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jenu.worker.JenuPageEvent;
import jenu.worker.PageState;
import jenu.worker.PageStats;
import jenu.worker.ThreadManager;

final class JenuResultsTable extends JTable
{
	public JenuResultsTable(ThreadManager tm)
	{
		super(new TableModel(tm));
		System.out.println(defaultRenderersByColumnClass);
		getTableHeader().setReorderingAllowed(true);
		getTableHeader().addMouseListener(new MyHeaderListener());
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setShowGrid(false);
	}

	protected Hashtable<Class<?>, Hashtable<Color,DefaultTableCellRenderer>> m_renderersByClass = new Hashtable<>();

	public TableCellRenderer getCellRenderer(int row, int column)
	{
		Object o = getValueAt(row, column);
		if (o == null)
			return super.getCellRenderer(row, column);

		Class<?> c = o.getClass();
		if (!m_renderersByClass.contains(c))
			m_renderersByClass.put(c, new Hashtable<>());

		Hashtable<Color,DefaultTableCellRenderer> renderersByColor = m_renderersByClass.get(c);
		Color color = getRunStateColor(((TableModel)dataModel).getRow(row).getRunState());
		if (!renderersByColor.contains(color))
		{
			DefaultTableCellRenderer r = new DefaultTableCellRenderer(); // (DefaultTableCellRenderer)
			r.setForeground(color);
			renderersByColor.put(color, r);
		}
		return renderersByColor.get(color);
	}

	private static Color getRunStateColor(PageState runState)
	{
		switch (runState)
		{case PENDING:
			return Color.gray;
		 case RUNNING:
			return Color.orange;
		 case RETRY:
			return Color.magenta;
		 case DONE:
			return Color.green;
		 case FAILED:
			return Color.red;
		}
		return null;
	}

	private final class MyHeaderListener extends MouseAdapter
	{
		int m_sortColumn = -1;
		boolean m_descending;

		public void mouseClicked(MouseEvent e)
		{
			int column = getTableHeader().columnAtPoint(e.getPoint());
			if (column >= 0)
			{	m_descending = column == m_sortColumn & !m_descending;
				m_sortColumn = column;
				((TableModel)dataModel).sortByColumn(m_sortColumn, m_descending);
			}
		}
	}

	/** View model */
	private final static class TableModel extends AbstractTableModel
	{
		private Vector<PageStats> m_statsAll = new Vector<PageStats>();

		// TODO: retain sort order on changes
		private int m_sortColumn = -1;
		private boolean m_sortDescending = false;

		public TableModel(ThreadManager tm)
		{
			tm.addPageListener(this::updateEvent);
		}

		private void updateEvent(JenuPageEvent e)
		{
			synchronized (this)
			{	if (e.isNew)
				{	// TODO: retain sort order
					m_statsAll.add(e.page);
			}	}
			if (e.isNew)
				fireTableRowsInserted(m_statsAll.size() - 1, m_statsAll.size() - 1);
			else
			{	int i = m_statsAll.indexOf(e.page);
				if (i >= 0)
					fireTableRowsUpdated(i, i);
			}
		}

		public enum Column
		{
			Address, RunState, Status, Error, Links_out, Links_in, Server, Type, Size, Lines, Title, Date, Level, Seconds;

			private final static Column[] values = values();

			public static Column fromOrdinal(int ordinal)
			{	if (ordinal < 0 || ordinal >= values.length)
					throw new Error("Invalid column index passed: " + ordinal);
				return values[ordinal];
			}
		}

		public int getRowCount()
		{
			return m_statsAll.size();
		}

		public PageStats getRow(int rowIndex)
		{
			return m_statsAll.elementAt(rowIndex);
		}

		public String getColumnName(int columnIndex)
		{
			return Column.fromOrdinal(columnIndex).name().replace('_', ' ');
		}

		public int getColumnCount()
		{
			return Column.values.length;
		}

		public synchronized Object getValueAt(int rowIndex, int columnIndex)
		{
			PageStats row = m_statsAll.get(rowIndex);

			switch (Column.fromOrdinal(columnIndex))
			{case Address:
				return row.sUrl;
			 case RunState:
				return row.getRunState();
			 case Status:
				return row.getStatus().isEmpty() ? null : row.getStatus();
			 case Error:
				return row.getErrorString();
			 case Links_out:
				{	int ret = row.getLinksOut().size();
					return ret != 0 ? ret : null;
				}
			 case Links_in:
				return row.getLinksIn().size();
			 case Server:
				return row.url != null ? row.url.getHost() : null;
			 case Type:
				return row.getContentType();
			 case Size:
				{	long ret = row.getSize();
					return ret == -1L ? null : ret;
				}
			 case Lines:
				{	long ret = row.getLines();
					return ret <= 0 ? null : ret;
				}
			 case Title:
				return row.getTitle();
			 case Date:
				return row.getDate();
			 case Level:
				{	int ret = row.getLevel();
					return ret == Integer.MAX_VALUE ? null : ret;
				}
			 case Seconds:
				{	long millis = row.getDuration();
					return millis < 0 ? null : millis / 1000.F;
				}
			}
			return null; // Keep compiler happy
		}

		public Class<?> getColumnClass(int columnIndex)
		{
			switch (Column.fromOrdinal(columnIndex))
			{case Address:
				return URL.class;
			 case RunState:
				return PageState.class;
			 case Status:
				return EnumSet.class;
			 case Error:
			 case Server:
			 case Type:
			 case Title:
				return String.class;
			 case Links_out:
			 case Links_in:
			 case Lines:
			 case Level:
				return Integer.class;
			 case Size:
				return Long.class;
			 case Date:
				return Date.class;
			 case Seconds:
				return Float.class;
			}
			return null; // Keep compiler happy
		}

		public synchronized void sortByColumn(int columnIndex, boolean descending)
		{
			m_sortColumn = columnIndex;
			m_sortDescending = descending;
			int dir = descending ? -1 : 1;

			if (m_statsAll != null && m_statsAll.size() > 0)
			{
				Comparator<PageStats> comp = null;
				switch (Column.fromOrdinal(columnIndex))
				{case Address:
					comp = (s1, s2) -> dir * s1.sUrl.compareTo(s2.sUrl); break;
				 case RunState:
					comp = (s1, s2) -> dir * nullComp(s1.getRunState(), s2.getRunState()); break;
				 case Status:
					comp = (s1, s2) -> dir * sortedSequenceComp(s1.getStatus(), s2.getStatus()); break;
				 case Error:
					comp = (s1, s2) -> dir * nullComp(s1.getErrorString(), s2.getErrorString()); break;
				 case Links_out:
					comp = (s1, s2) -> dir * Integer.compare(s1.getLinksOut().size(), s2.getLinksOut().size()); break;
				 case Links_in:
					comp = (s1, s2) -> dir * Integer.compare(s1.getLinksIn().size(), s2.getLinksIn().size()); break;
				 case Server:
					comp = (s1, s2) -> dir * nullComp(s1.url != null ? s1.url.getHost() : null, s2.url != null ? s2.url.getHost() : null); break;
				 case Type:
					comp = (s1, s2) -> dir * nullComp(s1.getContentType(), s2.getContentType()); break;
				 case Size:
					comp = (s1, s2) -> dir * nullComp(s1.getErrorString(), s2.getErrorString()); break;
				 case Lines:
					comp = (s1, s2) -> dir * Long.compare(s1.getSize(), s2.getSize()); break;
				 case Title:
					comp = (s1, s2) -> dir * nullComp(s1.getTitle(), s2.getTitle()); break;
				 case Date:
					comp = (s1, s2) -> dir * nullComp(s1.getDate(), s2.getDate()); break;
				 case Level:
					comp = (s1, s2) -> dir * Integer.compare(s1.getLevel(), s2.getLevel()); break;
				 case Seconds:
					comp = (s1, s2) -> dir * Long.compare(s1.getDuration(), s2.getDuration()); break;
				}
				m_statsAll.sort(comp);

				fireTableDataChanged();
			}
		}

		private static <T extends Comparable<T>> int nullComp(T o1, T o2)
		{
			if (o1 == o2)
				return 0;
			else if (o1 == null)
				return -1;
			else if (o2 == null)
				return 1;
			else
				return o1.compareTo(o2);
		}

		private static <T extends Comparable<T>> int sortedSequenceComp(Iterable<T> s1, Iterable<T> s2)
		{
			Iterator<T> i1 = s1.iterator(), i2 = s2.iterator();
			while (true)
			{	if (!i1.hasNext())
					return !i2.hasNext() ? 0 : -1;
				if (!i2.hasNext())
					return 1;
				T v1 = i1.next(), v2 = i2.next();
				if (v1 == null)
					return v2 == null ? 0 : -1;
				if (v2 == null)
					return 1;
				int cmp = v1.compareTo(v2);
				if (cmp != 0)
					return cmp;
			}
		}
	}
}