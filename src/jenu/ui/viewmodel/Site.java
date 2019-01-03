package jenu.ui.viewmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Pattern;

import static jenu.utils.Statics.*;

import jenu.worker.WorkingSet;

/** {@link WorkingSet} with UI extensions. */
public final class Site extends WorkingSet
{
	/** Window title */
	public String title;
	/** Last file used for open/save (if any) */
	public File lastFile;

	@Override public void reset()
	{	super.reset();
		title = null;
		lastFile = null;
	}

	/** Write current configuration to a file.
	 * @param file File to write
	 * @throws FileNotFoundException Cannot open target file.
	 * @throws IOException IO error during write.
	 */
	public void exportFile(File file) throws FileNotFoundException, IOException
	{
		java.util.Properties p = new Properties();
		if (!isEmpty(title))
			p.setProperty("title", title);
		int i = 0;
		for (String site : sites)
			p.setProperty("site." + ++i, site);
		i = 0;
		for (String start : startingPoints)
			p.setProperty("start." + ++i, start);
		i = 0;
		for (Pattern xcl : excludePatterns)
			p.setProperty("exclude." + ++i, xcl.toString());
		p.setProperty("checkExternalURLs", Boolean.toString(checkExternalURLs));
		p.setProperty("followExternalRedirects", Boolean.toString(followExternalRedirects));
		p.setProperty("maxWorkerThreads", Integer.toString(maxWorkerThreads));
		p.setProperty("timeout", Integer.toString(timeout));
		p.setProperty("maxDepth", Integer.toString(maxDepth));
		p.store(new OutputStreamWriter(new FileOutputStream(file)), "Jenu site configuration");
	}

	/** Read the current configuration from a file.
	 * @param file File to read.
	 * @throws FileNotFoundException Cannot read the file.
	 * @throws IOException IO error while reading content.
	 */
	public void importFile(File file) throws FileNotFoundException, IOException
	{
		java.util.Properties p = new Properties();
		p.load(new InputStreamReader(new FileInputStream(file)));
		sites.clear();
		startingPoints.clear();
		excludePatterns.clear();
		Enumeration<Object> en = p.keys();
		while (en.hasMoreElements())
		{	String s = (String)en.nextElement();
			String v = p.getProperty(s);
			if (s.startsWith("site."))
				sites.add(v);
			else if (s.startsWith("start."))
				startingPoints.add(v);
			else if (s.startsWith("exclude."))
				excludePatterns.add(Pattern.compile(v));
			else if (s.equals("title"))
				title = v;
			else if (s.equals("checkExternalURLs"))
				checkExternalURLs = Boolean.parseBoolean(v);
			else if (s.equals("followExternalRedirects"))
				followExternalRedirects = Boolean.parseBoolean(v);
			else if (s.equals("maxWorkerThreads"))
				maxWorkerThreads = Integer.parseInt(v);
			else if (s.equals("timeout"))
				timeout = Integer.parseInt(v);
			else if (s.equals("maxDepth"))
				maxDepth = Integer.parseInt(v);
		}
	}
}
