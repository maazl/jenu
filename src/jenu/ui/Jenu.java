package jenu.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Hashtable;

public final class Jenu extends JFrame
// implements PropertyChangeListener
{
	private static final long serialVersionUID = 1L;
	public static final int STATE_RUNNING = 0;
	public static final int STATE_PAUSED = 1;
	public static final int STATE_STOPPED = 2;

	private int m_state = STATE_STOPPED;

	private Hashtable<String,JenuAction> m_actions = new Hashtable<String,JenuAction>();
	private JDesktopPane m_desktop = null;

	// protected JenuSettings m_settings = new JenuSettings();

	public static void main(String args[])
	{
		Jenu jenu = new Jenu("Jenu");
		jenu.initialize();
		jenu.setVisible(true);
	}

	public Jenu(String title)
	{
		super(title);
	}

	private void initialize()
	{
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		initActions();
		setJMenuBar(new JenuMenu(this));
		m_desktop = new JDesktopPane();

		setContentPane(m_desktop);
		m_desktop.putClientProperty("JDesktopPane.dragMode", "outline");

		createNewWindow();
		// m_settings.addPropertyChangeListener(this);
		pack();
		setSize(new Dimension(500, 400));

	}

	void createNewWindow()
	{
		JInternalFrame internal = new JenuInternalFrame(this);
		internal.setVisible(true);
		m_desktop.add(internal);
		m_desktop.moveToFront(internal);
	}

	private void initActions()
	{
		// m_actions.put("checkURL", new ActionCheckURL(this));
		m_actions.put("new", new ActionNew(this));
		m_actions.put("exit", new ActionExit(this));
	}

	public JenuAction getAction(String actionName)
	{
		JenuAction action = m_actions.get(actionName);
		if (action == null)
			throw new Error("Action not defined (" + actionName + ")");
		return m_actions.get(actionName);
	}

	private void setRunningState(int state)
	{
		if (m_state == STATE_STOPPED)
			m_actions.get("checkURL").setEnabled(true);
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
}
