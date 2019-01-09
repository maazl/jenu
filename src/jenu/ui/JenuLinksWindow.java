package jenu.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jenu.model.Page;
import jenu.ui.viewmodel.LinksView;
import jenu.ui.viewmodel.LinksInView;
import jenu.ui.viewmodel.LinksOutView;
import jenu.worker.ThreadManager;

/** View a list of links of a certain document
 */
public final class JenuLinksWindow extends JenuFrame
{
	private final JenuTable<LinksView.LinkRow> m_table;
	private final JScrollPane m_scroll;
	private final WeakReference<ThreadManager> m_tm;

	/** List of open links windows by title */
	private final static HashMap<String,JenuLinksWindow> m_linkWindows = new HashMap<>();

	/** Open link view.
	 * @param tm Backend model. Might be null, when the model is already immutable, i.e. processing has completed.
	 * @param page Page which links should be shown.
	 * @param type Inbound or outbound links.
	 * @return The window just opened or an existing one with the same scope. */
	public static JenuLinksWindow openWindow(ThreadManager tm, Page page, LinkWindowType type)
	{	String title = (type == LinkWindowType.LinksIn ? "Links to " : "Links from ") + page.sUrl;
		JenuLinksWindow window = m_linkWindows.get(title);
		if (window == null) // cache miss
		{	window = new JenuLinksWindow(tm, title, type == LinkWindowType.LinksOut ? new LinksOutView(page) : new LinksInView(page));
			m_linkWindows.put(title, window);
		}
		window.setVisible(true);
		return window;
	}

	private JenuLinksWindow(ThreadManager tm, String title, LinksView model)
	{
		super(title);
		m_scroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		getContentPane().add(m_scroll, BorderLayout.CENTER);
		m_table = new JenuTable<>(model);
		m_table.addMouseListener(m_table.new MouseClickListener(this::doubleClickLink));
		m_scroll.setViewportView(m_table);

		setPreferredSize(new Dimension(500, 560));
		autoPlacement();

		pack();

		m_tm = new WeakReference<ThreadManager>(tm);
		if (tm != null)
			tm.addPageListener(model);
		model.refresh(); // initial content
	}

	@Override	public void dispose()
	{	JenuLinksWindow ret = m_linkWindows.remove(getTitle());
		assert ret == this;
		ThreadManager tm = m_tm.get();
		if (tm != null)
			tm.removePageListener((LinksView)m_table.getModel());
		super.dispose();
	}

	private void doubleClickLink(LinksView.LinkRow rowData, int col)
	{	switch (LinksView.Column.fromOrdinal(col))
		{case Target:
			JenuUIUtils.openInBrowser(rowData.link.getTargetUrl());
			break;
		 case Source:
			JenuUIUtils.openInBrowser(rowData.link.source.url);
			break;
		 default:
			break;
		}
	}
}
