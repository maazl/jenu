package jenu.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/** Variant of DefaultTableCellRenderer that automatically switches to multi-line HTML when the value contains newlines. */
class MultiLineCellRenderer extends DefaultTableCellRenderer
{
	@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{	Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		int height = c.getPreferredSize().height;
		if (table.getRowHeight(row) < height)
			table.setRowHeight(row, height);
		return c;
	}

	@Override protected void setValue(Object value)
	{	if (value instanceof String)
		{	String sVal = (String)value;
			if (sVal.indexOf('\n') >= 0 && !(sVal.startsWith("<html>") && sVal.endsWith("</html>")))
				value = "<html><nobr>" + JenuUIUtils.htmlEncodeLines(sVal) + "</nobr></html>";
		}
		super.setValue(value);
	}
}