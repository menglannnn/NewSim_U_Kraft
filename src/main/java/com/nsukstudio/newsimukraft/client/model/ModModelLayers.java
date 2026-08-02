package com.nsukstudio.newsimukraft.client.model;

import com.nsukstudio.newsimukraft.NewSimukraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

/** 所有自定义实体的 ModelLayerLocation 集中注册 */
public class ModModelLayers {

    public static final ModelLayerLocation FLOATING_BUILD_BOX = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(NewSimukraft.MODID, "floating_build_box"), "main");

    private ModModelLayers() {}
}
