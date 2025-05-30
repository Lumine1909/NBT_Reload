package dev.dewy.nbt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.dewy.nbt.api.registry.TagTypeRegistry;
import dev.dewy.nbt.api.snbt.SnbtConfig;
import dev.dewy.nbt.io.CompressionType;
import dev.dewy.nbt.io.NbtReader;
import dev.dewy.nbt.io.NbtWriter;
import dev.dewy.nbt.tags.collection.CompoundTag;
import lombok.NonNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Standard interface for reading and writing NBT data structures.
 */
public class Nbt {
    private @NonNull Gson gson;
    private @NonNull TagTypeRegistry typeRegistry;
    private @NonNull SnbtConfig snbtConfig;

    private final @NonNull NbtWriter writer;
    private final @NonNull NbtReader reader;

    /**
     * Constructs an instance of this class using a default {@link TagTypeRegistry} (supporting the standard 12 tag types).
     */
    public Nbt() {
        this(new TagTypeRegistry());
    }

    /**
     * Constructs an instance of this class using a given {@link TagTypeRegistry}, with a default GSON instance.
     *
     * @param typeRegistry the tag type registry to be used, typically containing custom tag entries.
     */
    public Nbt(@NonNull TagTypeRegistry typeRegistry) {
        this(typeRegistry, new Gson());
    }

    /**
     * Constructs an instance of this class using a given {@link TagTypeRegistry}, with a default {@link SnbtConfig} instance.
     *
     * @param typeRegistry the tag type registry to be used, typically containing custom tag entries.
     * @param gson the GSON instance to be used.
     */
    public Nbt(@NonNull TagTypeRegistry typeRegistry, @NonNull Gson gson) {
        this(typeRegistry, gson, new SnbtConfig());
    }

    /**
     * Constructs an instance of this class using a given {@link TagTypeRegistry}, {@code Gson} and an {@link SnbtConfig}.
     *
     * @param typeRegistry the tag type registry to be used, typically containing custom tag entries.
     * @param gson the GSON instance to be used.
     * @param snbtConfig the SNBT config object to be used.
     */
    public Nbt(@NonNull TagTypeRegistry typeRegistry, @NonNull Gson gson, @NonNull SnbtConfig snbtConfig) {
        this.typeRegistry = typeRegistry;
        this.gson = gson;
        this.snbtConfig = snbtConfig;

        this.writer = new NbtWriter(typeRegistry);
        this.reader = new NbtReader(typeRegistry);
    }

    /**
     * Writes the given root {@link CompoundTag} to a provided {@link DataOutput} stream.
     *
     * @param compound the NBT structure to write, contained within a {@link CompoundTag}.
     * @param output the stream to write to.
     * @throws IOException if any I/O error occurs.
     */
    public void toStream(@NonNull CompoundTag compound, @NonNull DataOutput output) throws IOException {
        this.writer.toStream(compound, output);
    }

    /**
     * Writes the given root {@link CompoundTag} to a {@link File} with no compression.
     *
     * @param compound the NBT structure to write, contained within a {@link CompoundTag}.
     * @param file the file to write to.
     * @throws IOException if any I/O error occurs.
     */
    public void toFile(@NonNull CompoundTag compound, @NonNull File file) throws IOException {
        this.toFile(compound, file, CompressionType.NONE);
    }

    /**
     * Writes the given root {@link CompoundTag} to a {@link File} using a certain {@link CompressionType}.
     *
     * @param compound the NBT structure to write, contained within a {@link CompoundTag}.
     * @param file the file to write to.
     * @param compression the compression to be applied.
     * @throws IOException if any I/O error occurs.
     */
    public void toFile(@NonNull CompoundTag compound, @NonNull File file, @NonNull CompressionType compression) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
             DataOutputStream dos = new DataOutputStream(getOutputStream(bos, compression))) {
            this.toStream(compound, dos);
        }
    }

    private OutputStream getOutputStream(OutputStream outputStream, CompressionType compression) throws IOException {
        switch (compression) {
            case NONE:
                return outputStream;
            case GZIP:
                return new GZIPOutputStream(outputStream);
            case ZLIB:
                return new DeflaterOutputStream(outputStream);
            default:
                throw new IllegalArgumentException("Unsupported compression type: " + compression);
        }
    }

    /**
     * Serializes the given root {@link CompoundTag} to a SNBT (Stringified NBT).
     *
     * @param compound the NBT structure to serialize to SNBT, contained within a {@link CompoundTag}.
     * @return the serialized SNBT string.
     */
    public String toSnbt(@NonNull CompoundTag compound) {
        return compound.toSnbt(0, this.typeRegistry, this.snbtConfig);
    }

    /**
     * Serializes the given root {@link CompoundTag} to a JSON {@link File}.
     *
     * @param compound the NBT structure to serialize to JSON, contained within a {@link CompoundTag}.
     * @param file the JSON file to write to.
     * @throws IOException if any I/O error occurs.
     */
    public void toJson(@NonNull CompoundTag compound, @NonNull File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(compound.toJson(0, this.typeRegistry), writer);
        }
    }

    /**
     * Converts the given root {@link CompoundTag} to a {@code byte[]} array.
     *
     * @param compound the NBT structure to write, contained within a {@link CompoundTag}.
     * @return the resulting {@code byte[]} array.
     * @throws IOException if any I/O error occurs.
     */
    public byte[] toByteArray(@NonNull CompoundTag compound) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            this.toStream(compound, dos);
            return baos.toByteArray();
        }
    }

    /**
     * Converts the given root {@link CompoundTag} to a Base64 encoded string.
     *
     * @param compound the NBT structure to write, contained within a {@link CompoundTag}.
     * @return the resulting Base64 encoded string.
     * @throws IOException if any I/O error occurs.
     */
    public String toBase64(@NonNull CompoundTag compound) throws IOException {
        return new String(Base64.getEncoder().encode(this.toByteArray(compound)), StandardCharsets.UTF_8);
    }

    /**
     * Reads an NBT data structure (root {@link CompoundTag}) from a {@link DataInput} stream.
     *
     * @param input the stream to read from.
     * @return the root {@link CompoundTag} read from the stream.
     * @throws IOException if any I/O error occurs.
     */
    public CompoundTag fromStream(@NonNull DataInput input) throws IOException {
        return this.reader.fromStream(input);
    }

    /**
     * Reads an NBT data structure (root {@link CompoundTag}) from a {@link File}.
     *
     * @param file the file to read from.
     * @return the root {@link CompoundTag} read from the stream.
     * @throws IOException if any I/O error occurs.
     */
    public CompoundTag fromFile(@NonNull File file) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             FileInputStream fis = new FileInputStream(file);
             DataInputStream in = new DataInputStream(getInputStream(bis, fis))) {
            return this.fromStream(in);
        }
    }

    private InputStream getInputStream(InputStream inputStream, FileInputStream fis) throws IOException {
        return switch (CompressionType.getCompression(fis)) {
            case NONE -> inputStream;
            case GZIP -> new GZIPInputStream(inputStream);
            case ZLIB -> new InflaterInputStream(inputStream);
            default -> throw new IllegalStateException("Illegal compression type. This should never happen.");
        };
    }

    /**
     * Deserializes an NBT data structure (root {@link CompoundTag}) from a JSON {@link File}.
     *
     * @param file the JSON file to read from.
     * @return the root {@link CompoundTag} deserialized from the JSON file.
     * @throws IOException if any I/O error occurs.
     */
    public CompoundTag fromJson(@NonNull File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return new CompoundTag().fromJson(gson.fromJson(reader, JsonObject.class), 0, this.typeRegistry);
        }
    }

    /**
     * Reads an NBT data structure (root {@link CompoundTag}) from a {@code byte[]} array.
     *
     * @param bytes the {@code byte[]} array to read from.
     * @return the root {@link CompoundTag} read from the stream.
     * @throws IOException if any I/O error occurs.
     */
    public CompoundTag fromByteArray(@NonNull byte[] bytes) throws IOException {
        try (DataInputStream bais = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(bytes)))) {
            return fromStream(bais);
        }
    }

    /**
     * Decodes an NBT data structure (root {@link CompoundTag}) from a Base64 encoded string.
     *
     * @param encoded the encoded Base64 string to decode.
     * @return the decoded root {@link CompoundTag}.
     * @throws IOException if any I/O error occurs.
     */
    public CompoundTag fromBase64(@NonNull String encoded) throws IOException {
        return fromByteArray(Base64.getDecoder().decode(encoded));
    }

    /**
     * Returns the {@link TagTypeRegistry} currently in use by this instance.
     *
     * @return the {@link TagTypeRegistry} currently in use by this instance.
     */
    public TagTypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    /**
     * Sets the {@link TagTypeRegistry} currently in use by this instance. Used to utilise custom-made tag types.
     *
     * @param typeRegistry the new {@link TagTypeRegistry} to be set.
     */
    public void setTypeRegistry(@NonNull TagTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;

        this.writer.setTypeRegistry(typeRegistry);
        this.reader.setTypeRegistry(typeRegistry);
    }

    /**
     * Returns the {@code Gson} currently in use by this instance.
     *
     * @return the {@code Gson} currently in use by this instance.
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Sets the {@code Gson} currently in use by this instance.
     *
     * @param gson the new {@code Gson} to be set.
     */
    public void setGson(@NonNull Gson gson) {
        this.gson = gson;
    }

    /**
     * Returns the {@link SnbtConfig} currently in use by this instance.
     *
     * @return the {@link SnbtConfig} currently in use by this instance.
     */
    public SnbtConfig getSnbtConfig() {
        return snbtConfig;
    }

    /**
     * Sets the {@link SnbtConfig} currently in use by this instance.
     *
     * @param snbtConfig the new {@link SnbtConfig} to be set.
     */
    public void setSnbtConfig(@NonNull SnbtConfig snbtConfig) {
        this.snbtConfig = snbtConfig;
    }
}