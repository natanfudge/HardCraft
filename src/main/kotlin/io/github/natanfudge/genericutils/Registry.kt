package io.github.natanfudge.genericutils


import io.github.natanfudge.genericutils.superclasses.KItem
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry


interface Registerable {
    val idPath: String
}

context (CommonInit)
fun register(vararg registerables: Registerable) {
    register(registerables.toList())
}

context (CommonInit)
fun register( registerables: List<Registerable>) {
    for(registerable in registerables) {
        when(registerable) {
            is KItem -> register(registerable)
        }
    }
}


context (CommonInit)
fun register(item: KItem) {
    Registry.register(Registries.ITEM, modId(item.idPath), item)
}


