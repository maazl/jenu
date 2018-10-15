package jenu.worker;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.*;
import java.util.*;
import com.quiotix.html.parser.HtmlParser;
import com.quiotix.html.parser.HtmlDocument;

//
// Grabs pages from the Internet, parses them, and finds embedded links.
//
final class PageGrabber extends Thread
{
	ThreadManager m_manager = null;
	PageStats m_stats = null;

	public PageGrabber(ThreadManager manager)
	{
		m_manager = manager;
		setPriority(MIN_PRIORITY);
	}

	public void reset(PageStats stats)
	{
		m_stats = stats;
	}

	public PageStats getStats()
	{
		return m_stats;
	}

	public void run()
	{
		m_stats.setRunning();
		m_manager.threadStarted(this);
		// System.out.println("starting: " + m_url);
		URL url = m_stats.url;
		if (url != null)
		{
			try
			{
				URLConnection connection = url.openConnection();
				if (connection instanceof HttpURLConnection)
				{
					HttpURLConnection httpConnection = (HttpURLConnection)connection;
					handleHTTPConnection(httpConnection);
				} else
				{
					System.out.println("Unknown URL Connection Type " + url);
					// handleGenericConnection(url, m_stats);
				}
			} catch (java.io.IOException ioe)
			{
				m_stats.setError(ErrorType.IOError, "Error making or reading from connection" + ioe.toString());
			}
		}
		// System.out.println("finished: " + url);
		m_stats.setDone();
		m_manager.threadFinished(this);
	}

	protected void handleHTTPConnection(HttpURLConnection connection) throws IOException
	{
		int responseCode = connection.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK)
		{
			m_stats.setError(ErrorType.HTTPError, connection.getResponseMessage());
		} else
		{
			m_stats.contentType = connection.getContentType();
			long lastModified = connection.getLastModified();
			if (lastModified > 0)
			{
				m_stats.date = new Date(lastModified);
			}
			CountingBufferedInputStream is = new CountingBufferedInputStream(connection.getInputStream());
			if (m_stats.contentType != null)
			{
				if (m_stats.contentType.equals("text/html"))
				{
					handleHTML(is);
				} else
				{
					// handleUnparsedData(is, stats);
				}
				m_stats.size = connection.getContentLengthLong();
				if (m_stats.size == -1L)
					m_stats.size = is.getBytesRead();
			}
		}
		connection.disconnect();
	}

	protected void handleHTML(CountingBufferedInputStream is)
	{
		HtmlParser parser = new HtmlParser(is);
		try
		{
			HtmlDocument doc = parser.HtmlDocument();
			doc.accept(new HtmlLinkGrabber(m_stats));
		} catch (com.quiotix.html.parser.ParseException e)
		{
			m_stats.setError(ErrorType.HTMLParseError, e.toString());
		} catch (com.quiotix.html.parser.TokenMgrError err)
		{
			m_stats.setError(ErrorType.HTMLParseError, err.toString());
		}
	}

	protected void handleUnparsedData(CountingBufferedInputStream is) throws IOException
	{
		// REad it, and chew it up.
		byte buffer[] = new byte[10000];
		while (is.read(buffer) != -1)
		{}
	}

	public String toString()
	{
		return m_stats.toString();
	}
}
