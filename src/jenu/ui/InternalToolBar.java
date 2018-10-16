package jenu.ui;

import javax.swing.*;

class InternalToolBar extends JToolBar
{
	protected JenuInternalFrame m_owner;

	JButton m_run, m_stop, m_pause;
	JTextField m_textField;

	public InternalToolBar(JenuInternalFrame owner)
	{
		super();
		setFloatable(false);
		add(new JLabel("URL:"));
		m_textField = new JTextField(10);
		add(m_textField);
		m_owner = owner;
		m_run = add(new ActionRun(m_owner));
		m_stop = add(new ActionStop(m_owner));
		m_pause = add(new ActionPause(m_owner));
		setStopped();
	}

	public String getURL()
	{
		return m_textField.getText();
	}

	public void setRunning()
	{
		m_run.setEnabled(false);
		m_stop.setEnabled(true);
		m_pause.setEnabled(true);
		m_textField.setEnabled(false);
	}

	public void setPaused()
	{
		m_run.setEnabled(true);
		m_stop.setEnabled(true);
		m_pause.setEnabled(false);
		m_textField.setEnabled(false);
	}

	public void setStopping()
	{
		m_run.setEnabled(false);
		m_stop.setEnabled(false);
		m_pause.setEnabled(false);
		m_textField.setEnabled(false);
	}

	public void setStopped()
	{
		m_run.setEnabled(true);
		m_stop.setEnabled(false);
		m_pause.setEnabled(false);
		m_textField.setEnabled(true);
	}
}
