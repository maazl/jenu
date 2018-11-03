package jenu.worker;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Locator;
import org.w3c.css.sac.Parser;

import com.steadystate.css.parser.HandlerBase;
import com.steadystate.css.parser.SACParserCSS3;

final class CssLinkGrabber extends HandlerBase
{
	public void handleCSS(InputSource is, int offset) throws IOException
	{	this.offset = offset;
		try
		{	Parser p = new SACParserCSS3();
			p.setDocumentHandler(this);
			p.setErrorHandler(this);
			p.parseStyleSheet(is);
		} catch (CSSException e)
		{	stats.setError(EventType.CSS_parse_error, e.toString());
		}
	}
	public void handleCSS(InputStream is) throws IOException
	{	InputSource s = new InputSource();
		s.setByteStream(is);
		handleCSS(s, 0);
	}
	public void handleCSS(String s, int offset)
	{	try
		{	InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(s));
			handleCSS(is, offset);
		} catch (IOException e)
		{	// StringReader can't throw IOException
			assert false;
		}
	}

	private final PageStats stats;
	private int offset;

	public CssLinkGrabber(PageStats stats)
	{	this.stats = stats;
	}

	@Override public void property(String name, LexicalUnit value, boolean important, Locator locator)
	{	if (value.getLexicalUnitType() == LexicalUnit.SAC_URI)
			stats.addLinkOut(new Link(Link.CSS, value.getStringValue(), stats.url, locator.getLineNumber() + offset));
	}
}