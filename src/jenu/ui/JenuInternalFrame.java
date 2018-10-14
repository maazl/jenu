package jenu.ui;

import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

import jenu.worker.ThreadManager;

public final class JenuInternalFrame extends JInternalFrame
{
	private static final long serialVersionUID = 1L;
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
		boolean okToStart = false;
		m_toolBar.setRunning();
		if (m_tm == null)
		{
			okToStart = true;
		} else
		{
			if (!m_tm.isAlive())
			{
				okToStart = true;
			}
		}
		if (okToStart)
		{
			m_tm = new ThreadManager(m_toolBar.getURL());
			m_tm.addThreadListener(m_statusBar);
			m_tm.addThreadListener(e -> { if (e.m_threadsRunning + e.m_urlsToStart == 0) m_toolBar.setStopped(); });
			m_table = new JenuResultsTable(m_tm);
			m_scroll.setViewportView(m_table);
			m_tm.start();
		}
	}

	public void stopRunning()
	{
		m_tm.stopRunning();
		m_toolBar.setStopping();
	}

	public void pauseRunning(boolean pause)
	{
		m_tm.pauseRunning(pause);
		m_toolBar.setPaused();
	}

	private class URLDisplay extends JScrollPane
	{
		private static final long serialVersionUID = 1L;

		public URLDisplay()
		{
			super(VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
		}
	}

}
