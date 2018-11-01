package com.iota.iri;

import static com.iota.iri.controllers.TransactionViewModel.VALUE_TRINARY_OFFSET;
import static com.iota.iri.controllers.TransactionViewModel.VALUE_TRINARY_SIZE;
import static com.iota.iri.controllers.TransactionViewModel.VALUE_USABLE_TRINARY_SIZE;

import java.util.Objects;

import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.hash.Curl;
import com.iota.iri.hash.SpongeFactory;
import com.iota.iri.model.Hash;

/**
 * @author Fabrizio Spataro <fabryprog@gmail.com>
 */
public class TransactionValidatorHelperDPOW {
    private static long MAX_TIMESTAMP_FUTURE = 2 * 60 * 60;
    private static long MAX_TIMESTAMP_FUTURE_MS = MAX_TIMESTAMP_FUTURE * 1000;
    
    
    private boolean hasInvalidTimestamp(TransactionViewModel transactionViewModel, Long snapshotTimestamp) {
    	final Long snapshotTimestampMs = snapshotTimestamp * 1000;
    	
        if (transactionViewModel.getAttachmentTimestamp() == 0) {
            return transactionViewModel.getTimestamp() < snapshotTimestamp && !Objects.equals(transactionViewModel.getHash(), Hash.NULL_HASH)
                    || transactionViewModel.getTimestamp() > (System.currentTimeMillis() / 1000) + MAX_TIMESTAMP_FUTURE;
        }
        return transactionViewModel.getAttachmentTimestamp() < snapshotTimestampMs
                || transactionViewModel.getAttachmentTimestamp() > System.currentTimeMillis() + MAX_TIMESTAMP_FUTURE_MS;
    }

    public void runValidation(TransactionViewModel transactionViewModel, final int minWeightMagnitude, Long snapshotTimestamp) {
        transactionViewModel.setMetadata();
        transactionViewModel.setAttachmentData();
        if(hasInvalidTimestamp(transactionViewModel, snapshotTimestamp)) {
            throw new StaleTimestampException("Invalid transaction timestamp.");
        }
        for (int i = VALUE_TRINARY_OFFSET + VALUE_USABLE_TRINARY_SIZE; i < VALUE_TRINARY_OFFSET + VALUE_TRINARY_SIZE; i++) {
            if (transactionViewModel.trits()[i] != 0) {
                throw new RuntimeException("Invalid transaction value");
            }
        }

        int weightMagnitude = transactionViewModel.weightMagnitude;
        if(weightMagnitude < minWeightMagnitude) {
            throw new RuntimeException("Invalid transaction hash");
        }

        if (transactionViewModel.value() != 0 && transactionViewModel.getAddressHash().trits()[Curl.HASH_LENGTH - 1] != 0) {
            throw new RuntimeException("Invalid transaction address");
        }
    }

    public TransactionViewModel validateTrits(final byte[] trits, int minWeightMagnitude, Long snapshotTimestamp) {
        TransactionViewModel transactionViewModel = new TransactionViewModel(trits, Hash.calculate(trits, 0, trits.length, SpongeFactory.create(SpongeFactory.Mode.CURLP81)));
        runValidation(transactionViewModel, minWeightMagnitude, snapshotTimestamp);
        return transactionViewModel;
    }

    public static class StaleTimestampException extends RuntimeException {
        public StaleTimestampException (String message) {
            super(message);
        }
    }
}
