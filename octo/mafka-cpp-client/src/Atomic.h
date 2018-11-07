#ifndef __MAFKA_ATOMIC_H__
#define __MAFKA_ATOMIC_H__

namespace mafka
{


#if defined(__GNUC__) && (((__GNUC__ == 4) && (__GNUC_MINOR__ >= 1)) || (__GNUC__ >= 5))

/* see http://gcc.gnu.org/onlinedocs/gcc-4.1.2/gcc/Atomic-Builtins.html */


/*32bit type*/
// AtomicSwap(ptr, value)
// Writes @value into @ptr, returning the previous value.
template<class T>
static inline T AtomicSwap(volatile T* mem, T value)
{
	return __sync_lock_test_and_set(mem, value);
}

// AtomicCAS(mem, prev, next)
// Atomically store the value @next into the pointer @mem, but only if the current value at @mem is @prev.
// Returns true if @next was successfully stored.
template<class T>
static inline bool AtomicCAS(volatile T* mem, T prev, T next)
{
	return __sync_bool_compare_and_swap(mem, prev, next);
}

// AtomicIncrement(ptr, count)
// Increment @ptr by @count, returning the previous value.
template<class T, class TAmount>
static inline T AtomicIncrement(volatile T* mem, TAmount count)
{
	return __sync_fetch_and_add(mem, count);
}

// AtomicIncrementReturn(ptr, count)
// Increment @ptr by @count, returning the new value.
template<class T, class TAmount>
static inline T AtomicIncrementReturn(volatile T* mem, TAmount count)
{
	return __sync_add_and_fetch(mem, count);
}


// AtomicDecrement(ptr, count)
// Decrement @ptr by @count, returning the previous value.
template<class T, class TAmount>
static inline T AtomicDecrement(volatile T* mem, TAmount count)
{
	return __sync_fetch_and_sub(mem, count);
}

// AtomicDecrementReturn(ptr, count)
// Decrement @ptr by @count, returning the new value.
template<class T, class TAmount>
static inline T AtomicDecrementReturn(volatile T* mem, TAmount count)
{
	return __sync_sub_and_fetch(mem, count);
}

#else /* not gcc > v4.1.2 */
	#error Need a compiler / libc that supports atomic operations, e.g. gcc v4.1.2 or later
#endif



}

#endif /*__MAFKA_ATOMIC_H__*/
