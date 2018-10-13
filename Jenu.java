import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.util.Hashtable;

public class Jenu 
    extends JFrame
	    //    implements PropertyChangeListener
{
    public static final int STATE_RUNNING = 0;
    public static final int STATE_PAUSED  = 1;
    public static final int STATE_STOPPED = 2;

    protected int m_state = STATE_STOPPED;

    protected Hashtable m_actions = new Hashtable();
    protected JDesktopPane m_desktop = null;
    
    //    protected JenuSettings m_settings = new JenuSettings();

    public static void main(String args[]) {
	Jenu jenu = new Jenu("Jenu");
	jenu.initialize();
	jenu.setVisible(true);
    }
    public Jenu (String title) {
	super(title);
    }
    
    protected void initialize() {
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }		
	});
	initActions();
	setJMenuBar(new JenuMenu(this));
	m_desktop = new JDesktopPane();

	setContentPane(m_desktop);
	m_desktop.putClientProperty("JDesktopPane.dragMode", "outline");
	
	createNewWindow();
	// m_settings.addPropertyChangeListener(this);
	pack();
	setSize(new Dimension(500, 400));
	
    }
    public void createNewWindow() {
	JInternalFrame internal = new JenuInternalFrame(this);
	internal.setVisible(true);
	m_desktop.add(internal);
	m_desktop.moveToFront(internal);
    }


    protected void initActions() {
	//m_actions.put("checkURL", new ActionCheckURL(this));
	m_actions.put("new",     new ActionNew(this));
	m_actions.put("exit",     new ActionExit(this));
    }

    public JenuAction getAction(String actionName) {
	JenuAction action = (JenuAction) m_actions.get(actionName);
	if (action == null) {
	    throw new Error("Action not defined (" + actionName +")");
	} else {
	    return (JenuAction) m_actions.get(actionName);
	}
    }

    protected void setRunningState(int state) {
	if (state == STATE_STOPPED) {
	    ((AbstractAction)m_actions.get("checkURL")).setEnabled(true);
	}
    }
    
    //    public JenuSettings getSettings() {
    //	return m_settings;
    //    }

    // Listen for changes in settings.
//    public void propertyChange(PropertyChangeEvent evt) {
//	  System.out.println(evt);
//	  if (evt.getSource() == m_settings) {
//	      ThreadManager tm = new ThreadManager(m_settings.getUrlToCheck(), );
//	      tm.start();
//	  }
//    }
}
