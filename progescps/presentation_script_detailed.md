# Programmed Escapist: Detailed Presentation Script

**Team Members:** Mahipus, Allawan, Eucare, Abad
**Date:** October 2025
**Course:** Object-Oriented Programming
**Institution:** University of the Immaculate Conception


## Speaker 1: Game Introduction and Overview (6-7 minutes)

**Visual Aids:** Title slide, game screenshots, team introduction, project timeline, technical stack diagram

**[Introduction]**
Good morning/afternoon everyone. My name is [Name], and I'm part of the development team for "Programmed Escapist." Today, our team - Mahipus, Allawan, Eucare, and Abad - will present our comprehensive Object-Oriented Programming project: an educational turn-based RPG that teaches programming concepts and cybersecurity principles through engaging gameplay.

**[Project Purpose]**
Programmed Escapist is an educational turn-based RPG designed to immerse players in a digital world where they battle software threats and malware. As a programmer-hero, players learn about programming paradigms, security vulnerabilities, and problem-solving in a fun, interactive way. This game serves multiple audiences: instructors can use it to teach security/programming concepts playfully, learners can practice concepts like debugging and penetration testing through mechanics, and players enjoy replayable content with character classes, faction quests, and item progression.

**[Development Timeline]**
Our project followed a structured 13-week development cycle with clear milestones:
- **Weeks 1-2**: Requirements gathering, UML design, and system architecture planning
- **Weeks 3-4**: Database schema creation with MySQL and phpMyAdmin, including ER diagrams
- **Weeks 5-7**: Core gameplay classes (Hero/Enemy/Combat) with OOP implementation
- **Weeks 8-10**: UI development with Java Swing and integration of quest/faction systems
- **Weeks 11-12**: Integration testing, bug fixing, and comprehensive documentation
- **Week 13**: Final polishing, presentation preparation, and deployment testing

**[Core Objectives]**
Our core objectives were to:
1. Build a turn-based combat engine illustrating advanced OOP patterns (inheritance, polymorphism, encapsulation)
2. Implement six distinct hero classes with unique skills, cooldowns, and strategic depth
3. Provide comprehensive quest and faction mechanics for variety and long-term progression
4. Create persistent classes with database integration for save/load functionality
5. Develop an educational platform that teaches both programming and cybersecurity concepts

**[Game Features Overview]**
The game features six distinct character classes, a comprehensive combat system with status effects, a world map with multiple locations, a quest system with main and side missions, faction reputation mechanics, and equipment/inventory management. All wrapped in a Java Swing graphical user interface with character sprites and animations.

**[Technical Stack]**
Our technical implementation includes:
- **Language**: Java 17+ with full OOP principles implementation
- **GUI Framework**: Java Swing with custom components and theming system
- **Database**: MySQL with phpMyAdmin for schema management and data persistence
- **Build System**: Apache Ant with build.xml configuration for automated compilation
- **Version Control**: Git for collaborative development and version management
- **IDE**: Apache NetBeans and VS Code for development environment
- **Testing**: JUnit framework for unit testing and integration tests

---

## Speaker 2: Character Classes and Combat System (6-7 minutes)

**Visual Aids:** Character class slides with detailed stats and abilities, combat screenshots, status effect examples, code snippets, OOP diagrams

**[Character Classes Introduction]**
Now let's dive into the heart of the gameplay: our six unique character classes, each representing different programming and security roles. Each class extends the abstract `Hero` base class, demonstrating inheritance and polymorphism - core OOP concepts we'll explore in detail.

**[Tank Classes]**
First, we have the tank classes designed for durability and crowd control:

**Debugger**
- **Role**: Tank/Analyst - Frontline defender with analytical capabilities
- **Base Stats**: HP: 250 (Highest), Mana: 100, Defense: High (25%), Attack: Medium (40-60)
- **Abilities**: Debug (analyzes enemies), Patch (self-heal), Inspect (reveals stats)
- **Passive**: Error Handler - 10% chance to mitigate damage
- **Best Against**: Complex bugs, System errors

**Architect**
- **Role**: Tank/Support Hybrid - Defensive specialist with team protection
- **Base Stats**: HP: 220, Mana: 90, Defense: Very High (30%), Attack: Low (30-45)
- **Abilities**: Design (party shield), Rally (defense boost), Unyielding Spirit (survival)
- **Passive**: Solid Foundation - +10% party max HP
- **Best Against**: Structural threats, Team-based combat

**[DPS Classes]**
Next, the damage dealers focused on high burst and sustained damage:

**Hacker**
- **Role**: DPS/Nuker - Offensive specialist with area damage capabilities
- **Base Stats**: HP: 180, Mana: 120, Attack: Very High (60-90), Defense: Low (10%)
- **Abilities**: Exploit (high damage), DDOS (area DoT), Rootkit (damage amplifier)
- **Passive**: Code Injection - 15% chance for mini-exploit on basic attacks
- **Best Against**: Security systems, Multiple weak enemies

**PenTester**
- **Role**: Stealth/DPS - Precision striker with evasion and critical capabilities
- **Base Stats**: HP: 160, Mana: 100, Attack: High (50-75), Defense: Low (10%)
- **Abilities**: Probe (analysis), Breach (armor penetration), Stealth (evasion boost)
- **Passive**: Backdoor - 15% chance to strike twice
- **Best Against**: High security targets, Armored enemies

**[Support Classes]**
Finally, the support roles providing utility and sustain:

**Tester**
- **Role**: Analyst/Support - Information gatherer with defensive capabilities
- **Base Stats**: HP: 200, Mana: 110, Attack: Medium (45-65), Defense: Medium (15%)
- **Abilities**: Scan (vulnerability detection), Verify (defense boost), Penetration Test (precision)
- **Passive**: Quality Assurance - 20% chance to detect enemy patterns
- **Best Against**: Unpatched systems, Unknown enemies

**Support**
- **Role**: Healer/Buffer - Pure support with healing and enhancement
- **Base Stats**: HP: 190, Mana: 130 (Highest), Attack: Low (35-50), Defense: Medium (15%)
- **Abilities**: Patch (single heal), Buffer (party stats), System Restore (group heal)
- **Passive**: Optimization - 10% mana cost reduction, 20% healing increase on low HP
- **Best Against**: War of attrition, Prolonged encounters

**[OOP Implementation Example]**
Here's how we implement inheritance and polymorphism in our character system:

```java
// Abstract base class
public abstract class Hero {
    protected int hp, maxHP, mana, maxMana;
    protected String name;

    public abstract void useSkill(int skillIndex, Enemy enemy);
    public abstract String getClassName();
}

// Concrete implementation
public class Debugger extends Hero {
    @Override
    public void useSkill(int skillIndex, Enemy enemy) {
        switch(skillIndex) {
            case 0: debug(enemy); break;
            case 1: patch(); break;
            case 2: inspect(enemy); break;
        }
    }

    private void debug(Enemy enemy) {
        enemy.addStatusEffect(new StatusEffect("Vulnerable", -20, 3));
        this.mana -= 20;
    }
}
```

**[Combat System Architecture]**
Our turn-based combat system features:
- **Initiative-based turn order** determined by speed stats with random variance
- **Action point system** with mana costs and ability cooldowns
- **Status effects system** with comprehensive buff/debuff mechanics:
  - Buffs: Enhanced stats, regeneration, shield/barrier effects
  - Debuffs: Vulnerability, corruption (DoT), stun/root effects, stat reductions
- **Damage calculation formula**: `damage = (baseAttack + weaponBonus) * multiplier * (1 - defense/100) * critMultiplier`
- **Critical hits**: Base 10% chance, modified by abilities and equipment (+50% damage)
- **Detailed combat logging** with turn-by-turn action results and stat changes

Each combat round follows this sequence:
1. Player selects action (attack/skill/item/defend)
2. Combat engine resolves player action with damage/effect calculations
3. Enemy AI determines and executes counter-action
4. Enemy action resolution with player damage/effect application
5. Status effects tick and apply (DoT, HoT, duration decreases)
6. Victory/defeat conditions checked (all enemies defeated or player HP <= 0)

---

## Speaker 3: World, Quests, and Factions (6-7 minutes)

**Visual Aids:** World map with location details, quest examples with code, faction information, equipment system diagrams, database schema excerpts

**[World Exploration System]**
Programmed Escapist features a rich game world with multiple unique locations, each with environmental effects that impact combat. Our `Location` class manages these areas with full environmental interaction.

**Key Locations:**
- **Void Cache**: Dark environment reducing visibility by 30%
- **Hidden Partition**: Stealth bonuses for PenTester class
- **Overclocked Springs**: Speed boosts but increased damage taken
- **Bug Bog**: Poison effects on enemies, healing penalties
- **Frozen Sector**: Slow effects, defense bonuses
- **Firewall Citadel**: NetGuard faction base, defense bonuses

**[Quest System Architecture]**
The quest system includes comprehensive progression mechanics with main campaigns, side missions, and choice & consequence systems.

**Quest Implementation:**
```java
public abstract class Quest {
    protected String title;
    protected String description;
    protected List<Objective> objectives;
    protected List<Reward> rewards;
    protected boolean completed;

    public abstract boolean checkCompletion();
    public abstract void onCompletion();
}
```

**[Faction System Implementation]**
Four major factions provide depth and replayability: NetGuard (security), CodeWeavers (programming), DataMiners (information), SystemRunners (infrastructure).

**Reputation Mechanics:**
Faction reputation ranges from Allied (90-100: full access) to Hostile (0-9: combat on sight). Players earn reputation through missions, trading, and defending faction interests.

**[Inventory & Equipment System]**
The game features a comprehensive equipment system with weapons, armor, consumables, and crafting materials. Equipment includes rarity levels from Common to Legendary with special effects and enhancement systems.

**Database Integration:**
```sql
CREATE TABLE players (
    player_id INT PRIMARY KEY,
    class_id INT,
    level INT,
    hp INT, max_hp INT,
    mana INT, max_mana INT,
    FOREIGN KEY (class_id) REFERENCES heroclasses(class_id)
);
```

---

## Speaker 4: Technical Implementation and Educational Value (6-7 minutes)

**Visual Aids:** Code snippets, UML diagrams, system architecture, educational concept mapping, test cases, performance metrics

**[Technical Architecture Overview]**
From a technical perspective, Programmed Escapist is built in Java using advanced object-oriented principles. The core systems include GameManager, Combat Engine, GameUI, and Data Management.

**Main Entry Point:**
```java
public class TurnBased_RPG {
    public static void main(String[] args) {
        System.out.println("Starting game (Swing UI)...");
        GameUI.launch();
    }
}
```

**[Advanced OOP Implementation]**
The project demonstrates advanced OOP concepts through practical implementation:

**Inheritance Hierarchy:**
```
Hero (abstract)
├── Debugger
├── Hacker
├── Tester
├── Architect
├── PenTester
└── Support
```

**Polymorphism Example:**
```java
// In Combat.java
public void executePlayerTurn(Hero player, Enemy enemy) {
    player.useSkill(selectedSkill, enemy);
    // Each hero class implements useSkill differently
}
```

**Encapsulation:**
```java
public class Hero {
    private int hp, maxHP;
    private List<StatusEffect> statusEffects;

    public int getHp() { return hp; }
    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
    }
}
```

**[Code Structure and Design Patterns]**
Our class hierarchy includes abstract `Hero` base class, concrete implementations, model classes (Enemy, Quest, Faction), and manager classes. We implemented Factory, Observer, Strategy, Command, and Singleton patterns.

**[UI Implementation with Swing]**
The Swing-based UI features character portraits, status bars, animated sprites, theme system, and input handling.

**Sprite Loading Example:**
```java
public class CharacterSprite {
    private Image idleSprite;
    private Image combatSprite;

    public void loadSprites(String className) {
        idleSprite = ImageIO.read(new File("src/progescps/72 Character Free/" + className + "/idle.png"));
        combatSprite = ImageIO.read(new File("src/progescps/72 Character Free/" + className + "/combat.png"));
    }
}
```

**[Educational Value and Learning Objectives]**
The game teaches OOP principles, debugging, testing methodology, system architecture, cybersecurity concepts, and problem-solving skills through integrated gameplay mechanics.

**[Testing and Quality Assurance Framework]**
Our testing approach includes unit tests, integration tests, and comprehensive test cases covering combat system, character classes, quest system, and UI components.

**[Conclusion and Q&A]**
In conclusion, Programmed Escapist successfully combines entertainment with education, creating an engaging platform for learning programming and cybersecurity concepts. The game demonstrates advanced OOP implementation while providing meaningful gameplay.

**Key Technical Achievements:**
- 6 fully implemented character classes with unique abilities
- Complete combat system with status effects and AI
- World map with environmental effects
- Quest and faction systems
- Equipment and inventory management
- Graphical UI with animations
- Database persistence layer
- Comprehensive documentation and testing

Thank you for your attention. We're now happy to take any questions.

---

## Presentation Tips

- **Timing**: Each speaker should practice their section to fit within the time limit
- **Transitions**: Use smooth handoffs between speakers
- **Visuals**: Prepare slides with screenshots, diagrams, and code examples
- **Demo**: Consider showing a short gameplay demo if time permits
- **Backup**: Have team members ready to cover if someone is absent

## Additional Resources

- Full game documentation in `README.md`
- Technical details in `Build_Summary.md`
- Test cases in `TEST_CASES.md`
- Database schema in `programmed_escapist.sql`
- UML diagrams in project documentation
- Source code in `src/progescps/` directory

## Slide Suggestions

**Speaker 1 Slides:**
- Title slide with team names and project name
- Project timeline Gantt chart
- Core objectives checklist
- Technical stack overview

**Speaker 2 Slides:**
- Character class comparison table
- Ability details with icons
- Code snippets for OOP concepts
- Combat flowchart diagram

**Speaker 3 Slides:**
- World map with location markers
- Faction comparison table
- Equipment rarity examples
- Database ER diagram excerpt

**Speaker 4 Slides:**
- Class diagram (Mermaid/UML)
- Code architecture overview
- Educational concept mapping
- Test case examples
