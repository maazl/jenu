package jenu.ui.viewmodel;

/** Total state of a row => color. */
public enum RowState
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
	/** Processing has completed without messages. */
	OK,
	/** Processing has completed with info messages. */
	INFO,
	/** Processing has completed with warnings. */
	WARNING,
	/** Processing has completed with errors. */
	ERROR;

	public static final RowState[] values = RowState.values();

	public static RowState valueOf(int ordinal)
	{	return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
	}
}
