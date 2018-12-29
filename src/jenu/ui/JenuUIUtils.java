package jenu.ui;

import java.awt.Color;

import jenu.ui.viewmodel.RowState;

final class JenuUIUtils
{
	private JenuUIUtils()
	{}

	public static Color getStateColor(RowState runState)
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
	private final static Color MyYELLOW = new Color(0x888800);
	private final static Color MyCYAN = new Color(0x008888);
}
