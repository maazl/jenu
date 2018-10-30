package jenu.ui.component;

public class StackedBarModelEvent extends java.util.EventObject
{
	protected int itemIndex;

	public int getItemIndex()
	{	return itemIndex;
	}

	public StackedBarModelEvent(StackedBarModel source, int index)
	{	super(source);
		itemIndex = index;
	}
	public StackedBarModelEvent(StackedBarModel source)
	{	this(source, -1);
	}
}
