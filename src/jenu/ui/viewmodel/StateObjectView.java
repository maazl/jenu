package jenu.ui.viewmodel;

import javax.swing.table.AbstractTableModel;

import jenu.utils.AtomicArraySet;

/** Base class for table models that handle a collection of StateObjects. */
public abstract class StateObjectView extends AbstractTableModel
{
	private final AtomicArraySet<StateObjectListener> listeners = new AtomicArraySet<>(new StateObjectListener[0]);

	public final boolean addStateObjectListener(StateObjectListener l)
	{	return listeners.add(l);
	}

	public final boolean removeStateObjectListener(StateObjectListener l)
	{	return listeners.remove(l);
	}

	protected final void fireStateObjectEvent(StateObject item, EventType type)
	{	StateObjectListener[] listeners = this.listeners.get();
		if (listeners.length != 0)
		{	StateObjectEvent e = new StateObjectEvent(this, item, type);
			for (StateObjectListener i : listeners)
				i.itemChanged(e);
		}
	}
}
