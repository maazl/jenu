package jenu.ui;

import javax.swing.JComponent;
import java.net.URL;
import java.net.MalformedURLException;

final class JenuSettings extends JComponent
{
	private static final long serialVersionUID = 1L;
	protected URL m_urlToCheck = null;
	protected boolean m_checkExternalLinks = false;
	protected int m_parallelThreads = 10;
	protected boolean m_askForPassword = true;
	protected int m_maximumDepth = 1000;

	public int getParallelThreads()
	{
		return m_parallelThreads;
	}

	public void setParallelThreads(int t)
	{
		m_parallelThreads = t;
	}

	public URL getUrlToCheck()
	{
		return m_urlToCheck;
	}

	public void setUrlToCheck(URL url)
	{
		m_urlToCheck = url;
	}

	public void setUrlToCheck(String url) throws MalformedURLException
	{
		m_urlToCheck = new URL(url);
	}

	public boolean getCheckExternalLinks()
	{
		return m_checkExternalLinks;
	}

	public void setCheckExternalLinks(boolean check)
	{
		m_checkExternalLinks = check;
	}

	public boolean getAskForPassword()
	{
		return m_askForPassword;
	}

	public void setAskForPassword(boolean ask)
	{
		m_askForPassword = ask;
	}

	public int getMaximumDepth()
	{
		return m_maximumDepth;
	}

	public void setMaximumDepth(int depth)
	{
		m_maximumDepth = depth;
	}

	public void firePropertyChange()
	{
		// This isn't a proper property change, it just notifies all listeners that
		// *something* in this object has change. The listeners should not use the value
		// but rather look at the settings.
		firePropertyChange("all", true, false);
	}
}
