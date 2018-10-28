package jenu.ui;

import java.awt.Component;
import java.net.URL;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import jenu.worker.EventType;
import jenu.worker.Link;
import jenu.worker.PageState;
import jenu.worker.PageStats;

final class JenuLinksTable extends JenuTable
{
	public JenuLinksTable(Collection<Link> data)
	{
		super(new TableModel(data));
		setRowSorter(new TableRowSorter<TableModel>(getModel()));
		getColumnModel().getColumn(Column.Events.ordinal()).setCellRenderer(new MultiLineCellRenderer());
		getColumnModel().getColumn(Column.Messages.ordinal()).setCellRenderer(new MultiLineCellRenderer());
	}

	@Override	public TableModel getModel()
	{	return (TableModel)super.getModel();
	}

	public void updateData(Collection<Link> data)
	{	getModel().updateData(data);
	}

	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		// state dependent foreground
		c.setForeground(getRunStateColor((PageState)getModel().getValueAt(convertRowIndexToModel(row), Column.Status)));
		return c;
	}

	enum Column
	{
		Tag, Target, Raw_target, Status, Events, Messages, Source, Source_line;

		private final static Column[] values = values();

		public static Column fromOrdinal(int ordinal)
		{	if (ordinal < 0 || ordinal >= values.length)
				throw new Error("Invalid column index passed: " + ordinal);
			return values[ordinal];
		}
	}

	/** View model */
	private final static class TableModel extends AbstractTableModel
	{
		private final Vector<Link> m_data;

		public TableModel(Collection<Link> data)
		{	m_data = new Vector<Link>(data);
		}

		public void updateData(Collection<Link> data)
		{	m_data.clear();
			m_data.addAll(data);
			fireTableDataChanged();
		}

		public int getRowCount()
		{	return m_data.size();
		}

		public Link getRow(int rowIndex)
		{	return m_data.elementAt(rowIndex);
		}

		public String getColumnName(int columnIndex)
		{	return Column.fromOrdinal(columnIndex).name().replace('_', ' ');
		}

		public int getColumnCount()
		{	return Column.values.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{	return getValueAt(rowIndex, Column.fromOrdinal(columnIndex));
		}
		public synchronized Object getValueAt(int rowIndex, Column column)
		{
			Link row = getRow(rowIndex);
			switch (column)
			{case Tag:
				return row.Type;
			 case Target:
				return row.Anchor != null ? row.Target + '#' + row.Anchor : row.Target;
			 case Raw_target:
				return row.OriginalTarget;
			 case Source:
				return row.Source;
			 case Source_line:
				return row.Line != 0 ? row.Line : null;
			 case Status:
				{	PageStats ps = row.getTargetPage();
					if (ps == null)
						return PageState.PENDING;
					if (Boolean.TRUE.equals(row.getAnchorState()) && ps.getEvents().size() == 1 && ps.getEvents().contains(EventType.Bad_anchor))
						return PageState.DONE;
					return ps.getPageState();
				}
			 case Events:
				{	PageStats ps = row.getTargetPage();
					if (ps == null)
						return null;
					EnumSet<EventType> ev = ps.getEvents();
					if (!Boolean.FALSE.equals(row.getAnchorState()) && ev.contains(EventType.Bad_anchor))
					{	ev = ev.clone();
						ev.remove(EventType.Bad_anchor);
					}
					return render(ev);
				}
			 case Messages:
				{	PageStats ps = row.getTargetPage();
					if (ps == null)
						return null;
					if (!Boolean.FALSE.equals(row.getAnchorState()))
						return ps.getPageMessages();
					return ps.getPageMessages() != null ? ps.getPageMessages() + "\n#" + row.Anchor : '#' + row.Anchor;
				}
			}
			return null; // Keep compiler happy
		}

		public Class<?> getColumnClass(int columnIndex)
		{
			switch (Column.fromOrdinal(columnIndex))
			{case Source:
				return URL.class;
			 case Source_line:
				return Integer.class;
			 case Status:
				return PageState.class;
			 default:
				return String.class;
			}
		}
	}
}