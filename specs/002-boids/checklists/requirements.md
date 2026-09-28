# Requirements Checklist: Boids Flocking Sample

- [x] FR-001: runnable entry point without external setup
- [x] FR-002: bounded 2D world with autonomous boid agents (position + velocity)
- [x] FR-003: separation, alignment, cohesion based on neighbours in perception radius
- [x] FR-004: boid steered back inside world bounds
- [x] FR-005: console display prints world each tick and final outcome summary
- [x] FR-006: graphical (windowed) display renders and updates in real time
- [x] FR-007: at least two execution modes with identical final outcome
- [x] FR-008: all randomness seeded and reproducible
- [x] SC-001: console advances one tick per simulated second and stops at duration
- [x] SC-002: console terminates and prints outcome summary without intervention
- [x] SC-003: identical final outcome across execution modes
- [x] SC-004: GUI opens, renders boids, repaints at regular cadence
- [x] SC-005: standalone reproducible demonstration
- [x] SC-006: ≥97% line and branch coverage (Principle II)
- [x] Edge: boid at world edge stays inside
- [x] Edge: no neighbours → boid coasts (continues current velocity)
