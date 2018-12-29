package jenu.worker;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import jenu.model.Link;
import jenu.model.Message;
import jenu.model.MessageType;
import jenu.model.Page;
import jenu.model.PageState;
import jenu.model.Severity;


/**
 * Records information for exactly one HTML page. Writable version on Page.
 */
final class PageStats extends Page
{
	/** Internal URL (injection from ThreadManager) */
	public boolean isInternal;

	/** Create a <em>valid</em> page.
	 * @param url URL of the page or null if no valid URL can be determined.
	 * @param sUrl URL of the page as string, never null. */
	public PageStats(URL url, String sUrl)
	{	super(url, sUrl);
	}

	/** schedule the current page for later processing
	 * @return false if the page has already been scheduled. */
	public boolean schedulePage()
	{	return state.compareAndSet(PageState.VIRGIN, PageState.PENDING);
	}
	public void setExcluded()
	{	boolean ret = state.compareAndSet(PageState.PENDING, PageState.EXCLUDED);
	  assert ret;
	}
	/** Notify about start of page processing */
	public void setRunning()
	{	boolean ret = state.compareAndSet(PageState.PENDING, PageState.RUNNING);
		assert ret;
		duration -= System.currentTimeMillis();
	}
	/** Notify about completion of page processing
	 * If the page is in state RUNNING duration is set to the time taken so far.
	 * Otherwise duration stays unchanged. */
	public void setDone()
	{	boolean ret = state.compareAndSet(PageState.RUNNING, PageState.DONE);
		if (ret)
			duration += System.currentTimeMillis();
		else
			state.set(PageState.DONE);
		for (Link l : linksIn)
			((LinkStats)l).checkAnchor();
	}
	/** Set failed page to RETRY state. This implicitly clears all collected information */
	public void setRetry()
	{	boolean ret = state.compareAndSet(PageState.RUNNING, PageState.RETRY);
		assert ret;
		// reset state
		events = null;

		contentType = null;
		size        = -1;
		lines       = -1;
		title       = null;
		date        = null;
		if (linksOut != null)
			linksOut.clear();
		anchors.setRelease(null);
	}

	/** Add page message */
	private void addMessage(Message e)
	{	if (events == null)
			events = new Vector<>();
		events.add(e);
	}
	final void addError(MessageType type, String text)
	{	addMessage(new Message(type, Severity.ERROR, text));
	}
	final void addWarning(MessageType type, String text)
	{	addMessage(new Message(type, Severity.Warning, text));
	}
	final void addInfo(MessageType type, String text)
	{	addMessage(new Message(type, Severity.Info, text));
	}

	/** Set MIME type */
	void setContentType(String contentType)
	{	this.contentType = contentType;
	}

	/** Set page size in bytes or -1 if unknown. */
	void setSize(long size)
	{	this.size = size;
	}

	/** Set no of code lines of document or -1 if unknown */
	void setLines(int lines)
	{	this.lines = lines;
	}

	/** Append string to document title */
	void setTitle(String title)
	{	this.title = title;
	}

	/** Set document/file modification date or null if unknown */
	void setDate(Date date)
	{	this.date = date;
	}

	/** add incoming link, thread-safe! */
	void addLinkIn(LinkStats l)
	{	l.setTarget(this);
		linksIn.add(l);
	}

	/** Set link depth. Subsequent calls can only <em>lower</em> the value.
	 * @param depth
	 * @return old value
	 */
	int reduceLevel(int depth)
	{	int old;
		do
		{	old = level.intValue();
			if (old <= depth)
				break;
		} while (!level.compareAndSet(old, depth));
		return old;
	}

	/** Add outgoing link */
	void addLinkOut(Link l)
	{	if (linksOut == null)
			linksOut = new Vector<>();
		linksOut.add(l);
	}

	private Map<String,Message> ensureAnchors()
	{	return anchors.weakLazyInit(() -> Collections.synchronizedMap(new HashMap<>()));
	}

	/** Internal placeholder for null because otherwise the is no atomic computeIfAbsent method. */
	private final static Message noMessage = new Message(MessageType.Bad_anchor, Severity.ERROR,  "");

	/** Add existing anchor defined in this document. */
	void addAnchor(String name)
	{	ensureAnchors().put(name, noMessage);
	}

	/** Check whether an anchor exists.
	 * The check must not be done unless the page has completed.
	 * @param name Anchor name to search for.
	 * @return null, if the anchor exists, an error Event if not. */
	Message checkAnchor(String name)
	{	if (state.get() != PageState.DONE)
			return null;
		Message ret = ensureAnchors().computeIfAbsent(name, (n) -> new Message(MessageType.Bad_anchor, Severity.ERROR, '#' + n));
		return ret == noMessage ? null : ret;
	}
}
