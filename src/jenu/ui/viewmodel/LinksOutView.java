package jenu.ui.viewmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import jenu.model.Link;
import jenu.model.Page;
import jenu.model.PageState;
import jenu.worker.PageEvent;
import jenu.worker.PageEventType;
import jenu.worker.PageListener;

public final class LinksOutView extends ALinkView implements PageListener
{
	private final Page page;

	public LinksOutView(Page page)
	{	this.page = page;
	}

	/** Mapping target Page - LinkRow */
	private final HashMap<Page,ArrayList<LinkRow>> map = new HashMap<>();

	private void addLink(Link link)
	{	LinkRow row = new LinkRow(link, data.size());
		data.add(row);
		map.compute(link.getTargetPage(), (key, value) ->
			{	if (value == null)
					value = new ArrayList<>();
				value.add(row);
				return value;
			} );
	}

	@Override public synchronized void refresh()
	{	map.clear();
		data.clear();
		if (page.getState() == PageState.DONE)
		{	Collection<Link> links = page.getLinksOut();
			data.ensureCapacity(links.size());
			for (Link link : links)
				addLink(link);
		}
		fireStateObjectEvent(null, EventType.INSERT);
		fireTableDataChanged();
	}

	@Override public synchronized void pageChanged(PageEvent e)
	{	if (e.page == page && e.type == PageEventType.STATE && e.page.getState() == PageState.DONE)
			refresh();
		else if (e.type == PageEventType.NEW || e.type == PageEventType.STATE)
		{	Collection<Link> links = e.page.getLinksIn();
			if (links.size() != 0)
				for (Link l : links.toArray(Link.noLinks))
				{	ArrayList<LinkRow> dep = map.get(l.source);
					if (dep != null)
						for (LinkRow row : dep)
							if (row.eventCache != null)
							{	row.eventCache = null;
								fireTableRowsUpdated(row.index, row.index);
								fireStateObjectEvent(row, EventType.UPDATE);
							}
				}
		}
	}
}
