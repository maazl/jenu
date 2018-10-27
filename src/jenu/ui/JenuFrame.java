package jenu.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;

import javax.swing.JFrame;

/** Base class for *any* Jenu frame windows.
 * This class keeps track of open windows and terminates the application when the last windows has closed.
 * @author mueller
 */
abstract class JenuFrame extends JFrame
{
	public JenuFrame(String title)
	{
		super("Jenu: " + title);
		addWindowListener(new WindowAdapter()
		{	public void windowOpened(WindowEvent e)
			{	addWindow(JenuFrame.this);
			}
			public void windowClosing(WindowEvent e)
			{	removeWindow(JenuFrame.this);
			}
		} );
	}

	private static int openFrameCount = 0;
	static final int xOffset = 30, yOffset = 30;
	static final int xDisplacement = 20, yDisplacement = 20;
	static final int maxDisplacements = 10;

	protected void autoPlacement()
	{	int count;
		synchronized (JenuFrame.class)
		{	count = openFrameCount++;
		}
		int overflow = (count / maxDisplacements) % 3 + 1;
		overflow = (((overflow & 1) << 1) - 1) * (overflow >> 1);
		count %= maxDisplacements;
		setLocation(xOffset + (count + overflow) * xDisplacement, yOffset + (count - overflow) * yDisplacement);
	}

	private static HashSet<JFrame> openWindows = new HashSet<>();

	private static synchronized void addWindow(JFrame window)
	{	openWindows.add(window);
	}

	private static synchronized void removeWindow(JFrame window)
	{	boolean b = openWindows.remove(window);
		assert b;
		// Terminate when last window is closed
		if (openWindows.size() == 0)
			System.exit(0);
	}
}
