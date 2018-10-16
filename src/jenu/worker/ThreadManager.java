package jenu.worker;

import java.util.Vector;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.ArrayUtils;


public final class ThreadManager
{
	private String m_base = null;

	// Threads keeps getting shoved onto here by PageGrabber
	// except for the first one, which is shoved on during RUN
	private HashMap<String,PageStats>	m_urlsAll			= new HashMap<String,PageStats>();
	private ArrayDeque<PageStats>	m_statsToStart		= new ArrayDeque<PageStats>();
	private int										m_statsDone				= 0;
	private HashSet<PageGrabber>	m_threadsRunning	= new HashSet<PageGrabber>();

	private boolean						m_scheduledStop			= false;
	private boolean						m_scheduledPause		= false;
	private int								m_concurrentThreads	= 10;

	private volatile JenuThreadListener[] m_threadListeners = new JenuThreadListener[0];
	private volatile JenuPageListener[] m_pageListeners = new JenuPageListener[0];

	public ThreadManager(String base_url)
	{
		m_base = base_url;
		addURL(base_url, null);
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

	private synchronized void addURL(String url, String origin)
	{
		String urlFile = getFile(url);
		if (url.startsWith(m_base))
		{
			PageStats stats = m_urlsAll.get(urlFile);
			if (stats == null)
			{	stats = new PageStats(urlFile);
				m_urlsAll.put(urlFile, stats);
				m_statsToStart.add(stats);
			}
			if (origin != null)
				stats.addLinkIn(origin);

			firePageEvent(stats, true);
		}
	}

	/**
	 * Called by the PageGrabber worker when a task hat finished.
	 * @param grabber Caller
	 * @return true: a new task has been scheduled by a call to grabber.reset(),
	 * false: the thread should die.
	 */
	synchronized boolean nextTask(PageGrabber grabber)
	{
		// Last task has finished
		{	PageStats stats = grabber.getStats();
			stats.setDone();
			++m_statsDone;
			firePageEvent(stats, false);

			Vector<String> linksOut = stats.linksOut;
			for (String url : linksOut)
				addURL(url, stats.sUrl);
		}
		// schedule new task?
		if (m_scheduledPause || m_scheduledStop || m_statsToStart.size() == 0)
		{	// no more work for now, thread will die.
			m_threadsRunning.remove(grabber);
			fireThreadEvent();
			return false;
		}
		do
		{	scheduleTask(grabber);
			// start further threads?
			grabber = startNextThread();
		} while (grabber != null);
		fireThreadEvent();
		return true;
	}

	public void start()
	{
		PageGrabber grabber;
		while ((grabber = startNextThread()) != null)
			scheduleTask(grabber);
		fireThreadEvent();
	}

	public synchronized boolean isAlive()
	{
		return m_threadsRunning.size() != 0;
	}

	public synchronized void stopRunning()
	{
		m_scheduledStop = true;
		m_statsToStart.clear();
		fireThreadEvent();
	}

	public synchronized void pauseRunning(boolean pause)
	{
		m_scheduledPause = pause;
		if (!pause)
			start();
	}

	/**
	 * Try to start new thread.
	 * @return new Thread or null if a new thread is currently not required or not wanted.
	 * You need to schedule a task to the new thread if you got one.
	 */
	private PageGrabber startNextThread()
	{
		if (m_threadsRunning.size() >= m_concurrentThreads || m_statsToStart.size() == 0)
			return null;
		PageGrabber grabber = new PageGrabber(this);
		m_threadsRunning.add(grabber);
		grabber.start();
		return grabber;
	}

	private void scheduleTask(PageGrabber grabber)
	{
		PageStats stats = m_statsToStart.pop();
		grabber.reset(stats);
		stats.setRunning();
		firePageEvent(stats, false);
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
		{	JenuThreadEvent e = new JenuThreadEvent(this, m_urlsAll.size(), m_statsDone, m_concurrentThreads, m_threadsRunning.size(), m_statsToStart.size());
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
