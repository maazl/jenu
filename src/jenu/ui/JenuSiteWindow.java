package jenu.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import jenu.Jenu;
import jenu.ui.component.StackedBar;
import jenu.ui.component.StackedBarColorModel;
import jenu.ui.component.StackedBarModel;
import jenu.ui.component.StackedBarModelEvent;
import jenu.ui.component.StackedBarModelListener;
import jenu.ui.viewmodel.RowState;
import jenu.ui.viewmodel.Site;
import jenu.ui.viewmodel.StateObject;
import jenu.ui.viewmodel.StateObjectEvent;
import jenu.ui.viewmodel.StateObjectListener;
import jenu.ui.viewmodel.TargetView;
import jenu.worker.ThreadManager;
import jenu.worker.WorkerEvent;
import jenu.worker.WorkerListener;

/** Main user interface for backend worker.
 * An instance of this class shows a frame window with a complete UI.
 * Create it via the static method @see openWindow.
 * Several Windows can coexist. */
public final class JenuSiteWindow extends JenuFrame
{
	private final ToolBar m_toolBar;
	private final JScrollPane m_scroll;
	private final StatusBar m_statusBar;
	private JenuResultsTable m_table = null;

	private final Site m_site;
	private ThreadManager m_tm = null;

	private static int openWindowCount = 0;
	private static String getTitle(String sitetitle)
	{	if (sitetitle == null)
			synchronized (JenuSiteWindow.class)
			{	sitetitle = Integer.toString(++openWindowCount);
			}
		return "Site " + sitetitle;
	}

	/** default configuration file. New instances of Site will use this file. */
	public final static File defaultSiteFile = new File(Jenu.userHome, ".jenuSite");

	private static final HashMap<Site,JenuSiteWindow> map = new HashMap<>();

	/** View an instance of the site checker window.
	 * If the same site is passed twice the existing window is shown.
	 * @param site Site to view or null to load default values.
	 * @return Window just opened. */
	public static JenuSiteWindow openWindow(Site site)
	{	if (site == null)
		{	site = new Site();
			try
			{	site.importFile(defaultSiteFile);
			} catch (IOException ex)
			{	// ignore this error here
			}
		}
		JenuSiteWindow win = map.computeIfAbsent(site, JenuSiteWindow::new);
		win.setVisible(true);
		return win;
	}

	/** Create new site checker window.
	 * Remember to call {@code setVisible(true)}.
	 * @param site Configuration to use, new Site() in doubt. */
	private JenuSiteWindow(Site site)
	{
		super(getTitle(site.title));
		m_site = site;

		setJMenuBar(new JMenuBar());
		makeMenu("Site", aNew, aOpen, aSave, aSaveAs, aSaveDefault, null, aReset, aClose);

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

	@Override public void setVisible(boolean b)
	{	if (b)
			bindData();
		super.setVisible(b);
	}

	@Override public void dispose()
	{
		if (m_tm != null && m_tm.isAlive())
			m_tm.stopRunning();
		super.dispose();
	}

	final FunctionalAction aRun = new FunctionalAction("Run", JenuSiteWindow.this::startRunning);
	private void startRunning(ActionEvent ev)
	{	if (m_tm != null && m_tm.isAlive())
			m_tm.pauseRunning(false);
		else if (fetchData())
		{	m_tm = new ThreadManager();
			m_table = new JenuResultsTable(m_tm);
			m_table.addMouseListener( new MouseAdapter()
				{	public void mouseClicked(MouseEvent e)
					{	if (e.getClickCount() >= 2)
						{	int column = m_table.columnAtPoint(e.getPoint());
							if (column >= 0)
							{	int row = m_table.rowAtPoint(e.getPoint());
								if (row >= 0)
									doubleClickLink(m_table.getModel().getRow(m_table.convertRowIndexToModel(row)), TargetView.Column.fromOrdinal(m_table.convertColumnIndexToModel(column)));
				}	}	}	} );

			m_scroll.setViewportView(m_table);

			m_tm.addThreadListener(m_statusBar);
			m_table.getModel().addStateObjectListener(m_statusBar);
			m_tm.addThreadListener(e ->
				{	SwingUtilities.invokeLater(() ->
						{	if (e.m_threadsRunning + e.m_urlsToStart == 0)
							{	m_toolBar.setStopped();
								m_tm = null;
						}	} );
				} );
			m_statusBar.reset();

			m_toolBar.setRunning();
			m_tm.start(m_site);
		}
	}

	final FunctionalAction aStop = new FunctionalAction("Stop", JenuSiteWindow.this::stopRunning);
	private void stopRunning(ActionEvent ev)
	{	if (m_tm != null)
		{	m_toolBar.setStopping();
			m_tm.stopRunning();
		}
	}

	final FunctionalAction aPause = new FunctionalAction("Pause", JenuSiteWindow.this::pauseRunning);
	private void pauseRunning(ActionEvent ev)
	{	if (m_tm != null)
		{	m_toolBar.setPaused();
			m_tm.pauseRunning(true);
		}
	}

	final FunctionalAction aNew = new FunctionalAction("New empty window", (e) -> openWindow(null));

	final FunctionalAction aOpen = new FunctionalAction("Open...", (e) ->
	{	JFileChooser fc = getFileChooser();
		if (fc.showOpenDialog(JenuSiteWindow.this) == JFileChooser.APPROVE_OPTION)
			loadFile(fc.getSelectedFile());
	} );

	private void loadFile(File file)
	{	try
		{	m_site.importFile(file);
			m_site.lastFile = file;
			bindData();
		} catch (IOException e)
		{	JOptionPane.showMessageDialog(this, e.toString(), "Failed to read file " + file.toString(), JOptionPane.ERROR_MESSAGE);
		}
	}

	final FunctionalAction aSave = new FunctionalAction("Save", (e) -> saveFile(null));

	final FunctionalAction aSaveAs = new FunctionalAction("Save as...", JenuSiteWindow.this::saveAs);
	private void saveAs(ActionEvent ev)
	{	JFileChooser fc = getFileChooser();
		if (fc.showSaveDialog(JenuSiteWindow.this) == JFileChooser.APPROVE_OPTION)
		{	File file = fc.getSelectedFile();
			if (fc.getFileFilter() == siteFileFilter && file.getName().indexOf('.') < 0)
				file = new File(file.toString() + ".jenuSite");
			saveFile(file);
			m_site.lastFile = file;
		}
	}

	final FunctionalAction aSaveDefault = new FunctionalAction("Seve as default", (e) -> saveFile(defaultSiteFile));

	private void saveFile(File file)
	{	fetchData();
		if (file == null)
			file = m_site.lastFile;
		try
		{	m_site.exportFile(file);
		} catch (IOException e)
		{	JOptionPane.showMessageDialog(this, e.toString(), "Failed to write file " + file.toString(), JOptionPane.ERROR_MESSAGE);
		}
	}

	final FunctionalAction aReset = new FunctionalAction("Clear form", JenuSiteWindow.this::reset);
	private void reset(ActionEvent ev)
	{	m_site.reset();
		bindData();
	}

	final FunctionalAction aClose = new FunctionalAction("Close", (e) -> JenuSiteWindow.this.dispose());

	private final static FileNameExtensionFilter siteFileFilter = new FileNameExtensionFilter("Jenu site file", "jenuSite");

	private JFileChooser getFileChooser()
	{	JFileChooser fc = new JFileChooser(m_site.lastFile);
		fc.addChoosableFileFilter(siteFileFilter);
		fc.setFileFilter(siteFileFilter);
		return fc;
	}

	private void bindData()
	{
		m_toolBar.m_site.setText(m_site.sites.size() != 0 ? m_site.sites.get(0) : "");
		aSave.setEnabled(m_site.lastFile != null);
	}

	private boolean fetchData()
	{
		String url = m_toolBar.m_site.getText().trim();
		if (url.length() == 0)
		{	switch (m_site.sites.size())
			{case 0:
				return false; // no change
			 case 1:
				m_site.sites.clear();
				return true;
			}
		} else
		{	if (m_site.sites.size() == 1 && url.equals(m_site.sites.get(0)))
				return true; // no change
			switch (m_site.sites.size())
			{case 0:
				m_site.sites.add(url);
				return true;
			 case 1:
				m_site.sites.set(0, url);
				return true;
			}
		}
		if (JOptionPane.showConfirmDialog(this, "This site contains multiple URLs. Replace them all by the one entered here?", "Overwrite multiple site URLs?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return false;
		m_site.sites.clear();
		m_site.sites.add(url);
		return true;
	}

	private void doubleClickLink(TargetView.PageRow rowData, TargetView.Column col)
	{	switch (col)
		{case Links_in:
			JenuLinksWindow.openWindow(m_tm, rowData.page, LinkWindowType.LinksIn);
			break;
		 case Links_out:
			JenuLinksWindow.openWindow(m_tm, rowData.page, LinkWindowType.LinksOut);
			break;
		 default:
			break;
		}
	}

	private JMenu makeMenu(String title, Action ... items)
	{	JMenu menu = new JMenu("Site");
		for (Action a : items)
			if (a == null)
				menu.addSeparator();
			else
				menu.add(a);
		getJMenuBar().add(menu);
		return menu;
	}

	private final class ToolBar extends JToolBar
	{
		final JButton m_run, m_stop, m_pause;
		JTextField m_site;

		public ToolBar()
		{
			setFloatable(false);
			add(new JLabel("URL "));
			add(m_site = new JTextField(10));
			addSeparator();
			m_run = add(aRun);
			m_pause = add(aPause);
			m_stop = add(aStop);
			setStopped();
		}

		public void setRunning()
		{
			m_run.setEnabled(false);
			m_stop.setEnabled(true);
			m_pause.setEnabled(true);
			m_site.setEnabled(false);
		}

		public void setPaused()
		{
			m_run.setEnabled(true);
			m_stop.setEnabled(true);
			m_pause.setEnabled(false);
			m_site.setEnabled(false);
		}

		public void setStopping()
		{
			m_run.setEnabled(false);
			m_stop.setEnabled(false);
			m_pause.setEnabled(false);
			m_site.setEnabled(false);
		}

		public void setStopped()
		{
			m_run.setEnabled(true);
			m_stop.setEnabled(false);
			m_pause.setEnabled(false);
			m_site.setEnabled(true);
		}
	}

	private final static class StatusBar extends JToolBar implements WorkerListener, StateObjectListener
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
			{	public Color getForegroundColorAt(int index)
				{	return Color.WHITE;
				}
				public Color getBackgroundColorAt(int index)
				{	return JenuUIUtils.getStateColor(RowState.values[index]);
				}
			});
			add(new JLabel("Threads running "));
			add(m_threadsRunning);
			addSeparator();
			add(new JLabel("Total URLs "));
			add(m_URLsBar);
			setFloatable(false);
		}

		@Override public void threadStateChanged(WorkerEvent e)
		{
			SwingUtilities.invokeLater(() ->
				{	m_threadsRunning.setMaximum(e.getSource().getWorkingSet().maxWorkerThreads);
					m_threadsRunning.setValue(e.m_threadsRunning);
				} );
		}

		@Override public void itemChanged(StateObjectEvent e)
		{
			SwingUtilities.invokeLater(() -> barModel.itemChanged(e));
		}

		public void reset()
		{	barModel.reset();
		}

		private final static class BarModel implements StackedBarModel
		{
			@SuppressWarnings("unchecked")
			private final HashSet<StateObject>[] categories = new HashSet[RowState.values.length];
			{ for (int i = 0; i < categories.length; ++i)
					categories[i] = new HashSet<>();
			}

			@Override public int getCount()
			{	return RowState.values.length;
			}

			@Override public float getValueAt(int index)
			{	return categories[index].size();
			}

			@Override public String getTextAt(int index)
			{	return null;
			}

			@Override public String getToolTipTextAt(int index)
			{	return RowState.values[index].name() + ": " + Integer.toString(categories[index].size());
			}

			public void itemChanged(StateObjectEvent e)
			{switchBlock:
				switch (e.type)
				{case INSERT:
					categories[e.item.getState().ordinal()].add(e.item);
					break;
				 case DELETE:
					for (HashSet<StateObject> cat : categories)
						if (cat.remove(e.item))
							break switchBlock;
					return; // Nothing changed
				 case UPDATE:
					HashSet<StateObject> myCat = categories[e.item.getState().ordinal()];
					if (!myCat.add(e.item))
						return; // Nothing changed
					for (HashSet<StateObject> cat : categories)
						if (cat != myCat && cat.remove(e.item))
							break;
					break;
				}
				fireChanged();
			}

			public void reset()
			{	for (HashSet<StateObject> cat : categories)
					cat.clear();
			}

			private final ArrayList<StackedBarModelListener> listeners = new ArrayList<>();
			@Override public void addStackedBarModelListener(StackedBarModelListener l)
			{	listeners.add(l);
			}
			@Override public void removeStackedBarModelListener(StackedBarModelListener l)
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
