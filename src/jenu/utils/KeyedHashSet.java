package jenu.utils;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class KeyedHashSet<K,E> extends AbstractSet<E> implements KeyedSet<K,E>
{
	private final transient HashMap<K,E> map;

	public final Function<? super E, ? extends K> keySelector;

	public KeyedHashSet(Function<? super E, ? extends K> keySelector)
	{	this.keySelector = keySelector;
		map = new HashMap<>();
	}
	public KeyedHashSet(Function<? super E, ? extends K> keySelector, int initialCapacity)
	{	this.keySelector = keySelector;
		map = new HashMap<>(initialCapacity);
	}
	public KeyedHashSet(Function<? super E, ? extends K> keySelector, int initialCapacity, float loadFactor)
	{	this.keySelector = keySelector;
		map = new HashMap<>(initialCapacity, loadFactor);
	}
	public KeyedHashSet(Function<? super E, ? extends K> keySelector, Collection<? extends E> initialContent)
	{	this(keySelector, initialContent.size());
		addAll(initialContent);
	}

	@Override public Iterator<E> iterator()
	{	return map.values().iterator();
	}

	@Override public int size()
	{	return map.size();
	}

	public Set<K> keySet()
	{	return map.keySet();
	}

	@Override public boolean contains(Object o)
	{	E elem = map.get(safeGetKey(o));
		return elem != null && elem.equals(o);
	}

	public boolean containsKey(Object o)
	{	return map.get(o) != null;
	}

	public E getByKey(K key)
	{	return map.get(key);
	}

	@Override public boolean add(E e)
	{	return map.putIfAbsent(keySelector.apply(e), e) == null;
	}

	@Override public void clear()
	{	map.clear();
	}

	@Override public boolean remove(Object o)
	{	return map.remove(safeGetKey(o), o);
	}

	@Override public boolean removeAll(Collection<?> c)
	{	boolean modified = false;
		for (Object e : c)
			modified |= remove(e);
		return modified;
	}

	/** Invokes keySelector on Object without ClassCastException.
	 * @param elem Element to check, might not be of type E.
	 * @return Key of elem or null if elem is not of type E. */
	@SuppressWarnings("unchecked")
	protected final K safeGetKey(Object elem)
	{	try
		{	return keySelector.apply((E)elem);
		} catch (ClassCastException ex)
		{	// It is impossible to avoid this exception because it does not happen
			// in the visible part of the code but in a bridge method generated at runtime instead.
			// See https://stackoverflow.com/questions/53466363/how-to-check-generic-method-parameter-to-avoid-classcastexception
			if (ex.getStackTrace()[0].getClassName() == getClass().getName())
				throw ex;
			return null;
		}
	}
}