# Distribuited PoW

## The project

This improvements enable iri to delegate the Proof of Work tasks.

## The Architecture

There are two actors:
 - 1 Main server 
 - N Worker

All actors are in the same hazelcast cluster. 

Main server execute "attachToTangle" task using Hazelcast Distribuited Task and delegate it into cluster.

Note: main server can be also a worker!

## Main purpose:

PoW task is the bottleneck and it is very important to whole vps performance. My solution help main server to distribute heavy tasks to the cluster.

## How to start it

The feature is into main iri jar. I added some settings:

 - [Server Side]
   - dpow enable flag
   - public server address
   - cluster group is
   - cluster password
 - [Client Side]
   - dpow enable flag
   - remote server address

## FAQ

### Main server how to delegate the PoW?

Main server execute PoW method via Hazelcast Distribuited Task send a signal to cluster. There are any methods to select the right worker. I used default method.

### Distribuited PoW executed by one or more workers?

Rate is 1 PoW : 1 worker

### Can you explain the performances?

Initial tests was good.

Now i am working to hazelcast cluster settings to optimize memory load.


## Node cmd line settings

To enable dPoW adding this args:

``` 
--distribuited-pow --public-address-pow fabryprog-iota.eye.rs 
```

## Worker cmd line settings

Execute IRI only with this args:

```
--distribuited-pow --server-pow fabryprog-iota.eye.rs
```

