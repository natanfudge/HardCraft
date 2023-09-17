package io.github.natanfudge.genericutils

import com.google.common.base.Supplier
import com.google.common.base.Suppliers
import dev.architectury.registry.registries.Registries
import io.github.natanfudge.genericutils.superclasses.KBlock
import io.github.natanfudge.genericutils.superclasses.KBlockEntityProvider
import io.github.natanfudge.genericutils.superclasses.KItem
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.registry.Registry


interface Registerable {
    val idPath: String
}

context (CommonInit)
fun register(vararg registerables: Registerable) {
    for(registerable in registerables) {
        when(registerable) {
            is KBlock -> register(registerable)
            is KItem -> register(registerable)
        }
    }
}

//TODO: forge registry won't like this, it wants deferred stuff, see  https://docs.architectury.dev/api/registry

/**
 * Will register the block and associated BlockItem and BlockEntityType with the KBlock's ID
 */
context(CommonInit)
fun register(block: KBlock) = with(block) {
    val id = modId(idPath)
    Registry.register(Registry.BLOCK, id, this)
    if (itemSettings != null) Registry.register(Registry.ITEM, id, block.item)
    if (this is KBlockEntityProvider) Registry.register(Registry.BLOCK_ENTITY_TYPE, id, blockEntityType)
}

context (CommonInit)
fun register(item: KItem) {
    Registry.register(Registry.ITEM, modId(item.idPath), item)
}

context(CommonInit)
fun register(type: ScreenHandlerType<*>, idPath: String) {
    Registry.register(Registry.SCREEN_HANDLER, modId(idPath), type)
}

