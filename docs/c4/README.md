# FTOC C4 Architecture Diagrams

This directory contains C4 architecture diagrams for the FTOC project in ASCII art format.

## What is C4?

The C4 model is a lean graphical notation technique for modeling the architecture of software systems. C4 stands for Context, Containers, Components, and Code - a set of hierarchical diagrams that provide different levels of abstraction.

## Available Diagrams

1. [Context Diagram](context-diagram.md) - Shows the FTOC system in the context of its users and external systems
2. [Container Diagram](container-diagram.md) - Shows the high-level technology choices and how responsibilities are distributed
3. [Component Diagram](component-diagram.md) - Shows how a container is made up of components and their interactions
4. [Code Diagram](code-diagram.md) - Shows how components are implemented as code (classes, interfaces)

## Diagram Legend

All diagrams are created using ASCII art with the following elements:

```
┌────────────┐
│            │  Boxes represent systems, containers, components, or classes
│            │
└────────────┘

       │
       │      Lines represent relationships or dependencies
       ▼
       
       uses
       ──────>  Labeled arrows describe the nature of relationships
```

## References

- [C4 Model](https://c4model.com/) - Official website for the C4 model