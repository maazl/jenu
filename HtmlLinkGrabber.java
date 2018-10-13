/*
 * HtmlDumper.java -- Dumps an HTML document tree. 
 * Copyright (C) 1999 Quiotix Corporation.  
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as 
 * published by the Free Software Foundation.  
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License (http://www.gnu.org/copyleft/gpl.txt)
 * for more details.
 */

import com.quiotix.html.parser.*;
import java.net.URL;
import java.io.*;


/** 
 * Simple HtmlVisitor which dumps out the document to the specified 
 * output stream. 
 *
 * @author Brian Goetz, Quiotix
 */

public class HtmlLinkGrabber extends HtmlVisitor {
    protected PageStats m_stats;
    
    public HtmlLinkGrabber(PageStats stats)     {m_stats = stats;}
    
    public void finish()                   {}
    
    public void visit(HtmlDocument.Tag t)        {
	if (t.tagName.equalsIgnoreCase("a")) {
	    URL newURL = null;
	    String value = attributesGet(t.attributeList, "href");
	    if (value != null) {
		m_stats.addLinkOut(value);
	    }
	}
	if (t.tagName.equalsIgnoreCase("img")) {
	    String value = attributesGet(t.attributeList, "src");
	    if (value != null) {
		m_stats.addLinkOut(value);
	    }
	}
    }
    protected String attributesGet
	(
	 HtmlDocument.AttributeList attributes,
	 String                     name
	 ) {
	java.util.Vector attrs = attributes.attributes;
	for (int i = 0; i < attrs.size(); i++) {
	    HtmlDocument.Attribute attribute = (HtmlDocument.Attribute) attrs.get(i);
	    if (attribute.name.equalsIgnoreCase(name)) {
		String value = attribute.value;
		int firstQuote = value.indexOf("\"");
		int lastQuote  = value.lastIndexOf("\"");
		firstQuote = (firstQuote == -1) ? 0 : firstQuote;
		lastQuote =  (lastQuote == -1)  ? value.length()-1 : lastQuote;
		String v = value.substring(firstQuote+1, lastQuote);
		return v;
	    } 
	}
	return null;
    }
    public void visit(HtmlDocument.EndTag t)     {}
    public void visit(HtmlDocument.Comment c)    {}
    public void visit(HtmlDocument.Text t)       {}
    public void visit(HtmlDocument.Newline n)    {}
    public void visit(HtmlDocument.Annotation a) {}
}

