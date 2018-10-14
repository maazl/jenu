package jenu.ui;

import javax.swing.AbstractAction;

abstract class JenuAction extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	protected Jenu m_owner = null;

	protected JenuAction(String title)
	{
		super(title);
	}

	public abstract void setEnabledFromState(int state);

	public boolean enabledWhenStopped(int state)
	{
		boolean enabled = true;
		switch (state)
		{
		case Jenu.STATE_RUNNING:
		case Jenu.STATE_PAUSED:
			enabled = false;
			break;
		case Jenu.STATE_STOPPED:
			enabled = true;
			break;
		default:
			throw new Error("Invalid state sent to JenuAction" + state);
		}
		return enabled;
	}

	public boolean disabledWhenStopped(int state)
	{
		return !enabledWhenStopped(state);
	}
}
