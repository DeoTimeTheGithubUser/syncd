package me.deotime.syncd.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

private val ConfigFile = File("config")

@Serializable(with = Config.Serializer::class)
object Config {

    internal val properties = mutableListOf<Property<Any?>>()


    var Name by property("no name")


    fun read() {
        Json.decodeFromString(Serializer, ConfigFile.readText())
    }

    init {
        ConfigFile.createNewFile()
        read()
    }

    fun write() {
        ConfigFile.writeText(Json.encodeToString(this))
    }

    object Serializer : KSerializer<Config> {
        override val descriptor = buildClassSerialDescriptor("Config")

        override fun serialize(encoder: Encoder, value: Config) {
            encoder.encodeInt(value.properties.size)
            value.properties.forEach {
                encoder.encodeString(it.name)
                encoder.encodeSerializableValue(it.valueSerializer, it.get())
            }
        }

        override fun deserialize(decoder: Decoder) = Config.apply {
            val byName = properties.associateBy { it.name }
            repeat(decoder.decodeInt()) {
                val property = byName[decoder.decodeString()] ?: return@repeat
                property.value.set(decoder.decodeSerializableValue(property.valueSerializer))
            }
        }
    }

    class Property<T> internal constructor(internal val valueSerializer: KSerializer<T>, default: T) {

        internal var value: AtomicReference<T> = AtomicReference(default)
        internal lateinit var name: String

        operator fun provideDelegate(ref: Config, prop: KProperty<*>): Property<T> {
            ref.properties.add(this as Property<Any?>)
            name = prop.name
            return this
        }


        operator fun getValue(ref: Config, prop: KProperty<*>) = get()

        fun get() = read().let { value.get() }


        operator fun setValue(ref: Config, prop: KProperty<*>, new: T) = set(new)

        fun set(new: T) {
            value.set(new)
            write()
        }


    }
    
    internal inline fun <reified T : Any> property(default: T) = 
        Property(serializer<T>(), default)


}