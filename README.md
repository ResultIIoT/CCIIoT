# CCIIoT: Unveiling Code Clones in the Eclipse IIoT Software Ecosystem

> A research repository containing tools and datasets for detecting and analyzing code clones in the Eclipse IIoT Software Ecosystem

## Overview

This repository is the public repository for our research paper on co-modified and different pattern code clone detection. It contains:

- **Detection Tool**: An enhanced co-modified code clone detection tool based on CCDetector
- **Analysis Module**: Data analysis tools for research insights
- **Public Dataset**: Curated dataset for reproducible research

## Repository Structure

```
CCIIoT/
├── CCDetector-Update/     # Enhanced detection tool based on CCDetector
├── AnalysisModule/        # Data analysis and processing tools
├── dataset/               # Public research dataset
└── README.md              # This file
```

## Components

### 1. CCDetector-Update

Enhanced code clone detection tool specifically designed for detecting co-modified code clones. This is an improved version of the original CCDetector with additional capabilities for identifying code clones that change together across different projects.

**Features:**

- Co-modified code clone detection
- Cross-project co-modified code clone detection

### 2. AnalysisModule

Data analysis toolkit for processing detection results and generating research insights.

**Capabilities:**

- Statistical analysis of clone patterns
- Evolution tracking of code clones
- Visualization tools for research data

### 3. Dataset

Public dataset containing:

- Code clone samples
- Cross project code clone samples

## Getting Started

### Prerequisites

- Java 8 or higher
- Python 3.7+
  - Required dependencies (see individual module documentation)

### Installation

```bash
# Clone the repository
git clone https://github.com/ResultIIoT/CCIIoT.git
cd CCIIoT

# Setup detection tool
cd CCDetector-Update
# Follow setup instructions in the folder

# Setup analysis module
cd ../AnalysisModule
```

## Usage

### Running the Detection Tool

```bash
cd CCDetector-Update
# Follow usage instructions in the folder
```

### Performing Analysis

```bash
cd AnalysisModule
# Run the specified Python file
```

## Dataset Information

The dataset includes:

- Code clone history data
- Cross project code clone history data

For detailed dataset information, see the `dataset/` directory.
