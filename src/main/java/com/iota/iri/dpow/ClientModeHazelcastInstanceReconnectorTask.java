package com.iota.iri.dpow;

import com.hazelcast.core.Hazelcast;

public class ClientModeHazelcastInstanceReconnectorTask implements Runnable {
	
	private com.hazelcast.config.Config config = null;
	
	public ClientModeHazelcastInstanceReconnectorTask(com.hazelcast.config.Config config) {
		this.config = config;
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				Thread.currentThread().sleep(30000);

				if(Hazelcast.getHazelcastInstanceByName("IRI").getCluster().getMembers().size() < 2) {
					Hazelcast.getHazelcastInstanceByName("IRI").shutdown();
					
					Hazelcast.newHazelcastInstance(config);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}