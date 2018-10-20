package jenu.ui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jenu.worker.PageState;
import jenu.worker.ThreadManager;

final class JenuResultsTable extends JTable
{
	public JenuResultsTable(ThreadManager tm)
	{
		super(new JenuResultsTableModel(tm));
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
		Color color = getRunStateColor(((JenuResultsTableModel)dataModel).getRow(row).getRunState());
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
			return Color.yellow;
		 case RETRY:
			return Color.magenta;
		 case DONE:
			return Color.green;
		 case FAILED:
			return Color.red;
		 default:
			throw new Error("Invalid runState");
		}
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
				((JenuResultsTableModel)dataModel).sortByColumn(m_sortColumn, m_descending);
			}
		}
	}
}