package jenu.ui;

final class ActionExit
	extends JenuAction
{
	private static final long serialVersionUID = 1L;

	public ActionExit(Jenu owner)
	{
		super("Exit");
		m_owner = owner;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		System.exit(0);
	}

	public void setEnabledFromState(int state)
	{
		setEnabled(enabledWhenStopped(state));
	}
}
