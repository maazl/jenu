import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.Frame;

public class ActionCheckURL
    extends JenuAction
{
    OpenURLDialog m_dialog = null;
    public ActionCheckURL(Jenu owner) {
	super("Check URL...");
	m_owner = owner;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
	if (m_dialog == null) {
	    //m_dialog = new OpenURLDialog(m_owner, m_owner.getSettings());
	}
 	m_dialog.setVisible(true);
    }

    public void setEnabledFromState(int state) {
	setEnabled(enabledWhenStopped(state));
    }
}
