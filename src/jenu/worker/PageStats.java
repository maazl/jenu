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

import static jenu.utils.Statics.*;

/**
 * Records information for exactly one HTML page.
 * @comment The read only method of this class are thread-safe while the mutating methods are not unless otherwise noted.
 */
public final class PageStats
{
	/** Dummy page for all excluded link targets. */
	public static final PageStats EXCLUDED = new PageStats("", false);
	static
	{	EXCLUDED.state = PageState.EXCLUDED;
	}

	/** Absolute URL of this document as String, primary key, blank for dummy entries. */
	public final String sUrl;
	/** Absolute URL as URL class, can be null if sUrl is broken. */
	public final URL url;
	/** Internal URL */
	public final boolean isInternal;

	PageStats(String strURL, boolean internal)
	{
		if (strURL == null)
			throw new NullPointerException();
		sUrl = strURL;
		URL url = null;
		if (strURL.length() != 0)
			try
			{	url = new URL(strURL);
			} catch (MalformedURLException e)
			{	setError(EventType.URL_error, e.getMessage());
			}
		this.url = url;
		isInternal = internal;
	}

	/** What is the task status of this page excluding anchor messages? */
	public PageState getPageState()
	{	return state;
	}
	/** What is the task status of this page including anchor messages? */
	public PageState getTotalState()
	{	return anchorMessages != null ? PageState.FAILED : state;
	}
	private volatile PageState state = PageState.PENDING;

	/** Which error types occurred, if any? */
	public EnumSet<EventType> getEvents()
	{	return events;
	}
	private final EnumSet<EventType> events = EnumSet.noneOf(EventType.class);

	/** Error text (mutilline) excluding anchor messages */
	public String getPageMessages()
	{	return messages;
	}
	private volatile String messages = null;
	/** Error text (mutilline) including anchor messages */
	public String getTotalMessages()
	{	return strcat(messages, anchorMessages, '\n');
	}
	private volatile String anchorMessages = null;

	/** Time taken to analyze this document. &lt; 0 =&gt; in progress. */
	public long getDuration()
	{	return duration;
	}
	private volatile long duration = 0;

	void setRunning()
	{
		state = PageState.RUNNING;
		duration -= System.currentTimeMillis();
	}

	void setInfo(EventType type, String message)
	{
		events.add(type);
		if (message == null)
			return;
		if (type == EventType.Bad_anchor)
			anchorMessages = strcat(anchorMessages, message, '\n');
		else
			messages = strcat(messages, message, '\n');
	}

	void setWarning(EventType type, String message)
	{
		state = PageState.WARNING;
		setInfo(type, message);
	}

	void setError(EventType type, String message)
	{
		state = PageState.FAILED;
		setInfo(type, message);
	}

	void setDone()
	{
		duration += System.currentTimeMillis();
		if (state != PageState.FAILED)
			state = PageState.DONE;
		// Check anchors of links added meanwhile
		linksIn.forEach(this::checkAnchor);
	}

	void setRetry()
	{
		state = PageState.RETRY;
		// reset state
		events.clear();
		messages = "";

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
	void appendTitle(String title)
	{	this.title = strcat(this.title, title);
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
		l.TargetPage = this;
		if (state.compareTo(PageState.DONE) >= 0)
			checkAnchor(l); // page is complete, check anchors immediately
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

	/** Referenced anchor names <em>not</em> defined in this document. */
	public Set<String> getBadAnchors()
	{	Set<String> ret = badAnchorsRO;
		return ret != null ? ret : Collections.<String>emptySet();
	}
	/** Add bad anchor, thread-safe
	 * @param name Name
	 * @return false: the bad anchor was identified before.
	 */
	private boolean addBadAnchor(String name)
	{	if (badAnchors == null)
		{	badAnchors = Collections.synchronizedSet(new HashSet<>());
			badAnchorsRO = Collections.unmodifiableSet(badAnchors);
		}
		return badAnchors.add(name);
	}
	private Set<String> badAnchors = null;
	private volatile Set<String> badAnchorsRO = null;


	private void checkAnchor(Link l)
	{	// already done?
		if (l.getAnchorState() != null) // check implies l.Anchor != null
			return;
		// There is a race condition here. But on worst case the check is done twice.
		boolean ok = getAnchors().contains(l.Anchor);
		l.setAnchorState(ok);
		if (!ok && addBadAnchor(l.Anchor))
			setError(EventType.Bad_anchor, '#' + l.Anchor);
	}

	public String toString()
	{
		String result =
			"URL         = " + url + '\n' +
			"status      = " + events + '\n' +
			"errorString = " + messages + '\n' +
			"contentType = " + contentType + '\n' +
			"size        = " + size + '\n' +
			"title       = " + title + '\n' +
			"date        = " + date + '\n' +
			"level       = " + level + '\n' +
			"linksIn     = " + linksIn + '\n' +
			"linksOut    = " + linksOut + '\n';
		return result;
	}
}
