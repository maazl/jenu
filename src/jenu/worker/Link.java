package jenu.worker;

import java.net.MalformedURLException;
import java.net.URL;

/** Represents a Link between two pages. */
public final class Link
{
	/** Link from CSS style (not necessarily from a style sheet) */
	public final static String CSS = "CSS";
	/** Virtual link from HTTP 30x redirect or directory URL */
	public final static String REDIRECT = "REDIRECT";

	/** Type or origin of this link, i.e. name of the tag, i.e. "a" or "img", "link" or "meta".
	 * Special values:
	 * - "CSS" => used for style sheet links, e.g. to background images.
	 * - "REDIRECT" => followed a HTTP redirect or a directory redirect, i.e. file:/some/folder -> file:/some/folder/.
	 * - null => used to identify starting points. They are no real links. */
	public final String Type;
	/** Target URL as in document */
	public final String OriginalTarget;
	/** Target URL, full qualified if possible, w/o anchor. */
	public final String Target;
	/** Target anchor, if any */
	public final String Anchor;
	/** Source URL, full qualified but w/o anchor. */
	public final URL Source;
	/** Source code line */
	public final int Line;

	PageStats TargetPage = null;
	/** Target Page including status */
	public PageStats getTargetPage()
	{	return TargetPage;
	}

	/** State of the link's anchor
	 * @return false: anchor not found */
	public Boolean getAnchorState()
	{	return AnchorState;
	}
	void setAnchorState(boolean ok)
	{	AnchorState = ok;
	}
	private volatile Boolean AnchorState = null;

	Link(String type, String target, URL source, int line)
	{	Type = type;
		OriginalTarget = target;
		Source = source;
		Line = line;
		// strip anchor
		int p = target.indexOf("#");
		if (p >= 0)
		{	Anchor = target.substring(p + 1);
			target = target.substring(0, p);
		} else
		{	Anchor = null;
			AnchorState = Boolean.TRUE;
		}
		// make absolute
		if (source != null)
			try
			{	target = new URL(source, target).toExternalForm();
			} catch (MalformedURLException e)
			{ // We can safely ignore this error here because it gets repeated by the PageGrabber.
			}
		Target = target;
	}

	public String toString()
	{	return Type + ": " + Source + " -> " + OriginalTarget;
	}
}
