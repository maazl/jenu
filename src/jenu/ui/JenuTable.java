package jenu.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jenu.model.Message;
import jenu.ui.viewmodel.RowState;
import jenu.ui.viewmodel.StateObject;
import jenu.ui.viewmodel.StateObjectView;

class JenuTable<R extends StateObject> extends JTable
{
	protected JenuTable(TableModel model)
	{
		super(model);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

		setDefaultRenderer(EnumSet.class, new EnumSetRenderer());
		setDefaultRenderer(Message[].class, new MessagesRenderer());

		getTableHeader().setReorderingAllowed(true);
		getTableHeader().addMouseListener(new HeaderMouseListener());
		setShowGrid(false);
	}

	@SuppressWarnings("unchecked")
	@Override	public StateObjectView<R> getModel()
	{	return (StateObjectView<R>)super.getModel();
	}

	protected final static Color zebraBackground = new Color(0xeeeeee);

	private int lastRow = -1;
	private Color rowColor;

	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);

		if (!isRowSelected(row))
		{	// Alternate background color
			c.setBackground((row & 1) != 0 ? getBackground() : zebraBackground);
		}

		if (row != lastRow)
		{	rowColor = JenuUIUtils.getStateColor(getModel().getRow(convertRowIndexToModel(row)).getState());
			lastRow = row;
		}
		c.setForeground(rowColor);

		return c;
	}

	@Override protected void paintComponent(Graphics g)
	{	lastRow = -1; // invalidate Cache
		super.paintComponent(g);
	}

	@Override public void setModel(TableModel dataModel)
	{
		super.setModel(dataModel);

		TableRowSorter<?> sorter = new TableRowSorter<>(dataModel);
		for (int i = 0; i < dataModel.getColumnCount(); ++i)
		{	Comparator<?> comp = getComparator(dataModel.getColumnClass(i));
			if (comp != null)
				sorter.setComparator(i, comp);
		}
		if (sortOrder != null && sortOrder.get(0).getColumn() >= 0) // can be called before constructor resulting in final field null!
			sorter.setSortKeys(sortOrder);
		setRowSorter(sorter);
	}

	@SuppressWarnings("unchecked")
	protected <T> Comparator<T> getComparator(Class<? extends T> cls)
	{
		if (cls == Message[].class)
			return (Comparator<T>)MessagesComparator.instance;
		return null;
	}

	protected static class MessagesComparator implements Comparator<Message[]>
	{
		protected final static Comparator<String> strComp = Comparator.comparing(Function.identity());

		public final static MessagesComparator instance = new MessagesComparator();

		@Override public int compare(Message[] o1, Message[] o2)
		{
			if (o1 == null || o1.length == 0)
				return o2 == null || o2.length == 0 ? 0 : -1;
			if (o2 == null || o2.length == 0)
				return 1;
			int i = 0;
			do
			{	int cmp = o1[i].compareTo(o2[i]);
				if (cmp != 0)
					return cmp;
				++i;
			} while (i < o1.length && i < o2.length);
			return Integer.compare(o1.length, o2.length);
		}
	}

	protected static class MessagesRenderer extends MultiLineCellRenderer
	{
		@Override protected void setValue(Object value)
		{	Message[] values = (Message[])value;
			if (values != null)
			{	switch (values.length)
				{case 0:
					value = null;
					break;
				 case 1: // fast path for only one message
					value = values[0].message;
					break;
				 default:
					StringBuffer sb = new StringBuffer();
					for (Message m : values)
					{	if (sb.length() != 0)
							sb.append('\n');
						sb.append(m.message);
					}
					value = sb.toString();
				}
			}
			super.setValue(value);
		}
	}

	protected static class EnumSetRenderer extends MultiLineCellRenderer
	{
		@Override protected void setValue(Object value)
		{	EnumSet<?> set = (EnumSet<?>)value;
			if (set != null)
			{	if (set.isEmpty())
					value = null;
				else
				{	StringBuffer sb = new StringBuffer();
					for (Object ev : set)
					{	if (sb.length() != 0)
							sb.append('\n');
						sb.append(ev);
					}
					for (int i = 0; i < sb.length(); i++)
						if (sb.charAt(i) == '_')
							sb.setCharAt(i, ' ');
					value = sb.toString();
				}
			}
			super.setValue(value);
		}
	}

	protected final ArrayList<SortKey> sortOrder = new ArrayList<SortKey>(1) {{ add(new SortKey(-1, SortOrder.UNSORTED)); }};

	protected SortOrder computeSortOrder(int column, SortOrder last)
	{
		switch (last)
		{case ASCENDING:
			return SortOrder.DESCENDING;
		 case DESCENDING:
			return SortOrder.ASCENDING;
		 default:
			// Check for data types that enforce descending order by default
			Class<?> type = getModel().getColumnClass(column);
			if (Number.class.isAssignableFrom(type) || Message[].class == type || RowState.class == type)
				return SortOrder.DESCENDING;
			return SortOrder.ASCENDING;
		}
	}

	private final class HeaderMouseListener extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			int column = getTableHeader().columnAtPoint(e.getPoint());
			if (column >= 0)
			{	column = convertColumnIndexToModel(column);
				RowSorter<? extends TableModel> sorter = getRowSorter();
				if (sorter != null)
				{	sortOrder.set(0, new SortKey(column, computeSortOrder(column, column == sortOrder.get(0).getColumn() ? sortOrder.get(0).getSortOrder() : SortOrder.UNSORTED)));
					sorter.setSortKeys(sortOrder);
				}
			}
		}
	}

	public class MouseClickListener extends MouseAdapter
	{
		private final ObjIntConsumer<R> eh;

		public MouseClickListener(ObjIntConsumer<R> eh)
		{	this.eh = eh;
		}

		public void mouseClicked(MouseEvent e)
		{	if (e.getClickCount() >= 2)
			{	int column = columnAtPoint(e.getPoint());
				if (column >= 0)
				{	column = convertColumnIndexToModel(column);
					int row = rowAtPoint(e.getPoint());
					if (row >= 0)
					{	row = convertRowIndexToModel(row);
						eh.accept(getModel().getRow(row), column);
		}	}	}	}
	}
}
