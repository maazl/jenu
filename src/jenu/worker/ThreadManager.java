package jenu.worker;

import java.util.Vector;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.lang3.ArrayUtils;


public final class ThreadManager extends Thread
{
	private String m_base = null;

	// Threads keeps getting shoved onto here by PageGrabber
	// except for the first one, which is shoved on during RUN
	private Set<String>				m_urlsAll					= new HashSet<String>();
	private Vector<PageStats>	m_statsAll				= new Vector<PageStats>();
	private Set<PageStats>		m_statsToStart		= new HashSet<PageStats>();
	private Set<PageStats>		m_statsDone				= new HashSet<PageStats>();
	private Set<PageStats>		m_statsRunning		= new HashSet<PageStats>();
	private Set<PageGrabber>	m_threadsRunning	= new HashSet<PageGrabber>();
	// private Vector m_threadPool = new Vector();

	// private Vector m_allThreads = new Vector();
	private boolean						m_scheduledStop			= false;
	private boolean						m_scheduledPause		= false;
	private int								m_concurrentThreads	= 10;

	private volatile JenuThreadListener[] m_threadListeners = new JenuThreadListener[0];
	private volatile JenuPageListener[] m_pageListeners = new JenuPageListener[0];

	public ThreadManager(String base_url)
	{
		m_base = base_url;
		addURL(base_url);
	}

	private static String getFile(String url)
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

				firePageEvent(stats, true);
				fireThreadEvent();
				notifyAll();
			}
		}
	}

	public void threadStarted(PageGrabber page)
	{
		firePageEvent(page.getStats(), false);
	}

	public synchronized void threadFinished(PageGrabber page)
	{
		m_threadsRunning.remove(page);
		// m_threadPool.add(page);
		PageStats stats = page.getStats();
		m_statsRunning.remove(stats);
		m_statsDone.add(stats);

		Vector<String> linksOut = stats.linksOut;
		firePageEvent(stats, false);
		fireThreadEvent();
		for (int i = 0; i < linksOut.size(); i++)
		{
			String url = linksOut.get(i);
			addURL(url);
		}
		notifyAll();
	}

	private synchronized void waitForThreadToFinish()
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

	private synchronized void waitForThreadToStart()
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

	public PageState getRowState(int row)
	{
		return m_statsAll.get(row).getRunState();
	}

	public void startNextThread()
	{
		PageStats stats = m_statsToStart.iterator().next();
		m_statsToStart.remove(stats);
		m_statsRunning.add(stats);
		PageGrabber pg = getPageGrabber(stats);
		m_threadsRunning.add(pg);
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

	// Methods for JenuThreadListeners
	public synchronized void addThreadListener(JenuThreadListener l)
	{
		m_threadListeners = ArrayUtils.add(m_threadListeners, l);
	}

	public synchronized void removeThreadListener(JenuThreadListener l)
	{
		m_threadListeners = ArrayUtils.removeElement(m_threadListeners, l);
	}

	private void fireThreadEvent()
	{
		JenuThreadListener[] l = m_threadListeners;
		if (l != null)
		{	JenuThreadEvent e = new JenuThreadEvent(this, m_statsAll.size(), m_statsDone.size(), m_concurrentThreads, m_threadsRunning.size(), m_statsToStart.size());
			for (JenuThreadListener i : l)
				i.threadStateChanged(e);
		}
	}

	// Methods for JenuPageListeners
	public synchronized void addPageListener(JenuPageListener l)
	{
		m_pageListeners = ArrayUtils.add(m_pageListeners, l);
	}

	public synchronized void removePageListener(JenuPageListener l)
	{
		m_pageListeners = ArrayUtils.removeElement(m_pageListeners, l);
	}

	private void firePageEvent(PageStats page, boolean isNew)
	{
		JenuPageListener[] l = m_pageListeners;
		if (l != null)
		{	JenuPageEvent e = new JenuPageEvent(this, page, isNew);
			for (JenuPageListener i : l)
				i.pageChanged(e);
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
