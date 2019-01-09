package jenu.model;

import java.net.MalformedURLException;
import java.net.URL;

/** Represents a Link between two pages. */
public class Link
{
	/** Link from CSS style (not necessarily from a style sheet) */
	public final static String CSS = "CSS";
	/** Virtual link from HTTP 30x redirect or directory URL */
	public final static String REDIRECT = "REDIRECT";
	/** Link from directory scan */
	public final static String DIRECTORY = "DIRECTORY";

	/** Type or origin of this link, i.e. name of the tag, i.e. "a" or "img", "link" or "meta".
	 * Special values:
	 * - "CSS" => used for style sheet links, e.g. to background images.
	 * - "REDIRECT" => followed a HTTP redirect or a directory redirect, i.e. file:/some/folder -> file:/some/folder/.
	 * - null => used to identify starting points. They are no real links. */
	public final String type;
	/** Source page */
	public final Page source;
	/** Source code line */
	public final int sourceLine;
	/** Target URL as in document */
	public final String originalTarget;
	/** Target anchor, if any */
	public final String getAnchor()
	{	int p = originalTarget.indexOf("#");
		return p >= 0 ? originalTarget.substring(p + 1) : null;
	}

	/** Get target link (unparsed) without any anchor.
	 * @return relative or absolute URL, but without anchor.
	 * If originalTarget consists only of an anchor "" is returned. */
	public final String getTargetLink()
	{	int p = originalTarget.indexOf("#");
		switch (p)
		{case -1:
			return originalTarget;
		 case 0:
			return "";
		 default:
			return originalTarget.substring(0, p);
		}
	}

	/** Get full qualified target URL including anchor.
	 * @return URL or null if not valid. */
	public final URL getTargetUrl()
	{	Page target = this.target;
		if (target == null)
			return null;
		String anchor = getAnchor();
		if (anchor == null)
			return target.url;
		try
		{	return new URL(target.sUrl + "#" + anchor);
		} catch (MalformedURLException e)
		{	return null; // can't help but should never happen if only an anchor is added.
		}
	}

	/** Target page
	 * @return might be null, if not yet processed; or Page.EXCLUDED in case of an excluded URL.
	 */
	public final Page getTargetPage()
	{	return target;
	}
	protected volatile Page target;

	/** State of the link
	 * @return null: everything OK */
	public final Message getEvent()
	{	return message;
	}
	protected volatile Message message = null;

	/** Create Link
	 * @param type Link type. Either an HTML tag name ore one of the constants in this class.
	 * @param target Raw target URL as found in the document (relative or absolute).
	 * @param source Source URL.
	 * @param line Source code line of the target URL in the source URL or -1 if not available..
	 */
	protected Link(String type, String target, Page source, int line)
	{	this.type = type;
		this.originalTarget = target;
		this.source = source;
		this.sourceLine = line;
	}

	public final static Link[] noLinks = new Link[0];
}
