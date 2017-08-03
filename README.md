Carbon Copy
===========

Carbon Copy is a distributed, SQL-compliant in-memory cache. It is designed to be your caching solution that speaks SQL. A carbon copy of your transactional database.

## Table of Contents
- [Overview](#overview)
  - [Roadmap](#roadmap)
- [Architecture](#architecture)
- [Components](#components)
  - [Data Structures](#data-structures)
  - [Catalog](#catalog)
  - [Query Execution](#query-execution)
- [How to build it](#how-to-build-it)

## Overview

Carbon Copy is an in-memory data store designed to serve up data from your transactional data store.

* read-only distributed in-memory data store
* gets fed by a Kafka event log
* a more resilient redis cluster with complex data structures
* provides a consistent view on data
* (almost) lock free ingest
* horizontally scalable & automatically distributed
* distributed SQL

### Roadmap

* extend the catalog (attach indexes to tables, etc.)
* query optimization by using indexes
* capturing stats
  * see [this paper](http://cidrdb.org/cidr2017/papers/p9-leis-cidr17.pdf) for ideas around gathering stats and data sampling
* monitoring and logging
* management endpoints (creating tables, indexes, etc.)
* distributed data structures
  * distributed BTrees see [this paper](http://www.vldb.org/pvldb/1/1453922.pdf)
* develop routing manager inside the cluster to forward queries to nodes with resident data ([this article](http://highscalability.com/blog/2012/8/20/the-performance-of-distributed-data-structures-running-on-a.html) illustrates the idea)
  * forward queries to data
  * develop distributed flavors of tables and indexes
    * tables can be sorted or hashed lists of GUIDs
    * indexes need more thinking :)


## Architecture

Carbon Copy provides the illusion of a relation database that is completely kept in-memory. It is based on the excellent [Galaxy framework](https://github.com/puniverse/galaxy). Galaxy does most of the heavy-lifting for Carbon Copy when it comes to coherence of data. In a nutshell, Galaxy takes processor cache line coherence protocols and implements them for distributed machines. Details about Galaxy can be found in its [documentation](http://docs.paralleluniverse.co/galaxy/). Carbon Copy builds the resemblance of complex data structures (such as tables and indexes) around the guarantees Galaxy provides.
In order to provide a JDBC interface for seamless replacement of a traditional database, it's using [Apache Calcite](https://calcite.apache.org/). Calcite comes provides a JDBC interface and an implementation of a query planner and optimizer. Carbon Copy implements Calcite interfaces and manages data access for Calcite.

## Components

Carbon Copy breaks down into the following (noteworthy) components

### Data Structures

Data structures are bootstrapped from what Galaxy calls cache lines. Those are ordinary and raw byte arrays. On top of those Carbon Copy builds so called DataBlocks. DataBlocks are the lowest level of structured data (a collection of everything that has a name and be stuffed into a byte array). Internally DataBlocks are a linked list of key-value-pairs. The key is the identity of a datum. The datum itself needs to be serializable to a byte array. These DataBlocks serve as an abstraction to Galaxy (by not requiring every data structure to deal with Galaxy primitives directly) and are the fundamental building block of all higher level data structures.

On top of these DataBlocks, the other data structures were built:

* regular hash maps
  * undust your Sedgewick -- it's all there :)

* distributed hash maps
  * do [rendezvous hashing](http://www.eecs.umich.edu/techreports/cse/96/CSE-TR-316-96.pdf) to assign a key to a node
  * utilize message passing to route gets and puts to the correct node
  * maintain their own hashes on each node
  * as routing decisions can be taken decentralized only the DistributedHash knows how to route requests

* BTree
  * again look up your Sedgewick

### Catalog

* is the directory of everything living in carbon copy
* keeps track of all high-level data structures (tables, indexes, result sets, etc.)
* keeps track of owners of these data structures and is responsible for making routing decisions
  * as far as the motivation for query forwarding goes read [this blog post](http://highscalability.com/blog/2012/8/20/the-performance-of-distributed-data-structures-running-on-a.html)

### Query Execution

All the heavy lifting of SQL parsing and optimizing is done by Apache Calcite. We're merely implementing their interfaces and feed their optimizer with data. A relatively optimized SQL operation then hits our API and we take it from there. Calcite doesn't know anything about distributed data structures or data placement in the cluster. We have to do all the work of finding out where data resides and what the smartest way to get it is. Implementations to Calcites interfaces and the carbon copy implementation of their API can be found in [this package](carbon-copy-service/src/main/java/org/carbon/copy/calcite).

### Intra-Node Transaction Management

Galaxy maintains transactions between nodes in the cluster and makes sure only one node can make updates to a particular block. What galaxy doesn't do is maintaining transactions between different threads on the same node. This behavior manifests a little oddly (mostly by "this line wasn't pinned" exceptions). They argue with increased flexibility if multiple threads can share a transaction (and honestly their code gets easier if they have to do less bookkeeping).
That leaves it to us to implement some basic way of transaction isolation just to make sure different "transactions" don't trample all over each other.

Bookkeeping of cache lines on galaxy side happens once per line. Meaning if different transactions pin the same block. There's only a record showing this block was pinned once. Committing transactions releases the cache lines question and the second transaction committing is left in the rain.

Pretty much all of the transaction business happens in the TxnManager (as it should). The [TxnManager](carbon-copy-service/src/main/java/org/carbon/copy/data/structures/TxnManagerImpl.java) deals with local and remote locks and pinning. Local locks refer to locks that prevent different threads inside a node to trample on each other. Remote locks prevents different nodes inside the cluster to trample on each other. The notion was introduced as Galaxy only supports remote locks out of the box. Internal transactions boil down to a lock mutex that is being created on a per block basis. Each thread needs to acquire a local lock and remote lock in order to modify a DataBlock.

The behavior is similar to a SQL query where the session waits until locks for all blocks in questions were acquired.

## How to build it

run tests...you guessed it

`mvn test`

bake a fat (shaded) jar

`mvn package`
