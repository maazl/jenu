package jenu.ui.viewmodel;

import jenu.model.Link;
import jenu.model.Page;
import jenu.model.PageState;
import jenu.worker.PageEvent;
import jenu.worker.PageEventType;
import jenu.worker.PageListener;

public final class LinksInView extends ALinkView implements PageListener
{
	private final Page page;

	public LinksInView(Page page)
	{	this.page = page;
	}

	private LinkRow addLink(Link link)
	{	LinkRow row = new LinkRow(link, data.size());
		data.add(row);
		return row;
	}

	@Override public synchronized void refresh()
	{	data.clear();
		Link[] links = page.getLinksIn().toArray(Link.noLinks);
		data.ensureCapacity(links.length);
		for (Link link : links)
			data.add(new LinkRow(link, data.size()));
		fireStateObjectEvent(null, EventType.INSERT);
		fireTableDataChanged();
	}

	@Override public synchronized void pageChanged(PageEvent e)
	{	if (e.page != page)
			return;
		if (e.type == PageEventType.STATE && e.page.getState() == PageState.DONE)
			refresh();
		else if (e.type == PageEventType.LINKSIN) // the last link is new
		{	Link[] links = e.page.getLinksIn().toArray(Link.noLinks);
			int index = addLink(links[links.length - 1]).index;
			fireTableRowsInserted(index, index);
		}
	}
}
