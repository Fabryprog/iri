/**
 * 
 */
package com.iota.iri.dpow;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;

import com.iota.iri.hash.PearlDiver;

/**
 * @author Fabrizio Spataro <fabryprog@gmail.com>
 *
 */
public class DistribuitedPOWTask implements Callable<Boolean>, Serializable {
    private static final long serialVersionUID = 8806321992274553604L;

	private byte[] transactionTrits;
	private Integer minWeightMagnitude;

	private DistribuitedPOWTask() {
	}

	public DistribuitedPOWTask(final byte[] transactionTrits, final Integer minWeightMagnitude) {
		this.transactionTrits = transactionTrits;
		this.minWeightMagnitude = minWeightMagnitude;
		System.out.println("<<<< INIT DONE! >>>>");
	}

	public Boolean call() {
		System.out.println("<<<< DISTRIBUITED PoW START! >>>>");
		long ts = Calendar.getInstance().getTimeInMillis();
		
		PearlDiver pearlDiver = new PearlDiver();
		Boolean res = pearlDiver.search(transactionTrits, minWeightMagnitude, 0);

		System.out.println("<<<< DISTRIBUITED PoW END! " + (Calendar.getInstance().getTimeInMillis() - ts) + " ms >>>>");

		return res;
	}

	public byte[] getTransactionTrits() {
		return transactionTrits;
	}

	public void setTransactionTrits(byte[] transactionTrits) {
		this.transactionTrits = transactionTrits;
	}

	public Integer getMinWeightMagnitude() {
		return minWeightMagnitude;
	}

	public void setMinWeightMagnitude(Integer minWeightMagnitude) {
		this.minWeightMagnitude = minWeightMagnitude;
	}

}