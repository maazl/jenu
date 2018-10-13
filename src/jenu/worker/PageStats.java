package jenu.worker;

import java.net.URL;
import java.util.Date;
import java.util.EnumSet;
import java.util.Vector;
import java.text.DateFormat;
import java.net.MalformedURLException;
import java.awt.Color;

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
	public Vector<String> linksIn     = new Vector<>();
	public Vector<String> linksOut    = new Vector<>();
	public String         server      = null;
	public URL            url         = null;
	public String         sUrl        = null;

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

	public PageState getRunState()
	{
		return runState;
	}

	public Color getRunStateColor()
	{
		switch (runState)
		{
		case PENDING:
			return Color.gray;
		case RUNNING:
			return Color.yellow;
		case RETRY:
			return Color.magenta;
		case DONE:
			return Color.green;
		case FAILED:
			return Color.red;
		default:
			throw new Error("Invalid runState");
		}
	}

	public void setPending()
	{
		runState = PageState.PENDING;
	}

	public void setRunning()
	{
		runState = PageState.RUNNING;
	}

	public void setError(ErrorType type, String message)
	{
		runState = PageState.FAILED;
		status.add(type);
		errorString += message;
	}

	public void setDone()
	{
		if (runState != PageState.FAILED)
		runState = PageState.DONE;
	}
	
	public void setRetry()
	{
		runState = PageState.RETRY;
	}

	public String getErrorString()
	{
		return errorString;
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
			setError(ErrorType.URLError, "Malformed URL " + u + "(" + e.getMessage() + "), with parent" + url);
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
		switch (columnIndex)
		{
		case 0:
			return URL.class;
		case 1:
			return PageState.class;
		case 3:
		case 6:
		case 7:
		case 8:
			return Integer.class;
		case 2:
		case 4:
		case 9:
		case 10:
			return String.class;
		case 5:
			return Date.class;
		case 11:
			return EnumSet.class;
		default:
			throw new Error("Called with invalid Column Number: " + columnIndex);
		}
	}
}
