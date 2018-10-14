package jenu.worker;

public enum PageState
{
	PENDING,
	RETRY,
	RUNNING,
	DONE,
	FAILED;
	
	/** Is the current state considered to be final, i.e. no further processing required? */
	public boolean isFinished()
	{
		return ordinal() >= DONE.ordinal();
	}
}
