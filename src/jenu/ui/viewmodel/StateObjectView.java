package jenu.ui.viewmodel;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import jenu.model.Message;
import jenu.model.MessageType;
import jenu.utils.AtomicArraySet;

/** Base class for table models that handle a collection of StateObjects.
 * @param <R> Row type of the view model based on this class. */
public abstract class StateObjectView<R extends StateObject> extends AbstractTableModel
{
	/** Data backend for the model. Mainly maintained by derived classes. */
	protected final ArrayList<R> data = new ArrayList<>();

	@Override public final int getRowCount()
	{	return data.size();
	}

	/** get entire Row
	 * @param rowIndex Physical index of the row to get.
	 * @return Requested row. */
	public R getRow(int rowIndex)
	{	return data.get(rowIndex);
	}

	/** (Re-)initialize the view model from the backend model.
	 * This method is required if the view model class is attached to a backend model that already contains data.
	 *
	 * This clears the entire state of the view model and loads new data using the abstract method {@link #fetchData()}.
	 * Observers are notified as required.
	 *
	 * You need to override this method if your view model class contains further state, not only {@link #data} */
	public synchronized void refresh()
	{	boolean changed = data.size() != 0;
		data.clear();
		fetchData();
		if (data.size() != 0)
		{	if (changed)
				fireStateObjectEvent(null, EventType.RESET, true);
			for (StateObject row : data)
				fireStateObjectEvent(row, EventType.INSERT, changed = true);
			if (!changed)
				return;
			fireStateObjectEvent(null, EventType.NONE, false);
		} else
		{	if (!changed)
				return;
			fireStateObjectEvent(null, EventType.RESET, false);
		}
		fireTableDataChanged();
	}

	/** Load new data from backend model into {@link #data}. */
	protected abstract void fetchData();


	private final AtomicArraySet<StateObjectListener> listeners = new AtomicArraySet<>(new StateObjectListener[0]);

	public final boolean addStateObjectListener(StateObjectListener l)
	{	return listeners.add(l);
	}

	public final boolean removeStateObjectListener(StateObjectListener l)
	{	return listeners.remove(l);
	}

	protected final void fireStateObjectEvent(StateObject item, EventType type, boolean more)
	{	StateObjectListener[] listeners = this.listeners.get();
		if (listeners.length != 0)
		{	StateObjectEvent e = new StateObjectEvent(this, item, type, more);
			for (StateObjectListener i : listeners)
				i.itemChanged(e);
		}
	}

	/** Check whether this Message should be considered a bad link. */
	protected static boolean isLinkError(Message m)
	{	return m.type != MessageType.Parse_error && m.type != MessageType.Internal_error;
	}
}
