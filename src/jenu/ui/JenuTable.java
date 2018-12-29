package jenu.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Function;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.sun.org.apache.xerces.internal.impl.dv.xs.DecimalDV;

import jenu.model.Message;
import jenu.ui.viewmodel.RowState;

abstract class JenuTable extends JTable
{
	protected JenuTable(TableModel model)
	{
		super(model);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

		setDefaultRenderer(EnumSet.class, new EnumSetRenderer());
		setDefaultRenderer(Message[].class, new MessagesRenderer());

		TableRowSorter<?> sorter = new TableRowSorter<>(model);
		for (int i = 0; i < model.getColumnCount(); ++i)
		{	Comparator<?> comp = getComparator(model.getColumnClass(i));
			if (comp != null)
				sorter.setComparator(i, comp);
		}
		setRowSorter(sorter);

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

	protected static class MessagesRenderer extends MultiLineCellRenderer<Message[]>
	{
		@Override protected String renderValue(Message[] values)
		{	if (values == null || values.length == 0)
				return "";
			if (values.length == 1) // fast path for only one message
				return values[0].message;
			StringBuffer sb = new StringBuffer();
			for (Message m : values)
			{	if (sb.length() != 0)
					sb.append('\n');
				sb.append(m.message);
			}
			return sb.toString();
		}
	}

	protected static class EnumSetRenderer extends MultiLineCellRenderer<EnumSet<?>>
	{
		@Override protected String renderValue(EnumSet<?> set)
		{	if (set.isEmpty())
				return "";
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

	private final class MyMouseListener extends MouseAdapter
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
}
