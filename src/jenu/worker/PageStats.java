package jenu.worker;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.MalformedURLException;

/**
 * Records information for exactly one HTML page.
 * @comment The read only method of this class are thread-safe while the mutating methods are not unless otherwise noted.
 */
public final class PageStats
{
	/** Absolute URL of this document as String, primary key */
	public final String sUrl;
	/** Absolute URL as URL class, can be null if sUrl is broken. */
	public final URL url;

	PageStats(String strURL)
	{
		if (strURL == null)
			throw new NullPointerException();
		sUrl = strURL;
		URL url = null;
		try
		{	url = new URL(strURL);
		} catch (MalformedURLException e)
		{	setError(ErrorType.URLError, e.getMessage());
		}
		this.url = url;
	}

	/** What is the task status of this page? */
	public PageState getRunState()
	{	return runState;
	}
	private volatile PageState runState = PageState.PENDING;

	/** Which error types occurred, if any? */
	public EnumSet<ErrorType> getStatus()
	{	return status;
	}
	private final EnumSet<ErrorType> status = EnumSet.noneOf(ErrorType.class);

	/** Err text (mutilline) */
	public String getErrorString()
	{	return errorString;
	}
	private volatile String errorString = "";

	/** Time taken to analyze this document. &lt; 0 =&gt; in progress. */
	public long getDuration()
	{	return duration;
	}
	private volatile long duration = 0;

	void setRunning()
	{
		runState = PageState.RUNNING;
		duration -= System.currentTimeMillis();
	}

	void setError(ErrorType type, String message)
	{
		runState = PageState.FAILED;
		status.add(type);
		errorString += '\n' + message;
	}

	void setDone()
	{
		duration += System.currentTimeMillis();
		if (runState != PageState.FAILED)
			runState = PageState.DONE;
	}

	void setRetry()
	{
		runState = PageState.RETRY;
		// reset state
		status.clear();
		errorString = "";

		contentType = null;
		size        = -1;
		lines       = -1;
		title       = null;
		date        = null;
		if (linksOut != null)
			linksOut.clear();
		if (anchors != null)
			anchors.clear();
	}


	/** MIME type */
	public String getContentType()
	{	return contentType;
	}
	void setContentType(String contentType)
	{	this.contentType = contentType;
	}
	private volatile String contentType = null;

	/** Object size in bytes or -1 if unknown. */
	public long getSize()
	{	return size;
	}
	void setSize(long size)
	{	this.size = size;
	}
	private volatile long size = -1;

	/** Source code lines of document or -1 if unknown */
	public int getLines()
	{	return lines;
	}
	void setLines(int lines)
	{	this.lines = lines;
	}
	private volatile int lines = -1;

	/** Document title or null if not available */
	public String getTitle()
	{	return title;
	}
	void setTitle(String title)
	{	this.title = title;
	}
	private volatile String title = null;

	/** Document/file modification date or null if unknown */
	public Date getDate()
	{	return date;
	}
	void setDate(Date date)
	{	this.date = date;
	}
	private volatile Date date = null;

	/** Get links from other documents that point to this object. */
	public Collection<Link> getLinksIn()
	{	return linksInRO;
	}
	/** add incoming link, thread-safe! */
	void addLinkIn(Link l)
	{	linksIn.add(l);
	}
	private final Vector<Link> linksIn = new Vector<>();
	private final Collection<Link> linksInRO = Collections.unmodifiableCollection(linksIn);


	/** Link depth of this document, i.e. shortest path from one of the starting points.
	 * 0 = starting point, Integer.MAX_VALUE = undefined. */
	public int getLevel()
	{	return level.intValue();
	}
	/** Set link depth. Subsequent calls can only <em>lower</em> the value.
	 * @param depth
	 * @return old value
	 */
	int reduceLevel(int depth)
	{	int old = level.intValue();
		while (old > depth)
		{ int old2 = old;
			old = level.compareAndExchange(old, depth);
			if (old2 == old)
				break;
		}
		return old;
	}
	private final AtomicInteger level = new AtomicInteger(Integer.MAX_VALUE);

	/** Get links from this document to other objects. */
	public Collection<Link> getLinksOut()
	{	Collection<Link> ret = linksOutRO;
		return ret != null ? ret : Collections.<Link>emptyList();
	}
	void addLinkOut(Link l)
	{	if (linksOut == null)
		{	linksOut = new Vector<>();
			linksOutRO = Collections.unmodifiableCollection(linksOut);
		}
		linksOut.add(l);
	}
	private Vector<Link> linksOut = null;
	private volatile Collection<Link> linksOutRO = null;

	/** Anchor names defined in this document. */
	public Set<String> getAnchors()
	{	Set<String> ret = anchorsRO;
		return ret != null ? ret : Collections.<String>emptySet();
	}
	void addAnchor(String name)
	{	if (anchors == null)
		{	anchors = Collections.synchronizedSet(new HashSet<>());
			anchorsRO = Collections.unmodifiableSet(anchors);
		}
		anchors.add(name);
	}
	private Set<String> anchors = null;
	private volatile Set<String> anchorsRO = null;


	public String toString()
	{
		String result =
			"URL         = " + url + "\n" +
			"status      = " + status + "\n" +
			"errorString = " + errorString + "\n" +
			"contentType = " + contentType + "\n" +
			"size        = " + size + "\n" +
			"title       = " + title + "\n" +
			"date        = " + date + "\n" +
			"level       = " + level + "\n" +
			"linksIn     = " + linksIn + "\n" +
			"linksOut    = " + linksOut + "\n";
		return result;
	}
}
