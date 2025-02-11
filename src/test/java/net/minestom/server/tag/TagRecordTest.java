package net.minestom.server.tag;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.minestom.testing.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.*;

class TagRecordTest {

    @Test
    void basic() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Structure("vec", Vec.class);
        var vec = new Vec(1, 2, 3);
        assertNull(handler.getTag(tag));
        handler.setTag(tag, vec);
        assertEquals(vec, handler.getTag(tag));
    }

    @Test
    void fromNBT() {
        var vecCompound = CompoundBinaryTag.builder()
                .putDouble("x", 1)
                .putDouble("y", 2)
                .putDouble("z", 3)
                .build();
        var handler = TagHandler.fromCompound(CompoundBinaryTag.from(Map.of("vec", vecCompound)));
        var tag = Tag.Structure("vec", Vec.class);
        assertEquals(new Vec(1, 2, 3), handler.getTag(tag));
    }

    @Test
    void fromNBTView() {
        var handler = TagHandler.fromCompound(CompoundBinaryTag.builder()
                .putDouble("x", 1)
                .putDouble("y", 2)
                .putDouble("z", 3)
                .build());
        var tag = Tag.View(Vec.class);
        assertEquals(new Vec(1, 2, 3), handler.getTag(tag));
    }

    @Test
    void basicSerializer() {
        var handler = TagHandler.newHandler();
        var serializer = TagRecord.serializer(Vec.class);
        serializer.write(handler, new Vec(1, 2, 3));
        assertEquals(new Vec(1, 2, 3), serializer.read(handler));
    }

    @Test
    void basicSnbt() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Structure("vec", Vec.class);
        var vec = new Vec(1, 2, 3);
        handler.setTag(tag, vec);
        assertEqualsSNBT("""
                {
                  "vec": {
                    "x":1D,
                    "y":2D,
                    "z":3D
                  }
                }
                """, handler.asCompound());
        handler.removeTag(tag);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    void nbtSerializer() {
        record CompoundRecord(CompoundBinaryTag compound) {
        }
        var test = new CompoundRecord(CompoundBinaryTag.from(Map.of("key", StringBinaryTag.stringBinaryTag("value"))));
        var handler = TagHandler.newHandler();
        var serializer = TagRecord.serializer(CompoundRecord.class);
        serializer.write(handler, test);
        assertEquals(test, serializer.read(handler));
    }

    @Test
    void unsupportedList() {
        record Test(List<Object> list) {
        }
        assertThrows(IllegalArgumentException.class, () -> Tag.Structure("test", Test.class));
    }

    @Test
    void unsupportedArray() {
        record Test(Object[] array) {
        }
        assertThrows(IllegalArgumentException.class, () -> Tag.Structure("test", Test.class));
    }

    @Test
    void forceRecord() {
        assertThrows(Throwable.class, () -> Tag.Structure("entity", Class.class.cast(Entity.class)));
    }

    @Test
    void invalidItem() {
        // ItemStack cannot become a record due to `ItemStack#toItemNBT` being serialized differently, and independently of
        // the item record components
        assertThrows(Throwable.class, () -> Tag.Structure("item", Class.class.cast(ItemStack.class)));
    }
}
