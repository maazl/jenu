package jenu.ui.component;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.util.stream.IntStream;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class StackedBar extends JComponent implements StackedBarModelListener
{
	protected StackedBarModel model = null;
	protected StackedBarColorModel colorModel = null;
	protected float[] values = new float[0];
	protected String[] texts = new String[0];
	protected int axis;

	public StackedBar(StackedBarModel model)
	{	UIDefaults ui = UIManager.getLookAndFeel().getDefaults();
		setPreferredSize(ui.getDimension("ProgressBar.horizontalSize"));
		setBorder(ui.getBorder("ProgressBar.border"));
		setModel(model);
		addComponentListener(new ComponentAdapter()
			{	public void componentResized(ComponentEvent e)
				{	calcSize();
				}
			});
	}

	public int getAxis()
	{	return axis;
	}

	public void setAxis(int axis)
	{	this.axis = axis;
		calcSize();
	}

	public void setModel(StackedBarModel model)
	{
		if (this.model != null)
			this.model.removeStackedBarModelListener(this);
		this.model = model;
		model.addStackedBarModelListener(this);
		dataChanged(new StackedBarModelEvent(model));
	}

	public void setColorModel(StackedBarColorModel model)
	{
		this.colorModel = model;
		for (int i = 0; i < getComponentCount(); ++i)
		{	JLabel l = (JLabel)getComponent(i);
			l.setBackground(colorModel.getBackgroundColorAt(i));
			l.setForeground(colorModel.getForegroundColorAt(i));
		}
	}

	public void dataChanged(StackedBarModelEvent e)
	{
		if (e.getSource() != model)
			return;
		if (e.itemIndex == -1)
		{	// full update
			int diff = getComponentCount() - model.getCount();
			while (diff < 0) // create new children
			{	if (colorModel == null)
					colorModel = DefaultColorModel.instance;
				add(createLabel());
				++diff;
			}
			while (diff > 0) // remove children
				remove(--diff);
		}
		calcSize();
		setTexts(e.itemIndex);
	}

	protected JLabel createLabel()
	{
		JLabel l = new JLabel();
		l.setOpaque(true);
		l.setBackground(colorModel.getBackgroundColorAt(getComponentCount()));
		l.setForeground(colorModel.getForegroundColorAt(getComponentCount()));
		l.setHorizontalAlignment(SwingConstants.CENTER);
		return l;
	}

	protected void calcSize()
	{
		int width = getWidth() - 2;
		int height = getHeight() - 2;
		if (width <= 0 || height <= 0)
			return;
		if (axis == BoxLayout.X_AXIS || axis == BoxLayout.LINE_AXIS)
		{	int[] sizes = calcDimensions(width);
			int sum = 1;
			for (int i = 0; i < getComponentCount(); ++i)
			{	JLabel label = (JLabel)getComponent(i);
				label.setVisible(sizes[i] != 0);
				label.setBounds(sum, 1, sizes[i], height);
				sum += sizes[i];
			}
		} else
		{	int[] sizes = calcDimensions(height);
			int sum = 1;
			for (int i = 0; i < getComponentCount(); ++i)
			{	JLabel label = (JLabel)getComponent(i);
				label.setVisible(sizes[i] != 0);
				label.setBounds(1, sum, width, sizes[i]);
				sum += sizes[i];
			}
		}
	}

	private int[] calcDimensions(int total)
	{
		float[] values = new float[getComponentCount()];
		float sum = 0;
		for (int i = 0; i < values.length; ++i)
			sum += values[i] = model.getValueAt(i);
		int[] ret = new int[values.length];
		if (sum == 0) // no data
			return ret;
		int isum = 0;
		for (int i = 0; i < values.length; ++i)
		{	values[i] *= total / sum;
			values[i] -= ret[i] = (int)Math.rint(values[i]);
			isum += ret[i];
		}
		isum -= total;
		if (isum != 0)
		{	int[] ix = IntStream.range(0, values.length)
				.boxed().sorted((i, j) -> Float.compare(values[i], values[j]))
				.mapToInt(ele -> ele).toArray();
			while (isum > 0)
				--ret[ix[--isum]];
			while (isum < 0)
				++ret[ix[ix.length + isum++]];
		}
		return ret;
	}

	private static final DecimalFormat defaultFormat = new DecimalFormat("0.#");

	protected void setTexts(int index)
	{
		if (index < 0)
			for (int i = 0; i < getComponentCount(); ++i)
				setTexts(i);
		else
		{	JLabel label = ((JLabel)getComponent(index));
			String text = model.getTextAt(index);
			if (text == null)
				text = defaultFormat.format(model.getValueAt(index));
			label.setText(text);
			label.setToolTipText(model.getToolTipTextAt(index));
		}
	}

	public static class DefaultColorModel implements StackedBarColorModel
	{
		public static DefaultColorModel instance = new DefaultColorModel();

		public static final Color[] colors = new Color[] { Color.RED, Color.BLUE, Color.GREEN, Color.GRAY, Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.WHITE };

		private DefaultColorModel()
		{}

		public Color getBackgroundColorAt(int index)
		{	return colors[index % colors.length];
		}

		public Color getForegroundColorAt(int index)
		{	return Color.BLACK;
		}
	}
}
