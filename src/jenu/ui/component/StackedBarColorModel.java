package jenu.ui.component;

import java.awt.Color;

public interface StackedBarColorModel
{
	/** Gets the background color of the index-th item. */
	public Color getBackgroundColorAt(int index);

	/** Gets the foreground color of the index-th item. */
	public Color getForegroundColorAt(int index);
}
