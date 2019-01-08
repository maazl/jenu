package jenu.ui.viewmodel;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.SwingUtilities.isEventDispatchThread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import jenu.model.Link;
import jenu.model.Page;
import jenu.model.PageState;
import jenu.worker.PageEvent;
import jenu.worker.PageEventType;
import jenu.worker.PageListener;

/** View model for outgoing links of a single page */
public final class LinksOutView extends LinksView
{
	private final Page page;

	public LinksOutView(Page page)
	{	this.page = page;
	}

	/** Mapping target Page - LinkRow */
	private final HashMap<Page,ArrayList<LinkRow>> map = new HashMap<>();

	@Override public synchronized void refresh()
	{	map.clear();
		super.refresh();
	}

	@Override protected LinkRow addNewRow(Link link)
	{	LinkRow row = super.addNewRow(link);
		map.compute(link.getTargetPage(), (key, value) ->
			{	if (value == null)
					value = new ArrayList<>();
				value.add(row);
				return value;
			} );
		return row;
	}

	@Override protected void fetchData()
	{	if (page.getState() == PageState.DONE)
		{	Collection<Link> links = page.getLinksOut();
			data.ensureCapacity(links.size());
			for (Link link : links)
				addNewRow(link);
		}
	}

	@Override public synchronized void pageChanged(PageEvent e)
	{	if (!isEventDispatchThread())
		{	invokeLater(() -> pageChanged(e));
			return;
		}

		if (e.page == page && e.type == PageEventType.STATE && e.page.getState() == PageState.DONE)
			refresh();
		else if (e.type == PageEventType.NEW || e.type == PageEventType.STATE)
		{	boolean changed = false;
			for (Link l : e.page.getLinksIn().toArray(Link.noLinks))
			{	ArrayList<LinkRow> dep = map.get(l.source);
				if (dep != null)
					for (LinkRow row : dep)
						if (row.eventCache != null)
						{	row.eventCache = null;
							fireTableRowsUpdated(row.index, row.index);
							fireStateObjectEvent(row, EventType.UPDATE, true);
							changed = true;
						}
			}
			if (changed)
				fireStateObjectEvent(null, EventType.NONE, false);
		}
	}
}
