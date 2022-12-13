package me.deotime.syncd.utils

import kotlin.experimental.ExperimentalTypeInference

class Cache<K, V> internal constructor(private val mapper: (K) -> V) : Map<K, V> {
    private val delegate = mutableMapOf<K, V>()

    override val entries get() = delegate.entries
    override val keys get() = delegate.keys
    override val size get() = delegate.size
    override val values get() = delegate.values

    override fun containsKey(key: K) = delegate.containsKey(key)
    override fun containsValue(value: V) = delegate.containsValue(value)
    override fun isEmpty() = delegate.isEmpty()

    override fun get(key: K): V = delegate.computeIfAbsent(key, mapper)

}

@OptIn(ExperimentalTypeInference::class)
fun <K, V> cache(@BuilderInference mapper: (K) -> V) = Cache(mapper)
