package jenu.worker;

import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.*;
import java.util.*;

import jenu.model.*;

/**
 * Grabs pages from the Internet, parses them, and finds embedded links.
 */
final class PageGrabber extends Thread
{
	private final ThreadManager m_manager;
	volatile PageStats m_stats = null;
	private InputStream m_input;

	static
	{	// Work around for incomplete Mime types in Java.
		// Add further mime types without loosing the standard content from the JVM.
		URLConnection.setFileNameMap(new FileNameMap()
			{	private final FileNameMap base = URLConnection.getFileNameMap();
				@Override public String getContentTypeFor(String fileName)
				{	if (fileName.endsWith(".css") || fileName.endsWith(".CSS"))
						return "text/css";
					return base.getContentTypeFor(fileName);
				}
			} );
	}

	public PageGrabber(ThreadManager manager)
	{
		m_manager = manager;
		setPriority(MIN_PRIORITY);
	}

	public void run()
	{
		do
		{	try
			{
				URLConnection connection = m_stats.url.openConnection();
				connection.setConnectTimeout(m_manager.getWorkingSet().timeout);
				connection.setReadTimeout(m_manager.getWorkingSet().timeout);
				connection.connect();
				if (connection instanceof HttpURLConnection)
					handleHTTPConnection((HttpURLConnection)connection);
				else
					handleGenericConnection(connection);
			} catch (java.io.IOException ioe)
			{	m_stats.addError(MessageType.IO_error, ioe.toString());
			} catch (Exception ex)
			{	m_stats.addError(MessageType.Internal_error, ex.toString());
			}
		} while (m_manager.nextTask(this));
	}

	private void handleHTTPConnection(HttpURLConnection connection) throws IOException
	{	String location;
		switch(connection.getResponseCode())
		{case HttpURLConnection.HTTP_OK:
			handleGenericConnection(connection);
			break;
		 case HttpURLConnection.HTTP_MOVED_TEMP:
		 case 307: // missing in HttpURLConnection
			location = connection.getHeaderField("Location");
			m_stats.addInfo(MessageType.Redirect, connection.getResponseCode() + " " + connection.getResponseMessage() + ' ' + location);
			m_stats.addLinkOut(new LinkStats(Link.REDIRECT, location, m_stats, 0));
			break;
		 case HttpURLConnection.HTTP_MOVED_PERM:
		 case 308: // missing in HttpURLConnection
			location = connection.getHeaderField("Location");
			m_stats.addError(MessageType.Redirect, connection.getResponseCode() + " " + connection.getResponseMessage() + ' ' + location);
			m_stats.addLinkOut(new LinkStats(Link.REDIRECT, location, m_stats, 0));
			break;
		 default:
			m_stats.addError(MessageType.HTTP_error, connection.getResponseCode() + " " + connection.getResponseMessage());
		}
	}

	private void handleGenericConnection(URLConnection connection) throws IOException
	{
		m_stats.setContentType(connection.getContentType());
		long lastModified = connection.getLastModified();
		if (lastModified > 0)
			m_stats.setDate(new Date(lastModified));
		m_input = connection.getInputStream();
		try
		{	m_stats.setSize(connection.getContentLengthLong());
			handleContent(m_stats.getContentType());
		} finally {
			m_input.close();
			m_input = null;
		}
	}

	private void handleContent(String mimeType) throws IOException
	{	String type = m_stats.getContentType();
		int p = type.indexOf(';');
		if (p >= 0)
			type = type.substring(0, p);
		switch (type)
		{case "text/html":
			new HtmlLinkGrabber(m_stats).handleHTML(m_input);
			return;
		 case "text/css":
			if (m_stats.isInternal)
				new CssLinkGrabber(m_stats).handleCSS(m_input);
			return;
		 case "text/plain":
			// Work around for inability to identify directories by FileUrlConnection.
			if (m_stats.sUrl.startsWith("file:"))
			{	if (m_stats.sUrl.charAt(m_stats.sUrl.length()-1) == '/')
					handleDirectory();
				else if (new File(m_stats.sUrl.substring(5)).isDirectory())
				{	m_stats.addInfo(MessageType.Redirect, "Directory redirect");
					m_stats.addLinkOut(new LinkStats(Link.REDIRECT, m_stats.sUrl + '/', m_stats, 0));
					return;
				}
			}
			return;
		}
	}

	private void handleDirectory() throws IOException
	{
		String s = readAllInputAsString();
		int last = 0;
		int p;
		while ((p = s.indexOf('\n', last)) > 0)
		{	m_stats.addLinkOut(new LinkStats(Link.DIRECTORY, s.substring(last, p), m_stats, 0));
			last = p + 1;
		}
	}

	private String readAllInputAsString() throws IOException
	{	ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[0xFFFF];
		for (int len = m_input.read(buffer); len != -1; len = m_input.read(buffer))
			os.write(buffer, 0, len);
		return os.toString();
	}
}
