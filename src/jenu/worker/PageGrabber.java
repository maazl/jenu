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
	private PageStats m_stats = null;
	private CountingBufferedInputStream m_input;

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
		do
		{	URL url = m_stats.url;
			try
			{
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(20000);
				connection.setReadTimeout(10000);
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
		int responseCode = connection.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK)
			m_stats.setError(ErrorType.HTTPError, connection.getResponseMessage());
		else
			handleGenericConnection(connection);
	}

	private void handleGenericConnection(URLConnection connection) throws IOException
	{
		m_stats.contentType = connection.getContentType();
		long lastModified = connection.getLastModified();
		if (lastModified > 0)
			m_stats.date = new Date(lastModified);
		try (CountingBufferedInputStream is = new CountingBufferedInputStream(connection.getInputStream()))
		{	if (m_stats.contentType != null)
			{	m_input = is;
				if (m_stats.contentType.equals("text/html"))
					handleHTML(is);
				else
				{	// handleUnparsedData(is, stats);
				}
				m_stats.size = connection.getContentLengthLong();
				if (m_stats.size == -1L)
					m_stats.size = is.getBytesRead();
				m_stats.lines = is.getLine();
			}
		}
	}

	private void handleHTML(BufferedInputStream is)
	{
		HtmlParser parser = new HtmlParser(is);
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

	/*private void handleUnparsedData(CountingBufferedInputStream is) throws IOException
	{
		// REad it, and chew it up.
		byte buffer[] = new byte[10000];
		while (is.read(buffer) != -1)
		{}
	}*/

	public String toString()
	{
		return m_stats.toString();
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
			String typeX = "";
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
					typeX = ".css";
				value = attributesGet(t.attributeList, "href");
			} else
			{	if (t.tagName.equalsIgnoreCase("title"))
					atTitle = true;
				return;
			}
			// Handle links
			if (value != null && value.length() != 0)
			{	Link link = new Link(t.tagName.toLowerCase() + typeX, value, m_stats.url, m_input.getLine());
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
					int firstQuote = value.indexOf("\"");
					int lastQuote = value.lastIndexOf("\"");
					firstQuote = (firstQuote == -1) ? 0 : firstQuote;
					lastQuote = (lastQuote == -1) ? value.length() - 1 : lastQuote;
					String v = value.substring(firstQuote + 1, lastQuote);
					return v;
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
				m_stats.title = t.text;
		}

		public void visit(HtmlDocument.Newline n)
		{}

		public void visit(HtmlDocument.Annotation a)
		{}
	}
}
