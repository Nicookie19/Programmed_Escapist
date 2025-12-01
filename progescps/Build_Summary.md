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
  - Debug (20 mana): Analyze enemy weaknesses
    - Deals 1.5x base damage
    - Cooldown: 4 turns
  - Patch (25 mana): Self-heal
    - Heals 20% of max HP
    - Cooldown: 5 turns
  - Inspect (15 mana): Reveal enemy information
    - Deals base damage
    - Cooldown: 3 turns
  - Breakpoint (30 mana): Set a breakpoint
    - Deals 2x base damage
    - Stuns enemy for next turn
- **Passive**: Auto-heal on kill
  - Regenerates 10% of max HP when enemy dies
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
  - Vulnerability Exploit (25 mana): Exploit vulnerability
    - Deals 2x base damage
    - Applies Defense Down (0.8x defense, 2 turns)
    - Cooldown: 5 turns
  - Code Injection (15 mana): Inject malicious code
    - Deals base damage
    - Applies Bleed (5 damage per turn, 3 turns)
  - DDoS Flood (20 mana): Flood with DDoS
    - Deals 1.5x base damage
    - Stuns enemy for next turn
    - Cooldown: 4 turns
  - Rootkit Installation (30 mana): Install rootkit
    - Deals 2.5x base damage
    - Applies Weakened (0.7x damage, 2 turns)
    - Cooldown: 5 turns
- **Passive**: Mana Regen & Surge
  - Regenerates 10% of max mana per turn
  - If mana below 20%, gains extra 10%
- **Best Against**: Security systems
- **Weakness**: Direct confrontation

#### Tester - Sam Byte, The Bug Hunter
- **Role**: Analyst/Support
- **Base Stats**:
  - HP: 220
  - Mana: 80
  - Attack: Medium
  - Defense: Medium
- **Abilities**:
  - Scan (15 mana): Scan for vulnerabilities
    - Deals base damage
    - Applies Bug Report (5 damage per turn, 3 turns)
    - Cooldown: 3 turns
  - Verify (10 mana): Bug Report
    - Deals base damage
    - Applies Bug Report (5 damage per turn, 3 turns)
    - Cooldown: 3 turns
  - Bug Report (10 mana): Report a bug
    - Deals base damage
    - Applies Bug Report (5 damage per turn, 3 turns)
    - Cooldown: 3 turns
  - Regression (15 mana): Run regression tests
    - Deals 2x base damage
    - Cooldown: None
- **Passive**: Critical Boost
  - Increased crit chance when HP above 80% (+10% crit, +5% base)
- **Best Against**: Unpatched systems
- **Weakness**: Overwhelming force

#### Architect - Linus Kernel, The System Designer
- **Role**: Tank/Support
- **Base Stats**:
  - HP: 320
  - Mana: 50
  - Defense: Very High
  - Attack: Low
- **Abilities**:
  - Design (20 mana): Design a flaw
    - Deals base damage to all enemies
    - 20% chance to stun enemy
    - Cooldown: 5 turns
  - Build (0 mana): Normal attack
  - Rally (15 mana): Rally the team
    - Boosts damage by 50% for next attack
    - Cooldown: 4 turns
  - Demolish (20 mana): Demolish defenses
    - Deals 2x base damage
- **Passive**: Unyielding Spirit
  - Survives lethal damage once per combat (leaves at 1 HP)
- **Best Against**: Structural threats
- **Weakness**: Agile enemies

#### PenTester - Vex Shadowblade, The Silent Intruder
- **Role**: Stealth/DPS
- **Base Stats**:
  - HP: 200
  - Mana: 60
  - Attack: High
  - Defense: Low
- **Abilities**:
  - Probe (15 mana): Use Probe
    - Deals base damage
    - Cooldown: 3 turns
  - Breach (20 mana): Use Breach
    - Deals 1.5x base damage
    - Cooldown: 4 turns
  - Stealth (15 mana): Use Stealth
    - Becomes untargetable for next enemy attack
    - Cooldown: 5 turns
  - Exploit Chain (25 mana): Use Exploit Chain
    - Deals 2x base damage
- **Passive**: Evasion Mastery
  - Chance to dodge attacks
- **Best Against**: High security targets
- **Weakness**: Sustained combat

#### Support - Elara Lightbringer, The System Maintainer
- **Role**: Healer/Buffer
- **Base Stats**:
  - HP: 240
  - Mana: 100
  - Attack: Low
  - Defense: Medium
- **Abilities**:
  - Patch (20 mana): Apply Patch
    - Deals 1.5x base damage to all enemies
    - Cooldown: 4 turns
  - Heal (0 mana): Normal attack
  - Buffer (15 mana): Cast Buffer
    - Reduces damage taken by 50% next turn
    - Cooldown: 3 turns
  - Restore (25 mana): Cast Restore
    - Heals 40% of max HP
    - 20% chance to reset cooldown of Patch or Buffer
- **Passive**: Regeneration & Code Grace
  - Regenerates 5% of max HP per turn
  - 20% chance to reset Patch or Buffer cooldown on Restore
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

### Database Design

#### Schema Overview
The game uses a MySQL database with a comprehensive schema supporting all game systems. The database is structured to separate static game content from dynamic player data.

#### Data Population Strategy
- **Pre-populated Tables (Static Game Assets)**: Core game content is pre-loaded into the database to ensure consistent gameplay experience:
  - `enemy`: All enemy types, stats, and behaviors (62 predefined enemies)
  - `equipment`: Weapons, armor, and items with their properties (49 equipment items)
  - `location`: Game world locations with descriptions and environmental effects (30 locations)
  - `faction`: Faction information and relationships (10 factions)
  - `locationfeature`: Location-specific features and points of interest

- **Empty Tables (Dynamic Player Data)**: Player-specific data starts empty and is populated during gameplay:
  - `hero`: Player character data and progression
  - `inventoryitem`: Player inventory and item management
  - `quest`: Active and completed quests
  - `achievement`: Player achievements and unlocks
  - `heroequipment`: Equipped items and bonuses
  - `herofaction`: Faction reputation and relationships
  - `heroachievement`: Achievement progress tracking
  - `questobjective`: Quest objectives and completion status
  - `questreward`: Quest reward tracking
  - `save_slots`: Game save data
  - `statuseffect`: Active status effects and buffs/debuffs

This design ensures that core game content remains consistent across all playthroughs while player progress and save data are managed separately.

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