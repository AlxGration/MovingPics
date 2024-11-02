package com.alexvinov.movingpics.utils

/**
 * Стек фиксированного размера.
 * При переполнении - удаляется элемент на дне стека
 */
class LimitedDeque<T>(
    private val limitSize: Int,
) {
    private val deque = ArrayDeque<T>(limitSize)

    fun push(element: T): Boolean {
        if (deque.size >= limitSize) deque.removeFirst()
        return deque.add(element)
    }

    fun pop(): T? = deque.removeLastOrNull()

    fun size(): Int = deque.size

    fun last(): T? = deque.lastOrNull()

    fun clear() {
        deque.clear()
    }
}
