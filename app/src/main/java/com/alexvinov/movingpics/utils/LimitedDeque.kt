package com.alexvinov.movingpics.utils

import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Стек фиксированного размера.
 * При переполнении - удаляется элемент на дне стека
 */
class LimitedDeque<T>(
    private val limitSize: Int,
) {
    private val deque = ConcurrentLinkedDeque<T>()

    fun push(element: T): Boolean {
        if (deque.size >= limitSize) deque.removeFirst()
        return deque.add(element)
    }

    fun pop(): T? = if (deque.isEmpty()) null else deque.removeLast()

    fun size(): Int = deque.size

    fun last(): T? = deque.lastOrNull()

    fun isEmpty(): Boolean = deque.isEmpty()

    fun clear() {
        deque.clear()
    }
}
