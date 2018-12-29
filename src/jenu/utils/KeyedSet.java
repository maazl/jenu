package jenu.utils;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/** Set of objects with embedded key.
 * @param <K> Key type
 * @param <E> Element type */
public interface KeyedSet<K,E> extends Set<E>, Collection<E>
{
	/** Returns a set of keys. You cannot add elements through this set
	 * but removing keys will remove the matching object from the collection.
	 * @return Set of keys. */
	Set<K> keySet();

	/** Check whether The set already contains an object with the specified key.
	 * @param key Key
	 * @return true: object found. */
	boolean containsKey(Object key);

	/** Retrieve element by it's key.
	 * @param key Key
	 * @return Matching element or null if no such element exists. */
	E getByKey(K key);

	/** Add new object if key does not exist.
	 * @param key Key to search for.
	 * @param factory Factory to create the object from the key if the key does not yet exist.
	 * @return The existing or created element. */
	default E createIfAbsent(K key, Function<? super K, ? extends E> factory)
	{	E e = getByKey(key);
		if (e == null)
			add(e = factory.apply(key));
		return e;
	}
}
