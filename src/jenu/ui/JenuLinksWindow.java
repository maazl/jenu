package jenu.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.Collection;
import java.util.HashMap;
import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import jenu.worker.Link;
import jenu.worker.PageStats;

/** View a list of links of a certain document
 */
public final class JenuLinksWindow extends JenuFrame
{
	private final JenuLinksTable m_table;
	private final JScrollPane m_scroll;

	/** List of open windows by title */
	private final static HashMap<String,JenuLinksWindow> m_linkWindows = new HashMap<>();

	/** Create new site checker window.
	 * @param title Window title. This is prefixed by "Jenu: "
	 * @param links Data to view.
	 * @return The window just opened or an existing one with the same scope.
	 * @remarks When an existing window is shown a refresh event is sent to the window. */
	public static JenuLinksWindow openNewWindow(PageStats stats, LinkWindowType type)
	{	String title;
		Collection<Link> links;
		if (type == LinkWindowType.LinksIn)
		{	title = "Links to ";
			links = stats.getLinksIn();
		} else
		{	title = "Links from ";
			links = stats.getLinksOut();
		}
		title += stats.sUrl;
		JenuLinksWindow window = m_linkWindows.get(title);
		if (window == null) // cache miss
		{	if (links.isEmpty())
				return null; // nothing to show
			window = new JenuLinksWindow(title, links);
			m_linkWindows.put(title, window);
		} else
		{	if (links.isEmpty()) // Link list now empty?
			{	window.dispose(); // should not happen, however discard window.
				return null;
			}
			window.m_table.updateData(links);
		}
		window.setVisible(true);
		return window;
	}

	private JenuLinksWindow(String title, Collection<Link> links)
	{
		super(title);

		m_scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		getContentPane().add(m_scroll, BorderLayout.CENTER);
		m_table = new JenuLinksTable(links);
		m_scroll.setViewportView(m_table);

		setPreferredSize(new Dimension(500, 560));
		autoPlacement();

		pack();
	}

	@Override	public void dispose()
	{	JenuLinksWindow ret = m_linkWindows.remove(getTitle());
		assert ret == this;
		super.dispose();
	}
}
