package jenu.worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import static jenu.utils.Statics.*;

import jenu.utils.Statics;
import jenu.utils.UrlUtils;

/** Working set to be executed by @see ThreadManager */
public class WorkingSet
{
	/** List of site URLs. All URLs that start with one of these URLs are considered to be <em>internal</em>
	 * and scanned recursively. All other URLs are external.
	 * If empty the path components of @see StatingPoints are used. */
	public final ArrayList<String> sites = new ArrayList<>();
	/** List of page URLs that should be used as starting point.
	 * From this Point every internal URL (according to @see Sites) are scanned recursively.
	 * If empty, the Starting point URLs are used. */
	public final ArrayList<String> startingPoints = new ArrayList<>();
	/** URL patterns to exclude from check. These are Regular expressions matched against the <em>full</em> URL.
	 * If one of them matches the URL is ignored. This applies to internal URLs as well as to external ones. */
	public final ArrayList<Pattern> excludePatterns = new ArrayList<Pattern>();

	/** Check external URLs as well.
	 * If true any external link is checked for validity too. But external URLs are never scanned recursively. */
	public boolean checkExternalURLs;
	/** Follow HTTP redirects on external URLs. Normally external URLs are not checked recursively. */
	public boolean followExternalRedirects;

	/** Maximum concurrent threads */
	public int maxWorkerThreads;
	/** Timeout in ms for connections. */
	public int timeout;
	/** Do not follow links that require more than MaxDepth documents from any of the starting points.
	 * This also restricts the maximum number of redirects which are considered to be a special kind of link. */
	public int maxDepth;

	/** create WorkingSet with default values. */
	public WorkingSet()
	{	reset();
	}

	/** Reset instance to it's initial state. */
	public void reset()
	{	sites.clear();
		startingPoints.clear();
		excludePatterns.clear();
		excludePatterns.add(defaultExclude);
		checkExternalURLs = true;
		followExternalRedirects = true;
		maxWorkerThreads = 10;
		timeout = 1000;
		maxDepth = 100;
	}
	private final static Pattern defaultExclude = Pattern.compile("/\\.");

	public void validate()
	{	// no empty strings
		sites.replaceAll(String::trim);
		sites.removeIf(Statics::isEmpty);
		startingPoints.replaceAll(String::trim);
		startingPoints.removeIf(Statics::isEmpty);

		// apply defaults
		if (sites.size() == 0)
			for (String sp : startingPoints)
				sites.add(UrlUtils.getPath(sp));

		// sort Sites and remove duplicates including implications
		Collections.sort(sites);
		removeAdjacentDuplicates(sites, (a, b) -> b.startsWith(a));
	}

	/** Checks whether an URL belongs to the current list of sites.
	 * @return true: yes */
	final boolean isInternalUrl(String url)
	{
		int p = Collections.binarySearch(sites, url);
		if (p >= 0)
			return true; // exact hit
		return p != -1 && url.startsWith(sites.get(-2 - p));
	}

	/** Checks whether any of the exclude patterns matches an URL.
	 * @return true: yes */
	final boolean isExcluded(String url)
	{
		for (Pattern rx : excludePatterns)
			if (rx.matcher(url).find())
				return true;
		return false;
	}
}
