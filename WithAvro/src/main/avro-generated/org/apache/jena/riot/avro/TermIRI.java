/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package org.apache.jena.riot.avro;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class TermIRI extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -4678152139515446457L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"TermIRI\",\"namespace\":\"org.apache.jena.riot.avro\",\"fields\":[{\"name\":\"iri\",\"type\":{\"type\":\"enum\",\"name\":\"Kind\",\"symbols\":[\"IRI\",\"PN\"]}},{\"name\":\"value\",\"type\":\"string\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<TermIRI> ENCODER =
      new BinaryMessageEncoder<TermIRI>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<TermIRI> DECODER =
      new BinaryMessageDecoder<TermIRI>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<TermIRI> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<TermIRI> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<TermIRI> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<TermIRI>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this TermIRI to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a TermIRI from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a TermIRI instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static TermIRI fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

   private org.apache.jena.riot.avro.Kind iri;
   private java.lang.CharSequence value;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public TermIRI() {}

  /**
   * All-args constructor.
   * @param iri The new value for iri
   * @param value The new value for value
   */
  public TermIRI(org.apache.jena.riot.avro.Kind iri, java.lang.CharSequence value) {
    this.iri = iri;
    this.value = value;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return iri;
    case 1: return value;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: iri = (org.apache.jena.riot.avro.Kind)value$; break;
    case 1: value = (java.lang.CharSequence)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'iri' field.
   * @return The value of the 'iri' field.
   */
  public org.apache.jena.riot.avro.Kind getIri() {
    return iri;
  }


  /**
   * Sets the value of the 'iri' field.
   * @param value the value to set.
   */
  public void setIri(org.apache.jena.riot.avro.Kind value) {
    this.iri = value;
  }

  /**
   * Gets the value of the 'value' field.
   * @return The value of the 'value' field.
   */
  public java.lang.CharSequence getValue() {
    return value;
  }


  /**
   * Sets the value of the 'value' field.
   * @param value the value to set.
   */
  public void setValue(java.lang.CharSequence value) {
    this.value = value;
  }

  /**
   * Creates a new TermIRI RecordBuilder.
   * @return A new TermIRI RecordBuilder
   */
  public static org.apache.jena.riot.avro.TermIRI.Builder newBuilder() {
    return new org.apache.jena.riot.avro.TermIRI.Builder();
  }

  /**
   * Creates a new TermIRI RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new TermIRI RecordBuilder
   */
  public static org.apache.jena.riot.avro.TermIRI.Builder newBuilder(org.apache.jena.riot.avro.TermIRI.Builder other) {
    if (other == null) {
      return new org.apache.jena.riot.avro.TermIRI.Builder();
    } else {
      return new org.apache.jena.riot.avro.TermIRI.Builder(other);
    }
  }

  /**
   * Creates a new TermIRI RecordBuilder by copying an existing TermIRI instance.
   * @param other The existing instance to copy.
   * @return A new TermIRI RecordBuilder
   */
  public static org.apache.jena.riot.avro.TermIRI.Builder newBuilder(org.apache.jena.riot.avro.TermIRI other) {
    if (other == null) {
      return new org.apache.jena.riot.avro.TermIRI.Builder();
    } else {
      return new org.apache.jena.riot.avro.TermIRI.Builder(other);
    }
  }

  /**
   * RecordBuilder for TermIRI instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<TermIRI>
    implements org.apache.avro.data.RecordBuilder<TermIRI> {

    private org.apache.jena.riot.avro.Kind iri;
    private java.lang.CharSequence value;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(org.apache.jena.riot.avro.TermIRI.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.iri)) {
        this.iri = data().deepCopy(fields()[0].schema(), other.iri);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.value)) {
        this.value = data().deepCopy(fields()[1].schema(), other.value);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
    }

    /**
     * Creates a Builder by copying an existing TermIRI instance
     * @param other The existing instance to copy.
     */
    private Builder(org.apache.jena.riot.avro.TermIRI other) {
      super(SCHEMA$);
      if (isValidValue(fields()[0], other.iri)) {
        this.iri = data().deepCopy(fields()[0].schema(), other.iri);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.value)) {
        this.value = data().deepCopy(fields()[1].schema(), other.value);
        fieldSetFlags()[1] = true;
      }
    }

    /**
      * Gets the value of the 'iri' field.
      * @return The value.
      */
    public org.apache.jena.riot.avro.Kind getIri() {
      return iri;
    }


    /**
      * Sets the value of the 'iri' field.
      * @param value The value of 'iri'.
      * @return This builder.
      */
    public org.apache.jena.riot.avro.TermIRI.Builder setIri(org.apache.jena.riot.avro.Kind value) {
      validate(fields()[0], value);
      this.iri = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'iri' field has been set.
      * @return True if the 'iri' field has been set, false otherwise.
      */
    public boolean hasIri() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'iri' field.
      * @return This builder.
      */
    public org.apache.jena.riot.avro.TermIRI.Builder clearIri() {
      iri = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'value' field.
      * @return The value.
      */
    public java.lang.CharSequence getValue() {
      return value;
    }


    /**
      * Sets the value of the 'value' field.
      * @param value The value of 'value'.
      * @return This builder.
      */
    public org.apache.jena.riot.avro.TermIRI.Builder setValue(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.value = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'value' field has been set.
      * @return True if the 'value' field has been set, false otherwise.
      */
    public boolean hasValue() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'value' field.
      * @return This builder.
      */
    public org.apache.jena.riot.avro.TermIRI.Builder clearValue() {
      value = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TermIRI build() {
      try {
        TermIRI record = new TermIRI();
        record.iri = fieldSetFlags()[0] ? this.iri : (org.apache.jena.riot.avro.Kind) defaultValue(fields()[0]);
        record.value = fieldSetFlags()[1] ? this.value : (java.lang.CharSequence) defaultValue(fields()[1]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<TermIRI>
    WRITER$ = (org.apache.avro.io.DatumWriter<TermIRI>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<TermIRI>
    READER$ = (org.apache.avro.io.DatumReader<TermIRI>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeEnum(this.iri.ordinal());

    out.writeString(this.value);

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.iri = org.apache.jena.riot.avro.Kind.values()[in.readEnum()];

      this.value = in.readString(this.value instanceof Utf8 ? (Utf8)this.value : null);

    } else {
      for (int i = 0; i < 2; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.iri = org.apache.jena.riot.avro.Kind.values()[in.readEnum()];
          break;

        case 1:
          this.value = in.readString(this.value instanceof Utf8 ? (Utf8)this.value : null);
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}










