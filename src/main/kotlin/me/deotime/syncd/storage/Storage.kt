package me.deotime.syncd.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

private val RegisteredStorages = mutableMapOf<String, Storage>()

// todo make this a seperate library and maybe make it not as weird
@Serializable(with = Storage.Serializer::class)
abstract class Storage {

    abstract val root: String
    abstract val name: String

    private val properties = mutableListOf<Property<Any?>>()


    private fun file() = File(root, name)

    private fun read() {
        if (!file().exists()) registerFile()
        val data = file().readText()
        if (data.isNotBlank()) Json.decodeFromString(Serializer, data)
    }

    private fun write() {
        if (!file().exists()) registerFile()
        file().writeText(Json.encodeToString(this))
    }

    private fun registerFile() {
        RegisteredStorages[name] = this
        File(root).mkdirs()
        file().createNewFile()
    }

    object Serializer : KSerializer<Storage> {
        override val descriptor = buildClassSerialDescriptor("Config")

        override fun serialize(encoder: Encoder, value: Storage) {
            encoder.encodeString(value.name)
            encoder.encodeInt(value.properties.size)
            value.properties.forEach {
                encoder.encodeString(it.name)
                encoder.encodeSerializableValue(it.valueSerializer, it._value.get())
            }

        }

        override fun deserialize(decoder: Decoder) = RegisteredStorages[decoder.decodeString()]?.apply {
            val byName = properties.associateBy { it.name }
            repeat(decoder.decodeInt()) {
                val property = byName[decoder.decodeString()] ?: return@repeat
                property._value.set(decoder.decodeSerializableValue(property.valueSerializer))
            }
        } ?: error("Unknown storage type found")
    }

    class Property<T>(internal val valueSerializer: KSerializer<T>, default: T) {

        internal var _value: AtomicReference<T> = AtomicReference(default)
        internal lateinit var name: String

        operator fun provideDelegate(ref: Storage, prop: KProperty<*>): Property<T> {
            @Suppress("UNCHECKED_cAST")
            ref.properties += this as Property<Any?>
            name = prop.name
            return this
        }


        operator fun getValue(ref: Storage, prop: KProperty<*>): T = ref.read().let { _value.get() }

        operator fun setValue(ref: Storage, prop: KProperty<*>, new: T) {
            _value.set(new)
            ref.write()
        }


    }

    protected inline fun <reified T : Any> property(default: T) =
        Property(serializer<T>(), default)


}