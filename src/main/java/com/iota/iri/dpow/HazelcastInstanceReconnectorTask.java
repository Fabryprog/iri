package com.iota.iri.dpow;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.Member;

public class HazelcastInstanceReconnectorTask implements Runnable {
	
	private com.hazelcast.config.Config config = null;
	
	public HazelcastInstanceReconnectorTask(com.hazelcast.config.Config config) {
		this.config = config;
	}
	
	private void restart() {
		Hazelcast.getHazelcastInstanceByName("IRI").shutdown();
		
		Hazelcast.newHazelcastInstance(config);
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				Thread.currentThread().sleep(30000);
				
				boolean connectedToMainMember = false; 
				for(Member m : Hazelcast.getHazelcastInstanceByName("IRI").getCluster().getMembers()) {
					if(!m.isLiteMember()) {
						connectedToMainMember = true;
					}
				}
				
				if(!connectedToMainMember) {
					restart();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}