package jenu.worker;

import java.util.Comparator;

class StatsComparator implements Comparator<PageStats>
{
	int	m_column;
	int	m_order;

	public StatsComparator(int column, int order)
	{
		m_order = order;
		m_column = column;
	}

	public int compare(PageStats o1, PageStats o2)
	{
		int result = -2;
		switch (m_order)
		{
		case PageStats.ASCENDING:
			result = compare_ascending(o1, o2);
			break;
		case PageStats.DECENDING:
			result = -compare_ascending(o1, o2);
			break;
		default:
			throw new Error("Invalid direction value given to compare");
		}
		return result;
	}

	public int compare_ascending(PageStats s1, PageStats s2)
	{
		int result;
		if (s1 == null && s2 == null)
			result = 0;
		else if (s1 == null && s2 != null)
			result = -1;
		else if (s1 != null && s2 == null)
			result = 1;
		else
		{
			switch (m_column)
			{
			case 0:
				result = s1.url.toExternalForm().compareTo(s2.url.toExternalForm());
				break;
			case 1:
				result = Integer.compare(s1.runState, s2.runState);
				break;
			case 2:
				result = nullComp(s1.contentType, s2.contentType);
				break;
			case 3:
				result = Integer.compare(s1.size, s2.size);
				break;
			case 4:
				result = nullComp(s1.title, s2.title);
				break;
			case 5:
				result = nullComp(s1.date, s2.date);
				break;
			case 6:
				result = Integer.compare(s1.level, s2.level);
				break;
			case 7:
				result = Integer.compare(s1.linksOut.size(), s2.linksOut.size());
				break;
			case 8:
				result = Integer.compare(s1.linksIn.size(), s2.linksIn.size());
				break;
			case 9:
				result = nullComp(s1.url.getHost(), s2.url.getHost());
				break;
			case 10:
				result = nullComp(s1.errorString, s2.errorString);
				break;
			case 11:
				result = Integer.compare(s1.status, s2.status);
				break;
			default:
				throw new Error("Invalid column index passed to compare");
			}
		}
		return result;
	}

	protected <T extends Comparable<T>> int nullComp(T o1, T o2)
	{
		if (o1 == o2)
			return 0;
		else if (o1 == null)
			return -1;
		else if (o2 == null)
			return 1;
		else
			return o1.compareTo(o2);
	}
}