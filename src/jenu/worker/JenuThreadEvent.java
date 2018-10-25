package jenu.worker;

import java.util.EventObject;

public class JenuThreadEvent extends EventObject
{
	public final int
		m_totalUrlsToCheck,
		m_urlsDone,
		m_threadsRunning,
		m_urlsToStart;

	public JenuThreadEvent(
		ThreadManager source,
		int totalUrlsToCheck,
		int urlsDone,
		int threadsRunning,
		int urlsToStart)
	{
		super(source);
		m_totalUrlsToCheck = totalUrlsToCheck;
		m_urlsDone = urlsDone;
		m_threadsRunning = threadsRunning;
		m_urlsToStart = urlsToStart;
	}

	public ThreadManager getSource()
	{	return (ThreadManager)super.getSource();
	}
}
