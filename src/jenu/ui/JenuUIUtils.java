package jenu.ui;

import java.awt.Color;

import jenu.worker.PageState;

final class JenuUIUtils
{
	private JenuUIUtils()
	{}

	public static Color getStateColor(PageState runState)
	{
		switch (runState)
		{default: // PENDING
			return Color.blue;
		 case RUNNING:
			return MyCYAN;
		 case RETRY:
			return Color.magenta;
		 case EXCLUDED:
			return Color.gray;
		 case DONE:
			return MyGREEN;
		 case WARNING:
			return Color.orange;
		 case FAILED:
			return Color.red;
		}
	}
	private final static Color MyGREEN = new Color(0x00aa00);
	private final static Color MyCYAN = new Color(0x008888);
}
