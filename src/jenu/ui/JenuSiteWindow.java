package jenu.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import jenu.ui.JenuResultsTable.Column;
import jenu.ui.component.StackedBar;
import jenu.ui.component.StackedBarColorModel;
import jenu.ui.component.StackedBarModel;
import jenu.ui.component.StackedBarModelEvent;
import jenu.ui.component.StackedBarModelListener;
import jenu.worker.JenuPageEvent;
import jenu.worker.JenuPageListener;
import jenu.worker.JenuThreadEvent;
import jenu.worker.JenuThreadListener;
import jenu.worker.PageState;
import jenu.worker.PageStats;
import jenu.worker.ThreadManager;
import jenu.worker.WorkingSet;

/** Main user interface for backend worker.
 * An instance of this class shows a frame window with a complete UI.
 * Create it via the static method @see openNewWindow.
 * Several Windows can coexist.
 */
public final class JenuSiteWindow extends JenuFrame
{
	private final ToolBar m_toolBar;
	private final JScrollPane m_scroll;
	private final StatusBar m_statusBar;
	private JenuResultsTable m_table = null;
	private ThreadManager m_tm = null;

	private static int openWindowCount = 0;

	/** Create new site checker window.
	 * @param title Window title. This is prefixed by "Jenu: ".
	 *        If null "Site #" is used.
	 * @return The window just opened */
	public static JenuSiteWindow openNewWindow(String title)
	{
		if (title == null)
			synchronized (JenuSiteWindow.class)
			{	title = "Site " + ++openWindowCount;
			}
		JenuSiteWindow window = new JenuSiteWindow(title);
		window.setVisible(true);
		return window;
	}

	private JenuSiteWindow(String title)
	{
		super(title);

		setJMenuBar(new Menu());

		m_toolBar = new ToolBar();
		getContentPane().add(m_toolBar, BorderLayout.NORTH);

		m_statusBar = new StatusBar();
		getContentPane().add(m_statusBar, BorderLayout.SOUTH);

		m_scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		getContentPane().add(m_scroll, BorderLayout.CENTER);

		setPreferredSize(new Dimension(800, 560));
		autoPlacement();

		pack();
	}

	public void dispose()
	{
		if (m_tm != null && m_tm.isAlive())
			m_tm.stopRunning();
		super.dispose();
	}

	private void startRunning()
	{
		if (m_tm != null && m_tm.isAlive())
			m_tm.pauseRunning(false);
		else
		{	m_tm = new ThreadManager();
			m_table = new JenuResultsTable(m_tm);
			m_table.addMouseListener( new MouseAdapter()
				{	public void mouseClicked(MouseEvent e)
					{	if (e.getClickCount() >= 2)
						{	int column = m_table.columnAtPoint(e.getPoint());
							if (column >= 0)
							{	int row = m_table.rowAtPoint(e.getPoint());
								if (row >= 0)
									doubleClickLink(m_table.getModel().getRow(m_table.convertRowIndexToModel(row)), Column.fromOrdinal(m_table.convertColumnIndexToModel(column)));
				}	}	}	} );

			m_scroll.setViewportView(m_table);

			m_tm.addThreadListener(m_statusBar);
			m_tm.addPageListener(m_statusBar);
			m_tm.addThreadListener(e ->
				{	SwingUtilities.invokeLater(() ->
						{	if (e.m_threadsRunning + e.m_urlsToStart == 0)
							{	m_toolBar.setStopped();
								m_tm = null;
						}	} );
				} );
			m_statusBar.reset();
			WorkingSet ws = new WorkingSet();
			String url = m_toolBar.getSite();
			if (url.length() != 0)
				ws.Sites.add(url);
			url = m_toolBar.getURL();
			if (url.length() != 0)
				ws.StartingPoints.add(url);

			m_toolBar.setRunning();
			m_tm.start(ws);
		}
	}

	private void stopRunning()
	{
		if (m_tm != null)
		{	m_toolBar.setStopping();
			m_tm.stopRunning();
		}
	}

	private void pauseRunning()
	{
		if (m_tm != null)
		{	m_toolBar.setPaused();
			m_tm.pauseRunning(true);
		}
	}

	private static void doubleClickLink(PageStats rowData, Column col)
	{	switch (col)
		{case Links_in:
			JenuLinksWindow.openNewWindow(rowData, LinkWindowType.LinksIn);
			break;
		 case Links_out:
			JenuLinksWindow.openNewWindow(rowData, LinkWindowType.LinksOut);
			break;
		 default:
			break;
		}
	}

	private final class Menu extends JMenuBar
	{
		public Menu()
		{
			super();
			add(createFileMenu());
			// add(createViewMenu());
			// add(createOptionsMenu());
			// add(createHelpMenu());
		}

		private JMenu createFileMenu()
		{
			JMenu menu = new JMenu("File");
			menu.add(new AbstractAction("New window")
				{	public void actionPerformed(ActionEvent e)
					{	openNewWindow(null);
					}
				} );
			/*menu.add(new AbstractAction("Open...")
			{	public void actionPerformed(java.awt.event.ActionEvent e)
				{	OpenURLDialog open = new OpenURLDialog(owner);
					open.show();
				}
			} );*/
			menu.addSeparator();
			// menu.addSeparator();
			menu.add(new AbstractAction("Close")
				{	public void actionPerformed(ActionEvent e)
					{	JenuSiteWindow.this.dispose();
					}
				} );
			return menu;
		}

		/*private JMenu createOptionsMenu()
		{
			JMenu menu = new JMenu("Options");
			menu.add(new JMenuItem("Preferences..."));
			return menu;
		}

		private JMenu createHelpMenu()
		{
			JMenu menu = new JMenu("Help");
			menu.add(new JMenuItem("Help"));
			return menu;
		}

		private JMenu createViewMenu()
		{
			JMenu menu = new JMenu("View");
			menu.add(new JCheckBoxMenuItem("Toolbar"));
			menu.add(new JCheckBoxMenuItem("Status Bar"));
			return menu;
		}*/
	}

	private final class ToolBar extends JToolBar
	{
		final JButton m_run, m_stop, m_pause;
		final JTextField m_site, m_url;

		public ToolBar()
		{
			setFloatable(false);
			add(new JLabel("Site "));
			add(m_site = new JTextField(10));
			addSeparator();
			add(new JLabel("Start URL "));
			add(m_url = new JTextField(10));
			addSeparator();
			m_run = add(new AbstractAction("Run")
				{	public void actionPerformed(ActionEvent e)
					{	startRunning();
					}
				} );
			m_stop = add(new AbstractAction("Stop")
				{	public void actionPerformed(ActionEvent e)
					{	stopRunning();
					}
				} );
			m_pause = add(new AbstractAction("Pause")
				{	public void actionPerformed(ActionEvent e)
					{	pauseRunning();
					}
				} );
			setStopped();
		}

		public String getSite()
		{
			return m_site.getText();
		}

		public String getURL()
		{
			return m_url.getText();
		}

		public void setRunning()
		{
			m_run.setEnabled(false);
			m_stop.setEnabled(true);
			m_pause.setEnabled(true);
			m_site.setEnabled(false);
			m_url.setEnabled(false);
		}

		public void setPaused()
		{
			m_run.setEnabled(true);
			m_stop.setEnabled(true);
			m_pause.setEnabled(false);
			m_site.setEnabled(false);
			m_url.setEnabled(false);
		}

		public void setStopping()
		{
			m_run.setEnabled(false);
			m_stop.setEnabled(false);
			m_pause.setEnabled(false);
			m_site.setEnabled(false);
			m_url.setEnabled(false);
		}

		public void setStopped()
		{
			m_run.setEnabled(true);
			m_stop.setEnabled(false);
			m_pause.setEnabled(false);
			m_site.setEnabled(true);
			m_url.setEnabled(true);
		}
	}

	private final static class StatusBar extends JToolBar implements JenuThreadListener, JenuPageListener
	{
		JProgressBar m_threadsRunning;
		StackedBar m_URLsBar;
		BarModel barModel = new BarModel();

		public StatusBar()
		{
			m_threadsRunning = new JProgressBar(0)
				{	public String getString()
					{	return getValue() + "/" + getMaximum();
					}
				};
			m_threadsRunning.setStringPainted(true);
			m_threadsRunning.setMaximumSize(new Dimension(100,30));
			//m_threadsRunning.setPreferredSize(new Dimension(30,20));
			//m_urlsDone = new ProgressBar(0);
			m_URLsBar = new StackedBar(barModel);
			m_URLsBar.setColorModel(new StackedBarColorModel()
			{	private final PageState[] values = PageState.values();
				public Color getForegroundColorAt(int index)
				{	return Color.WHITE;
				}
				public Color getBackgroundColorAt(int index)
				{	return JenuUIUtils.getStateColor(values[index]);
				}
			});
			add(new JLabel("Threads running "));
			add(m_threadsRunning);
			addSeparator();
			add(new JLabel("Total URLs "));
			add(m_URLsBar);
			setFloatable(false);
		}

		public void threadStateChanged(JenuThreadEvent e)
		{
			SwingUtilities.invokeLater(() ->
				{	m_threadsRunning.setMaximum(e.getSource().getWorkingSet().MaxWorkerThreads);
					m_threadsRunning.setValue(e.m_threadsRunning);
				} );
		}

		public void pageChanged(JenuPageEvent e)
		{
			SwingUtilities.invokeLater(() -> barModel.pageChanged(e));
		}

		public void reset()
		{	barModel.reset();
		}

		private final static class BarModel implements StackedBarModel, JenuPageListener
		{
			@SuppressWarnings("unchecked")
			private final HashSet<String>[] categories = new HashSet[PageState.values().length];
			{ for (int i = 0; i < categories.length; ++i)
					categories[i] = new HashSet<>();
			}

			public int getCount()
			{	return PageState.values().length;
			}

			public float getValueAt(int index)
			{	return categories[index].size();
			}

			public String getTextAt(int index)
			{	return null;
			}

			public void pageChanged(JenuPageEvent e)
			{
				if (!e.isNew)
				{	if (categories[e.page.getPageState().ordinal()].contains(e.page.sUrl))
						return; // Nothing changed
					for (HashSet<String> cat : categories)
						if (cat.remove(e.page.sUrl))
							break;
				}
				categories[e.page.getPageState().ordinal()].add(e.page.sUrl);
				fireChanged();
			}

			public void reset()
			{	for (HashSet<String> cat : categories)
					cat.clear();
			}

			private final ArrayList<StackedBarModelListener> listeners = new ArrayList<>();
			public void addStackedBarModelListener(StackedBarModelListener l)
			{	listeners.add(l);
			}
			public void removeStackedBarModelListener(StackedBarModelListener l)
			{	listeners.remove(l);
			}
			private void fireChanged()
			{	StackedBarModelEvent e = new StackedBarModelEvent(this, -1);
				for (StackedBarModelListener l : listeners)
					l.dataChanged(e);
			}
		}
	}
}
