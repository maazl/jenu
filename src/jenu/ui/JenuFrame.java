package jenu.ui;

import javax.swing.JFrame;

/** Base class for Jenu frame windows. Adds some sugar. */
abstract class JenuFrame extends JFrame
{
	public JenuFrame(String title)
	{	super("Jenu: " + title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	@Override public void setTitle(String title)
	{	super.setTitle("Jenu: " + title);
	}

	private static int openFrameCount = 0;
	static final int xOffset = 30, yOffset = 30;
	static final int xDisplacement = 20, yDisplacement = 20;
	static final int maxDisplacements = 10;

	/** Do placement of frame window automatically avoiding full overlaps when possible. */
	protected final void autoPlacement()
	{	int count;
		synchronized (JenuFrame.class)
		{	count = openFrameCount++;
		}
		int overflow = (count / maxDisplacements) % 3 + 1;
		overflow = (((overflow & 1) << 1) - 1) * (overflow >> 1);
		count %= maxDisplacements;
		setLocation(xOffset + (count + overflow) * xDisplacement, yOffset + (count - overflow) * yDisplacement);
	}

}
