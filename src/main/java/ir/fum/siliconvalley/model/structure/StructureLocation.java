package ir.fum.siliconvalley.model.structure;

import java.io.Serializable;

public sealed interface StructureLocation extends Serializable permits VertexLocation, EdgeLocation {
}
