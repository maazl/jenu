package jenu.worker;

import jenu.model.Link;
import jenu.model.Message;

/** Writable version of Link. */
final class LinkStats extends Link
{
	/** Set link message */
	void setMessage(Message message)
	{	this.message = message;
	}

	/** Create Link
	 * @param type Link type. Either an HTML tag name ore one of the constants in this class.
	 * @param target Raw target URL as found in the document (relative or absolute).
	 * @param source Source URL.
	 * @param line Source code line of the target URL in the source URL or -1 if not available..
	 */
	public LinkStats(String type, String target, PageStats source, int line)
	{	super(type, target, source, line);
	}

	public void setTarget(PageStats target)
	{	this.target = target;
	}

	/** Check whether the anchor (if any) exists in the target page.
	 * @return true: link state changed; false: no change. */
	boolean checkAnchor()
	{	Message newMsg = null;
		if (target != null)
		{	String anchor = getAnchor();
			if (anchor != null)
				newMsg = ((PageStats)target).checkAnchor(anchor);
		}
		if (message == newMsg)
			return false;
		message = newMsg;
		return true;
	}
}
