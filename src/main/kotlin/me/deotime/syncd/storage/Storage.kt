package me.deotime.syncd.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.serializer
import me.deotime.syncd.storage.Storage.Property.Companion.properties
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty


// todo make this a seperate library and maybe make it not as weird
@Serializable(with = Storage.Serializer::class)
interface Storage {

    val root: String
    val name: String


    private fun file() = File(root, name)

    private fun read() {
        registerFile()
        val data = file().readText()
        if (data.isNotBlank()) Json.decodeFromString(Serializer, data)
    }

    private fun write() {
        registerFile()
        file().writeText(Json.encodeToString(Serializer, this))
    }

    private fun registerFile() {
        RegisteredStorages[name] = this
        if (!file().exists()) {
            File(root).mkdirs()
            file().createNewFile()
        }
    }

    object Serializer : KSerializer<Storage> {
        override val descriptor = buildClassSerialDescriptor("Config")

        override fun serialize(encoder: Encoder, value: Storage) {

            val data = buildJsonObject {
                put("name", value.name)
                putJsonArray("properties") {
                    value.properties.forEach {
                        addJsonObject {
                            put("name", it.name)
                            put("value", Json.encodeToJsonElement(it.valueSerializer, it._value.get()))
                        }
                    }
                }
            }

            (encoder as? JsonEncoder)?.encodeJsonElement(data) ?: error("Storage can only be serialized as JSON.")


        }

        override fun deserialize(decoder: Decoder): Storage {
            if (decoder !is JsonDecoder) error("Storage can only be deserialized as JSON.")
            val data = decoder.decodeJsonElement().jsonObject
            val name = data["name"]?.jsonPrimitive?.content
            return RegisteredStorages[name]?.apply {
                val byName = properties.associateBy { it.name }
                data["properties"]?.jsonArray?.forEach {
                    val property = byName[it.jsonObject["name"].toString()] ?: return@forEach
                    property._value.set(
                        Json.decodeFromString(
                            property.valueSerializer,
                            it.jsonObject["value"].toString()
                        )
                    )
                }
            } ?: error("Unknown storage type found: $name")
        }
    }

    class Property<T>(internal val valueSerializer: KSerializer<T>, default: T) {

        internal var _value: AtomicReference<T> = AtomicReference(default)
        internal lateinit var name: String


        operator fun provideDelegate(ref: Storage, prop: KProperty<*>): Property<T> {
            @Suppress("UNCHECKED_CAST")
            Properties.computeIfAbsent(ref.name) { mutableListOf() } += this as Property<Any?>
            name = prop.name
            return this
        }


        operator fun getValue(ref: Storage, prop: KProperty<*>): T = ref.read().let { _value.get() }

        operator fun setValue(ref: Storage, prop: KProperty<*>, new: T) {
            _value.set(new)
            ref.write()
        }

        companion object {
            private val Properties = mutableMapOf<String, MutableList<Property<Any?>>>()
            val Storage.properties get() = Properties[name] ?: mutableListOf()
        }


    }

    companion object {
        private val RegisteredStorages = mutableMapOf<String, Storage>()

        inline fun <reified T : Any> Storage.property(default: T) = Property(serializer(), default)
    }


}

