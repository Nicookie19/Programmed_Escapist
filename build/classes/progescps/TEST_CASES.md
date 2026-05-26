# Programmed Escapist — Test Cases (Black-box)

This file contains representative black-box test cases for the Programmed Escapist project. Each entry lists the feature under test, a short test case (user action / input), and the expected outcome.

Table legend: Feature | Test Case | Expected Outcome

## Core UI & Startup

| Feature | Test Case | Expected Outcome |
|---|---|---|
| Launch / Splash / Main Menu | User runs the application (`progescps.TurnBased_RPG`) and waits for UI to appear | Application displays splash screen then main menu; "New Game", "Load Game", "Quit" buttons visible |
| Start New Game | Click "New Game" from main menu, select a class (e.g., Hacker), confirm | Game UI opens, selected class label shown, HP/mana values reflect class defaults, input box focused |
| Start while previous game running | While a game loop is active, click "New Game" | Previous game loop is requested to stop, pending inputs cleared, new game starts with fresh state (no stale input consumed) |

## Character / Hero selection

| Feature | Test Case | Expected Outcome |
|---|---|---|
| Character selection validation | Attempt to start game without selecting a class (if allowed by UI) | UI prevents start or selects default class; no null-pointer or crash occurs |
| Allowed equipment enforcement | Equip a weapon not allowed by the chosen class via inventory UI (if UI supports) | System rejects the equip action and shows message; hero's equipment unchanged |

## Combat & Skills

| Feature | Test Case | Expected Outcome |
|---|---|---|
| Basic attack | In combat, choose "Attack" action | Enemy HP decreases by a value between player's min and max damage; combat log prints damage; enemy remains alive or dies accordingly |
| Skill with cooldown | Use a class skill that sets a cooldown (e.g., Debugger's Debug) and then attempt to use it again before cooldown expires | First use applies effect and consumes mana; subsequent attempt is rejected and a message indicates remaining cooldown |
| Insufficient mana | Attempt to use a skill when mana < required | Skill is not executed; mana unchanged; friendly message shown and no crash occurs |
| Enemy special action | Force enemy to use special ability (via seeded RNG or test enemy) | Enemy performs special and expected game state change occurs (e.g., apply status to player) |
| Flee attempt | Select "Flee" during combat | If flee succeeds per design, combat ends and player returns to travel/menu; if fails, combat continues and enemy gets a turn |
| Status effects ticking | Apply a status effect (burn/poison) and advance turns | Each turn, the effect deals its damage or applies modifier until duration reaches zero, then restore original stats |

## Quest and Faction mechanics

| Feature | Test Case | Expected Outcome |
|---|---|---|
| Add quest | Receive a new quest from QuestManager | Quest appears in active quests; updating objectives progresses quest; upon completion rewards applied to hero and quest moves to completed list |
| Update quest progress | Trigger the quest's objective (simulate an objective completion) | Quest progress increments; when final objective completed, reward applied (gold/xp/item) and quest removed from active list |

## Inventory, Items & Equipment

| Feature | Test Case | Expected Outcome |
|---|---|---|
| Add item to inventory | Acquire an item (e.g., "Energy Drink") | Item is added to inventory list; weight accumulates; if over capacity, UI warns but still accepts unless design prevents it |
| Equip / Unequip item | Equip a valid weapon and armor, then unequip | Equipment updates correctly and damage/defense values change accordingly |
| Invalid equip | Try to equip an item not present in inventory | Action rejected; inventory unchanged; friendly message shown |

## Save/Load and Persistence

| Feature | Test Case | Expected Outcome |
|---|---|---|
| Save game (slot) | From in-game UI, save to slot 1 with name "TestSave" | `game_saves` entry created (or `saves/<slot>` file). On successful save, UI shows confirmation and save metadata (time/version) |
| Load game | Load previously saved slot 1 | Player state (hp, mana, level, equipped items, inventory, quests) is restored exactly as at save time |
| Invalid load slot | Attempt to load from an empty/nonexistent slot | UI displays understandable error and no crash occurs |
| Autosave behavior | Trigger autosave (if implemented) and then crash/restart and load autosave | Autosave slot exists and restores latest state |

## Input & UI Robustness

| Feature | Test Case | Expected Outcome |
|---|---|---|
| InputProvider blocking | Submit input via UI text field and ensure GameManager.nextLine() receives it | Submitted input unblocks game loop; correct action executed |
| Clear pending on new game | While queue has pending strings, click New Game | Pending queue is cleared before new game; stale inputs are not consumed by the new run |
| Console capture | Generate log output (System.out.println) and verify it appears in UI console area | Console area appends messages and auto-scrolls; no UI freeze occurs |

## Edge / Negative Cases

| Feature | Test Case | Expected Outcome |
|---|---|---|
| Corrupt save JSON | Manually edit saved state's JSON to invalid JSON and attempt load | Load fails gracefully with an error message; app does not crash (either skip load or create fresh state) |
| Missing resources | Remove a required resource (e.g., image used by UI) and launch the UI | Application handles missing resource gracefully (placeholder used) and does not crash |
| Stop request race | Rapidly press "New Game" multiple times | Only one new run starts; previous requests are stopped and no duplicate game loops remain active |

## Acceptance Criteria / How to mark PASS

- For each test case, run the steps and confirm the Expected Outcome exactly or acceptably matches. Record PASS/FAIL, timestamp, tester name and any error logs.
- For persistence tests, verify saved data either in `saves/` files or in the database tables described in the SQL dump.

---

If you'd like, I can:
- add these test cases as a CSV or spreadsheet in `docs/` for easier tracking,
- add a small test runner script that performs some of the "smoke" checks automatically (start UI headless tests where possible), or
- convert selected test cases into unit/integration tests (JUnit) for parts of the code (e.g., Combat.calculateDamage, StatusEffect.apply/tick/restore).
