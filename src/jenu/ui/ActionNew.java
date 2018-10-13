package jenu.ui;

final class ActionNew extends JenuAction
{
	private static final long serialVersionUID = 1L;
	public Jenu owner = null;

	public ActionNew(Jenu owner)
	{
		super("New Window");
		m_owner = owner;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		m_owner.createNewWindow();
	}

	public void setEnabledFromState(int state)
	{
		setEnabled(true);
	}
}
