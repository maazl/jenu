package jenu.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;

import javax.swing.*;
import java.util.Hashtable;

public final class Jenu extends JFrame
// implements PropertyChangeListener
{
	private JDesktopPane m_desktop = null;

	// protected JenuSettings m_settings = new JenuSettings();

	public static void main(String args[])
	{
		Jenu jenu = new Jenu("Jenu Link Checker");
		jenu.initialize();
		jenu.setVisible(true);
		jenu.createNewWindow();
	}

	public Jenu(String title)
	{
		super(title);
	}

	private void initialize()
	{
		addWindowListener(new WindowAdapter()
		{	public void windowClosing(WindowEvent e)
			{	System.exit(0);
			}
		});
		setJMenuBar(new Menu());
		m_desktop = new JDesktopPane();

		setContentPane(m_desktop);
		m_desktop.putClientProperty("JDesktopPane.dragMode", "outline");

		// m_settings.addPropertyChangeListener(this);
		pack();
		setSize(new Dimension(500, 400));
	}

	private void createNewWindow()
	{
		JInternalFrame internal = new JenuInternalFrame(this);
		m_desktop.add(internal);
		internal.setVisible(true);
		internal.toFront();
		try
		{	internal.setSelected(true);
		} catch (PropertyVetoException e) {}
	}

	// public JenuSettings getSettings() {
	// return m_settings;
	// }

	// Listen for changes in settings.
	// public void propertyChange(PropertyChangeEvent evt) {
	// System.out.println(evt);
	// if (evt.getSource() == m_settings) {
	// ThreadManager tm = new ThreadManager(m_settings.getUrlToCheck(), );
	// tm.start();
	// }
	// }

	private final class Menu extends JMenuBar
	{
		public Menu()
		{
			super();
			add(createFileMenu());
			// add(createViewMenu());
			// add(createOptionsMenu());
			// add(createHelpMenu());
		}

		private JMenu createFileMenu()
		{
			JMenu menu = new JMenu("File");
			menu.add(new AbstractAction("New window")
				{	public void actionPerformed(ActionEvent e)
					{	createNewWindow();
					}
				} );
			/*menu.add(new AbstractAction("Open...")
			{	public void actionPerformed(java.awt.event.ActionEvent e)
				{	OpenURLDialog open = new OpenURLDialog(owner);
					open.show();
				}
			} );*/
			menu.addSeparator();
			// menu.addSeparator();
			menu.add(new AbstractAction("Exit")
				{	public void actionPerformed(ActionEvent e)
					{	System.exit(0);
					}
				} );
			return menu;
		}

		/*private JMenu createOptionsMenu()
		{
			JMenu menu = new JMenu("Options");
			menu.add(new JMenuItem("Preferences..."));
			return menu;
		}

		private JMenu createHelpMenu()
		{
			JMenu menu = new JMenu("Help");
			menu.add(new JMenuItem("Help"));
			return menu;
		}

		private JMenu createViewMenu()
		{
			JMenu menu = new JMenu("View");
			menu.add(new JCheckBoxMenuItem("Toolbar"));
			menu.add(new JCheckBoxMenuItem("Status Bar"));
			return menu;
		}*/
	}
}
