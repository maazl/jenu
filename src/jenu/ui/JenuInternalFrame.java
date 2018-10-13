package jenu.ui;

import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import jenu.worker.ThreadManager;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Hashtable;

public final class JenuInternalFrame extends JInternalFrame
{
	private static final long serialVersionUID = 1L;
	static int openFrameCount = 0;
	static final int xOffset = 30, yOffset = 30;
	@SuppressWarnings("unused")
	private Jenu m_owner;
	private InternalToolBar m_toolBar = null;
	private ThreadManager m_tm = null;
	private MyTable m_table = null;
	private URLDisplay m_scroll = null;
	private InternalStatusBar m_statusBar = null;

	public JenuInternalFrame(Jenu owner)
	{
		super("Document " + openFrameCount++,
			true, // resizeable
			true, // closeable
			true, // maximizable
			true);// iconifyable
		m_owner = owner;
		setLocation(xOffset + openFrameCount * 20, yOffset + openFrameCount * 20);
		setSize(new Dimension(50, 50));
		// this.setJMenuBar(new JenuMenu(m_owner));
		m_toolBar = new InternalToolBar(this);
		getContentPane().add(m_toolBar, BorderLayout.NORTH);
		m_statusBar = new InternalStatusBar();
		getContentPane().add(m_statusBar, BorderLayout.SOUTH);
		m_scroll = new URLDisplay();
		getContentPane().add(m_scroll, BorderLayout.CENTER);
		setPreferredSize(new Dimension(400, 200));
		grabFocus();
		pack();
	}

	public void startRunning()
	{
		boolean okToStart = false;
		m_toolBar.setRunning();
		if (m_tm == null)
		{
			okToStart = true;
		} else
		{
			if (!m_tm.isAlive())
			{
				okToStart = true;
			}
		}
		if (okToStart)
		{
			m_tm = new ThreadManager(m_toolBar.getURL(), this);
			m_tm.addThreadListener(m_statusBar);
			m_table = new MyTable(m_tm);
			m_scroll.setViewportView(m_table);
			m_tm.start();
		}
	}

	public void stopRunning()
	{
		m_tm.stopRunning();
		m_toolBar.setStopping();
	}

	public void setStopped()
	{
		m_toolBar.setStopped();
	}

	public void pauseRunning(boolean pause)
	{
		m_tm.pauseRunning(pause);
		m_toolBar.setPaused();
	}

	private class MyTable extends JTable
	{
		private static final long serialVersionUID = 1L;

		public MyTable(TableModel tm)
		{
			super(tm);
			System.out.println(defaultRenderersByColumnClass);
			getTableHeader().setReorderingAllowed(true);
			getTableHeader().addMouseListener(new MyHeaderListener());
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setShowGrid(false);
		}

		// public void tableChanged(TableModelEvent e) {
		//
		// int firstRow = e.getFirstRow();
		// int lastRow = e.getLastRow();
		// int cols = getColumnCount();
		// if (firstRow >= 0 || lastRow >= 0) {
		// ThreadManager tm = (ThreadManager) dataModel;
		// for (int row = firstRow; row <= lastRow; row++) {
		// Color c = tm.getRowStateColor(row);
		// for (int col = 0; col < cols; col++) {
		// DefaultTableCellRenderer r = (DefaultTableCellRenderer) getCellRenderer(row,
		// col);
		// r.setForeground(c);
		// }
		// }
		// }
		// super.tableChanged(e);
		// }

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
			Color color = ((ThreadManager)dataModel).getRowStateColor(row);
			if (!renderersByColor.contains(color))
			{
				DefaultTableCellRenderer r = new DefaultTableCellRenderer(); // (DefaultTableCellRenderer)
																																			// getDefaultRenderer(c).clone();
				r.setForeground(color);
				renderersByColor.put(color, r);
			}
			return renderersByColor.get(color);
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
					m_tm.sortByColumn(m_sortColumn, m_descending);
				}
			}
		}
	}

	private class URLDisplay extends JScrollPane
	{
		private static final long serialVersionUID = 1L;

		public URLDisplay()
		{
			super(VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
		}
	}

}
