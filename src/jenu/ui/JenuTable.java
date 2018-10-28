package jenu.ui;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import jenu.worker.EventType;
import jenu.worker.PageState;

abstract class JenuTable extends JTable
{
	protected JenuTable(TableModel model)
	{
		super(model);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
		getTableHeader().setReorderingAllowed(true);
		getTableHeader().addMouseListener(new MyMouseListener());
		setShowGrid(false);
	}

	protected final static Color zebraBackground = new Color(0xeeeeee);

	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);

		if (!isRowSelected(row))
		{	// Alternate background color
			c.setBackground((row & 1) != 0 ? getBackground() : zebraBackground);
		}

		return c;
	}

	protected static Color getRunStateColor(PageState runState)
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

	protected static String render(EnumSet<?> set)
	{	if (set.isEmpty())
			return null;
		StringBuffer sb = new StringBuffer();
		for (Object ev : set)
		{	if (sb.length() != 0)
				sb.append('\n');
			sb.append(ev);
		}
		for (int i = 0; i < sb.length(); i++)
			if (sb.charAt(i) == '_')
				sb.setCharAt(i, ' ');
		return sb.toString();
	}

	protected static class MultiLineCellRenderer extends JTextArea implements TableCellRenderer
	{
		public MultiLineCellRenderer()
		{	//setLineWrap(true);
			//setWrapStyleWord(true);
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
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
			setText((value == null) ? "" : value.toString());
			int height = getPreferredSize().height;
			if (table.getRowHeight(row) < height)
				table.setRowHeight(row, height);
			return this;
		}
	}

	protected final ArrayList<SortKey> sortOrder = new ArrayList<SortKey>(1) {{ add(new SortKey(-1, SortOrder.ASCENDING)); }};

	private final class MyMouseListener extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{ int column = getTableHeader().columnAtPoint(e.getPoint());
			if (column >= 0)
			{	RowSorter<? extends TableModel> sorter = getRowSorter();
				if (sorter != null)
				{	sortOrder.set(0, new SortKey(column, column == sortOrder.get(0).getColumn() && sortOrder.get(0).getSortOrder() == SortOrder.ASCENDING ? SortOrder.DESCENDING : SortOrder.ASCENDING));
					sorter.setSortKeys(sortOrder);
				}
			}
		}
	}
}
