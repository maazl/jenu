package jenu.ui;

import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

import jenu.worker.JenuThreadEvent;
import jenu.worker.JenuThreadListener;
import jenu.worker.ThreadManager;
import jenu.worker.WorkingSet;

public final class JenuInternalFrame extends JInternalFrame implements JenuThreadListener
{
	static int openFrameCount = 0;
	static final int xOffset = 30, yOffset = 30;
	@SuppressWarnings("unused")
	private Jenu m_owner;
	private InternalToolBar m_toolBar = null;
	private ThreadManager m_tm = null;
	private JenuResultsTable m_table = null;
	private URLDisplay m_scroll = null;
	private InternalStatusBar m_statusBar = null;

	public JenuInternalFrame(Jenu owner)
	{
		super("Document " + openFrameCount++,
			true, // resizeable
			true, // closeable
			true, // maximizable
			true);// iconifyable
		m_owner = owner;
		setLocation(xOffset + openFrameCount * 20, yOffset + openFrameCount * 20);
		setSize(new Dimension(50, 50));
		// this.setJMenuBar(new JenuMenu(m_owner));
		m_toolBar = new InternalToolBar(this);
		getContentPane().add(m_toolBar, BorderLayout.NORTH);
		m_statusBar = new InternalStatusBar();
		getContentPane().add(m_statusBar, BorderLayout.SOUTH);
		m_scroll = new URLDisplay();
		getContentPane().add(m_scroll, BorderLayout.CENTER);
		setPreferredSize(new Dimension(400, 200));
		grabFocus();
		pack();
	}

	public void startRunning()
	{
		if (m_tm != null && m_tm.isAlive())
			m_tm.pauseRunning(false);
		else
		{	m_tm = new ThreadManager();
			m_table = new JenuResultsTable(m_tm);
			m_scroll.setViewportView(m_table);

			m_tm.addThreadListener(m_statusBar);
			m_tm.addThreadListener(this);

			WorkingSet ws = new WorkingSet();
			String url = m_toolBar.getSite();
			if (url.length() != 0)
				ws.Sites.add(url);
			url = m_toolBar.getURL();
			if (url.length() != 0)
				ws.StartingPoints.add(url);

			m_toolBar.setRunning();
			m_tm.start(ws);
		}
	}

	public void threadStateChanged(JenuThreadEvent e)
	{
		if (e.m_threadsRunning + e.m_urlsToStart == 0)
		{	m_toolBar.setStopped();
			m_tm = null;
		}
	}

	public void stopRunning()
	{
		if (m_tm != null)
		{	m_toolBar.setStopping();
			m_tm.stopRunning();
		}
	}

	public void pauseRunning()
	{
		if (m_tm != null)
		{	m_toolBar.setPaused();
			m_tm.pauseRunning(true);
		}
	}

	private class URLDisplay extends JScrollPane
	{
		public URLDisplay()
		{
			super(VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
		}
	}
}
