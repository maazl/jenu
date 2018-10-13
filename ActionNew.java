import javax.swing.Action;
import javax.swing.AbstractAction;

public class ActionNew
    extends JenuAction
{
    public Jenu owner = null;
    public ActionNew(Jenu owner) {
	super("New Window");
	m_owner = owner;
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
	m_owner.createNewWindow();
    }
    public void setEnabledFromState(int state) {
	setEnabled(true);
    }
}
