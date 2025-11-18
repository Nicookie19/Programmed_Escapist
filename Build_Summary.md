# Programmed Escapist

A graphical RPG adventure set in a digital world where you play as one of six specialized character classes battling bugs, glitches, and malware in a quest to restore system stability.

## Quick Start

```bash
# From the ProgEscps directory
java -cp src progescps.TurnBased_RPG
```

## Latest Updates (November 2025)

### UI Improvements
- Added graphical character sprites for all classes
- Enhanced sprite animations (idle, combat, movement states)
- Centered character portraits with improved layout
- Fixed HP/mana bar display
- Added detailed error reporting
- Improved visual feedback

### Game Enhancements
- Enhanced combat animations
- Improved character class balancing
- Added better error handling
- Enhanced resource management
- Improved save/load system

## Features

### Character Classes

#### Debugger - Jack Overflow, The Bug Slayer
- **Role**: Tank/Analyst
- **Base Stats**:
  - HP: 250 (Highest base HP)
  - Mana: 100
  - Defense: High
  - Attack: Medium
- **Abilities**:
  - Debug (20 mana): Analyze enemy and reveal weaknesses
    - Reduces enemy defense by 20%
    - Duration: 3 turns
    - Cooldown: 4 turns
  - Patch (30 mana): Self-heal and fortification
    - Heals 40 HP
    - Increases defense by 25%
    - Duration: 2 turns
    - Cooldown: 5 turns
  - Inspect (25 mana): Deep system analysis
    - Reveals all enemy stats
    - Shows enemy skill cooldowns
    - Increases critical hit chance by 15%
    - Cooldown: 3 turns
- **Passive**: Error Handler
  - 10% chance to automatically mitigate damage
  - Reduces critical hit damage taken by 20%
- **Best Against**: Complex bugs, System errors
- **Weakness**: Rapid attack patterns

#### Hacker - Maya Firewall, The Code Breaker
- **Role**: DPS/Nuker
- **Base Stats**:
  - HP: 180
  - Mana: 120
  - Attack: Very High
  - Defense: Low
- **Abilities**:
  - Exploit (35 mana): High damage single target
    - Deals 150% base damage
    - 25% chance to apply vulnerability
    - Cooldown: 3 turns
  - DDOS (40 mana): Area damage over time
    - Deals 60% damage per turn
    - Affects all enemies
    - Duration: 3 turns
    - Cooldown: 6 turns
  - Rootkit (30 mana): Stealth damage amplifier
    - Plants hidden damage amplifier
    - +30% damage for 3 turns
    - Can stack up to 2 times
    - Cooldown: 4 turns
- **Passive**: Code Injection
  - Each attack has 15% chance to apply a mini-exploit
  - Bonus 10% damage to vulnerable targets
- **Best Against**: Security systems
- **Weakness**: Direct confrontation

#### Tester - Sam Byte, The Bug Hunter
- **Role**: Analyst/Support
- **Base Stats**:
  - HP: 200
  - Mana: 110
  - Attack: Medium
  - Defense: Medium
- **Abilities**:
  - Scan (25 mana): Find vulnerabilities
    - Reveals enemy weakness
    - Increases party accuracy by 20%
    - Duration: 2 turns
    - Cooldown: 3 turns
  - Verify (30 mana): System validation
    - Increases party defense by 25%
    - Removes one debuff
    - Duration: 3 turns
    - Cooldown: 4 turns
  - Penetration Test (35 mana): Precision strike
    - 200% accuracy
    - Ignores 50% of target defense
    - Cooldown: 4 turns
- **Passive**: Quality Assurance
  - 20% chance to detect enemy attack pattern
  - Bonus damage against revealed weaknesses
- **Best Against**: Unpatched systems
- **Weakness**: Overwhelming force

#### Architect - Linus Kernel, The System Designer
- **Role**: Tank/Support
- **Base Stats**:
  - HP: 220
  - Mana: 90
  - Defense: Very High
  - Attack: Low
- **Abilities**:
  - Design (30 mana): Create defensive structure
    - Generates shield for party
    - Absorbs 100 damage
    - Duration: 3 turns
    - Cooldown: 5 turns
  - Rally (35 mana): Team defense boost
    - Increases party defense by 40%
    - Duration: 2 turns
    - Cooldown: 4 turns
  - Unyielding Spirit (50 mana): Ultimate survival
    - Prevents death for 1 turn
    - Heals 30% max HP if surviving
    - Cooldown: 8 turns
- **Passive**: Solid Foundation
  - Increases party max HP by 10%
  - Reduces structure decay by 20%
- **Best Against**: Structural threats
- **Weakness**: Agile enemies

#### PenTester - Vex Shadowblade, The Silent Intruder
- **Role**: Stealth/DPS
- **Base Stats**:
  - HP: 160
  - Mana: 100
  - Attack: High
  - Defense: Low
- **Abilities**:
  - Probe (20 mana): Analyze defenses
    - Reveals target weakpoints
    - Increases critical chance by 30%
    - Duration: 2 turns
    - Cooldown: 3 turns
  - Breach (40 mana): Defense penetration
    - Ignores target armor
    - 175% base damage
    - Cooldown: 4 turns
  - Stealth (35 mana): Tactical advantage
    - 80% evasion rate
    - +50% critical damage
    - Duration: 2 turns
    - Cooldown: 5 turns
- **Passive**: Backdoor
  - 15% chance to strike twice
  - Bonus damage from stealth
- **Best Against**: High security targets
- **Weakness**: Sustained combat

#### Support - Elara Lightbringer, The System Maintainer
- **Role**: Healer/Buffer
- **Base Stats**:
  - HP: 190
  - Mana: 130 (Highest base mana)
  - Attack: Low
  - Defense: Medium
- **Abilities**:
  - Patch (30 mana): Single target heal
    - Restores 120 HP
    - Removes one debuff
    - Cooldown: 3 turns
  - Buffer (35 mana): Party enhancement
    - Increases party stats by 25%
    - Duration: 3 turns
    - Cooldown: 5 turns
  - System Restore (50 mana): Emergency recovery
    - Heals party for 80 HP
    - Removes all debuffs
    - Cooldown: 6 turns
- **Passive**: Optimization
  - Reduces party mana costs by 10%
  - Healing increased by 20% on low HP targets
- **Best Against**: War of attrition
- **Weakness**: Solo combat

### Combat System

#### Core Mechanics
- **Turn-Based Combat**
  - Initiative system based on speed stats
  - Action queue with priority system
  - Reaction system for certain abilities
  - Combat log with detailed action results

#### Action Types
- **Basic Attack**
  - Base damage determined by attack stat
  - Accuracy check against enemy evasion
  - Critical hit chance (base 10%)
  - Various damage types (physical, logical, viral)

- **Abilities**
  - Unique character-specific skills
  - Mana cost and cooldown management
  - Area of effect possibilities
  - Combo potential with other characters

- **Items**
  - Consumables for healing and buffs
  - Limited inventory space
  - Quick-use slot system
  - Crafting components

#### Status Effects
- **Buffs**
  - Enhanced Stats (+Attack, +Defense, etc.)
  - Regeneration (HP/Mana per turn)
  - Shield/Barrier effects
  - Speed boosts

- **Debuffs**
  - Vulnerability (increased damage taken)
  - Corruption (damage over time)
  - Stun/Root effects
  - Stat reductions

#### Strategic Elements
- **Mana Management**
  - Limited mana pool
  - Regeneration mechanics
  - Resource efficiency planning
  - Emergency reserves

- **Position System**
  - Front line/Back line positioning
  - Position-based damage modifiers
  - Movement abilities and costs
  - Formation bonuses

- **Team Synergy**
  - Combo abilities between classes
  - Support skill interactions
  - Team composition bonuses
  - Chain reaction mechanics

#### Combat Resolution
- **Victory Conditions**
  - All enemies defeated
  - Objective completion
  - Survival requirements

- **Rewards**
  - Experience points
  - Loot drops
  - Skill points
  - Quest progression

### World Exploration
- Multiple unique locations with environmental effects:
  - Void Cache
  - Hidden Partition
  - Overclocked Springs
  - Bug Bog
  - Frozen Sector
  - And many more!
- Location-specific enemies and features
- Environmental effects that impact combat

### Quest System

#### Main Campaign
- **Story Chapters**
  - Linear main story progression
  - Major plot developments
  - Character-specific story arcs
  - Critical path missions

- **Chapter Structure**
  - Introduction cutscene/briefing
  - Multiple mission objectives
  - Boss encounters
  - Story revelations
  - Chapter rewards

#### Side Missions
- **Optional Quests**
  - Character development missions
  - Faction reputation quests
  - Resource gathering tasks
  - Training missions

- **Discovery System**
  - Hidden quest triggers
  - Environmental clues
  - NPC conversations
  - Optional areas

#### Quest Types
- **Combat Missions**
  - Enemy elimination
  - Defense scenarios
  - Escort missions
  - Boss battles

- **Stealth Operations**
  - Infiltration missions
  - Data extraction
  - Covert operations
  - Timed escapes

- **Puzzle Quests**
  - Code breaking challenges
  - System hacking
  - Logic puzzles
  - Pattern recognition

#### Choice & Consequence
- **Decision Points**
  - Multiple quest resolutions
  - Branching dialogue options
  - Faction allegiance choices
  - Moral decisions

- **Impact System**
  - Reputation changes
  - Future quest availability
  - NPC relationship changes
  - World state effects

#### Reward Structure
- **Mission Rewards**
  - Experience points
  - Credits/Currency
  - Unique items
  - Skill points
  - Faction standing

- **Achievement System**
  - Mission-specific challenges
  - Optional objectives
  - Hidden achievements
  - Completion bonuses

#### Quest Tracking
- **Journal System**
  - Active quest log
  - Completed missions history
  - Quest objective tracking
  - Mission briefings

- **Map Integration**
  - Quest markers
  - Objective locations
  - Point of interest tracking
  - Discovery zones

### Faction System

#### Major Factions

##### NetGuard (Security Specialists)
- **Ideology**: System protection and stability
- **Strengths**: Defense, Monitoring
- **Base Location**: Firewall Citadel
- **Special Units**: Security Protocols, Watchdogs
- **Technology**: Advanced encryption, Defensive matrices

##### CodeWeavers (Programming Elite)
- **Ideology**: Code mastery and innovation
- **Strengths**: Creation, Manipulation
- **Base Location**: Binary Forge
- **Special Units**: Script Weavers, Logic Shapers
- **Technology**: Code synthesis, Pattern algorithms

##### DataMiners (Information Brokers)
- **Ideology**: Knowledge acquisition and trading
- **Strengths**: Analysis, Collection
- **Base Location**: Cache Haven
- **Special Units**: Data Scouts, Archive Keepers
- **Technology**: Data extraction, Pattern recognition

##### SystemRunners (Infrastructure Masters)
- **Ideology**: System optimization and maintenance
- **Strengths**: Support, Enhancement
- **Base Location**: Core Nexus
- **Special Units**: Grid Managers, Resource Controllers
- **Technology**: System boosting, Resource management

#### Reputation System

##### Standing Levels
- **Allied** (90-100): Full access to faction resources
- **Friendly** (70-89): Special mission access
- **Respected** (50-69): Trade benefits
- **Neutral** (30-49): Basic interaction
- **Suspicious** (10-29): Limited access
- **Hostile** (0-9): Combat on sight

##### Reputation Mechanics
- **Earning Reputation**
  - Completing faction missions
  - Trading valuable data
  - Defending faction interests
  - Eliminating faction enemies

- **Losing Reputation**
  - Failed missions
  - Supporting rivals
  - Breaking faction rules
  - Damaging faction assets

#### Faction Benefits

##### Resource Access
- **Equipment**
  - Faction-specific gear
  - Special modifications
  - Unique consumables
  - Advanced tools

- **Services**
  - Special training
  - Equipment repairs
  - Intel sharing
  - Fast travel nodes

##### Special Features
- **Faction Bases**
  - Safe zones
  - Trading hubs
  - Mission centers
  - Training facilities

- **Faction Abilities**
  - Special combat moves
  - Unique buffs
  - Team bonuses
  - Resource boosts

#### Inter-Faction Relations

##### Alliance System
- **Joint Operations**
  - Combined missions
  - Shared resources
  - Trading benefits
  - Mutual defense

- **Rivalry Mechanics**
  - Territory disputes
  - Resource competition
  - Ideological conflicts
  - Power struggles

##### Dynamic Events
- **Faction Wars**
  - Territory control
  - Resource battles
  - Influence campaigns
  - Special rewards

- **Cooperation Events**
  - Joint threats
  - Shared goals
  - System-wide challenges
  - Combined rewards

### Inventory & Equipment

#### Equipment Types

##### Weapons
- **Code Injectors**
  - High damage, low defense
  - Status effect chance
  - Mana regeneration boost
  - Special ability modifiers

- **System Tools**
  - Balanced stats
  - Utility effects
  - Resource generation
  - Combat flexibility

- **Defense Modules**
  - High defense, lower damage
  - Shield generation
  - Damage reflection
  - Status resistance

##### Armor
- **Firewalls**
  - Heavy protection
  - Status immunity
  - Movement penalty
  - Energy consumption

- **Neural Networks**
  - Adaptive defense
  - Learning capability
  - Enhanced recovery
  - Special ability boost

- **Quantum Shields**
  - Variable protection
  - Phase shifting
  - Energy manipulation
  - Probability alteration

#### Item Categories

##### Consumables
- **Recovery Items**
  - Health restoration
  - Mana replenishment
  - Status cure
  - Temporary buffs

- **Combat Items**
  - Damage dealers
  - Area effects
  - Status inducers
  - Tactical advantages

##### Crafting Materials
- **Base Components**
  - Code fragments
  - System resources
  - Energy cores
  - Raw data

- **Special Materials**
  - Rare algorithms
  - Quantum particles
  - Encrypted data
  - Unique patterns

#### Equipment Management

##### Inventory System
- **Storage Space**
  - 50 base slots
  - Expandable capacity
  - Quick access slots
  - Category sorting

- **Item Management**
  - Stack similar items
  - Split stacks
  - Quick transfer
  - Auto-sort function

##### Equipment Slots
- **Character Equipment**
  - Weapon slot
  - Armor slot
  - Accessory slots (2)
  - Tool slots (3)

- **Quick Access**
  - Combat items (4)
  - Recovery items (4)
  - Utility items (2)
  - Special items (2)

#### Enhancement System

##### Upgrade Mechanics
- **Enhancement Levels**
  - +1 to +10 scaling
  - Success rate system
  - Failure protection
  - Special bonuses

- **Modification**
  - Stat customization
  - Effect addition
  - Special properties
  - Unique attributes

##### Equipment Features
- **Rarity Levels**
  - Common (white)
  - Uncommon (green)
  - Rare (blue)
  - Epic (purple)
  - Legendary (gold)

- **Special Properties**
  - Set bonuses
  - Hidden effects
  - Conditional boosts
  - Unique abilities

### Game Progression

#### Character Development

##### Level System
- **Experience Points**
  - Combat rewards
  - Quest completion
  - Discovery bonuses
  - Achievement rewards

- **Level Benefits**
  - Stat increases
  - Skill points
  - New abilities
  - Equipment access

##### Skill Trees
- **Primary Skills**
  - Class-specific abilities
  - Passive improvements
  - Combat techniques
  - Special moves

- **Secondary Skills**
  - Universal abilities
  - Utility functions
  - Support capabilities
  - Movement skills

#### Advancement Mechanics

##### Stat Growth
- **Base Stats**
  - Health Points (HP)
  - Mana Points (MP)
  - Attack Power
  - Defense Rating
  - Speed

- **Advanced Stats**
  - Critical Rate
  - Evasion
  - Accuracy
  - Recovery Rate
  - Resource Efficiency

##### Specialization
- **Class Mastery**
  - Ultimate abilities
  - Passive mastery
  - Stat bonuses
  - Special features

- **Equipment Proficiency**
  - Weapon mastery
  - Armor expertise
  - Tool efficiency
  - Resource optimization

#### Achievement System

##### Progress Tracking
- **Combat Achievements**
  - Enemy defeats
  - Boss victories
  - Perfect battles
  - Special conditions

- **Quest Achievements**
  - Story completion
  - Side quest mastery
  - Hidden objectives
  - Speed runs

##### Reward Structure
- **Achievement Rewards**
  - Unique items
  - Special abilities
  - Cosmetic rewards
  - Title unlocks

- **Milestone Bonuses**
  - Permanent stat boosts
  - Special features
  - Advanced options
  - Bonus content

#### End Game Content

##### Advanced Challenges
- **Elite Missions**
  - High difficulty
  - Special conditions
  - Unique rewards
  - Time limits

- **Special Events**
  - Limited time
  - Unique mechanics
  - Exclusive rewards
  - Community goals

##### Mastery System
- **Character Mastery**
  - Advanced builds
  - Skill combinations
  - Perfect execution
  - Speed running

- **Content Mastery**
  - 100% completion
  - All achievements
  - Secret endings
  - Ultimate challenges
- Equipment upgrades
- Skill improvements
- Faction reputation gains
- Achievement tracking

## Technical Details

### System Requirements
- Java Runtime Environment (JRE) 21 or later
- Minimum 2GB RAM
- 500MB disk space
- Graphics support for Swing UI
- Keyboard and mouse input

### Controls
- **Mouse**: Select options and targets
- **Enter**: Confirm selections
- **Esc**: Cancel/Menu
- **Arrow Keys**: Navigate menus
- **1-6**: Quick-select abilities

### Project Structure
```
ProgEscps/
├── src/progescps/          # Source code
│   ├── Character classes   # Hero implementations
│   ├── Combat system      # Battle mechanics
│   ├── UI components     # Visual elements
│   └── Game systems      # Core functionality
├── build/                  # Compiled classes
├── saves/                  # Save files
└── resources/             # Game assets
```

### Build Instructions
```bash
# Compile the project
javac -d build src/progescps/*.java

# Run the game
java -cp build progescps.TurnBased_RPG
```

## Educational Value

### Programming Concepts
- Object-Oriented Programming through class system
- Inheritance and polymorphism in character classes
- Interface implementation in game systems
- Exception handling and error management
- Resource management and cleanup

### Cybersecurity Learning
- Understanding security vulnerabilities
- Learning penetration testing concepts
- Practicing system analysis
- Understanding defense mechanisms
- Risk assessment and mitigation

### Problem-Solving Skills
- Strategic combat decisions
- Resource management
- Risk/reward evaluation
- System analysis
- Bug identification and fixing

## Future Plans

### Upcoming Features
- Additional character customization
- More enemy types and behaviors
- Enhanced combat effects
- Sound system implementation
- Achievement system
- Tutorial system for new players
- Enhanced UI customization

## Contributing

This project is part of the academic curriculum at University of the Immaculate Conception. Please contact the development team for contribution guidelines.
- Skill improvements
- Achievement tracking
- Optional hardcore mode with permadeath

## Current Updates

### Version 1.01.0
- All core game systems implemented
- Six playable character classes
- Turn-based combat system
- Quest and faction systems
- World map with multiple locations
- Basic UI with color support
- Save/load game functionality

### Recent Fixes
- Fixed package structure issues
- Resolved compilation errors in multiple files
- Improved code stability and compatibility
- Enhanced UI theme consistency

## Current Game Situation

The game is currently in a stable, playable state with all core features implemented. Players can:

1. Create a new character from six different classes
2. Explore a world with multiple unique locations
3. Engage in turn-based combat with various enemies
4. Complete quests and earn rewards
5. Interact with factions and build reputation
6. Save and load game progress

The game uses a text-based interface with optional ANSI color support for enhanced visual experience.

## How to Play

1. Compile the game using `javac -d build/classes src/progescps/*.java`
2. Run the game using `java -cp build/classes progescps.TurnBased_RPG`
3. Follow the on-screen instructions to create a character and begin your adventure

## Future Development Plans

- Enhanced UI with more visual elements
- Additional character classes and abilities
- Expanded world with more locations
- More complex quest chains and storylines
- Advanced enemy AI and combat mechanics
- Multiplayer capabilities