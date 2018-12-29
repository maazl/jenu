package jenu.utils;

import java.util.Iterator;
import java.util.Objects;

public final class Statics
{
	private Statics() {}

	/** Concatenate two strings where each of them might be null which is considered to be the same as "".
	 * @param s1 left string
	 * @param s2 right string
	 * @return concatenation of s1 + s2 if both of them are not null,
	 * s1 or s2 if exactly one of them is not null and
	 * null if both are null. */
	public static String strcat(final String s1, final String s2)
	{	if (s1 == null)
			return s2;
		if (s2 == null)
			return s1;
		return s1 + s2;
	}
	/** Concatenate two strings with delimiter where each of them might be null.
	 * @param s1 left string
	 * @param s2 right string
	 * @return concatenation of s1 + delim + s2 if both of them are not null,
	 * s1 or s2 if exactly one of them is not null and
	 * null if both are null. */
	public static String strcat(final String s1, final String s2, char delim)
	{	if (s1 == null)
			return s2;
		if (s2 == null)
			return s1;
		return s1 + delim + s2;
	}

	/** Remove adjacent duplicate from a collection.
	 * This will only remove <em>all</em> duplicates if the collection is sorted.
	 * @param collection Collection to process.
	 */
	public static void removeAdjacentDuplicates(Iterable<?> collection)
	{	Object last = null;
		Iterator<?> it = collection.iterator();
		while (it.hasNext())
		{	Object url = it.next();
			if (url.equals(last))
				it.remove();
			else
				last = url;
		}
	}

	/** Locate element in an array by linear search.
	 * @param array Array
	 * @param value Object to search for
	 * @return Index of value in array or -1 if not found. */
	public static <T> int indexOf(T[] array, T value)
	{	int i = 0;
		while (i < array.length)
		{	if (Objects.equals(array[i], value))
				return i;
			++i;
		}
		return -1;
	}

	/*public static <E> SortedSet<E> asSortedSet(Comparator<? super E> comparator, Collection<E> data)
	{	return data instanceof SortedSet && ((SortedSet<E>)data).comparator().equals(comparator)
			? (SortedSet<E>)data : new ArraySet<E>(comparator, data);
	}*/
}
