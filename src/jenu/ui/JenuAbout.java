package jenu.ui;

import java.awt.Component;

import javax.swing.JOptionPane;

final class JenuAbout
{
	public static void openWindow(Component parent)
	{
		JOptionPane.showMessageDialog(parent, AboutText, "Jenu: About", JOptionPane.PLAIN_MESSAGE);
	}

	private final static String AboutText = "<html>"
		+ "<h1>Jenu Link Checker version 1.0</h1>"
		+ "<h2>Contributors</h2>"
		+ "<ul>"
		+ "<li>Marcel Müller (Jenu 1.0)</li>"
		+ "<li>Caleb Crome (up to Jenu 0.0.2)</li>"
		+ "</ul>"
		+ "<h2>License</h2>"
		+ "<ul>"
		+ "<li>Jenu is published under Artistic License. (No further details are specified by the original author.)</li>"
		+ "<li><a href=\"http://vrici.lojban.org/~cowan/XML/tagsoup/\">TagSoup</a> and <a href=\\\"http://cssparser.sourceforge.net/\\\">CSS Parser</a> are licensed under the <a href=\"http://opensource.org/licenses/apache2.0.php\">Apache License, Version 2.0</a>.</li>"
		+ "<li><a href=\"http://www.w3.org/TR/SAC\">SAC</a> is distributable under W3C® Software Notice and License.</li>"
		+ "</ul>"
		+ "</html>";
}
