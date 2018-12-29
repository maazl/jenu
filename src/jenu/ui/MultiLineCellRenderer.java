package jenu.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

class MultiLineCellRenderer<T> extends JTextArea implements TableCellRenderer
{
	public MultiLineCellRenderer()
	{	//setLineWrap(true);
		//setWrapStyleWord(true);
		setOpaque(true);
	}

	@SuppressWarnings("unchecked")
	@Override public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (isSelected)
		{
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} else
		{
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		setFont(table.getFont());
		if (hasFocus)
		{
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			if (table.isCellEditable(row, column))
			{
				setForeground(UIManager.getColor("Table.focusCellForeground"));
				setBackground(UIManager.getColor("Table.focusCellBackground"));
			}
		} else
		{
			setBorder(new EmptyBorder(1, 1, 1, 1));
		}
		setText(renderValue((T)value));
		int height = getPreferredSize().height;
		if (table.getRowHeight(row) < height)
			table.setRowHeight(row, height);
		return this;
	}

	protected String renderValue(T value)
	{	return value == null ? "" : value.toString();
	}
}