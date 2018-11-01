/**
 * 
 */
package com.iota.iri.dpow;

import java.io.Serializable;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.iota.iri.TransactionValidator;
import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.hash.PearlDiver;
import com.iota.iri.model.Hash;
import com.iota.iri.service.API;
import com.iota.iri.utils.Converter;

/**
 * @author Fabrizio Spataro <fabryprog@gmail.com>
 *
 */
public class DistribuitedPOWTask implements Callable<List<String>>, Serializable, HazelcastInstanceAware {
    private static final Logger log = LoggerFactory.getLogger(DistribuitedPOWTask.class);

    private static final long serialVersionUID = 8806321992274553604L;

	private TransactionValidator transactionValidator;
	private Hash trunkTransaction;
	private Hash branchTransaction;
	private int minWeightMagnitude;
	private List<String> trytes;
	
	private transient HazelcastInstance hazelcastInstance;

	private DistribuitedPOWTask() {
	}

	public DistribuitedPOWTask(final TransactionValidator transactionValidator, final Hash trunkTransaction, final Hash branchTransaction, final int minWeightMagnitude,
			final List<String> trytes) {
		this.trunkTransaction = trunkTransaction;
		this.branchTransaction = branchTransaction;
		this.minWeightMagnitude = minWeightMagnitude;
		this.trytes = trytes;
		
		log.info("<<<< INIT DONE! >>>>");
	}

	public List<String> call() {
		log.info("<<<< DISTRIBUITED PoW START! >>>>");
		long ts = Calendar.getInstance().getTimeInMillis();
		
		final List<TransactionViewModel> transactionViewModels = new LinkedList<>();

		Hash prevTransaction = null;
		PearlDiver pearlDiver = new PearlDiver();

		byte[] transactionTrits = Converter.allocateTritsForTrytes(API.TRYTES_SIZE);

		for (final String tryte : trytes) {
			long timestamp = System.currentTimeMillis();
			try {
				Converter.trits(tryte, transactionTrits, 0);
				// branch and trunk
				System.arraycopy((prevTransaction == null ? trunkTransaction : prevTransaction).trits(), 0,
						transactionTrits, TransactionViewModel.TRUNK_TRANSACTION_TRINARY_OFFSET,
						TransactionViewModel.TRUNK_TRANSACTION_TRINARY_SIZE);
				System.arraycopy((prevTransaction == null ? branchTransaction : trunkTransaction).trits(), 0,
						transactionTrits, TransactionViewModel.BRANCH_TRANSACTION_TRINARY_OFFSET,
						TransactionViewModel.BRANCH_TRANSACTION_TRINARY_SIZE);

				// attachment fields: tag and timestamps
				// tag - copy the obsolete tag to the attachment tag field only if tag isn't
				// set.
				if (IntStream
						.range(TransactionViewModel.TAG_TRINARY_OFFSET,
								TransactionViewModel.TAG_TRINARY_OFFSET + TransactionViewModel.TAG_TRINARY_SIZE)
						.allMatch(idx -> transactionTrits[idx] == ((byte) 0))) {
					System.arraycopy(transactionTrits, TransactionViewModel.OBSOLETE_TAG_TRINARY_OFFSET,
							transactionTrits, TransactionViewModel.TAG_TRINARY_OFFSET,
							TransactionViewModel.TAG_TRINARY_SIZE);
				}

				Converter.copyTrits(timestamp, transactionTrits,
						TransactionViewModel.ATTACHMENT_TIMESTAMP_TRINARY_OFFSET,
						TransactionViewModel.ATTACHMENT_TIMESTAMP_TRINARY_SIZE);
				Converter.copyTrits(0, transactionTrits,
						TransactionViewModel.ATTACHMENT_TIMESTAMP_LOWER_BOUND_TRINARY_OFFSET,
						TransactionViewModel.ATTACHMENT_TIMESTAMP_LOWER_BOUND_TRINARY_SIZE);
				Converter.copyTrits(API.MAX_TIMESTAMP_VALUE, transactionTrits,
						TransactionViewModel.ATTACHMENT_TIMESTAMP_UPPER_BOUND_TRINARY_OFFSET,
						TransactionViewModel.ATTACHMENT_TIMESTAMP_UPPER_BOUND_TRINARY_SIZE);

				if (!pearlDiver.search(transactionTrits, minWeightMagnitude, 0)) {
					transactionViewModels.clear();
					break;
				}
				// validate PoW - throws exception if invalid
				final TransactionViewModel transactionViewModel = transactionValidator
						.validateTrits(transactionTrits, transactionValidator.getMinWeightMagnitude());

				transactionViewModels.add(transactionViewModel);
				prevTransaction = transactionViewModel.getHash();
			} catch(Exception e) {} 
		} //for

		final List<String> elements = new LinkedList<>();
		for (int i = transactionViewModels.size(); i-- > 0;) {
			elements.add(Converter.trytes(transactionViewModels.get(i).trits()));
		}

		log.info("<<<< DISTRIBUITED PoW END! " + (Calendar.getInstance().getTimeInMillis() - ts) + " ms >>>>");

		return elements;
	}
	
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}
}