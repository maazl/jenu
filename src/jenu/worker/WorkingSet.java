package jenu.worker;

import java.util.ArrayList;

/** Working set to be executed by @see ThreadManager */
public class WorkingSet
{
	/** List of site URLs. All URLs that start with one of these URLs are considered to be <em>internal</em>
	 * and scanned recursively. All other URLs are external.
	 * If empty the path components of @see StatingPoints are used. */
	public ArrayList<String> Sites = new ArrayList<>();
	/** List of page URLs that should be used as starting point.
	 * From this Point every internal URL (according to @see Sites) are scanned recursively.
	 * If empty, the Starting point URLs are used. */
	public ArrayList<String> StartingPoints = new ArrayList<>();
	/** URL patterns to exclude from check. These are Regular expressions matched against the full URL.
	 * If one of them Matches the URL is ignored. This applies to internal URLs as well as to external ones. */
	public ArrayList<String> ExcludePatterns = new ArrayList<String>() {{ add("/\\."); }};

	/** Check external URLs as well.
	 * If true any external link is checked for validity too. But external URLs are never scanned recursively. */
	public boolean CheckExternalURLs = true;
	/** Follow HTTP redirects on external URLs. Normally external URLs are not checked recursively. */
	public boolean FollowExternalRedirects = true;

	/** Maximum concurrent threads */
	public int MaxWorkerThreads = 10;
	/** Timeout in ms for connections. */
	public int Timeout = 10000;
	/** Do not follow links that require more than MaxDepth documents from any of the starting points.
	 * This also restricts the maximum number of redirects which are considered to be a special kind of link. */
	public int MaxDepth = 100;
}
