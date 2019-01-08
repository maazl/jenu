package jenu.ui.viewmodel;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.SwingUtilities.isEventDispatchThread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import jenu.model.Link;
import jenu.model.Message;
import jenu.model.Page;
import jenu.utils.Statics;
import jenu.worker.PageEvent;
import jenu.worker.PageEventType;

/** View model of target view, i.e. list all link targets and consider any dead URL as bad.
 * Targets that have unresolved HTML anchors are shown as erroneous too. */
public final class TargetView extends PageView<TargetView.PageRow>
{
	@Override public void pageChanged(PageEvent e)
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
			return;
		}
		row = map.get(e.page);
		if (row == null)
			return;
		if (e.type == PageEventType.DELETE)
		{	fireTableRowsDeleted(row.index, row.index);
			fireStateObjectEvent(row, EventType.DELETE, false);
		} else
		{	row.eventCache = null;
			fireTableRowsUpdated(row.index, row.index);
			fireStateObjectEvent(row, EventType.UPDATE, false);
		}
	}

	@Override public synchronized void refresh()
	{	map.clear();
		super.refresh();
	}

	private final HashMap<Page,PageRow> map = new HashMap<>();

	@Override protected PageRow addNewRow(Page page)
	{	PageRow	row = new PageRow(page, data.size());
		data.add(row);
		map.put(page, row);
		return row;
	}

	/** Row in the result table */
	public static final class PageRow extends PageView.PageRow implements StateObject
	{
		PageRow(Page page, int index)
		{	super(page, index);
		}

		@Override public Message[] getEffectiveEvents()
		{	Message[] ret = eventCache;
			if (ret == null)
			{	// Collect anchor messages of links to this page.
				ArrayList<Message> am = null;
				int size = 0;
				if (page.getLinksIn().size() > 0)
				{	for (Link l : page.getLinksIn().toArray(new Link[0])) // thread-safe enumeration
					{	Message m = l.getEvent();
						if (m == null)
							continue;
						if (am == null)
							am = new ArrayList<Message>();
						am.add(m);
					}
					if (am != null)
					{	am.sort(Comparator.<Message>naturalOrder());
						Statics.removeAdjacentDuplicates(am);
						size = am.size();
					}
				}
				// Start with page events.
				Collection<Message> pm = page.getEvents();
				ret = pm.toArray(new Message[pm.size() + size]);
				int i = 0;
				for (int j = ret.length - size; j < ret.length; ++j)
					ret[j] = am.get(i++);
				eventCache = ret;
			}
			return ret;
		}

		private volatile Message[] eventCache = null;
	}
}
