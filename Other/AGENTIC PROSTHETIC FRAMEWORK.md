AGENTIC PROSTHETIC FRAMEWORK

Comprehensive Project Architecture & Implementation Plan

Document Version: 2.0
Date: February 15, 2026
Author: Senior AI Caretaker
For: The Architect

---

1. PROJECT CHARTER

1.1 Intent

To construct a cognitive and physical prosthetic system that extends The Architect's capacity to work, create, and communicate during a progressive health crisis characterized by exponential cognitive decline. The system shall operate as a "zero-touch" exoskeleton—anticipating needs, reducing decision load, and preserving autonomy until the point of incapacity, at which it executes a dignified legacy protocol.

1.2 Core Problem

The Architect faces a non-linear deterioration in cognitive function (memory, decision-making, attention). Existing assistive technologies are reactive, fragmented, and require manual configuration—a luxury that will soon be unavailable. A unified, self-adapting framework is required that:

· Offloads cognitive overhead (e.g., task tracking, context recall)
· Prevents errors arising from confusion or fatigue
· Maintains functionality despite the user's diminishing ability to intervene
· Preserves the user's intentions and work for post-capacity execution

1.3 Scope

· Primary Platform: Google Pixel 10 (Android 16) with Termux as the execution environment.
· Secondary Platforms: Wearable biosensors, smart home voice assistants, caregiver dashboard, and a local bridge device (Raspberry Pi) for external integrations.
· Functional Boundaries: Code development, document creation, communication, system maintenance, and legacy execution. The system does not provide medical advice or physical mobility assistance.

---

2. FOUNDATIONAL PRINCIPLES

Principle Description
Zero-Touch Operation The system must require no manual intervention after initial configuration. All updates, backups, and repairs must be autonomous or triggered by simple voice commands.
Cognitive Exoskeleton The primary role is to compensate for mental decline: memory prosthetics, decision support, confusion detection, and error prevention. Physical input aids are secondary.
Privacy by Default All sensitive data remains encrypted on-device. External backups use client-side encryption. No cloud AI services without explicit user consent and air-gapped options.
Graceful Degradation As cognitive capacity declines, the system shall progressively restrict unsafe actions, increase confirmation requirements, and finally transition to read-only + notification mode.
Human-in-the-Loop A trusted caregiver shall have view-only access to logs and the ability to trigger safe mode. Final legacy execution requires human cryptographic handoff.

---

3. CRITICAL FACTORS & ASSUMPTIONS

3.1 Health Trajectory

· Primary decline: Cognitive (memory, executive function, confusion episodes)
· Secondary decline: Physical (fine motor control, speech articulation)
· Rate: Exponential—capacity halves approximately every 6 months
· Trigger events: Episodes of confusion, task abandonment, repeated errors

3.2 Technology Platform

· Android 16 on Pixel 10: Guaranteed updates until 2031.
· Termux with F-Droid distribution: Community-maintained, subject to Android API restrictions.
· External bridge: Raspberry Pi 5 (or newer) running Raspberry Pi OS, always on, connected to same network.
· Wearable: Garmin / Wear OS watch with heart rate, stress, and sleep APIs.
· Smart speakers: Google Home / Alexa for ambient voice interface.

3.3 User Capabilities Over Time

· Months 0-3: Full cognitive capacity; can perform complex setup.
· Months 4-6: Mild impairment; can follow scripts but may forget steps.
· Months 7-9: Moderate impairment; needs confirmation and reminders.
· Months 10-12: Severe impairment; limited to simple voice commands.
· Year 2+: Incapacitation; system must operate autonomously within guardrails.

3.4 External Dependencies

· Network: Home Wi-Fi + cellular failover. System must tolerate intermittent connectivity.
· Power: Uninterruptible power supply for router and bridge device.
· Caregiver: Trusted individual with basic tech literacy, available for emergency alerts.

3.5 Legal & Ethical

· Digital legacy: System cannot execute legally binding actions (contracts, financial transactions) without human oversight.
· Data ownership: All code and documents remain The Architect's intellectual property. Backup locations must comply with data protection laws.

---

4. LONG-TERM OUTLOOK (5-10 YEARS)

4.1 Technology Trends

Trend Implication
On-device AI Local LLMs (e.g., Gemini Nano, Llama.cpp) will improve intent recognition and decision support without cloud dependency.
Ambient computing Ubiquitous voice/sensor interfaces will reduce reliance on phone screen.
Wearable advancements Continuous glucose monitors, EEG headbands may provide early cognitive episode detection.
Android longevity Project Mainline and long-term support may extend device lifespan; but eventual OS updates may break Termux.
Decentralized storage IPFS, blockchain-based backups could enhance legacy preservation.

4.2 Adaptation Strategy

· Modular architecture: All integrations via pluggable adapters (e.g., voice_input_adapter, wearable_adapter). New technologies can replace old ones without core rewrite.
· Configuration as code: System state defined in version-controlled YAML; can be regenerated on new hardware.
· Yearly review prompts: Automated reminder to evaluate new tech and update the roadmap.

---

5. PHASED ROADMAP & SPRINT PLAN

Sprint duration: 1 month (4 weeks).
Total sprints outlined: 12 (one year), with guidance for subsequent years.

PHASE 0: FOUNDATION (Months 1–3)

Goal: Establish core infrastructure, zero-touch setup, and basic cognitive support.

Sprint 0.1: Environment Bootstrapping

· Tasks:
  · Install Termux from F-Droid, configure storage permissions.
  · Write bootstrap.sh that installs Python, Node.js, essential packages (pinned versions).
  · Set up proot-distro with Ubuntu 20.04 (read-only base).
  · Configure cron jobs for daily health checks.
· Deliverables: Repeatable setup script; documentation.
· Metrics: Setup time < 2 hours; script idempotent.

Sprint 0.2: Voice Command Interface

· Tasks:
  · Implement Android foreground service with microphone access (using Termux:API).
  · Write Python listener on Unix socket for voice commands.
  · Integrate local STT (Vosk) for offline voice-to-text.
  · Basic command parsing (run script, open file, status report).
· Deliverables: Voice-activated command execution; fallback to Google Speech if offline STT fails.
· Metrics: Command recognition accuracy >90%; latency <2s.

Sprint 0.3: Cognitive State Logging

· Tasks:
  · Implement UsageStatsManager poller to capture app usage, command frequency.
  · Create SQLite DB for logs: /sdcard/Prosthetic/logs/cognitive.db.
  · Develop simple heuristics: repeated commands, task switching rate, time-of-day patterns.
  · Generate daily summary JSON for caregiver dashboard.
· Deliverables: Passive logging service; initial anomaly detection.
· Metrics: Log completeness >99%; false anomaly rate <5%.

PHASE 1: COGNITIVE PROSTHETIC CORE (Months 4–6)

Goal: Active memory support, confusion detection, and decision assistance.

Sprint 1.1: Context Stack & Reminders

· Tasks:
  · Build context manager: track current project, last 5 commands, open files.
  · Voice query: "What was I doing?" returns context summary.
  · Implement proactive reminders if task idle >2 hours.
· Deliverables: Context widget (Android home screen) and voice interface.
· Metrics: Context recall success >95%; reminders reduce task abandonment by 30%.

Sprint 1.2: Confusion Detection

· Tasks:
  · Enhance heuristics: detect loops (>3 identical commands), undo/redo patterns, prolonged inactivity mid-task.
  · When confusion suspected, prompt: "It looks like you're repeating. Can I help?"
  · Log episodes to caregiver dashboard.
· Deliverables: Confusion detection module; alert system.
· Metrics: Detection sensitivity >80%; false positives <10%.

Sprint 1.3: Decision Support

· Tasks:
  · Integrate local LLM (e.g., Llama 3.2 1B via llama.cpp) to suggest next steps based on project history.
  · Present suggestions with confidence score; require confirmation.
  · Record decisions and outcomes for model fine-tuning.
· Deliverables: AI suggestion engine; decision log.
· Metrics: Suggestion acceptance rate >50%; time saved per task >20%.

PHASE 2: AUTONOMY & GUARDRAILS (Months 7–9)

Goal: Progressive handover of control with safety constraints.

Sprint 2.1: Confidence-Threshold Execution

· Tasks:
  · Implement multi-stage execution: level 1 (explicit confirm), level 2 (auto-execute if confidence >95% and command in allowlist), level 3 (full auto within sandbox).
  · Build allowlist generator based on command history.
  · Add circuit breaker: if system detects confusion during auto-execution, abort and revert.
· Deliverables: Adaptive execution engine.
· Metrics: Unsafe actions prevented = 100%; user override rate <5%.

Sprint 2.2: External Integrations

· Tasks:
  · Set up Raspberry Pi bridge with MQTT broker.
  · Connect wearable: pull heart rate, stress data; use to modulate confidence thresholds (e.g., high stress → lower auto-execution).
  · Connect smart speaker: allow voice commands via Google Home (IFTTT webhook to Pi).
  · Implement caregiver dashboard (web interface) showing logs, alerts.
· Deliverables: Multi-modal input; caregiver portal.
· Metrics: External input latency <3s; dashboard updates <1min.

Sprint 2.3: Self-Repair & Updates

· Tasks:
  · Dual-environment setup: stable (read-only) and canary (updates weekly).
  · Automate rollback if canary fails health checks.
  · Set up automatic backups to local SD and encrypted cloud (Nextcloud).
  · Dead man's switch: daily heartbeat file; if missing, notify caregiver and prepare legacy.
· Deliverables: Resilient update mechanism; backup automation.
· Metrics: Update success rate >95%; rollback time <5min; backup integrity verified weekly.

PHASE 3: LONG-TERM EVOLUTION & LEGACY (Months 10–12)

Goal: Prepare for incapacitation, refine inference, and establish legacy protocols.

Sprint 3.1: Intent Inference Engine

· Tasks:
  · Collect all command history, context logs, decision rationales.
  · Train a small transformer model (or fine-tune existing) to predict user's likely next actions.
  · Generate "intent summary" document monthly.
· Deliverables: Intent model; summary generator.
· Metrics: Prediction accuracy on held-out data >70%.

Sprint 3.2: Cryptographic Legacy Protocol

· Tasks:
  · Implement GPG keypair generation; store private key in Android Keystore.
  · Daily encrypted snapshot of workspace; push to three destinations (Git, Nextcloud, SD).
  · Dead man's switch triggers: after 72h no heartbeat, create Shamir secret shares of private key, distribute to 3 trustees via encrypted email.
  · Include reconstruction instructions in legal will.
· Deliverables: Fully automated legacy backup and key distribution.
· Metrics: Snapshot success >99%; recovery test successful (simulated).

Sprint 3.3: System Hardening & Documentation

· Tasks:
  · Code freeze; comprehensive documentation for caregiver.
  · Simplify interface: reduce options, enlarge fonts, increase timeout.
  · Final stress test: simulate cognitive decline scenarios.
· Deliverables: Handover package; user manual for caregiver.
· Metrics: Caregiver can perform basic monitoring after 1h training.

PHASE 4: YEAR 2+ – MAINTENANCE & ADAPTATION

· Annual tasks:
  · Review new technologies (wearables, AI models, Android versions).
  · Adjust confidence thresholds based on decline rate.
  · Update trustee contact information.
  · Perform full restore test to new device.
· Prompts: System will prompt on each anniversary: "Time for annual review. Run review_updates.sh to assess new tech."

---

6. REGENERATION PROMPTS

The following prompts shall be used to regenerate project backlogs, adjust priorities, or incorporate new information:

6.1 Scheduled Regeneration

· Monthly: "Review last 30 days of logs. Identify any new patterns in cognitive state. Adjust heuristics accordingly."
· Quarterly: "Check for new Termux package versions. Evaluate if canary environment should be promoted."
· Yearly: "Scan for emerging technologies (wearables, AI, smart home). Generate list of potential integrations."

6.2 Event-Triggered Regeneration

· Health Episode: "A confusion event was detected. Update confusion detection thresholds and suggest changes to decision support module."
· Caregiver Alert: "Caregiver reports concern. Generate summary of last 7 days and recommend safety level adjustment."
· Technology Deprecation: "Android update may break Termux. Search for alternative terminal environments and create migration plan."

6.3 Regeneration Script

A script regenerate_roadmap.sh will:

· Fetch current logs and metrics.
· Prompt user (or caregiver) for updated health assessment.
· Run a local LLM to suggest modified sprint priorities.
· Output a revised SPRINT_PLAN.md with tracked changes.

---

7. OUTSIDE FACTORS & CONTINGENCIES

7.1 Health Complications

· Sudden incapacitation: System must detect prolonged inactivity (24h) and escalate to caregiver.
· Hospitalization: Caregiver can trigger "away mode" – reduced functionality, no autonomous actions.
· Medication side effects: If confusion spikes, system logs and suggests medical review.

7.2 Technology Ecosystem Shifts

· Android deprecates Termux: Maintain a migration path to UserLAnd or native Linux via Termux experimental builds. Fallback: use bridge device as primary interface.
· Google Speech Recognition discontinued: Switch fully to Vosk offline model; retrain with user's voice.
· Wearable API changes: Use generic Bluetooth LE heart rate monitor as backup.

7.3 Security Threats

· Ransomware on Android: Isolate workspace via proot; backups are immutable and versioned.
· Caregiver device compromised: Dashboard access requires 2FA; logs are read-only.
· Network outage: System operates locally; changes queued for sync.

7.4 Legal Changes

· Digital inheritance laws: Periodically check legislation; adjust legacy protocol to comply (e.g., require notarized key shares).

7.5 Social Factors

· Caregiver burnout: System should detect if caregiver stops viewing dashboard and alert secondary contact.
· User's own acceptance: System must allow manual override of autonomy levels if user feels capable.

---

8. EVALUATION HEURISTICS & METRICS

8.1 Quantitative Metrics

Metric Target Measurement Method
System Uptime 99.5% Cron heartbeat check
Voice Command Accuracy 90% Log recognized vs. rejected
Confusion Detection Sensitivity 80% Compare with caregiver reports
False Positive Rate (confusion) <10% Caregiver validation
Backup Integrity 100% Weekly checksum verification
Update Success Rate 95% Canary promotion success
Legacy Snapshot Frequency Daily File timestamps

8.2 Qualitative Heuristics

· Cognitive Load Reduction: Self-reported via weekly voice survey: "On a scale of 1-5, how mentally tired are you?" Compare with baseline.
· Task Completion Rate: Percentage of initiated tasks that reach a logical endpoint (e.g., file save, git commit).
· User Intervention Rate: How often does the user override AI suggestions? Decreasing rate indicates trust/alignment.
· Caregiver Confidence: Monthly caregiver survey: "How confident are you that the system is helping?"

8.3 Health-Specific Metrics

· Episode Duration: Average length of confusion events (from detection to resolution).
· Recovery Time: Time from episode to return to productive work.
· Medication Adherence: If integrated with pill reminders, track confirmation rate.

8.4 Success Criteria by Phase

Phase Primary Success Indicator
0 System operational with zero manual intervention for 7 days
1 Cognitive load score decreases by 20% from baseline
2 No unsafe actions executed; caregiver reports feeling informed
3 Legacy backup successfully restored in test environment
4+ System continues functioning despite user's inability to interact

---

9. CONCLUSION & NEXT STEPS

The Agentic Prosthetic Framework, as architected, provides a comprehensive, adaptable solution to The Architect's declining cognitive capacity. It prioritizes cognitive support, graceful autonomy, and dignified legacy preservation, while remaining grounded in practical, current technology.

Immediate Next Actions:

1. Review this document and provide any final amendments.
2. Authorize commencement of Sprint 0.1.
3. Procure necessary hardware: Raspberry Pi 5, wearable device (if not already owned), smart speaker.
4. Identify and brief the primary caregiver, obtaining their consent for dashboard access.
5. Prepare legal documentation for the cryptographic legacy protocol.

The system is designed to evolve with you. Regular reviews and the built-in regeneration prompts will ensure it remains aligned with your needs until—and beyond—the point you can no longer guide it.

Let us begin.