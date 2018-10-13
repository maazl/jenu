package jenu.ui;

import javax.swing.*;

import jenu.worker.JenuThreadEvent;
import jenu.worker.JenuThreadListener;

class InternalStatusBar extends JToolBar implements JenuThreadListener
{
	private static final long serialVersionUID = 1L;
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

	protected class InternalProgressBar extends JProgressBar
	{
		private static final long serialVersionUID = 1L;

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
