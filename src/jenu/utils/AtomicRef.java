package jenu.utils;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/** Extension of AtomicReference */
public class AtomicRef<T> extends AtomicReference<T>
{
	/** Lazy initialization of current value with partial serialization.
	 * @param factory Factory for the reference. It is only invoked when the current value is null.
	 * But it might be called while another thread sets the value. In this case the object of factory
	 * is discarded and the instance of the other thread is returned.
	 * @return Returns the value of getAcquire() if not null or factory() otherwise.
	 */
	public T weakLazyInit(Supplier<? extends T> factory)
	{	T val = getAcquire();
		if (val == null)
		{	val = factory.get();
			if (!compareAndSet(null, val))
				val = getAcquire(); // Another thread did the job
		}
		return val;
	}
}
