import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/** This class implements several of the actions for each thread. */
public class ActionStop
    extends AbstractAction
{
    protected JenuInternalFrame m_parent = null;
    public ActionStop(JenuInternalFrame parent) {
	super("Stop");
	m_parent = parent;
    }

    public void actionPerformed(ActionEvent e) {
	m_parent.stopRunning();
    }
}
