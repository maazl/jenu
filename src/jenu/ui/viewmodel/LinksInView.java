package jenu.ui.viewmodel;

import static javax.swing.SwingUtilities.*;

import jenu.model.Link;
import jenu.model.Page;
import jenu.model.PageState;
import jenu.worker.PageEvent;
import jenu.worker.PageEventType;
import jenu.worker.PageListener;

/** View model for incoming links to a single page */
public final class LinksInView extends LinksView
{
	private final Page page;

	public LinksInView(Page page)
	{	this.page = page;
	}

	@Override protected void fetchData()
	{	Link[] links = page.getLinksIn().toArray(Link.noLinks);
		data.ensureCapacity(links.length);
		for (Link link : links)
			addNewRow(link);
	}

	@Override public synchronized void pageChanged(PageEvent e)
	{	if (!isEventDispatchThread())
		{	invokeLater(() -> pageChanged(e));
			return;
		}

		if (e.page != page)
			return;
		if (e.type == PageEventType.STATE && e.page.getState() == PageState.DONE)
			refresh();
		else if (e.type == PageEventType.LINKSIN) // the last link is new
		{	Link[] links = e.page.getLinksIn().toArray(Link.noLinks);
			LinkRow row = addNewRow(links[links.length - 1]);
			fireStateObjectEvent(row, EventType.INSERT, false);
			fireTableRowsInserted(row.index, row.index);
		}
	}
}
