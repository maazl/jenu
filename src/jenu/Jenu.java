package jenu;

import java.io.File;

import jenu.ui.JenuSiteWindow;

public final class Jenu
{
	public final static File userHome = new File(System.getProperty("user.home"));

	public static void main(String args[])
	{
		// TODO: handle command line arguments, e.g. to work in batch mode or to open a saved configuration.

		// Open one initial window by default
		JenuSiteWindow.openWindow(null);
	}

	private Jenu()
	{ }
}
