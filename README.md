# GitHub Setup – The Postural Interview

## README.md – Advanced Research-Oriented Version

````markdown
# The Postural Interview

AI-powered Human-Robot Interaction system for controlled stress induction and electromyographic postural analysis.

---

## Abstract

The Postural Interview is an experimental Human-Robot Interaction (HRI) system designed to investigate postural muscular responses during controlled social stress scenarios mediated by a social robot.

The system simulates a stressful job interview using the Furhat social robot integrated with Large Language Models (LLMs) to generate adaptive conversational behavior while preserving experimental reproducibility.

The project combines:

- Human-Robot Interaction (HRI)
- Conversational AI
- Social Robotics
- Surface Electromyography (sEMG)
- Signal Processing
- Event-correlated physiological analysis
- AI-assisted software development

Developed as a Bachelor's Thesis project in Computer Science at the University of Salerno.

---

## Research Goal

The objective of the project is to analyze how controlled social stress influences postural muscular activation during a simulated evaluative interaction with a robot.

The system was designed to:

- induce controlled psychological stress;
- maintain conversational realism through adaptive AI;
- synchronize conversational events with physiological acquisition;
- analyze muscular activation patterns through surface electromyography.

---

## System Architecture

The system is composed of four main components:

1. Furhat Social Robot
2. Conversational State Machine
3. GPT-based Adaptive Language Module
4. EMG Signal Processing Pipeline

### Main Workflow

```text
Participant
    ↓
Furhat Social Robot
    ↓
Conversational State System
    ↓
LLM / GPT Integration
    ↓
Stress Induction Events
    ↓
EMG Acquisition (Shimmer3)
    ↓
Signal Processing Pipeline
    ↓
Event-Correlated Analysis
    ↓
Postural Pattern Classification
````

---

## Experimental Protocol

The experimental scenario reproduces a simulated job interview divided into multiple phases:

### 1. Baseline Phase

Initial relaxed posture acquisition.

### 2. Warm-up Phase

Neutral conversational interaction.

### 3. Stress Induction Phase

Three controlled stressors are introduced:

- Cognitive Load Stressor
- Competency Challenge Stressor
- Evaluative Silence Stressor

### 4. Recovery Phase

Post-interview muscular recovery recording.

---

## Conversational AI System

The conversational architecture was implemented using:

- Kotlin
- Furhat SDK
- Finite-state conversational design
- GPT-based constrained adaptive generation

The system preserves protocol reproducibility while generating contextual conversational responses.

### Key Features

- Adaptive follow-up generation
- Controlled conversational flow
- Event logging and synchronization
- GPT fallback mechanisms
- CV-based personalization

---

## EMG Signal Processing Pipeline

The physiological analysis module processes surface electromyography signals acquired from:

- Upper trapezius muscles
- Lumbar erector spinae muscles

### Processing Steps

1. DC offset removal
2. Signal rectification
3. RMS moving window calculation
4. Baseline normalization
5. Event-correlated analysis
6. Rule-based postural classification

---

## Technologies

### Programming Languages

- Kotlin
- Java

### AI & Conversational Systems

- GPT Integration
- Large Language Models (LLMs)
- Prompt Engineering
- GitHub Copilot

### Robotics & HRI

- Furhat SDK
- Human-Robot Interaction
- Conversational State Machines

### Signal Processing

- sEMG Analysis
- RMS Processing
- CSV Event Synchronization
- Physiological Data Analysis

---

## Repository Structure

```text
the-postural-interview/
│
├── src/
├── assets/
├── config/
├── docs/
│   ├── EMG_ANALYSIS_GUIDE.md
│   ├── GPT_INTEGRATION.md
│   └── PROTOCOL_README.md
│
├── cv/
├── screenshots/
└── README.md
```

---

## Research Context

The project explores how social robotics and AI-driven conversational systems can be integrated into psychophysiological experimental protocols.

Particular attention was dedicated to balancing:

- conversational naturalness;
- experimental control;
- reproducibility;
- physiological synchronization.

---

## Future Developments

Potential future extensions include:

- multimodal physiological acquisition;
- heart rate variability integration;
- galvanic skin response analysis;
- machine learning classification models;
- larger experimental samples;
- advanced behavioral analytics.

---

## Thesis Reference

Bachelor's Thesis:

“The Postural Interview – Controlled stress induction in a simulated interview with a social robot and electromyographic analysis of postural variations”

Department of Computer Science University of Salerno Academic Year 2025/2026

---

## Author

Antonio Del Giudice

B.Sc. in Computer Science University of Salerno

```
```
