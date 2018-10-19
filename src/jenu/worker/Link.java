package jenu.worker;

import java.net.URL;

/** Represents a Link between two pages. */
public class Link
{
	/** Type or origin of this link, i.e. name of the tag, i.e. "a" or "img" or "link".
	 * The special value "link.css" is used for style sheet links.
	 * The special value null is used to identify starting points. They are no real links. */
	public final String Type;
	/** Target URL */
	public final String Target;
	/** Source URL, full qualified but w/o anchor. */
	public final URL Source;
	/** Source code line */
	public final int Line;

	public Link(String type, String target, URL source, int line)
	{	Type = type;
		Target = target;
		Source = source;
		Line = line;
	}

	public String toString()
	{	return Type + ": " + Source + " -> " + Target;
	}
}
