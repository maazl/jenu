package jenu.worker;

import java.net.URL;
import java.util.Date;
import java.util.EnumSet;
import java.util.Vector;
import java.text.DateFormat;
import java.net.MalformedURLException;

/**
 * Records information for exactly one HTML page.
 */
public final class PageStats
{
	PageState             runState    = PageState.PENDING;
	public EnumSet<ErrorType> status  = EnumSet.noneOf(ErrorType.class);
	String                errorString = "";
	public String         contentType = null;
	public int            size        = -1;
	public String         title       = null;
	public Date           date        = null;
	public int            level       = -1;
	public Vector<String> linksOut    = new Vector<>();
	public String         server      = null;
	public URL            url         = null;
	public String         sUrl        = null;
	public Vector<String> linksIn     = new Vector<>();

	protected DateFormat df = DateFormat.getDateInstance();

	public PageStats(String strURL)
	{
		if (strURL == null)
		{
			throw new NullPointerException();
		}
		sUrl = strURL;
		try
		{
			url = new URL(strURL);
		} catch (MalformedURLException e)
		{
			setError(ErrorType.URLError, e.getMessage());
		}
	}

	public String toString()
	{
		String result =
			"URL         = " + url + "\n" +
			"status      = " + status + "\n" +
			"errorString = " + errorString + "\n" +
			"contentType = " + contentType + "\n" +
			"size        = " + size + "\n" +
			"title       = " + title + "\n" +
			"date        = " + date + "\n" +
			"level       = " + level + "\n" +
			"linksIn     = " + linksIn + "\n" +
			"linksOut    = " + linksOut + "\n";
		return result;
	}

	void addLinkIn(String u)
	{
		linksIn.add(u);
	}

	public boolean equals(Object o)
	{
		boolean result;
		//String s = "Comparing " + this.sUrl + " to ";
		if (o instanceof String)
		{
			//s += o;
			result = sUrl.equals(o);
		} else if (o instanceof PageStats)
		{
			//s += ((PageStats)o).sUrl;
			result = sUrl.equals(((PageStats)o).sUrl);
		} else
		{
			result = super.equals(o);
		}
		if (result)
		{
			// System.out.println("" + result + " " + s);
		}
		return result;
	}

	public PageState getRunState()
	{
		return runState;
	}

	void setRunning()
	{
		runState = PageState.RUNNING;
	}

	void setError(ErrorType type, String message)
	{
		runState = PageState.FAILED;
		status.add(type);
		errorString += message;
	}

	void setDone()
	{
		if (runState != PageState.FAILED)
		runState = PageState.DONE;
	}
	
	void setRetry()
	{
		runState = PageState.RETRY;
		// reset state
		errorString = "";
		contentType = null;
		size        = -1;
		title       = null;
		date        = null;
		level       = -1;
		linksOut.clear();
		server      = null;
		url         = null;
		sUrl        = null;
	}

	public String getErrorString()
	{
		return errorString;
	}

	void addLinkOut(String u)
	{
		// System.out.println(u);
		try
		{
			URL u_url = new URL(url, u);
			linksOut.add(u_url.toExternalForm());
		} catch (MalformedURLException e)
		{
			setError(ErrorType.URLError, "Malformed URL " + u + "(" + e.getMessage() + "), with parent" + url);
		}
	}

	void setTime(String time)
	{
		try
		{
			date = df.parse(time);
		} catch (java.text.ParseException e)
		{}
	}
}
