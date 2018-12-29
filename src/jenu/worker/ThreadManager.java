package jenu.worker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashSet;

import jenu.model.Link;
import jenu.model.MessageType;
import jenu.utils.AtomicArraySet;
import jenu.utils.KeyedHashSet;

/** Worker class that processes a WorkingSet. */
public final class ThreadManager
{
	/** Current WorkingSet to be executed. */
	private WorkingSet cfg;
	public WorkingSet getWorkingSet()
	{	return cfg;
	}

	private boolean scheduledStop = true;
	private boolean scheduledPause;

	private final ArrayDeque<PageStats> statsToStart   = new ArrayDeque<>();
	private int                         statsDone      = 0;
	private final HashSet<PageGrabber>  threadsRunning = new HashSet<>();

	private final KeyedHashSet<String,PageStats> pagesByUrl = new KeyedHashSet<>(page -> page.sUrl);

	public void reset()
	{
		pagesByUrl.clear();
		statsDone = 0;
		firePageEvent(null, PageEventType.DELETE);
	}

	/** Initialize worker to analyze a site.
	 * @param set Configuration to execute. */
	public synchronized void start(WorkingSet set)
	{
		if (threadsRunning.size() != 0)
			throw new IllegalStateException("Cannot schedule a new working set while executing.");

		cfg = set;
		scheduledStop = false;
		scheduledPause = false;

		set.validate();

		// normalize site URLs
		for (int i = 0; i < cfg.sites.size(); ++i)
		{	PageStats page = getPage(null, cfg.sites.get(i));
			if (page.url != null)
				cfg.sites.set(i, page.url.toExternalForm());
		}

		// schedule starting points
		for (String sp : cfg.startingPoints)
			followLink(new LinkStats(null, sp, null, 0));

		// and now go parallel ...
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

	/** Request a PageStats object for a link.
	 * @param context
	 * @param url
	 * @return
	 */
	private PageStats getPage(URL context, String url)
	{	assert url != null;
		URL asURL = null;
		MalformedURLException error = null;
		try
		{	asURL = new URL(context, url);
			url = asURL.toExternalForm();
		} catch (MalformedURLException e)
		{	error = e;
		}
		PageStats page = pagesByUrl.getByKey(url);
		if (page == null)
		{	// new page
			page = new PageStats(asURL, url);
			if (error != null)
			{	page.addError(MessageType.URL_error, error.getMessage());
				page.setDone();
			}
			pagesByUrl.add(page);
		}
		return page;
	}

	private void followLinks(PageStats page)
	{
		if (page.isInternal || cfg.followExternalRedirects)
			for (Link link : page.getLinksOut())
				if (page.isInternal || link.type == Link.REDIRECT) // Allow redirects always because w/o FollowExternalRedirects you won't get that far
					followLink((LinkStats)link);
	}

	private void followLink(LinkStats link)
	{
		int level = link.source != null ? link.source.getLevel() : -1;
		PageStats page = (PageStats)link.getTargetPage();

		// already done?
		boolean isNew = false;
		if (page == null)
		{	if (level >= cfg.maxDepth)
				return; // too deep

			page = getPage(link.source != null ? link.source.url : null, link.getTargetUrl());

			if (page.schedulePage())
			{	// My thread added the page
				isNew = true;
				if (cfg.isInternalUrl(page.sUrl))
					page.isInternal = true;
				if (!cfg.isExcluded(page.sUrl) && (cfg.checkExternalURLs || page.isInternal))
					statsToStart.add(page);
				else
					page.setExcluded();
			}

			if (link.type != null)
			{	page.addLinkIn(link);
				link.checkAnchor(); // check anchors immediately?
			}
		}

		// Apply level
		++level;
		int old = page.reduceLevel(level);
		if (old != Integer.MAX_VALUE && old > level)
			followLinks(page); // level lowered, i.e. found a shorter path => apply recursively

		firePageEvent(page, isNew ? PageEventType.NEW : PageEventType.LINKSIN);
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
		{	PageStats stats = grabber.m_stats;
			stats.setDone();
			++statsDone;
			followLinks(stats);
			firePageEvent(stats, PageEventType.STATE);
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

	/** Try to start new thread.
	 * @return new Thread or null if a new thread is currently not required or not wanted.
	 * You need to schedule a task to the new thread if you got one. */
	private PageGrabber startNextThread()
	{
		if (threadsRunning.size() >= cfg.maxWorkerThreads || statsToStart.size() == 0)
			return null;
		PageGrabber grabber = new PageGrabber(this);
		threadsRunning.add(grabber);
		grabber.start();
		return grabber;
	}

	private void scheduleTask(PageGrabber grabber)
	{
		PageStats stats = statsToStart.pop();
		grabber.m_stats = stats;
		stats.setRunning();
		firePageEvent(stats, PageEventType.STATE);
	}

	// Methods for JenuThreadListeners
	private final AtomicArraySet<WorkerListener> threadListeners	= new AtomicArraySet<>(new WorkerListener[0]);

	public final boolean addThreadListener(WorkerListener l)
	{	return threadListeners.add(l);
	}

	public final boolean removeThreadListener(WorkerListener l)
	{	return threadListeners.remove(l);
	}

	private void fireThreadEvent()
	{	WorkerListener[] listeners = threadListeners.get();
		if (listeners.length != 0)
		{	WorkerEvent e = new WorkerEvent(this, pagesByUrl.size(), statsDone, threadsRunning.size(), statsToStart.size());
			for (WorkerListener i : listeners)
				i.threadStateChanged(e);
		}
	}

	// Methods for JenuPageListeners
	private final AtomicArraySet<PageListener> pageListeners = new AtomicArraySet<>(new PageListener[0]);

	public final boolean addPageListener(PageListener l)
	{	return pageListeners.add(l);
	}

	public final boolean removePageListener(PageListener l)
	{	return pageListeners.remove(l);
	}

	private void firePageEvent(PageStats page, PageEventType type)
	{	PageListener[] listeners = pageListeners.get();
		if (listeners.length != 0)
		{	PageEvent e = new PageEvent(this, page, type);
			for (PageListener i : listeners)
				i.pageChanged(e);
		}
	}
}
