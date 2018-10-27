package jenu;

import jenu.ui.JenuSiteWindow;

public final class Jenu
{
	public static void main(String args[])
	{
		// TODO: handle command line arguments, e.g. to work in batch mode or to open a saved configuration.

		// Open one initial window by default
		JenuSiteWindow.openNewWindow(null);
	}

	private Jenu()
	{ }
}
