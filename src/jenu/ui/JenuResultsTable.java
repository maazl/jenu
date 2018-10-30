package jenu.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import jenu.worker.JenuPageEvent;
import jenu.worker.PageState;
import jenu.worker.PageStats;
import jenu.worker.ThreadManager;

final class JenuResultsTable extends JenuTable
{
	public JenuResultsTable(ThreadManager tm)
	{
		super(new TableModel(tm));
		setMinimumSize(new Dimension(800,0));
		setRowSorter(new TableRowSorter<TableModel>(getModel()));
		getColumnModel().getColumn(Column.Events.ordinal()).setCellRenderer(new MultiLineCellRenderer());
		getColumnModel().getColumn(Column.Messages.ordinal()).setCellRenderer(new MultiLineCellRenderer());
	}

	@Override	public TableModel getModel()
	{	return (TableModel)super.getModel();
	}

	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		// state dependent foreground
		c.setForeground(JenuUIUtils.getStateColor(getModel().getRow(convertRowIndexToModel(row)).getTotalState()));
		return c;
	}

	enum Column
	{
		Address, Status, Events, Messages, Title, Links_out, Links_in, Anchors, Server, Type, Size, Lines, Date, Level, Seconds;

		private final static Column[] values = values();

		public static Column fromOrdinal(int ordinal)
		{	if (ordinal < 0 || ordinal >= values.length)
				throw new Error("Invalid column index passed: " + ordinal);
			return values[ordinal];
		}
	}

	/** View model */
	final static class TableModel extends AbstractTableModel
	{
		private Vector<PageStats> m_statsAll = new Vector<PageStats>();

		public TableModel(ThreadManager tm)
		{
			tm.addPageListener((e) -> SwingUtilities.invokeLater(() -> updateEvent(e)));
		}

		private void updateEvent(JenuPageEvent e)
		{
			if (e.isNew)
			{	m_statsAll.add(e.page);
				fireTableRowsInserted(m_statsAll.size() - 1, m_statsAll.size() - 1);
			} else
			{	int i = m_statsAll.indexOf(e.page);
				if (i >= 0)
					fireTableRowsUpdated(i, i);
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
			 case Status:
				return row.getTotalState();
			 case Events:
				return render(row.getEvents());
			 case Messages:
				return row.getTotalMessages();
			 case Links_out:
				{	int ret = row.getLinksOut().size();
					return ret == 0 ? null : ret;
				}
			 case Links_in:
				{	long ret = row.getLinksIn().size();
					return ret == 0 ? null : ret;
				}
			 case Anchors:
				{	int ret = row.getAnchors().size();
					return ret == 0 ? null : ret;
				}
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
				{	String ret = row.getTitle();
					return ret != null ? "<html>" + ret + "</html>" : null;
				}
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
			 case Status:
				return PageState.class;
			 case Events:
			 case Messages:
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
			}
			return null; // Keep compiler happy
		}

		/*private static <T extends Comparable<T>> int nullComp(T o1, T o2)
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
				int cmp = nullComp(v1, v2);
				if (cmp != 0)
					return cmp;
			}
		}*/
	}
}