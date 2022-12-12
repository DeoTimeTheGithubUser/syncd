package me.deotime.syncd.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.deotime.syncd.config.project.Project
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

private val ConfigFile = File("config")
private val RegisteredProperties = mutableListOf<Config.Property<Any?>>()

@Serializable(with = Config.Serializer::class)
object Config {



    var Projects by property(listOf<Project>())


    fun read() {
        val data = ConfigFile.readText()
        if(data.isNotBlank()) Json.decodeFromString(Serializer, data)
    }

    fun write() {
        ConfigFile.writeText(Json.encodeToString(this))
    }

    init {
        ConfigFile.createNewFile()
        read()
    }

    object Serializer : KSerializer<Config> {
        override val descriptor = buildClassSerialDescriptor("Config")

        override fun serialize(encoder: Encoder, value: Config) {
            encoder.encodeInt(RegisteredProperties.size)
            RegisteredProperties.forEach {
                encoder.encodeString(it.name)
                encoder.encodeSerializableValue(it.valueSerializer, it._value.get())
            }
        }

        override fun deserialize(decoder: Decoder) = Config.apply {
            val byName = RegisteredProperties.associateBy { it.name }
            repeat(decoder.decodeInt()) {
                val property = byName[decoder.decodeString()] ?: return@repeat
                property._value.set(decoder.decodeSerializableValue(property.valueSerializer))
            }
        }
    }

    class Property<T> internal constructor(internal val valueSerializer: KSerializer<T>, default: T) {

        internal var _value: AtomicReference<T> = AtomicReference(default)
        internal lateinit var name: String

        operator fun provideDelegate(ref: Config, prop: KProperty<*>): Property<T> {
            @Suppress("UNCHECKED_cAST")
            RegisteredProperties += this as Property<Any?>
            name = prop.name
            return this
        }


        operator fun getValue(ref: Config, prop: KProperty<*>) = get()
        fun get(): T = read().let { _value.get() }

        operator fun setValue(ref: Config, prop: KProperty<*>, new: T) = set(new)
        fun set(new: T) {
            _value.set(new)
            write()
        }



    }

    inline fun <T> KMutableProperty0<T>.update(closure: (T) -> T) {
        (getDelegate() as Property<T>).let { it.set(it.get().let(closure)) }
    }
    
    private inline fun <reified T : Any> property(default: T) =
        Property(serializer<T>(), default)


}