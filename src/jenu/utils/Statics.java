package jenu.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiPredicate;

public final class Statics
{
	private Statics() {}

	/** Check if string is null or empty
	 * @param s String to check
	 * @return true: s is null or empty. */
	public static boolean isEmpty(String s)
	{	return s == null || s.length() == 0;
	}

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

	/** Remove adjacent duplicate from a collection using a costom test function.
	 * @param collection Collection to process.
	 * @param comparer Test function. The first argument is always the previous element of the collection
	 * while the second argument is the current one to be removed when the predicate returns true.
	 * If more than two subsequent elements satisfy the condition the first argument will still remain the same
	 * instead of passing adjacent pairs of elements. */
	public static <T> void removeAdjacentDuplicates(Iterable<T> collection, BiPredicate<? super T, ? super T> comparer)
	{	if (collection instanceof Collection && ((Collection<T>)collection).size() < 1)
			return;
		Iterator<T> it = collection.iterator();
		if (!it.hasNext())
			return;
		T last = it.next();
		while (it.hasNext())
		{	T next = it.next();
			if (comparer.test(last, next))
				it.remove();
			else
				last = next;
		}
	}
	/** Remove adjacent duplicates from a collection.
	 * This will only remove <em>all</em> duplicates if the collection is sorted.
	 * @param collection Collection to process. Null elements are allowed. */
	public static void removeAdjacentDuplicates(Iterable<?> collection)
	{	removeAdjacentDuplicates(collection, Objects::equals);
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
