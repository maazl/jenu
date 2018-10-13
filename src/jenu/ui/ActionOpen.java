package jenu.ui;

final class ActionOpen extends JenuAction
{
	private static final long serialVersionUID = 1L;

	public ActionOpen(Jenu owner)
	{
		super("Open...");
		m_owner = owner;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		// OpenURLDialog open = new OpenURLDialog(owner);
		// open.show();
	}

	public void setEnabledFromState(int state)
	{
		setEnabled(enabledWhenStopped(state));
	}
}
