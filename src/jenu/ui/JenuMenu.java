package jenu.ui;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;

final class JenuMenu extends JMenuBar
{
	private static final long serialVersionUID = 1L;
	Jenu m_owner = null;

	public JenuMenu(Jenu owner)
	{
		super();
		m_owner = owner;
		add(createFileMenu());
		// add(createViewMenu());
		// add(createOptionsMenu());
		// add(createHelpMenu());
	}

	protected JMenu createFileMenu()
	{
		JMenu menu = new JMenu("File");
		menu.add(m_owner.getAction("new"));
		// menu.add( m_owner.getAction("open") );
		menu.addSeparator();
		menu.addSeparator();
		menu.add(m_owner.getAction("exit"));
		return menu;
	}

	protected static JMenu createOptionsMenu()
	{
		JMenu menu = new JMenu("Options");
		menu.add(createMenuItem("Preferences..."));
		return menu;
	}

	protected static JMenu createHelpMenu()
	{
		JMenu menu = new JMenu("Help");
		menu.add(createMenuItem("Help"));
		return menu;
	}

	protected static JMenu createViewMenu()
	{
		JMenu menu = new JMenu("View");
		menu.add(createCheckMenuItem("Toolbar"));
		menu.add(createCheckMenuItem("Status Bar"));
		return menu;
	}

	protected static JMenuItem createMenuItem(String title)
	{
		JMenuItem menuItem = new JMenuItem(title);
		return menuItem;
	}

	protected static JMenuItem createCheckMenuItem(String title)
	{
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(title);
		return menuItem;
	}
}
