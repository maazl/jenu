package jenu.ui;

import javax.swing.*;

import jenu.worker.JenuThreadEvent;
import jenu.worker.JenuThreadListener;

final class InternalStatusBar extends JToolBar implements JenuThreadListener
{
	JProgressBar m_threadsRunning, m_urlsDone;

	public InternalStatusBar()
	{
		m_threadsRunning = new InternalProgressBar(0);
		m_urlsDone = new InternalProgressBar(0);
		add(new JLabel("Threads Running"));
		add(m_threadsRunning);
		add(new JLabel("Total URLS"));
		add(m_urlsDone);
		setFloatable(false);
	}

	public void threadStateChanged(JenuThreadEvent e)
	{
		m_threadsRunning.setMaximum(e.m_maxThreadsRunning);
		m_threadsRunning.setValue(e.m_threadsRunning);

		m_urlsDone.setMaximum(e.m_totalUrlsToCheck);
		m_urlsDone.setValue(e.m_urlsDone);
	}

	private class InternalProgressBar extends JProgressBar
	{
		public InternalProgressBar(int max)
		{
			super(0, max);
			setStringPainted(true);
		}

		public String getString()
		{
			return getValue() + "/" + getMaximum();
		}
	}

}
