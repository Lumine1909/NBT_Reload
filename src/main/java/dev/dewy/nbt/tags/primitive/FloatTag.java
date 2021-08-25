package dev.dewy.nbt.tags.primitive;

import dev.dewy.nbt.TagTypeRegistry;
import dev.dewy.nbt.TagType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class FloatTag extends NumericalTag<Float> {
    private float value;

    public FloatTag(String name, float value) {
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public byte getTypeId() {
        return TagType.FLOAT.getId();
    }

    @Override
    public Float getValue() {
        return this.value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public void write(DataOutput output, int depth, TagTypeRegistry registry) throws IOException {
        output.writeFloat(this.value);
    }

    @Override
    public FloatTag read(DataInput input, int depth, TagTypeRegistry registry) throws IOException {
        this.value = input.readFloat();

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FloatTag floatTag = (FloatTag) o;

        return Float.compare(floatTag.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return (value != 0.0f ? Float.floatToIntBits(value) : 0);
    }
}
