package jenu.ui;

import javax.swing.*;

final class InternalToolBar extends JToolBar
{
	final JenuInternalFrame m_owner;

	final JButton m_run, m_stop, m_pause;
	final JTextField m_site, m_url;

	public InternalToolBar(JenuInternalFrame owner)
	{
		m_owner = owner;

		setFloatable(false);
		add(new JLabel("Site:"));
		add(m_site = new JTextField(10));
		add(new JLabel("Start URL:"));
		add(m_url = new JTextField(10));
		m_run = add(new ActionRun(m_owner));
		m_stop = add(new ActionStop(m_owner));
		m_pause = add(new ActionPause(m_owner));
		setStopped();
	}

	public String getSite()
	{
		return m_site.getText();
	}

	public String getURL()
	{
		return m_url.getText();
	}

	public void setRunning()
	{
		m_run.setEnabled(false);
		m_stop.setEnabled(true);
		m_pause.setEnabled(true);
		m_site.setEnabled(false);
		m_url.setEnabled(false);
	}

	public void setPaused()
	{
		m_run.setEnabled(true);
		m_stop.setEnabled(true);
		m_pause.setEnabled(false);
		m_site.setEnabled(false);
		m_url.setEnabled(false);
	}

	public void setStopping()
	{
		m_run.setEnabled(false);
		m_stop.setEnabled(false);
		m_pause.setEnabled(false);
		m_site.setEnabled(false);
		m_url.setEnabled(false);
	}

	public void setStopped()
	{
		m_run.setEnabled(true);
		m_stop.setEnabled(false);
		m_pause.setEnabled(false);
		m_site.setEnabled(true);
		m_url.setEnabled(true);
	}
}
