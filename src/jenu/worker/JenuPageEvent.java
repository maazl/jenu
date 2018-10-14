package jenu.worker;

import java.util.EventObject;

public class JenuPageEvent extends EventObject
{
	private static final long serialVersionUID = 1L;

	public final PageStats page;
	public final boolean isNew;

	public JenuPageEvent(Object source, PageStats page, boolean isNew)
	{
		super(source);
		this.page = page;
		this.isNew = isNew;
	}
}
