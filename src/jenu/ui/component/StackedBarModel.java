package jenu.ui.component;

public interface StackedBarModel
{
	/** Gets the number of items in the Model. */
  public int getCount();

	/** Gets the relative size of the index-th item. */
	public float getValueAt(int index);

	/** Gets the display text of the index-th item. */
  public String getTextAt(int index);

  public void addStackedBarModelListener(StackedBarModelListener l);

  public void removeStackedBarModelListener(StackedBarModelListener l);
}
