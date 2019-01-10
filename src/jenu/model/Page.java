package jenu.model;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jenu.utils.AtomicRef;

/** Provides information for exactly one page.
 * All methods of this class are thread-safe, but iteration over returned collections is not.
 *
 * Furthermore the result of all getter methods (unless otherwise noted)
 * is stable as soon as {@link #getState} returns greater than {@code RUNNUNG}.
 * In this case no further actions are required for enumeration. */
public abstract class Page
{
	/** Absolute URL of this document as String, primary key, never null. */
	public final String sUrl;
	/** Absolute URL as URL class, null if and only if sUrl is invalid. */
	public final URL url;

	/** Create a <em>valid</em> page.
	 * @param url URL of the page or null if no valid URL can be determined.
	 * @param sUrl URL of the page as string, never null. */
	protected Page(URL url, String sUrl)
	{	assert sUrl != null;
		this.url = url;
		this.sUrl = sUrl;
		this.state = new AtomicReference<>(PageState.VIRGIN);
	}

	/** What is the task status of this page excluding incoming links? */
	public final PageState getState()
	{	return state.get();
	}
	protected final AtomicReference<PageState> state;

	/** Which events occurred, never null. */
	public final Collection<Message> getEvents()
	{	return events != null ? events : Collections.emptyList();
	}
	protected volatile Vector<Message> events = null;

	/** Time taken to analyze this document. &lt; 0 =&gt; in progress. */
	public final long getDuration()
	{	return duration;
	}
	protected volatile long duration = 0;


	/** MIME type if known. */
	public final String getContentType()
	{	return contentType;
	}
	protected volatile String contentType = null;

	/** Object size in bytes or -1 if unknown. */
	public final long getSize()
	{	return size;
	}
	protected volatile long size = -1;

	/** Source code lines of document or -1 if unknown. */
	public final int getLines()
	{	return lines;
	}
	protected volatile int lines = -1;

	/** Document title or null if not available. */
	public final String getTitle()
	{	return title;
	}
	protected volatile String title = null;

	/** Document/file modification date or null if unknown. */
	public final Date getDate()
	{	return date;
	}
	protected volatile Date date = null;

	/** Get links from other documents that point to this object.
	 * Attention: this collection can change even when {@link #getState} returns {@code DONE}.
	 * @return List of links to this object. Keep in mind that the collection is synchronized but mutable.
	 * So iteration is not thread safe, but calling toArray is. */
	public final Collection<Link> getLinksIn()
	{	return linksIn;
	}
	protected final Vector<Link> linksIn = new Vector<>();


	/** Link depth of this document, i.e. shortest path from one of the starting points.
	 * Attention: the level can change even when {@link #getState} returns {@code DONE} because a shorter path could be found.
	 * @return 0 = starting point; Integer.MAX_VALUE = undefined;
	 * anything else = at least n links from a starting point away. */
	public int getLevel()
	{	return level.intValue();
	}
	protected final AtomicInteger level = new AtomicInteger(Integer.MAX_VALUE);

	/** Get links from this document to other objects.
	 * @return List of links from this object. Keep in mind that the collection is synchronized but mutable.
	 * So iteration is not thread safe, but calling toArray is.
	 * However, once {@link #getState} returns greater than {@code RUNNUNG}. The collection is frozen. */
	public Collection<Link> getLinksOut()
	{	Collection<Link> ret = linksOut;
		return ret != null ? ret : Collections.<Link>emptyList();
	}
	protected Vector<Link> linksOut = null;

	/** Anchor names defined in this document.
	 * @return List of Anchors associated with an optional message.
	 * If the associated message is noMessage the anchor exists.
	 * Otherwise it is an error message. */
	public Map<String,Message> getAnchors()
	{	Map<String,Message> ret = anchors.get();
		return ret != null ? ret : Collections.<String,Message>emptyMap();
	}
	protected final AtomicRef<Map<String,Message>> anchors = new AtomicRef<>();

	/** Placeholder for null because otherwise the is no atomic computeIfAbsent method. */
	public final static Message noMessage = new Message(MessageType.Bad_anchor, Severity.NONE, "");
}
