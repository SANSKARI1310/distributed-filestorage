Distributed Multi-Tenant File Storage Platform
A production-oriented distributed file-storage platform built to explore
multi-tenancy, object storage, reactive request routing, tenant
fairness, resilience, and distributed ownership.

The project separates file metadata from file bytes, routes API traffic
through a reactive gateway to multiple backend instances, and uses
controlled experiments to study performance, noisy-neighbour effects,
failure recovery, and consistency.
Project status: Core file-storage functionality and the separate
gateway/multi-node setup have been implemented. Phase 3 observability
and fairness infrastructure has been prepared, with final benchmark
runs intentionally deferred. Current development is focused on Phase
4 --- Resilience and Failure Experiments.

Project goals
- Build a multi-tenant file platform with organization-based
  read/write authorization.
- Store file metadata in PostgreSQL and file bytes in S3-compatible
  object storage.
- Route requests through a reactive gateway to multiple backend
  instances.
- Measure throughput, tail latency, resource usage, and
  noisy-neighbour effects.
- Evaluate tenant-aware admission and scheduling using a weighted fair
  scheduler.
- Test resilience under backend-node and dependency failures.
- Explore metadata/data separation and metadata scalability.
- Explore Redis-backed ownership coordination and fencing against
  stale-owner writes.
- Produce reproducible experiments, architecture documentation, and
  interview-ready design explanations.


