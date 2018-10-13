package jenu.worker;

import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.text.DateFormat;
import java.net.MalformedURLException;
import java.util.Comparator;
import java.awt.Color;

public final class PageStats
{
	public static final int	OK							= 0;
	public static final int	IOError					= 1 << 1;
	public static final int	HTMLParseError	= 1 << 2;
	public static final int	InvalidLinks		= 1 << 3;
	public static final int	HTTPError				= 1 << 4;
	public static final int	URLError				= 1 << 5;

	public static final int	PENDING	= 0;
	public static final int	RUNNING	= 1;
	public static final int	DONE		= 2;
	public static final int	RETRY		= 3;

	public static final int	ASCENDING	= 1;
	public static final int	DECENDING	= 2;

	int										runState		= PENDING;
	public int						status			= OK;
	String								errorString	= "";
	public String					contentType	= null;
	public int						size				= -1;
	public String					title				= null;
	public Date						date				= null;
	public int						level				= -1;
	public Vector<String>	linksIn			= new Vector<>();
	public Vector<String>	linksOut		= new Vector<>();
	public String					server			= null;
	public URL						url					= null;
	public String					sUrl				= null;

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
			setStatus(URLError);
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

	public void addLinkIn(String u)
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

	public void setStatus(int status)
	{
		this.status |= status;
	}

	public int getRunState()
	{
		return runState;
	}

	public Color getRunStateColor()
	{
		Color result = null;
		switch (runState)
		{
		case PENDING:
			result = Color.gray;
			break;
		case RUNNING:
			result = Color.yellow;
			break;
		case RETRY:
			result = Color.magenta;
			break;
		case DONE:
			switch (status)
			{
			case OK:
				result = Color.green;
				break;
			default:
				result = Color.red;
				break;
			}
			break;
		default:
			throw new Error("Invalid runState");
		}
		return result;
	}

	public void setPending()
	{
		runState = PENDING;
	}

	public void setRunning()
	{
		runState = RUNNING;
	}

	public void setDone()
	{
		runState = DONE;
	}

	public void setRetry()
	{
		runState = RETRY;
	}

	public String getErrorString()
	{
		return errorString;
	}

	public void setErrorString(String s)
	{
		errorString += s;
	}

	public void clearErrorString()
	{
		errorString = "";
	}

	public void addLinkOut(String u)
	{
		// System.out.println(u);
		try
		{
			URL u_url = new URL(url, u);
			linksOut.add(u_url.toExternalForm());
		} catch (MalformedURLException e)
		{
			setErrorString("Malformed URL " + u + ", with parent" + url);
			setStatus(URLError);
		}
	}

	public void setTime(String time)
	{
		try
		{
			date = df.parse(time);
		} catch (java.text.ParseException e)
		{}
	}

	// helpers for the TableModel
	private static final String columnNames[] = {
		// 0       1            2      3       4        5       6        7            8           9         10       11
		"Address", "RunState", "Type", "Size", "Title", "Date", "Level", "Links Out", "Links In", "Server", "Error", "Status"
	};

	public Object getColumn(int columnIndex)
	{
		Object result = null;
		switch (columnIndex)
		{
		case 0:
			result = url;
			break;
		case 1:
			result = runState;
			break;
		case 2:
			result = contentType;
			break;
		case 3:
			result = size;
			break;
		case 4:
			result = title;
			break;
		case 5:
			result = date;
			break;
		case 6:
			result = level;
			break;
		case 7:
			result = linksOut.size();
			break;
		case 8:
			result = linksIn.size();
			break;
		case 9:
			result = url.getHost();
			break;
		case 10:
			result = errorString;
			break;
		case 11:
			result = status;
			break;
		default:
			throw new Error("Invalid column index passed to getColumn");
		}
		return result;
	}

	public static Comparator<PageStats> getComparator(int columnIndex, int direction)
	{
		return new StatsComparator(columnIndex, direction);
	}

	public static String getColumnName(int columnIndex)
	{
		return columnNames[columnIndex];
	}

	public static int getColumnCount()
	{
		return columnNames.length;
	}

	public static Class<?> getColumnClass(int columnIndex)
	{
		Class<?> result = null;
		switch (columnIndex)
		{
		case 0:
			result = URL.class;
			break;
		case 1:
		case 3:
		case 6:
		case 7:
		case 8:
		case 11:
			result = Integer.class;
			break;
		case 2:
		case 4:
		case 9:
		case 10:
			result = String.class;
			break;
		case 5:
			result = Date.class;
			break;
		default:
			throw new Error("Called with invalid Column Number: " + columnIndex);
		}

		return result;
	}
}
