package jenu.worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import static jenu.utils.Statics.*;
import jenu.utils.UrlUtils;

/** Working set to be executed by @see ThreadManager */
public class WorkingSet
{
	/** List of site URLs. All URLs that start with one of these URLs are considered to be <em>internal</em>
	 * and scanned recursively. All other URLs are external.
	 * If empty the path components of @see StatingPoints are used. */
	public ArrayList<String> sites = new ArrayList<>();
	/** List of page URLs that should be used as starting point.
	 * From this Point every internal URL (according to @see Sites) are scanned recursively.
	 * If empty, the Starting point URLs are used. */
	public ArrayList<String> startingPoints = new ArrayList<>();
	/** URL patterns to exclude from check. These are Regular expressions matched against the <em>full</em> URL.
	 * If one of them matches the URL is ignored. This applies to internal URLs as well as to external ones. */
	public ArrayList<Pattern> excludePatterns = new ArrayList<Pattern>() {{ add(Pattern.compile("/\\.")); }};

	/** Check external URLs as well.
	 * If true any external link is checked for validity too. But external URLs are never scanned recursively. */
	public boolean checkExternalURLs = true;
	/** Follow HTTP redirects on external URLs. Normally external URLs are not checked recursively. */
	public boolean followExternalRedirects = true;

	/** Maximum concurrent threads */
	public int maxWorkerThreads = 10;
	/** Timeout in ms for connections. */
	public int timeout = 10000;
	/** Do not follow links that require more than MaxDepth documents from any of the starting points.
	 * This also restricts the maximum number of redirects which are considered to be a special kind of link. */
	public int maxDepth = 100;

	public void validate()
	{
		// apply defaults
		if (startingPoints.size() == 0)
			startingPoints.addAll(sites);
		else if (sites.size() == 0)
			for (String sp : startingPoints)
				sites.add(UrlUtils.getPath(sp));

		// sort Sites and remove duplicates
		Collections.sort(sites);
		removeAdjacentDuplicates(sites);
	}

	/** Checks whether an URL belongs to the current list of sites.
	 * @return true: yes */
	boolean isInternalUrl(String url)
	{
		int p = Collections.binarySearch(sites, url);
		if (p >= 0)
			return true; // exact hit
		return p != -1 && url.startsWith(sites.get(-2 - p));
	}

	/** Checks whether any of the exclude patterns matches an URL.
	 * @return true: yes */
	boolean isExcluded(String url)
	{
		for (Pattern rx : excludePatterns)
			if (rx.matcher(url).find())
				return true;
		return false;
	}
}
