Distributed BufferCache
=======================

[TOC levels=2,3]: # "Table of Contents"

# Table of Contents
- [Overview](#overview)
  - [Roadmap](#roadmap)
- [Architecture](#architecture)
- [Components](#components)
  - [Data Structures](#data-structures)
  - [Query Parsing](#query-parsing)
  - [Query Planning](#query-planning)
  - [Query Execution](#query-execution)
- [How to build it](#how-to-build-it)

## Overview

* read-only distributed in-memory data store
* a more resilient redis cluster with complex data structures

### Roadmap

* joins!
* query optimization by using indexes
* capturing stats
* monitoring and logging
* management endpoints (creating tables, indexes, etc.)
* develop routing manager inside the cluster to forward queries to nodes with resident data ([this article](http://highscalability.com/blog/2012/8/20/the-performance-of-distributed-data-structures-running-on-a.html) illustrates the idea)

## Architecture

* short intro how galaxy works
* probably link the high scalability article
* explain your fanciness on top of galaxy (aka complex data structures)

## Components

Software components that are worth mentioning here.

### Data Structures

* lowest level of structured data
* serves as abstraction to not deal with galaxy concepts directly
* probably explain things on the example of a data block
* after that mention a bunch of other data structures and we're good

### Query Parsing

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
