package io.github.natanfudge.genericutils


import io.github.natanfudge.genericutils.superclasses.KItem
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry


interface Registerable<T> {
    val idPath: String
    val registry: Registry<T>
    val component: T
}

context (CommonInit)
fun register(vararg registerables: Registerable<*>) {
    register(registerables.toList())
}

context (CommonInit)
fun register(registerables: List<Registerable<*>>) {
    for (registerable in registerables) {
        val registry = registerable.registry as Registry<Any?>
        Registry.register(registry, modId(registerable.idPath), registerable.component)
    }
}


//context (CommonInit)
//fun register(item: KItem) {
//    Registry.register(Registries.ITEM, modId(item.idPath), item)
//}


