package jenu.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jenu.ui.viewmodel.RowState;

final class JenuUIUtils
{
	private JenuUIUtils()
	{}

	/** Home directory of the user to store configuration files. */
	public final static File userHome = new File(System.getProperty("user.home"));

	/** Determine foreground color from row state.
	 * @param runState State of the table row.
	 * @return Foreground color. */
	public static Color getStateColor(RowState runState)
	{	switch (runState)
		{default: // PENDING
			return Color.blue;
		 case RUNNING:
			return MyCYAN;
		 case RETRY:
			return Color.magenta;
		 case EXCLUDED:
			return Color.gray;
		 case OK:
			return MyGREEN;
		 case INFO:
			return MyYELLOW;
		 case WARNING:
			return Color.orange;
		 case ERROR:
			return Color.red;
		}
	}
	private final static Color MyGREEN = new Color(0x00aa00);
	private final static Color MyYELLOW = new Color(0xaa9900);
	private final static Color MyCYAN = new Color(0x008888);

	/** Construct a menu from a set of actions.
	 * @param title Menu title
	 * @param items Actions to add. {@code null} entries create separators.
	 * @return new menu. In general you want to add the result to a menu bar. */
	public static JMenu makeMenu(String title, Action ... items)
	{	JMenu menu = new JMenu(title);
		for (Action a : items)
			if (a == null)
				menu.addSeparator();
			else
				menu.add(a);
		return menu;
	}
	/** Construct a menu from a set of menu items.
	 * @param title Menu title
	 * @param items Menu items to add. {@code null} entries create separators.
	 * @return new menu. In general you want to add the result to a menu bar. */
	public static JMenu makeMenu(String title, JMenuItem... items)
	{	JMenu menu = new JMenu(title);
		for (JMenuItem a : items)
			if (a == null)
				menu.addSeparator();
			else
				menu.add(a);
		return menu;
	}

	/** Put a set of buttons in a group to build a radio button group.
	 * @param buttons Set of buttons.
	 * @return The created button group. Typically you can safely ignore this return value. */
	public static ButtonGroup makeButtonGroup(AbstractButton... buttons)
	{	ButtonGroup group = new ButtonGroup();
		for (AbstractButton b : buttons)
			group.add(b);
		return group;
	}

	/** Open an URL in external browser.
	 * @param url URL to open. null is allowed too and causes no action.
	 * @return null: success or no action because url is null;
	 * else Something went wrong. Either the URL is broken
	 * or the platform does not provide the requested service. */
	public static Exception openInBrowser(URL url)
	{	if (url == null)
			return null;
		try
		{	Desktop.getDesktop().browse(url.toURI());
			return null;
		} catch (IOException | URISyntaxException e)
		{	return e;
		}
	}
}
