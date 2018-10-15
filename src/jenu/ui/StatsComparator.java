package jenu.ui;

import java.util.Comparator;
import java.util.Iterator;

import jenu.worker.PageStats;

final class StatsComparator implements Comparator<PageStats>
{
	int	m_column;
	int	m_direction;

	public StatsComparator(int column, boolean descending)
	{
		m_direction = descending ? -1 : 1;
		m_column = column;
	}

	public int compare(PageStats s1, PageStats s2)
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
				result = nullComp(s1.getRunState(), s2.getRunState());
				break;
			case 2:
				result = nullComp(s1.contentType, s2.contentType);
				break;
			case 3:
				result = Long.compare(s1.size, s2.size);
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
				result = nullComp(s1.getErrorString(), s2.getErrorString());
				break;
			case 11:
				result = sortedSequenceComp(s1.status, s2.status);
				break;
			default:
				throw new Error("Invalid column index passed to compare");
			}
		}
		return result * m_direction;
	}

	private static <T extends Comparable<T>> int nullComp(T o1, T o2)
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

	private static <T extends Comparable<T>> int sortedSequenceComp(Iterable<T> s1, Iterable<T> s2)
	{
		Iterator<T> i1 = s1.iterator(), i2 = s2.iterator();
		while (true)
		{	if (!i1.hasNext())
				return !i2.hasNext() ? 0 : -1;
			if (!i2.hasNext())
				return 1;
			T v1 = i1.next(), v2 = i2.next();
			if (v1 == null)
				return v2 == null ? 0 : -1;
			if (v2 == null)
				return 1;
			int cmp = v1.compareTo(v2);
			if (cmp != 0)
				return cmp;
		}
	}
}