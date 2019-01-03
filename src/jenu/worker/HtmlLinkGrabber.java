package jenu.worker;

import java.io.IOException;
import java.io.InputStream;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import jenu.model.Link;
import jenu.model.MessageType;
import static jenu.utils.Statics.*;

final class HtmlLinkGrabber extends DefaultHandler
{
	public void handleHTML(InputStream is) throws IOException
	{
		try
		{	XMLReader p = new Parser();
			p.setContentHandler(this);
			p.setErrorHandler(this);
			InputSource s = new InputSource();
			s.setSystemId(stats.sUrl);
			s.setByteStream(is);
			p.parse(s);
		} catch (SAXException e)
		{	stats.addError(MessageType.Parse_error, e.toString());
		}
	}

	private final PageStats stats;

	public HtmlLinkGrabber(PageStats stats)
	{	this.stats = stats;
	}

	private enum AtTag
	{	TITLE,
		STYLE,
	}
	private AtTag at;
	private int start;
	private final StringBuilder sb = new StringBuilder();
	private CssLinkGrabber cssGrabber;

	@Override public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		at = null;
		sb.setLength(0);
		start = locator.getLineNumber();

		String target = null;
		if (localName.equalsIgnoreCase("a"))
		{	target = attributesGet(atts, "name");
			if (target != null)
				stats.addAnchor(target);
			if (!stats.isInternal) // only check for anchors at external pages.
				return;
			target = attributesGet(atts, "href");
		} else if (!stats.isInternal) // only check for anchors at external pages.
			return;
		else if (localName.equalsIgnoreCase("img") || localName.equalsIgnoreCase("frame"))
			target = attributesGet(atts, "src");
		else if (localName.equalsIgnoreCase("link"))
			target = attributesGet(atts, "href");
		else if (localName.equalsIgnoreCase("meta"))
		{	if ("refresh".equalsIgnoreCase(attributesGet(atts, "http-equiv")))
			{	target = attributesGet(atts, "content");
				int p = target.toLowerCase().indexOf("url=");
				if (p < 0)
					return;
				target = target.substring(p + 4).trim();
			} else
				return;
		} else if (localName.equalsIgnoreCase("title"))
		{	at = AtTag.TITLE;
			return;
		} else if (localName.equalsIgnoreCase("style"))
		{	at = AtTag.STYLE;
			return;
		} else
			return; //tag not relevant

		// Handle links
		if (isEmpty(target) || target.startsWith("javascript:") || target.startsWith("mailto:")) // exclude JavaScript & mail URLs too
			return;
		Link link = new LinkStats(localName.toLowerCase(), target, stats, getLine());
		stats.addLinkOut(link);
	}

	@Override public void characters(char[] c, int start, int length) throws SAXException
	{	if (at != null)
			sb.append(c, start, length);
	}

	@Override public void endElement(String arg0, String arg1, String arg2) throws SAXException
	{	if (at == null)
			return;
		switch (at)
		{case TITLE:
			stats.setTitle(sb.toString());
			break;
		 case STYLE:
			{	if (cssGrabber == null)
					cssGrabber = new CssLinkGrabber(stats);
				cssGrabber.handleCSS(sb.toString(), start);
			}
		}
		at = null;
		sb.setLength(0);
	}

	@Override public void endDocument() throws SAXException
	{	stats.setLines(locator.getLineNumber());
	}

	private static String attributesGet(Attributes attributes, String name)
	{
		for (int i = 0; i < attributes.getLength(); i++)
		{	String qn = attributes.getQName(i);
			if (qn.equalsIgnoreCase(name))
				return attributes.getValue(i);
		}
		return null;
	}

	@Override public void warning(SAXParseException ex) throws SAXException
	{	if (stats.isInternal) // no errors in external pages
			stats.addWarning(MessageType.Parse_error, formatException(ex));
	}

	@Override public void error(SAXParseException ex) throws SAXException
	{	if (stats.isInternal) // no errors in external pages
			stats.addError(MessageType.Parse_error, formatException(ex));
	}

	private static String formatException(SAXParseException ex)
	{	StringBuilder sb = new StringBuilder();
		sb.append('@').append(ex.getLineNumber()).append(':').append(ex.getColumnNumber()).append(' ')
			.append(ex.getMessage());
		return sb.toString();
	}

	private Locator locator = null;

	private int getLine()
	{	return locator != null ? locator.getLineNumber() : -1;
	}

	@Override public void setDocumentLocator(final Locator locator)
	{	this.locator = locator;
	}
}