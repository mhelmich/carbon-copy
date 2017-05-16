Distributed BufferCache or Carbon Copy
======================================

Distributed BufferCache is an in-memory, distributed data warehouse. It is designed to be the read-only backend to your transactional database!

## Table of Contents
- [Overview](#overview)
  - [Roadmap](#roadmap)
- [Architecture](#architecture)
- [Components](#components)
  - [Data Structures](#data-structures)
  - [Catalog](#catalog)
  - [Query Parsing](#query-parsing)
  - [Query Planning](#query-planning)
  - [Query Execution](#query-execution)
- [How to build it](#how-to-build-it)

## Overview

Distributed BufferCache is an in-memory data store designed to serve up data from your transactional data store.

* read-only distributed in-memory data store
* a more resilient redis cluster with complex data structures
* provides a consistent view on your data
* lock free ingest
* horizontally scalable & automatically distributed
* distributed SQL

### Roadmap

* joins!
* extend the catalog (attach indexes to tables, etc.)
* query optimization by using indexes
* capturing stats
* monitoring and logging
* management endpoints (creating tables, indexes, etc.)
* develop routing manager inside the cluster to forward queries to nodes with resident data ([this article](http://highscalability.com/blog/2012/8/20/the-performance-of-distributed-data-structures-running-on-a.html) illustrates the idea)
  * forward queries to data
  * develop distributed flavors of tables and indexes
    * tables can be sorted lists of GUIDs
    * indexes need more thinking :)


## Architecture

* short intro how [galaxy](https://github.com/puniverse/galaxy) works
* probably link the high scalability article
* explain the fanciness on top of galaxy (aka complex data structures)

## Components

Software components that are worth mentioning here.

### Data Structures

* lowest level of structured data
* serves as abstraction to not deal with galaxy concepts directly
* probably explain things on the example of a data block
* after that mention a bunch of other data structures and we're good
* right now the implicit design assumption is: one data structure is owned by exactly one node -- this can be changed however

### Catalog

* is the directory of everything living in dist-bc
* keeps track of all high-level data structures (tables, indexes, result sets, etc.)
* keeps track of owners of these data structures and is responsible for making routing decisions

### Query Parsing

* simple ANTLR parser that picks a SQL apart and feeds into dist-bc
* do I really need to explain how SQL parsing works?

### Query Planning

* this could be interesting in case I ever get around to implement less naive planners :)
* something like [this](http://cidrdb.org/cidr2017/papers/p9-leis-cidr17.pdf) for example

### Query Execution

* again not sure that's really necessary unless I build something fancy

## How to build it

run tests...you guessed it

`mvn test`

bake a fat jar

`mvn package`
