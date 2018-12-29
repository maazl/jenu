package jenu.ui;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.table.TableCellRenderer;

import jenu.ui.viewmodel.TargetView;
import jenu.worker.ThreadManager;

final class JenuResultsTable extends JenuTable
{
	public JenuResultsTable(ThreadManager tm)
	{
		super(new TargetView());
		setMinimumSize(new Dimension(800,0));
		tm.addPageListener(getModel());
	}

	@Override	public TargetView getModel()
	{	return (TargetView)super.getModel();
	}

	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		// state dependent foreground
		c.setForeground(JenuUIUtils.getStateColor(getModel().getRow(convertRowIndexToModel(row)).getState()));
		return c;
	}
}