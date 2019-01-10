package jenu.ui.viewmodel;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.SwingUtilities.isEventDispatchThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jenu.model.Link;
import jenu.model.Message;
import jenu.model.Page;
import jenu.model.PageState;
import jenu.worker.PageEvent;
import jenu.worker.PageEventType;

/** View model of source view, i.e. list all link sources and consider any dead target URL as bad. */
public class SourceView extends PageView<SourceView.PageRow>
{
	@Override public synchronized void pageChanged(PageEvent e)
	{	if (!isEventDispatchThread())
		{	invokeLater(() -> pageChanged(e));
			return;
		}

		if (e.page == null)
		{	if (e.type == PageEventType.DELETE)
				refresh();
			return;
		}

		PageRow row;
		if (e.type == PageEventType.NEW)
		{	row = addNewRow(e.page);
			fireTableRowsInserted(row.index, row.index);
			fireStateObjectEvent(row, EventType.INSERT, false);
		} else if (e.type == PageEventType.LINKSIN)
			return; // Ignore LINKSIN changes. New pages linking to this page are updated anyway.
		{	row = map.get(e.page);
			if (row == null)
				return;
			invalidateRow(row, false);
		}
		// invalidate rows of /incoming/ links as well.
		if (e.page.getState() == PageState.PENDING)
			return; // No update on links to pending objects. They do not add any value.
		boolean changed = false;
		for (Link l : e.page.getLinksIn().toArray(Link.noLinks))
		{	PageRow row2 = map.get(l.source);
			if (row2 != null)
			{	changed = true;
				invalidateRow(row2, true);
			}
		}
		if (changed)
			fireStateObjectEvent(row, EventType.NONE, false);
	}

	@Override public synchronized void refresh()
	{	map.clear();
		super.refresh();
	}

	/** Mapping Page - PageRow */
	private final HashMap<Page,PageRow> map = new HashMap<>();

	@Override protected PageRow addNewRow(Page page)
	{	PageRow row = new PageRow(page, data.size());
		data.add(row);
		map.put(page, row);
		return row;
	}

	private void invalidateRow(PageRow row, boolean more)
	{	row.eventCache = null;
		fireTableRowsUpdated(row.index, row.index);
		fireStateObjectEvent(row, EventType.UPDATE, more);
	}

	/** Row in the result table */
	public static final class PageRow extends PageView.PageRow implements StateObject
	{
		PageRow(Page page, int index)
		{	super(page, index);
		}

		@Override public RowState getState()
		{	RowState ret = super.getState();
			if (ret != RowState.OK)
			{	if (isPending)
					return RowState.PENDING;
				else if (isRunning)
					return RowState.RUNNING;
			}
			return ret;
		}

		@Override public Message[] getEffectiveEvents()
		{	Message[] ret = eventCache;
			if (ret != null)
				return ret;

			isPending = isRunning = false;
			if (page.getState() != PageState.DONE)
				return eventCache = Message.none;
			final Link[] links = page.getLinksOut().toArray(Link.noLinks); // Thread-safe snapshot
			if (links.length == 0)
				return eventCache = Message.none;

			final ArrayList<Message> result = new ArrayList<>();
			// List of processed targets and target anchors
			final HashSet<String> processed = new HashSet<String>();

			for (Link l : links)
			{	Page target = l.getTargetPage();
				if (target == null) // no target or target pending?
				{	isPending = true;
					continue;
				}
				if (!processed.add(target.sUrl))
					continue;
				// Forward target messages
				for (Message m : target.getEvents())
					if (isLinkError(m))
						result.add(m);
				int cmp = target.getState().compareTo(PageState.RUNNING);
				if (cmp <= 0)
				{	if (cmp == 0)
						isRunning = true;
					else
						isPending = true;
					continue;
				}
				// has new anchor?
				String anchor = l.getAnchor();
				if (anchor == null)
					continue; // no anchor => done
				String anchorUrl = target.sUrl + "#" + anchor;
				if (!processed.add(anchorUrl))
					continue; // this anchor is already done
				// create anchor message?
				Message m = target.getAnchors().get(anchor);
				if (m != null && m != Page.noMessage)
					result.add(new Message(m.type, m.level, anchorUrl));
			}

			return eventCache = result.toArray(Message.none);
		}
		private volatile Message[] eventCache = null;

		private volatile boolean isPending;
		private volatile boolean isRunning;
	}
}
