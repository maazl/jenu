import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.text.DateFormat;
import java.net.MalformedURLException;
import java.util.Comparator;
import java.lang.Comparable;
import java.awt.Color;

public class PageStats {
    
    public static final int OK             = 0;
    public static final int IOError        = 1 << 1;
    public static final int HTMLParseError = 1 << 2;
    public static final int InvalidLinks   = 1 << 3;
    public static final int HTTPError      = 1 << 4;
    public static final int URLError       = 1 << 5;

    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int DONE    = 2;
    public static final int RETRY   = 3;

    public static final int ASCENDING = 1;
    public static final int DECENDING = 2;

    private int     runState   = PENDING;
    public  int     status     = OK;
    private String  errorString= "";
    public String  contentType= null;
    public int     size       = -1;
    public String  title      = null;
    public Date    date       = null;
    public int     level      = -1;
    public Vector  linksIn    = new Vector();
    public Vector  linksOut   = new Vector();
    public String  server     = null;
    public URL     url        = null;
    public String  sUrl       = null;

    protected DateFormat df =  DateFormat.getDateInstance();

    private PageStats() {}
    public PageStats (String strURL) {
	if (strURL == null) {
	    throw new NullPointerException();
	}
	sUrl = strURL;
	try {
	    url = new URL(strURL);
	} catch (MalformedURLException e) {
	    setStatus(URLError);
	}
    }
    
    public String toString() {
	String result =
	    "URL         = " + url +    "\n" +
	    "status      = " + status + "\n" +
	    "errorString = " + errorString + "\n" +
	    "contentType = " + contentType + "\n" +
	    "size        = " + size + "\n" +
	    "title       = " + title + "\n" +
	    "date        = " + date + "\n" +
	    "level       = " + level    + "\n" +
	    "linksIn     = " + linksIn  + "\n" + 
	    "linksOut    = " + linksOut + "\n" ;
	return result;
    }

    public void addLinkIn(String u) {
	linksIn.add(u);
    }

    public boolean equals(Object o) {
	boolean result;
	String s = "Comparing " + this.sUrl + " to ";
	if (o instanceof String) {
	    s += o;
	    result = sUrl.equals(o);
	} else if (o instanceof PageStats) {
	    s += ((PageStats)o).sUrl;
	    result =  sUrl.equals(((PageStats)o).sUrl);
	} else {
	    result = super.equals(o);
	}
	if (result) {
	    // System.out.println("" + result + " " + s);
	}
	return result;
    }
    public void setStatus(int status) {
	status |= status;
    }
    public int   getRunState()      { return runState; };
    public Color getRunStateColor() {
	Color result = null;
	switch (runState) {
	case PENDING: result = Color.gray; break;
	case RUNNING: result = Color.yellow; break;
	case RETRY:   result = Color.magenta; break;
	case DONE:
	    switch (status) {
	    case OK:  result = Color.green; break;
	    default:  result = Color.red;   break;
	    }
	    break;
	default:
	    throw new Error("Invalid runState");
	}
	return result;
    }
    public void setPending()       { runState = PENDING; }
    public void setRunning()       { runState = RUNNING; }
    public void setDone()          { runState = DONE; }
    public void setRetry()         { runState = RETRY; }
    
    public String getErrorString() { return errorString; }
    public void   setErrorString(String s) { errorString += s; }
    public void   clearErrorString() { errorString = ""; }

    public void addLinkOut(String u) {
	//	System.out.println(u);
	try {
	    URL u_url = new URL(url, u);
	    linksOut.add(u_url.toExternalForm());
	} catch (MalformedURLException e) {
	    setErrorString("Malformed URL " + u + ", with parent" + url);
	    setStatus(URLError);
	}
    }

    public void setTime(String time) {
	try {
	    date = df.parse(time);
	} catch (java.text.ParseException e) {
	}
    }

    // helpers for the TableModel
    protected static String columnNames[] = {
	// 0          1        2        3       4       5        6          7           8          9       10       11
	"Address", "RunState", "Type", "Size", "Title", "Date", "Level", "Links Out", "Links In", "Server","Error", "Status"
    };
    protected static final Class s_intClass    = new Integer(1).getClass();
    protected static final Class s_stringClass = new String("").getClass();
    protected static final Class s_dateClass   = new Date().getClass();
	
    public Object getColumn(int columnIndex) {
	Object result = null;
	switch (columnIndex) {
	case 0:  result = url;                 break;
	case 1:  result = new Integer(runState);  break;
	case 2:  result = contentType;         break;
	case 3:  result = new Integer(size);   break;
	case 4:  result = title;               break;
	case 5:  result = date;                break;
	case 6:  result = new Integer(level);  break;
	case 7:  result = new Integer(linksOut.size());     break;
	case 8:  result = new Integer(linksIn.size());      break;
	case 9:  result = url.getHost();              break;
	case 10: result = errorString;         break;
	case 11: result = new Integer(status); break;
	default: throw new Error("Invalid column index passed to getColumn");
	}
	return result;
    }

    public Comparator getComparator(int columnIndex, int direction) {
	return new PageStats.StatsComparator(columnIndex, direction);
    }

    public static String getColumnName(int columnIndex) {
	return columnNames[columnIndex];
    }
    public static int getColumnCount() {
	return columnNames.length;
    }
    public static Class getColumnClass(int columnIndex) {
	Class result = null;
	switch (columnIndex) {
	case 0:   
	    result = URL.class;
	    break;
	case 1: 
	case 3: 
	case 6:
	case 7:
	case 8:
	case 11:
	    result = s_intClass;
	    break;
	case 2:
	case 4:
	case 9:
	case 10:
	    result = s_stringClass;
	    break;
	case 5:
	    result = s_dateClass;
	    break;
	default:
	    throw new Error("Called with invalid Column Number: " + columnIndex);
	}

	return result;
    }

    public class StatsComparator 
	implements Comparator
    {
	int m_column;
	int m_order;

	public StatsComparator(int column, int order) {
	    m_order  = order;
	    m_column = column;
	}
	public int compare(Object o1, Object o2) {
	    int result = -2;
	    switch (m_order) {
	    case ASCENDING: result =  compare_ascending(o1, o2);  break;
	    case DECENDING: result = -compare_ascending(o1, o2); break;
	    default:   throw new Error("Invalid direction value given to compare");
	    }
	    return result;
	}

	public int compare_ascending(Object o1, Object o2) {
	    PageStats s1 = ((PageGrabber)o1).getStats();
	    PageStats s2 = ((PageGrabber)o2).getStats();
	    int result;
	    if      (s1 == null && s2 == null) result =  0;
	    else if (s1 == null && s2 != null) result = -1;
	    else if (s1 != null && s2 == null) result =  1;
	    else {
		switch (m_column) {
		case 0:  result = s1.url.toExternalForm().compareTo(s2.url.toExternalForm());  break;
		case 1:  result = intComp(s1.runState, s2.runState) ;                      break;
		case 2:  result = nullComp(s1.contentType, s2.contentType);                 break;
		case 3:  result = intComp(s1.size, s2.size);                               break;
		case 4:  result = nullComp(s1.title, s2.title);                             break;
		case 5:  result = nullComp(s1.date, s2.date);                              break;
		case 6:  result = intComp(s1.level, s2.level);                             break;
		case 7:  result = intComp(s1.linksOut.size(), s2.linksOut.size());         break;
		case 8:  result = intComp(s1.linksIn.size(), s2.linksIn.size());           break;
		case 9:  result = nullComp(s1.url.getHost(), s2.url.getHost());             break;
		case 10: result = nullComp(s1.errorString, s2.errorString);                 break;
		case 11: result = intComp(s1.status, s2.status);                           break;
		default: throw new Error("Invalid column index passed to compare");
		}
	    }
	    return result;
	}
	protected int nullComp(Comparable o1, Comparable o2) {
	    int result;
	    if      (o1 == null && o2 == null) result =  0;
	    else if (o1 == null && o2 != null) result = -1;
	    else if (o1 != null && o2 == null) result =  1;
	    else result = o1.compareTo(o2);
	    return result;
	}
	public boolean equals(Object o) {
	    return (compare(this, o) == 0);
	}
	protected int intComp(int o1, int o2) {
	    if (o1 > o2) {
		return 1;
	    } else if (o1 < o2) {
		return -1;
	    } else {
		return 0;
	    }
	}
	protected int strComp(String s1, String s2) {
	    int result;
	    if (s1 == null) {
		if (s2 == null) {
		    result = 0;
		} else {
		    result = -1;
		}
	    } else if (s2 == null) {
		result = 1;
	    } else {
		result = s1.compareTo(s2);
	    }
	    return result;
	}
    }
}
