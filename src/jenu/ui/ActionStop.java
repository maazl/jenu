package jenu.ui;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/** This class implements several of the actions for each thread. */
final class ActionStop extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	protected JenuInternalFrame m_parent = null;

	public ActionStop(JenuInternalFrame parent)
	{
		super("Stop");
		m_parent = parent;
	}

	public void actionPerformed(ActionEvent e)
	{
		m_parent.stopRunning();
	}
}
