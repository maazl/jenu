package jenu.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Icon;

/** Variant of Action that can deal with Lambdas */
public class FunctionalAction extends AbstractAction
{
	/** Action to be performed */
	public final ActionListener action;

	/** Create FunctionalAction with name and icon.
	 * @param name {@inheritDoc}
	 * @param icon {@inheritDoc}
	 * @param action Action to perform. */
	public FunctionalAction(String name, Icon icon, ActionListener action)
	{	super(name, icon);
		this.action = action;
	}
	/** Create FunctionalAction with name only.
	 * @param name {@inheritDoc}
	 * @param action Action to perform. */
	public FunctionalAction(String name, ActionListener action)
	{	super(name);
		this.action = action;
	}

	@Override public void actionPerformed(ActionEvent e)
	{	action.actionPerformed(e);
	}
}