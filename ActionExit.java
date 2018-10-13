import javax.swing.Action;
import javax.swing.AbstractAction;

public class ActionExit
    extends JenuAction
{
    public ActionExit(Jenu owner) {
	super("Exit");
	m_owner = owner;
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
	System.exit(0);
    }

    public void setEnabledFromState(int state) {
	setEnabled(enabledWhenStopped(state));
    }
    
}
