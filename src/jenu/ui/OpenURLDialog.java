package jenu.ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.URL;
import java.net.MalformedURLException;

final class OpenURLDialog extends JDialog implements ActionListener
{
	Frame m_owner = null;
	JenuSettings m_settings = null;
	JComboBox<String> m_url = null;
	JCheckBox m_external = null;
	JButton m_cancel = null, m_ok = null;

	public OpenURLDialog(Frame owner, JenuSettings settings)
	{
		super(owner, "Starting Point", true);
		m_owner = owner;
		m_settings = settings;
		initializeUI();
		setState();
	}

	public void setVisible(boolean b)
	{
		setState();
		super.setVisible(b);
	}

	protected void setState()
	{
		m_url.getEditor().setItem(m_settings.getUrlToCheck());
		m_external.setSelected(m_settings.getCheckExternalLinks());
	}

	protected void getState() throws MalformedURLException
	{
		String urlString = (String)m_url.getEditor().getItem();
		URL url = new URL(urlString);
		if (url.getFile().equals(""))
		{
			url = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/");
		}
		m_settings.setUrlToCheck(url);
		m_settings.setCheckExternalLinks(m_external.isSelected());
		m_settings.firePropertyChange();
	}

	protected void initializeUI()
	{
		Container c = getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

		JLabel l = new JLabel("What address do you want to check?");
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		c.add(l);
		m_url = new JComboBox<String>()
		{
			public boolean isEditable()
			{
				return true;
			}
		};
		m_url.setAlignmentX(Component.LEFT_ALIGNMENT);
		c.add(m_url);
		m_external = new JCheckBox("Check external links");
		m_external.setAlignmentX(Component.LEFT_ALIGNMENT);
		c.add(m_external);

		JPanel buttonBar = new JPanel();
		buttonBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		c.add(buttonBar);

		buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.X_AXIS));
		m_ok = new JButton("OK");
		buttonBar.add(m_ok);
		m_cancel = new JButton("Cancel");
		buttonBar.add(m_cancel);

		m_ok.addActionListener(this);
		m_cancel.addActionListener(this);
		m_ok.setDefaultCapable(true);

		pack();
	}

	// The ActionListener Interface.
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == m_ok)
		{
			try
			{	getState();
			} catch (MalformedURLException ex)
			{	JOptionPane.showMessageDialog(this, "URL: is not a valid URL", "Malformed URL", JOptionPane.OK_OPTION);
			}
		} else if (e.getSource() == m_cancel)
		{
			setVisible(false);
		}
	}
}
