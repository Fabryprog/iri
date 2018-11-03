package com.iota.iri.model;

import com.iota.iri.hash.Curl;
import com.iota.iri.hash.Sponge;
import com.iota.iri.hash.SpongeFactory;
import com.iota.iri.storage.Indexable;
import com.iota.iri.utils.Converter;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

public final class Hash implements Serializable, Indexable, HashId {
	private static final long serialVersionUID = -5626778977648890707L;
	
	public static final Integer SIZE_IN_TRITS = 243;
    public static final Integer SIZE_IN_BYTES = 49;

    public static final Hash NULL_HASH = new Hash(new byte[Curl.HASH_LENGTH]);

    private final String lock = new String();
    private ByteSafe byteSafe;
    private TritSafe tritSafe;

    private final class ByteSafe implements Serializable {
    	private static final long serialVersionUID = -2355588169697768116L;

		private final Byte[] bytes;
        private final Integer hashcode;

        private ByteSafe(byte[] bytes) {
            Objects.requireNonNull(bytes, "ByteSafe is attempted to be initialized with a null byte array");
            this.bytes = ArrayUtils.toObject(bytes);
            this.hashcode = Arrays.hashCode(bytes);
        }
    }

    private final class TritSafe implements Serializable {
		private static final long serialVersionUID = -5135400307221688762L;

		private Byte[] trits;

        private TritSafe(byte[] trits) {
            this.trits = ArrayUtils.toObject(Objects.requireNonNull(trits, "TritSafe is attempted to be initialized with a null int array"));
        }
    }


    public static Hash calculate(SpongeFactory.Mode mode, byte[] trits) {
        return calculate(trits, 0, trits.length, SpongeFactory.create(mode));
    }

    public static Hash calculate(byte[] bytes, int tritsLength, final Sponge curl) {
        byte[] trits = new byte[tritsLength];
        Converter.getTrits(bytes, trits);
        return calculate(trits, 0, tritsLength, curl);
    }

    public static Hash calculate(final byte[] tritsToCalculate, int offset, int length, final Sponge curl) {
        byte[] hashTrits = new byte[SIZE_IN_TRITS];
        curl.reset();
        curl.absorb(tritsToCalculate, offset, length);
        curl.squeeze(hashTrits, 0, SIZE_IN_TRITS);
        return new Hash(hashTrits);
    }

    public Hash() {
    }

    public Hash(final byte[] source, final int sourceOffset, final int sourceSize) {
        if(sourceSize < SIZE_IN_TRITS) {
            byte[] dest = new byte[SIZE_IN_BYTES];
            System.arraycopy(source, sourceOffset, dest, 0, sourceSize - sourceOffset > source.length ? source.length - sourceOffset : sourceSize);
            this.byteSafe = new ByteSafe(dest);
        } else {
            byte[] dest = new byte[SIZE_IN_TRITS];
            System.arraycopy(source, sourceOffset, dest, 0, dest.length);
            this.tritSafe = new TritSafe(dest);
        }
    }

    public Hash(final byte[] bytes) {
        this(bytes, 0, bytes.length == 243 ? SIZE_IN_TRITS : SIZE_IN_BYTES);
    }

    public Hash(final byte[] trits, final int offset) {
        this(trits, offset, SIZE_IN_TRITS);
    }

    public Hash(final String trytes) {
        this.tritSafe = new TritSafe(new byte[SIZE_IN_TRITS]);
        byte[] t = new byte[this.tritSafe.trits.length];
        
        Converter.trits(trytes, t, 0);
        
        this.tritSafe.trits = ArrayUtils.toObject(t);
    }

    private void fullRead(byte[] src) {
        if (src != null) {
            synchronized (lock) {
                if (byteSafe != null || tritSafe != null) {
                    throw new IllegalStateException("I cannot be initialized with data twice.");
                }
                byte[] dest = new byte[SIZE_IN_BYTES];
                System.arraycopy(src, 0, dest, 0, Math.min(dest.length, src.length));
                byteSafe = new ByteSafe(dest);
            }
        }
    }

    public int trailingZeros() {
        final byte[] trits = trits();
        int index = SIZE_IN_TRITS;
        int zeros = 0;
        while (index-- > 0 && trits[index] == 0) {
            zeros++;
        }
        return zeros;
    }

    public byte[] trits() {
        TritSafe safe = tritSafe;
        if (safe == null) {
            synchronized (lock) {
                if (tritSafe == null) {
                    Objects.requireNonNull(byteSafe, "I need my bytes to be initialized in order to construct trits.");
                    byte[] src = bytes();
                    byte[] dest = new byte[Curl.HASH_LENGTH];
                    Converter.getTrits(src, dest);
                    tritSafe = new TritSafe(dest);
                }
                safe = tritSafe;
            }
        }
        return ArrayUtils.toPrimitive(safe.trits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Hash hash = (Hash) o;
        return Arrays.equals(bytes(), hash.bytes());
    }

    @Override
    public int hashCode() {
        bytes();
        return byteSafe.hashcode;
    }

    @Override
    public String toString() {
        return Converter.trytes(trits());
    }

    public byte[] bytes() {
        ByteSafe safe = byteSafe;
        if (safe == null) {
            synchronized (lock) {
                if (byteSafe == null) {
                    Objects.requireNonNull(tritSafe, "I need my trits to be initialized in order to construct bytes.");
                    byte[] src = trits();
                    byte[] dest = new byte[SIZE_IN_BYTES];
                    Converter.bytes(src, 0, dest, 0, src.length);
                    byteSafe = new ByteSafe(dest);
                }
                safe = byteSafe;
            }
        }
        return ArrayUtils.toPrimitive(safe.bytes);
    }


    @Override
    public void read(byte[] src) {
        fullRead(src);
    }

    @Override
    public Indexable incremented() {
        return null;
    }

    @Override
    public Indexable decremented() {
        return null;
    }

    @Override
    public int compareTo(Indexable indexable) {
        Hash hash = (indexable instanceof Hash) ? (Hash) indexable : new Hash(indexable.bytes());
        if (this.equals(hash)) {
            return 0;
        }
        long diff = Converter.longValue(hash.trits(), 0, SIZE_IN_TRITS) - Converter.longValue(trits(), 0, SIZE_IN_TRITS);
        if (Math.abs(diff) > Integer.MAX_VALUE) {
            return diff > 0L ? Integer.MAX_VALUE : Integer.MIN_VALUE + 1;
        }
        return (int) diff;
    }

	public ByteSafe getByteSafe() {
		return byteSafe;
	}

	public void setByteSafe(ByteSafe byteSafe) {
		this.byteSafe = byteSafe;
	}

	public TritSafe getTritSafe() {
		return tritSafe;
	}

	public void setTritSafe(TritSafe tritSafe) {
		this.tritSafe = tritSafe;
	}
}