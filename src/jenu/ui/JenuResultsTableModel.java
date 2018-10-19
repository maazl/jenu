package jenu.ui;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import jenu.worker.JenuPageEvent;
import jenu.worker.PageState;
import jenu.worker.PageStats;
import jenu.worker.ThreadManager;

/** View model for JenuInternalFrame */
final class JenuResultsTableModel extends AbstractTableModel
{
	private Vector<PageStats> m_statsAll = new Vector<PageStats>();

	private int m_sortColumn = -1;
	private boolean m_sortDescending = false;

	public JenuResultsTableModel(ThreadManager tm)
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

	public synchronized void sortByColumn(int columnIndex, boolean descending)
	{
		m_sortColumn = columnIndex;
		m_sortDescending = descending;

		if (m_statsAll != null && m_statsAll.size() > 0)
		{
			Collections.sort(m_statsAll, new StatsComparator(columnIndex, descending));
			fireTableDataChanged();
		}
	}

	static final String[] columnNames = {
		// 0       1            2      3       4        5       6        7            8           9         10       11
		"Address", "RunState", "Type", "Size", "Title", "Date", "Level", "Links Out", "Links In", "Server", "Error", "Status"
	};

	public int getRowCount()
	{
		return m_statsAll.size();
	}

	public PageStats getRow(int rowIndex)
	{
		return m_statsAll.elementAt(rowIndex);
	}

	public synchronized Object getValueAt(int rowIndex, int columnIndex)
	{
		PageStats row = m_statsAll.get(rowIndex);

		switch (columnIndex)
		{
		case 0:
			return row.sUrl;
		case 1:
			return row.getRunState();
		case 2:
			return row.contentType;
		case 3:
			return row.size == -1L ? null : row.size;
		case 4:
			return row.title;
		case 5:
			return row.date;
		case 6:
			return row.level == -1 ? null : row.level;
		case 7:
			return row.linksOut.size();
		case 8:
			return row.linksIn.size();
		case 9:
			return row.url != null ? row.url.getHost() : null;
		case 10:
			return row.getErrorString();
		case 11:
			return row.status;
		default:
			throw new Error("Invalid column index passed to getColumn");
		}
	}

	public String getColumnName(int columnIndex)
	{
		return columnNames[columnIndex];
	}

	public int getColumnCount()
	{
		return columnNames.length;
	}

	public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			return URL.class;
		case 1:
			return PageState.class;
		case 6:
		case 7:
		case 8:
			return Integer.class;
		case 3:
			return Long.class;
		case 2:
		case 4:
		case 9:
		case 10:
			return String.class;
		case 5:
			return Date.class;
		case 11:
			return EnumSet.class;
		default:
			throw new Error("Called with invalid Column Number: " + columnIndex);
		}
	}
}