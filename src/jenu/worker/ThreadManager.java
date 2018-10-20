package jenu.worker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;


public final class ThreadManager
{
	public int concurrentThreads = 10;

	/** Current WorkingSet to be executed. */
	private WorkingSet Cfg;
	private boolean    scheduledStop = true;
	private boolean    scheduledPause;

	private HashMap<String, PageStats> urlsAll        = new HashMap<String, PageStats>();
	private ArrayDeque<PageStats>      statsToStart   = new ArrayDeque<PageStats>();
	private int                        statsDone      = 0;
	private HashSet<PageGrabber>       threadsRunning = new HashSet<PageGrabber>();


	/** Initialize worker to analyze a site.
	 * @param set Configuration to execute. */
	public synchronized void start(WorkingSet set)
	{
		if (threadsRunning.size() != 0)
			throw new IllegalStateException("Cannot schedule a new working set while executing.");

		Cfg = set;
		scheduledStop = false;
		scheduledPause = false;

		// apply defaults
		if (Cfg.StartingPoints.size() == 0)
			Cfg.StartingPoints.addAll(Cfg.Sites);
		else if (Cfg.Sites.size() == 0)
			for (String sp : Cfg.StartingPoints)
				Cfg.Sites.add(getPath(sp));

		// normalize Site URLs
		URL rootURL = null;
		for (int i = 0; i < Cfg.Sites.size(); ++i)
		{	try
			{	URL url = new URL(Cfg.Sites.get(i));
				if (rootURL == null)
					rootURL = url;
				Cfg.Sites.set(i, url.toExternalForm());
			} catch (MalformedURLException e)
			{ } // We can safely ignore this error here because it gets repeated by the PageGrabber.
		}

		// sort Sites and remove duplicates
		{	Collections.sort(Cfg.Sites);
			String last = null;
			Iterator<String> it = Cfg.Sites.iterator();
			while (it.hasNext())
			{	String url = it.next();
				if (url.equals(last))
					it.remove();
				else
					last = url;
			}
		}

		// schedule starting points
		for (String sp : Cfg.StartingPoints)
			addLink(new Link(null, sp, rootURL, 0), 0);

		// and nor go parallel ...
		startThreads();
	}

	/** Check whether this instance is still working. */
	public synchronized boolean isAlive()
	{
		return threadsRunning.size() != 0;
	}

	/** Interrupt the current working set.
	 * The method returns immediately. The operation is deferred. */
	public synchronized void stopRunning()
	{
		scheduledStop = true;
		statsToStart.clear();
		fireThreadEvent();
	}

	/** Pause the current working set
	 * @param pause true: pause, false: resume. */
	public synchronized void pauseRunning(boolean pause)
	{
		scheduledPause = pause;
		if (!pause)
			startThreads();
	}

	/** Wait for all threads to finish. This works for ordinary completion as well as
	 * for pause or stop requests. The method returns if no more worker thread is running. */
	public void waitForCompletion()
	{
		Thread[] threads;
		synchronized(this)
		{	if (!scheduledStop)
				throw new IllegalStateException("Cannot wait for completion unless stopRunning() has been called.");
			threads = threadsRunning.toArray(new Thread[threadsRunning.size()]);
		}
		for (Thread t : threads)
			try
			{	t.wait();
			} catch (InterruptedException e)
			{ }
	}

	private void addLink(Link link, int level)
	{
		if (Cfg.CheckExternalURLs || isInternalUrl(link.Target))
		{
			PageStats stats = urlsAll.get(link.Target);
			boolean isNew = stats == null;
			if (isNew)
			{	stats = new PageStats(link.Target);
				urlsAll.put(link.Target, stats);
				statsToStart.add(stats);
			}

			if (link.Type != null)
				stats.addLinkIn(link);

			applyLevelRecursive(stats, level);

			firePageEvent(stats, isNew);
		}
	}

	private boolean applyLevelRecursive(PageStats stats, int level)
	{	++level;
		if (!stats.setLevel(level))
			return false;
		// level lowered, i.e. found a shorter path => apply recursively
		for (Link l : stats.linksOut)
		{	PageStats stats2 = urlsAll.get(l.Target);
			if (stats2 != null)
				if (applyLevelRecursive(stats2, level))
					firePageEvent(stats2, false);
		}
		return true;
	}

	/** Checks whether an URL belong to the current list of sites.
	 * @return true: yes */
	private boolean isInternalUrl(String url)
	{
		int p = Collections.binarySearch(Cfg.Sites, url);
		if (p >= 0)
			return true; // exact hit
		return p != -1 && url.startsWith(Cfg.Sites.get(-2 - p));
	}

	/** Retrieve path component of URL */
	private static String getPath(String url)
	{	int lastSlash = -1;
		for (int i = 0; i < url.length(); ++i)
		{	switch (url.charAt(i))
			{case '/':
				lastSlash = i;
			 default:
				continue;
			 case '?':
			 case '#':
				break;
			}
			break;
		}
		return lastSlash >= 0 ? url.substring(0, lastSlash + 1) : url;
	}

	/** Initially start threads until the limit is reached.
	 * This function MUST be called from synchronized context. */
	private void startThreads()
	{
		PageGrabber grabber;
		while ((grabber = startNextThread()) != null)
			scheduleTask(grabber);
		fireThreadEvent();
	}

	/** Called by the PageGrabber worker when a task hat finished.
	 * @param grabber Caller
	 * @return true: a new task has been scheduled by a call to grabber.reset(),
	 * false: the thread should die. */
	synchronized boolean nextTask(PageGrabber grabber)
	{
		// Last task has finished
		{	PageStats stats = grabber.getStats();
			stats.setDone();
			++statsDone;
			firePageEvent(stats, false);

			if (isInternalUrl(stats.sUrl))
				for (Link link : stats.linksOut)
					addLink(link, stats.level);
		}
		// schedule new task?
		if (scheduledPause || scheduledStop || statsToStart.size() == 0)
		{	// no more work for now, thread will die.
			threadsRunning.remove(grabber);
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

	/**
	 * Try to start new thread.
	 * @return new Thread or null if a new thread is currently not required or not wanted.
	 * You need to schedule a task to the new thread if you got one.
	 */
	private PageGrabber startNextThread()
	{
		if (threadsRunning.size() >= concurrentThreads || statsToStart.size() == 0)
			return null;
		PageGrabber grabber = new PageGrabber(this);
		threadsRunning.add(grabber);
		grabber.start();
		return grabber;
	}

	private void scheduleTask(PageGrabber grabber)
	{
		PageStats stats = statsToStart.pop();
		grabber.reset(stats);
		stats.setRunning();
		firePageEvent(stats, false);
	}

	// Methods for JenuThreadListeners
	private volatile JenuThreadListener[]	threadListeners	= new JenuThreadListener[0];

	public synchronized void addThreadListener(JenuThreadListener l)
	{
		threadListeners = ArrayUtils.add(threadListeners, l);
	}

	public synchronized void removeThreadListener(JenuThreadListener l)
	{
		threadListeners = ArrayUtils.removeElement(threadListeners, l);
	}

	private void fireThreadEvent()
	{
		JenuThreadListener[] l = threadListeners;
		if (l != null)
		{	JenuThreadEvent e = new JenuThreadEvent(this, urlsAll.size(), statsDone, concurrentThreads, threadsRunning.size(), statsToStart.size());
			for (JenuThreadListener i : l)
				i.threadStateChanged(e);
		}
	}

	// Methods for JenuPageListeners
	private volatile JenuPageListener[]		pageListeners		= new JenuPageListener[0];

	public synchronized void addPageListener(JenuPageListener l)
	{
		pageListeners = ArrayUtils.add(pageListeners, l);
	}

	public synchronized void removePageListener(JenuPageListener l)
	{
		pageListeners = ArrayUtils.removeElement(pageListeners, l);
	}

	private void firePageEvent(PageStats page, boolean isNew)
	{
		JenuPageListener[] l = pageListeners;
		if (l != null)
		{	JenuPageEvent e = new JenuPageEvent(this, page, isNew);
			for (JenuPageListener i : l)
				i.pageChanged(e);
		}
	}
}
