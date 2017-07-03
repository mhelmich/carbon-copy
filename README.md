Carbon Copy
===========

Carbon Copy is an in-memory, distributed data warehouse. It is designed to be the read-only backend to your transactional database!

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
* a more resilient redis cluster with complex data structures
* provides a consistent view on your data
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
In order to provide a JDBC interface for seamless replacement of a traditional database, it's using [Apache Calcite](https://calcite.apache.org/).

## Components

Carbon Copy breaks down into the following (noteworthy) components

### Data Structures

* built on top of Galaxys raw byte arrays
* lowest level of structured data
* serves as abstraction to not deal with galaxy concepts directly
* a data block for example is a linked list of key-value-pairs that is serialized into a byte array
* on top of data blocks more complicated data structures are being built (such as hashes and BTrees)
* right now the implicit design assumption is: one data structure is owned by exactly one node -- this can be changed however

### Catalog

* is the directory of everything living in carbon copy
* keeps track of all high-level data structures (tables, indexes, result sets, etc.)
* keeps track of owners of these data structures and is responsible for making routing decisions
  * as far as the motivation for query forwarding goes read [this blog post](http://highscalability.com/blog/2012/8/20/the-performance-of-distributed-data-structures-running-on-a.html)

### Query Execution

* not sure that's really necessary unless I build query partitioning and forwarding

## How to build it

run tests...you guessed it

`mvn test`

bake a fat jar

`mvn package`
