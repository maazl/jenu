package jenu.worker;

import java.net.MalformedURLException;
import java.net.URL;

/** Represents a Link between two pages. */
public class Link
{
	/** Type or origin of this link, i.e. name of the tag, i.e. "a" or "img" or "link".
	 * The special value "link.css" is used for style sheet links.
	 * The special value null is used to identify starting points. They are no real links. */
	public final String Type;
	/** Target URL as in document */
	public final String OriginalTarget;
	/** Target URL, full qualified if possible w/o anchor. */
	public final String Target;
	/** Source URL, full qualified but w/o anchor. */
	public final URL Source;
	/** Source code line */
	public final int Line;

	public Link(String type, String target, URL source, int line)
	{	Type = type;
		OriginalTarget = target;
		Source = source;
		Line = line;
		// strip anchor
		int p = target.indexOf("#");
		if (p >= 0)
			target = target.substring(0, p);
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
