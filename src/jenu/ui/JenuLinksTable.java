package jenu.ui;

import java.awt.Component;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import jenu.ui.viewmodel.ALinkView;
import jenu.worker.ThreadManager;

final class JenuLinksTable extends JenuTable
{
	public JenuLinksTable(ALinkView model)
	{
		super(model);
		setRowSorter(new TableRowSorter<ALinkView>(getModel()));
	}

	@Override	public ALinkView getModel()
	{	return (ALinkView)super.getModel();
	}

	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		// state dependent foreground
		c.setForeground(JenuUIUtils.getStateColor(getModel().getRow(convertRowIndexToModel(row)).getState()));
		return c;
	}
}