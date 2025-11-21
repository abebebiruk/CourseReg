# Weighted Course Registration System

## Overview

Course registration at Pomona currently relies on a combination of time-tickets and limited priority rules that often leave students without their preferred classes. Students have no transparent understanding of how choices are processed, and when high-demand classes fill, students who need them for graduation or major progress often fall behind.

Our project proposes a **weighted, preference-based course lottery system**. In this model:
- Each student selects a desired course they want to take
- Rankings are converted into weighted lottery entries
- Scores can increase based on additional factors such as:
  - Major/minor status
  - Higher class year etc...
- The system then selects students for each course proportionally to their priority weights
- Creates a fairer and more transparent registration process

## Features

- **Pre-requisite Checking**: Validates that students meet course prerequisites before registration
- **Weighted Assignment**: Assigns class seats to students based on weighted priority scores
- **Waitlist Reasons**: Gives specific reasons why a student might be waitlisted for a particular class


## Data

### Course Data
- Contains all available classes for SP2026
- For the purpose of this project we included a filtered course data containing only CS classes provided in SP2026
- Contains seat availability information etc...

### Student Data
- **Fields**:
  - Student ID
  - Student Name
  - Past classes taken
  - Requested classes
  - Graduation Year
  - CS Major type
    - CS major
    - CS minor
    - Non-major

### Synthetic Dataset
The project uses synthetic data(Student Data) for simulation and testing purposes.
