package jenu.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import jenu.worker.WorkingSet;

class SiteEditor extends JDialog implements ActionListener
{
	private final static HashMap<WorkingSet,SiteEditor> instances = new HashMap<>();

	public static SiteEditor openWindow(Frame owner, WorkingSet data)
	{
		SiteEditor editor = instances.get(data);
		if (editor == null)
			instances.put(data, editor = new SiteEditor(owner, data));
		editor.setVisible(true);
		return editor;
	}

	private final WorkingSet data;

	private final JPanel contentPanel = new JPanel();
	private final JTextArea tSites;
	private final JTextArea tStarts;
	private final JTextArea tExcludes;
	private final JCheckBox cbCheckExternal;
	private final JCheckBox cbExternalRedirect;
	private final JSpinner sMaxDepth;
	private final JSpinner sThreads;
	private final JSpinner sTimeout;

	private int result = JOptionPane.CLOSED_OPTION;
	public int getResult()
	{	return result;
	}

	private SiteEditor(Frame owner, WorkingSet data)
	{
		super(owner, "Site properties");
		this.data = data;
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		setBounds(100, 100, 640, 400);
		setMinimumSize(new Dimension(450,340));
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {120, 417};
		gbl_contentPanel.rowHeights = new int[] {75, 75, 75, 23, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[] {0.0, 1.0};
		gbl_contentPanel.rowWeights = new double[] {1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);

		tSites = addLine("<html>Site root URLs<br/>(one URL per line)</html>", new JTextArea(), 0, GridBagConstraints.BOTH);
		tStarts = addLine("<html>Starting points<br/>(one URL per line)</html>", new JTextArea(), 1, GridBagConstraints.BOTH);
		tExcludes = addLine("<html>Excluding...<br/>(one Regex per line)</html>", new JTextArea(), 2, GridBagConstraints.BOTH);

		cbCheckExternal = addLine("Options", new JCheckBox("Check external URLs"), 3, GridBagConstraints.NONE, 0);
		cbExternalRedirect = addLine(null, new JCheckBox("Folow external redirects"), 4);

		sMaxDepth = addLine("Max. recursion depth", new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1)), 5);
		sThreads = addLine("Max. number of threads", new JSpinner(new SpinnerNumberModel(10, 1, 100, 1)), 6);
		sTimeout = addLine("Connection timeout (s)", new JSpinner(new SpinnerNumberModel(1, 0.0, 100.0, 1)), 7, GridBagConstraints.NONE, 0);

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			JButton okButton = new JButton("OK");
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("Cancel");
			buttonPane.add(cancelButton);
			okButton.addActionListener(this);
			cancelButton.addActionListener(this);
			okButton.setDefaultCapable(true);
		}
	}

	private void setState()
	{
		tSites.setText(String.join("\n", data.sites));
		tStarts.setText(String.join("\n", data.startingPoints));
		StringJoiner joiner = new StringJoiner("\n");
		for (Pattern p : data.excludePatterns)
			joiner.add(p.toString());
		tExcludes.setText(joiner.toString());
		cbCheckExternal.setSelected(data.checkExternalURLs);
		cbExternalRedirect.setSelected(data.followExternalRedirects);
		sMaxDepth.setValue(data.maxDepth);
		sThreads.setValue(data.maxWorkerThreads);
		sTimeout.setValue(data.timeout / 1000.);
	}

	private void getState() throws PatternSyntaxException
	{
		data.sites.clear();
		for (String s : tSites.getText().split("\\r?\\n", -1))
			data.sites.add(s);
		data.startingPoints.clear();
		for (String s : tStarts.getText().split("\\r?\\n", -1))
			data.startingPoints.add(s);
		data.excludePatterns.clear();
		for (String s : tExcludes.getText().split("\\r?\\n", -1))
			data.excludePatterns.add(Pattern.compile(s));
		data.checkExternalURLs = cbCheckExternal.isSelected();
		data.followExternalRedirects = cbExternalRedirect.isSelected();
		data.maxDepth = (int)sMaxDepth.getValue();
		data.maxWorkerThreads = (int)sThreads.getValue();
		data.timeout = (int)((double)sTimeout.getValue() * 1000.);
	}

	@Override public void setVisible(boolean b)
	{	if (b)
			setState();
		super.setVisible(b);
	}

	@Override public void dispose()
	{	instances.remove(data);
		super.dispose();
	}

	@Override public void actionPerformed(ActionEvent e)
	{	switch(e.getActionCommand())
		{default:
			return;
		 case "OK":
			try
			{	getState();
			} catch (PatternSyntaxException ex)
			{	JOptionPane.showMessageDialog(this, "Exclude pattern is no valid Regex", ex.toString(), JOptionPane.OK_OPTION);
				return;
			}
			result = JOptionPane.OK_OPTION;
			break;
		 case "Cancel":
			result = JOptionPane.CANCEL_OPTION;
		}
		dispose();
	}

	private <C extends JComponent> C addLine(String label, C editor, int row, int fill, int inset)
	{	GridBagConstraints gbc;
		if (label != null)
		{	JLabel l = new JLabel(label);
			gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 0, inset, 5);
			gbc.gridx = 0;
			gbc.gridy = row;
			contentPanel.add(l, gbc);
		}

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = fill;
		if (inset != 0)
			gbc.insets = new Insets(0, 0, inset, 0);
		gbc.gridx = 1;
		gbc.gridy = row;
		contentPanel.add(editor, gbc);

		return editor;
	}
	private <C extends JComponent> C addLine(String label, C editor, int row, int fill)
	{	return addLine(label, editor, row, fill, 5);
	}
	private <C extends JComponent> C addLine(String label, C editor, int row)
	{	return addLine(label, editor, row, GridBagConstraints.NONE, 5);
	}
}
