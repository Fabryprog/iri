package com.iota.iri.dpow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.Member;

public class HazelcastInstanceReconnectorTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(HazelcastInstanceReconnectorTask.class);

	private com.hazelcast.config.Config config = null;
	
	public HazelcastInstanceReconnectorTask(com.hazelcast.config.Config config) {
		this.config = config;
	}
	
	private void restart() {
		log.info("<< Cluster Restarting >>>");
		
		Hazelcast.getHazelcastInstanceByName("IRI").shutdown();
		
		Hazelcast.newHazelcastInstance(config);
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				Thread.currentThread().sleep(30000);
				
				log.info("Checking Cluster status [Cluster members: " + Hazelcast.getHazelcastInstanceByName("IRI").getCluster().getMembers().size() + " ]");
				
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