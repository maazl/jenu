package jenu.worker;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.*;
import java.util.*;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Grabs pages from the Internet, parses them, and finds embedded links.
 */
final class PageGrabber extends Thread
{
	private final ThreadManager m_manager;
	PageStats m_stats = null;
	private InputStream m_input;

	public PageGrabber(ThreadManager manager)
	{
		m_manager = manager;
		setPriority(MIN_PRIORITY);
	}

	public void run()
	{
		do
		{	URL url = m_stats.url;
			try
			{
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(m_manager.getWorkingSet().Timeout);
				connection.setReadTimeout(m_manager.getWorkingSet().Timeout);
				connection.connect();
				if (connection instanceof HttpURLConnection)
					handleHTTPConnection((HttpURLConnection)connection);
				else
					handleGenericConnection(connection);
			} catch (java.io.IOException ioe)
			{	m_stats.setError(EventType.IO_error, ioe.toString());
			} catch (Throwable ex)
			{	m_stats.setError(EventType.Internal_error, ex.toString());
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
			m_stats.setInfo(EventType.Redirect, connection.getResponseMessage() + ' ' + location);
			m_stats.addLinkOut(new Link(Link.REDIRECT, location, m_stats.url, 0));
			break;
		 case HttpURLConnection.HTTP_MOVED_PERM:
		 case 308: // missing in HttpURLConnection
			location = connection.getHeaderField("Location");
			m_stats.setError(EventType.Redirect, connection.getResponseMessage() + ' ' + location);
			m_stats.addLinkOut(new Link(Link.REDIRECT, location, m_stats.url, 0));
			break;
		 default:
			m_stats.setError(EventType.HTTP_error, connection.getResponseMessage());
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
	{
		switch (String.valueOf(m_stats.getContentType()))
		{case "text/html":
			handleHTML();
			return;
			//case "text/css":
			// TODO: parse css
		 case "text/plain":
			// Work around for inability to identify directories by FileUrlConnection.
			if (m_stats.sUrl.startsWith("file:"))
			{	if (m_stats.sUrl.charAt(m_stats.sUrl.length()-1) == '/')
					handleDirectory();
				else if (new File(m_stats.sUrl.substring(5)).isDirectory())
				{	m_stats.setInfo(EventType.Redirect, "Directory redirect");
					m_stats.addLinkOut(new Link(Link.REDIRECT, m_stats.sUrl + '/', m_stats.url, 0));
					return;
				}
			}
			return;
		}
	}

	private void handleHTML() throws IOException
	{
		try
		{	XMLReader p = new Parser();
			p.setContentHandler(new HtmlLinkGrabber());
			InputSource s = new InputSource();
			s.setSystemId(m_stats.sUrl);
			s.setByteStream(m_input);
			p.parse(s);
		} catch (SAXException e)
		{	m_stats.setError(EventType.HTML_parse_error, e.toString());
		}
	}

	private void handleDirectory() throws IOException
	{
		String s = new String(m_input.readAllBytes());
		int last = 0;
		int p;
		while ((p = s.indexOf('\n', last)) > 0)
		{	m_stats.addLinkOut(new Link("a", s.substring(last, p), m_stats.url, 0));
			last = p + 1;
		}
	}


	final class HtmlLinkGrabber extends DefaultHandler
	{
		private boolean atTitle = false;

		@Override public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
		{
			atTitle = false;
			String value = null;
			String type = null;
			if (localName.equalsIgnoreCase("a"))
			{	value = attributesGet(atts, "name");
				if (value != null)
					m_stats.addAnchor(value);
				value = attributesGet(atts, "href");
			} else if (localName.equalsIgnoreCase("img") || localName.equalsIgnoreCase("frame"))
				value = attributesGet(atts, "src");
			else if (localName.equalsIgnoreCase("link"))
			{	value = attributesGet(atts, "type");
				if ("text/css".equals(value))
					type = "link.css";
				value = attributesGet(atts, "href");
			} else if (localName.equalsIgnoreCase("meta"))
			{	if ("refresh".equalsIgnoreCase(attributesGet(atts, "http-equiv")))
				{	value = attributesGet(atts, "content");
					int p = value.toLowerCase().indexOf("url=");
					if (p < 0)
						return;
					value = value.substring(p + 4).trim();
				} else
					return;
			} else if (localName.equalsIgnoreCase("title"))
			{	atTitle = true;
				return;
			} else
				return; //tag not relevant

			// Handle links
			if (value != null && value.length() != 0)
			{	Link link = new Link(type != null ? type : localName.toLowerCase(), value, m_stats.url, getLine());
				m_stats.addLinkOut(link);
			}
		}

		@Override public void characters(char[] c, int start, int length) throws SAXException
		{	if (atTitle)
				m_stats.appendTitle(new String(c, start, length));
		}

		@Override public void endElement(String arg0, String arg1, String arg2) throws SAXException
		{	atTitle = false;
		}

		@Override public void endDocument() throws SAXException
		{	m_stats.setLines(locator.getLineNumber());
		}

		private String attributesGet(Attributes attributes, String name)
		{
			for (int i = 0; i < attributes.getLength(); i++)
			{	String qn = attributes.getQName(i);
				if (qn.equalsIgnoreCase(name))
					return attributes.getValue(i);
			}
			return null;
		}

		private Locator locator = null;

		private int getLine()
		{	return locator != null ? locator.getLineNumber() : -1;
		}

		@Override public void setDocumentLocator(final Locator locator)
		{	this.locator = locator;
		}
	}
}
