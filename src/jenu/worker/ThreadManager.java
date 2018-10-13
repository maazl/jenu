package jenu.worker;

import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import java.awt.Color;
import javax.swing.table.TableModel;

import jenu.ui.JenuInternalFrame;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

public class ThreadManager extends Thread implements TableModel
{
	protected String m_base = null;

	// Threads keeps getting shoved onto here by PageGrabber
	// except for the first one, which is shoved on during RUN
	protected Set<String>				m_urlsAll					= new HashSet<String>();
	protected Vector<PageStats>	m_statsAll				= new Vector<PageStats>();
	protected Set<PageStats>		m_statsToStart		= new HashSet<PageStats>();
	protected Set<PageStats>		m_statsDone				= new HashSet<PageStats>();
	protected Set<PageStats>		m_statsRunning		= new HashSet<PageStats>();
	protected Set<PageGrabber>	m_threadsRunning	= new HashSet<PageGrabber>();
	// protected Vector m_threadPool = new Vector();

	// protected Vector m_allThreads = new Vector();
	protected boolean						m_scheduledStop			= false;
	protected boolean						m_scheduledPause		= false;
	protected JenuInternalFrame	m_owner							= null;
	protected int								m_concurrentThreads	= 10;

	protected Vector<JenuThreadListener> m_threadListeners = new Vector<>();

	public ThreadManager(String base_url, JenuInternalFrame owner)
	{
		m_base = base_url;
		m_owner = owner;
		addURL(base_url);
	}

	public String getFile(String url)
	{
		if (url == null)
			return null;

		int i;
		if ((i = url.indexOf("#")) >= 0)
		{
			// System.out.println("url = " + url);
			// System.out.println("mod = " + url.substring(0, i-1));
			return url.substring(0, i - 1);
		}
		return url;
	}

	public synchronized void addURL(String url)
	{
		String urlFile = getFile(url);
		if (url.startsWith(m_base))
		{
			if (m_urlsAll.contains(urlFile))
			{
				// System.out.println(url + " is already here");
				// PageStats s = (PageStats) m_statsAll.get(m_statsAll.indexOf(urlFile));
				// s.addLinkIn(url);
			} else
			{
				m_urlsAll.add(urlFile);
				PageStats stats = new PageStats(urlFile);
				m_statsAll.add(stats);
				m_statsToStart.add(stats);
			}
			int row = m_statsAll.size() - 1;
			fireTableModelListeners(new TableModelEvent(this, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
			fireThreadEvent();
			notifyAll();
		}
	}

	public synchronized void threadFinished(PageGrabber page)
	{
		m_threadsRunning.remove(page);
		// m_threadPool.add(page);
		PageStats stats = page.getStats();
		m_statsRunning.remove(stats);
		m_statsDone.add(stats);

		Vector<String> linksOut = stats.linksOut;
		int row = m_statsAll.indexOf(stats);
		fireTableModelListeners(new TableModelEvent(this, row));
		fireThreadEvent();
		for (int i = 0; i < linksOut.size(); i++)
		{
			String url = linksOut.get(i);
			addURL(url);
		}
		notifyAll();
	}

	public synchronized void waitForThreadToFinish()
	{
		if (m_threadsRunning.size() > 0)
		{
			try
			{
				// System.out.print("Wait: " + m_threadsRunning.size());
				wait();
			} catch (InterruptedException e)
			{}
		}
	}

	public synchronized void waitForThreadToStart()
	{
		if (m_statsToStart.size() == 0)
		{
			try
			{
				wait();
			} catch (InterruptedException e)
			{}
		}
	}

	public synchronized void waitForUnPause()
	{
		while (m_scheduledPause && !m_scheduledStop)
		{
			try
			{
				wait();
			} catch (InterruptedException e)
			{}
		}
	}

	public synchronized void waitForAllThreadsToFinish()
	{
		while (m_threadsRunning.size() > 0)
		{
			try
			{
				wait();
			} catch (InterruptedException e)
			{}
		}
	}

	public void run()
	{
		// Go until there are no threads on m_threadsToStart or m_threadsRunning.
		System.out.println("Starting...");
		fireThreadEvent();
		while (!m_scheduledStop && (m_statsToStart.size() > 0 || m_threadsRunning.size() > 0))
		{
			waitForUnPause();
			if (m_threadsRunning.size() > m_concurrentThreads)
			{
				System.out.println("Error:  started too many threads.");
			}
			while (m_threadsRunning.size() >= m_concurrentThreads)
			{
				waitForThreadToFinish();
			}
			if (m_statsToStart.size() > 0 && m_threadsRunning.size() < m_concurrentThreads)
			{
				// start one of the threads.
				startNextThread();
			}
			if (m_statsToStart.size() == 0)
			{
				waitForThreadToStart();
			}
		}
		waitForAllThreadsToFinish();
		m_owner.setStopped();
		fireThreadEvent();
		System.out.println("Ending...");
		m_urlsAll = null;
		m_statsToStart = null;
		m_statsDone = null;
		m_statsRunning = null;
		m_threadsRunning = null;
		System.gc();
	}

	public synchronized void stopRunning()
	{
		m_scheduledStop = true;
	}

	public synchronized void pauseRunning(boolean pause)
	{
		m_scheduledPause = pause;
	}

	public synchronized void sortByColumn(int columnIndex, int direction)
	{
		if (m_statsAll != null)
		{
			if (m_statsAll.size() > 0)
			{
				Comparator<PageStats> c = PageStats.getComparator(columnIndex, direction);
				Collections.sort(m_statsAll, c);
				fireTableModelListeners(new TableModelEvent(this));
			}
		}
	}

	public int getRowState(int row)
	{
		return m_statsAll.get(row).getRunState();
	}

	public Color getRowStateColor(int row)
	{
		return m_statsAll.get(row).getRunStateColor();
	}

	public void startNextThread()
	{
		PageStats stats = m_statsToStart.iterator().next();
		m_statsToStart.remove(stats);
		m_statsRunning.add(stats);
		PageGrabber pg = getPageGrabber(stats);
		m_threadsRunning.add(pg);
		int row = m_statsAll.indexOf(stats);
		fireTableModelListeners(new TableModelEvent(this, row));
		fireThreadEvent();
		pg.start();
	}

	public PageGrabber getPageGrabber(PageStats stats)
	{
		PageGrabber result = null;
		// if (m_threadPool.size() > 0) {
		// result = (PageGrabber) m_threadPool.get(0);
		// m_threadPool.remove(0);
		// } else {
		result = new PageGrabber(this);
		// }
		result.reset(stats);
		return result;
	}

	public void fireTableModelListeners(TableModelEvent event)
	{
		Vector<TableModelListener> listeners = new Vector<>(m_tableModelListeners);

		for (TableModelListener l : listeners)
			l.tableChanged(event);
	}

	/*
	 * The tableModel interface. This uses the status of all the threads as the
	 * table data.
	 */
	protected Vector<TableModelListener> m_tableModelListeners = new Vector<>();

	public int getRowCount()
	{
		return m_statsAll.size();
	}

	public int getColumnCount()
	{
		return PageStats.getColumnCount();
	}

	public String getColumnName(int columnIndex)
	{
		return PageStats.getColumnName(columnIndex);
	}

	public Class<?> getColumnClass(int columnIndex)
	{
		return PageStats.getColumnClass(columnIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}

	public synchronized Object getValueAt(int rowIndex, int columnIndex)
	{
		return m_statsAll.get(rowIndex).getColumn(columnIndex);
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{}

	public void addTableModelListener(TableModelListener l)
	{
		m_tableModelListeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l)
	{
		m_tableModelListeners.remove(l);
	}

	// Methods for JenuThreadListeners
	public synchronized void addThreadListener(JenuThreadListener l)
	{
		if (!m_threadListeners.contains(l))
		{
			m_threadListeners.add(l);
		}
	}

	public synchronized void removeThreadListener(JenuThreadListener l)
	{
		if (m_threadListeners.contains(l))
		{
			m_threadListeners.remove(l);
		}
	}

	public synchronized void fireThreadEvent()
	{
		JenuThreadEvent e = new JenuThreadEvent(this, m_statsAll.size(), m_statsDone.size(), m_concurrentThreads,
			m_threadsRunning.size(), m_statsToStart.size());
		JenuThreadListener l[] = new JenuThreadListener[m_threadListeners.size()];
		m_threadListeners.copyInto(l);
		for (int i = 0; i < l.length; i++)
		{
			l[i].threadStateChanged(e);
		}
	}

	// public class TmURL
	// extends URL
	// {
	// public TmURL(String spec) {super(spec);}
	// public TmURL(String protocol, String host, int port, String file) {
	// super(protocol, host, port, file); }
	// public TmURL(String protocol, String host, int port, String file,
	// URLStreamHandler handler) { super (protocol, host, port, file, handler); }
	// public TmURL(String protocol, String host, String file) { super(protocol,
	// host, file); }
	// public TmURL(URL context, String spec) { super(context, spec); }
	// public TmURL(URL context, String spec, URLStreamHandler handler) { super
	// (context, spec, handler); }
	//
	// public boolean equals(Object o) {
	// return sameFile((TmURL) o);
	// }
	// }
}
