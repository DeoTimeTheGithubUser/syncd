package me.deotime.syncd.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object RemoteScope : CoroutineScope by CoroutineScope(Dispatchers.Default)