# Matchmaker Paxos

State machine replication protocols, like MultiPaxos and Raft, are at the heart
of nearly every strongly consistent distributed database. To tolerate machine
failures, these protocols must replace failed machines with live machines, a
process known as reconfiguration. Reconfiguration has become increasingly
important over time as the need for frequent reconfiguration has grown. Despite
this, reconfiguration has largely been neglected in the literature. In this
paper, we present Matchmaker Paxos and Matchmaker MultiPaxos, a reconfigurable
consensus and state machine replication protocol respectively. Our protocols
can perform a reconfiguration with little to no impact on the latency or
throughput of command processing; they can perform a reconfiguration in one
round trip (theoretically) and a few milliseconds (empirically); they provide a
number of theoretical insights; and they present a framework that can be
generalized to other replication protocols in a way that previous
reconfiguration techniques can not. We provide proofs of correctness for the
protocols and optimizations, and present empirical results from an open source
implementation.

For instructions on how to build and execute this code, refer
[here](https://github.com/mwhittaker/frankenpaxos).
