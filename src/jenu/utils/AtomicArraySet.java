package jenu.utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static jenu.utils.Statics.*;

/** Array based set with lock-free atomic {@link add}/{@link remove}.
 * Use {@link get} to retrieve the current content atomically.
 *
 * Note that this implementation is optimized for only a few elements in the set.
 * Furthermore it is optimized for rare modifications.
 * This typically fits the needs for the observable pattern.
 *
 * @param <L> Element type. */
public final class AtomicArraySet<L> extends AtomicReference<L[]>
{
	/** Create the atomic array.
	 * @param array Initial value. This is mandatory to circumvent type erasure.
	 * Typically you want to pass an empty array. */
	public AtomicArraySet(L[] array)
	{	super(array);
		if (array == null)
			throw new NullPointerException();
	}

	/** Atomically add an element to the array unless it already contains this element.
	 * @param l Element to add.
	 * @return <code>true</code>: element added,
	 * <code>false<code>: element already in the set, no changes made. */
	public boolean add(L l)
	{	L[] oldval, newval;
		do
		{	oldval = get();
			if (indexOf(oldval, l) >= 0)
				return false;
			newval = Arrays.copyOf(oldval, oldval.length + 1);
			newval[oldval.length] = l;
		} while (!compareAndSet(oldval, newval));
		return true;
	}

	/** Atomically remove an element from the set.
	 * @param l Element to remove.
	 * @return <code>true</code>: element removed, <code>false</code>: element not found. */
	@SuppressWarnings("unchecked")
	public boolean remove(L l)
	{	L[] oldval, newval;
		do
		{	oldval = get();
			int i = indexOf(oldval, l);
			if (i < 0)
				return false;
			newval = (L[])Array.newInstance(oldval.getClass().getComponentType(), oldval.length - 1);
			System.arraycopy(oldval, 0, newval, 0, i);
			System.arraycopy(oldval, i + 1, newval, i, newval.length - i);
		} while (!compareAndSet(oldval, newval));
		return true;
	}
}
