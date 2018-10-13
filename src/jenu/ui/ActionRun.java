package jenu.ui;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/** This class implements several of the actions for each thread. */
final class ActionRun extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	protected JenuInternalFrame m_parent = null;

	public ActionRun(JenuInternalFrame parent)
	{
		super("Run");
		m_parent = parent;
	}

	public void actionPerformed(ActionEvent e)
	{
		m_parent.startRunning();
	}
}
