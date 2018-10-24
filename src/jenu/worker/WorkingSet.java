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
	/** Check external URLs as well.
	 * If true any external link is checked for validity too. But external URLs are never scanned recursively. */
	public boolean CheckExternalURLs = true;
	/** Timeout in ms for connections. */
	public int Timeout = 10000;
}
