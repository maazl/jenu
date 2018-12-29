package jenu.model;

/** Worker state of a page */
public enum PageState
{
	/** The page has not yet been scheduled for processing and maybe it wont be scheduled.
	 * This is for instance used for the root page. */
	VIRGIN,
	/** The page has not yet been processed. */
	PENDING,
	/** The page has been processed but this should be repeated because of a potentially temporary error. */
	RETRY,
	/** The page is currently checked. */
	RUNNING,
	/** The page has been excluded from processing */
	EXCLUDED,
	/** Page processing has completed. MUST BE THE LAST ONE. */
	DONE;
}
