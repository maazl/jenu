import java.util.EventObject;

public class JenuThreadEvent 
    extends EventObject
{
    public int 
	m_totalUrlsToCheck,
	m_urlsDone,
	m_maxThreadsRunning,
	m_threadsRunning,
	m_urlsToStart;
	
    public JenuThreadEvent
	(
	 Object source,
	 int totalUrlsToCheck,
	 int urlsDone,
	 int maxThreadsRunning,
	 int threadsRunning,
	 int urlsToStart
	 ) 
    {
	super(source);
	m_totalUrlsToCheck = totalUrlsToCheck;
	m_urlsDone = urlsDone;
	m_maxThreadsRunning = maxThreadsRunning;
	m_threadsRunning = threadsRunning;
	m_urlsToStart = urlsToStart;
    }
}
