package jenu.worker;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.*;
import java.util.*;
import com.quiotix.html.parser.HtmlParser;
import com.quiotix.html.parser.HtmlVisitor;
import com.quiotix.html.parser.HtmlDocument;

/**
 * Grabs pages from the Internet, parses them, and finds embedded links.
 */
final class PageGrabber extends Thread
{
	private final ThreadManager m_manager;
	PageStats m_stats = null;
	private CountingInputStream m_input;

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
			{	m_stats.setError(ErrorType.IOError, ioe.toString());
			} catch (Throwable ex)
			{	m_stats.setError(ErrorType.InternalError, ex.toString());
			}
		} while (m_manager.nextTask(this));
	}

	private void handleHTTPConnection(HttpURLConnection connection) throws IOException
	{
		switch(connection.getResponseCode())
		{case HttpURLConnection.HTTP_OK:
			handleGenericConnection(connection);
			break;
		 case HttpURLConnection.HTTP_MOVED_TEMP:
		 case 307: // missing in HttpURLConnection
			m_stats.setInfo(ErrorType.Redirect, connection.getResponseMessage());
			m_stats.addLinkOut(new Link(Link.REDIRECT, connection.getHeaderField("Location"), m_stats.url, 0));
			break;
		 case HttpURLConnection.HTTP_MOVED_PERM:
		 case 308: // missing in HttpURLConnection
			m_stats.setError(ErrorType.Redirect, connection.getResponseMessage());
			m_stats.addLinkOut(new Link(Link.REDIRECT, connection.getHeaderField("Location"), m_stats.url, 0));
			break;
		 default:
			m_stats.setError(ErrorType.HTTPError, connection.getResponseMessage());
		}
	}

	private void handleGenericConnection(URLConnection connection) throws IOException
	{
		m_stats.setContentType(connection.getContentType());
		long lastModified = connection.getLastModified();
		if (lastModified > 0)
			m_stats.setDate(new Date(lastModified));
		try (CountingInputStream is = new CountingInputStream(connection.getInputStream()))
		{	m_input = is;
			m_stats.setSize(connection.getContentLengthLong());
			handleContent(m_stats.getContentType());
		}
	}

	private void handleContent(String mimeType) throws IOException
	{
		switch (String.valueOf(m_stats.getContentType()))
		{case "text/html":
			handleHTML();
			handleEnd(false);
			return;
			//case "text/css":
			// TODO: parse css
		 case "text/plain":
			// Work around for inability to identify directories by FileUrlConnection.
			if (m_stats.sUrl.startsWith("file:"))
			{	if (m_stats.sUrl.charAt(m_stats.sUrl.length()-1) == '/')
					handleDirectory();
				else if (new File(m_stats.sUrl.substring(5)).isDirectory())
				{	m_stats.setInfo(ErrorType.Redirect, "Directory redirect");
					m_stats.addLinkOut(new Link(Link.REDIRECT, m_stats.sUrl + '/', m_stats.url, 0));
					return;
				}
			}
			handleEnd(false);
			return;
		 default:
			handleEnd(true);
		}
	}

	private void handleEnd(boolean binary) throws IOException
	{
		if (m_stats.getSize() == -1L)
		{	m_input.skipToEnd();
			m_stats.setSize(m_input.getBytesRead());
		}
		if (!binary)
			m_stats.setLines(m_input.getLine());
	}

	private void handleHTML()
	{
		HtmlParser parser = new HtmlParser(m_input);
		try
		{
			HtmlDocument doc = parser.HtmlDocument();
			doc.accept(new HtmlLinkGrabber());
		} catch (com.quiotix.html.parser.ParseException e)
		{
			m_stats.setError(ErrorType.HTMLParseError, e.toString());
		} catch (com.quiotix.html.parser.TokenMgrError err)
		{
			m_stats.setError(ErrorType.HTMLParseError, err.toString());
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


	final class HtmlLinkGrabber extends HtmlVisitor
	{
		boolean atTitle = false;

		public void finish()
		{}

		public void visit(HtmlDocument.Tag t)
		{
			atTitle = false;
			String value = null;
			String type = null;
			if (t.tagName.equalsIgnoreCase("a"))
			{	value = attributesGet(t.attributeList, "name");
				if (value != null)
					m_stats.addAnchor(value);
				value = attributesGet(t.attributeList, "href");
			} else if (t.tagName.equalsIgnoreCase("img"))
				value = attributesGet(t.attributeList, "src");
			else if (t.tagName.equalsIgnoreCase("link"))
			{	value = attributesGet(t.attributeList, "type");
				if ("text/css".equals(value))
					type = "link.css";
				value = attributesGet(t.attributeList, "href");
			} else if (t.tagName.equalsIgnoreCase("meta"))
			{	if ("refresh".equalsIgnoreCase(attributesGet(t.attributeList, "http-equiv")))
				{	value = attributesGet(t.attributeList, "content");
					int p = value.toLowerCase().indexOf("url=");
					if (p < 0)
						return;
					value = value.substring(p + 4).trim();
				} else
					return;
			} else if (t.tagName.equalsIgnoreCase("title"))
			{	atTitle = true;
				return;
			} else
				return; //tag not relevant

			// Handle links
			if (value != null && value.length() != 0)
			{	Link link = new Link(type != null ? type : t.tagName.toLowerCase(), value, m_stats.url, m_input.getLine());
				m_stats.addLinkOut(link);
			}
		}

		private String attributesGet(
			HtmlDocument.AttributeList attributes,
			String name)
		{
			@SuppressWarnings("unchecked")
			Vector<HtmlDocument.Attribute> attrs = attributes.attributes;
			for (HtmlDocument.Attribute attribute : attrs)
			{
				if (attribute.name.equalsIgnoreCase(name))
				{
					String value = attribute.value;
					if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length()-1) == '"')
						value = value.substring(1, value.length() - 1);
					return value;
				}
			}
			return null;
		}

		public void visit(HtmlDocument.EndTag t)
		{	atTitle = false;
		}

		public void visit(HtmlDocument.Comment c)
		{}

		public void visit(HtmlDocument.Text t)
		{
			if (atTitle)
				m_stats.setTitle(t.text);
		}

		public void visit(HtmlDocument.Newline n)
		{}

		public void visit(HtmlDocument.Annotation a)
		{}
	}
}
