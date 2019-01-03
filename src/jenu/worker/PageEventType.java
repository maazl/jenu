package jenu.worker;

/** Kind of PageEvent. */
public enum PageEventType
{	/** The Page has not yet been tracked. */
	NEW,
	/** The Page state has changed */
	STATE,
	/** The Set of incoming links to this page has changed. */
	LINKSIN,
	/** Remove page */
	DELETE
}
