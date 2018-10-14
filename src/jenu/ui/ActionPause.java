package jenu.ui;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/** This class implements several of the actions for each thread. */
final class ActionPause extends AbstractAction
{
	protected JenuInternalFrame m_parent = null;

	public ActionPause(JenuInternalFrame parent)
	{
		super("Pause");
		m_parent = parent;
	}

	public void actionPerformed(ActionEvent e)
	{
		m_parent.pauseRunning(true);
	}
}
