# Judge Q&A — rehearse this

How to use it: answer out loud in under a minute. The **Trap** line is the follow-up that usually comes next. If you do not know, say what you assumed and what you would ask the customer. Do not invent behavior the code does not have.

Defaults, unless a question says otherwise: warm above **5.0°C** for **15 min**, resolve at or below **4.0°C** for **10 min**. Freeze below **2.0°C** for **15 min**, resolve at or above **3.0°C** for **10 min**. Gap reset **more than 30 min**. Time is the **sensor timestamp**, not when the server received the row.

---

## 1. Framing

**Q. The brief never defined “too warm” or “too long.” Why should we trust your numbers?**

A. They are assumptions, not facts. I anchored them to a typical pharma reefer band of 2–8°C, then warned *early*: page only after 15 minutes above 5°C, not on the first sample and not only after the load is already at 8°C. Fifteen minutes is longer than a door-open blip and short enough to act. Every number is in `application.yml`. I would ask operations for the real set point and the maximum minutes before they call the driver.

Trap: “So you might be paging too early.” Yes. Early warning trades some extra alerts for not waiting until cargo is already out of spec. If they want 8°C for 30 minutes, it is a config change, not a rewrite.

**Q. The brief only mentioned too warm. Why freeze alerts?**

A. Pharma and grocery both spoil when they freeze. The same sustain and hysteresis pattern covers `LOW_TEMP`. Warm is still the brief’s main case; freeze is the symmetric risk I chose to include and document.

**Q. What did you optimize for?**

A. Dispatch trust. One open incident per truck, no page per reading, no second page while that incident is still open. Missed sustained failures and alert fatigue are both failures; the 15-minute gate is the balance.

---

## 2. What counts as a violation

**Q. A reading is exactly 5.0°C for an hour. Alert?**

A. No. Warm starts only when temperature is **strictly above** 5.0. Five on the nose is not a warm episode.

**Q. Exactly 2.0°C for an hour?**

A. No. Freeze starts only when temperature is **strictly below** 2.0.

**Q. 5.1°C for 14 minutes, then back to 4°C?**

A. No alert. Status can show **Warming** while the episode is open. When it leaves the warm range before 15 minutes, the warm timer is cleared. That is the blip filter. NF-01 demonstrates a short warm blip.

**Q. 5.1°C at t=0 and the next reading is 5.1°C at t=15?**

A. One `HIGH_TEMP` alert. Sustain is `>= 15 minutes` of sensor time from the first warm reading to the current one. `started_at` is that first warm reading, not the moment we decided to page. The page is created on the reading that crosses the line.

Trap: “So you waited 15 minutes to tell anyone.” Yes. Those 15 minutes are the product decision that kills blips. The alert records when the excursion **began**.

**Q. One reading at 12°C and then the sensor dies?**

A. No alert. Duration is zero until a later reading is still warm at least 15 minutes later. A single spike can never open an alert. If the sensor dies during **Warming**, we also do not raise a “sensor silent” alert in v1. That is a known hole.

**Q. Hot at t=0, next sample at t=20 is already 4°C?**

A. No alert. The reading that would have proved 15 minutes is no longer a violation, so the episode is dropped. We do not infer that it “must have been hot in between.”

**Q. What about 2.5°C sitting there all day?**

A. No freeze alert. 2.5 is not below 2. It is also outside the dashboard “OK” band (3.0–5.0), so the truck shows **Watch**, not OK and not Freezing. I would flag that band to the customer: Watch means “not in the comfort band, not yet an incident.”

---

## 3. Hysteresis and the dead zone

**Q. Why not resolve as soon as it drops back through 5°C?**

A. A truck hovering at 4.9 / 5.1 would open and close all day. Resolve requires **at or below 4°C for 10 minutes**. The 1°C gap is hysteresis.

**Q. It has an open warm alert and sits at 4.5°C for two hours.**

A. The alert **stays open**. 4.5 is not above 5, so it is not still “in violation,” and it is not at or below 4, so the resolve clock never starts. The dead zone is intentional: we do not call it recovered until it is clearly back. Same idea on freeze: an open freeze alert stays open while temperature is between 2 and 3.

Trap: “That could leave a stale page.” Yes, until it spends 10 minutes at or below 4, or a human closes it. v1 has no acknowledge/snooze. I would tell ops that recovery is strict on purpose.

**Q. Resolve clock starts, then one reading pops back to 4.5°C.**

A. The resolve timer **clears**. It is not accumulated credit. They need **10 continuous minutes** at or below 4, measured from the first cool reading to a later reading that is still cool.

**Q. First cool reading is 3.5°C and the next cool reading is 10 minutes later.**

A. Alert resolves `NORMAL` on that second reading. `resolved_at` is that reading’s timestamp. One cool sample is never enough, because its duration is zero.

---

## 4. Same incident vs new incident

**Q. It stays at 9°C for three hours. How many alerts?**

A. One. While it is open we only update `peak_temp_c`. We do not page again.

**Q. It recovers properly, then overheats again tomorrow.**

A. A **second** alert. A new episode after `RESOLVED` is a new incident. NF-02 is this case: one resolved warm alert, then a second one left open. The first peak was 9.2°C; the open one tracks its own peak (8.6°C).

**Q. Why can a truck have only one open alert?**

A. Temperature cannot be both above 5 and below 2. Two open pages for one box is how the old system trained people to ignore it. If a warm incident **becomes** a sustained freeze, the warm alert closes as `SUPERSEDED` and a freeze alert opens.

**Q. Open warm alert, then 1°C for 5 minutes, then back to 4°C.**

A. The warm alert **stays open**. A short trip into freeze does not mean the warm incident recovered, and it is not long enough to supersede. I will not close a real page because of a 5-minute plunge.

**Q. Open warm alert, then below 2°C for 15 minutes.**

A. Warm alert becomes `RESOLVED` / `SUPERSEDED`. A `LOW_TEMP` alert opens. `started_at` on the freeze alert is the first freeze reading of that episode.

---

## 5. Gaps and silence

**Q. Warm for 10 minutes, no data for 45 minutes, then 8°C again.**

A. **No alert yet.** A gap **longer than 30 minutes** clears episode timers. The post-gap reading starts a **new** warm clock. We do not pretend the trailer stayed hot during an outage. Exactly 30 minutes does **not** reset; only **more than** 30. NF-04 is the gap case.

Trap: “You might miss a failure that started before the outage.” Correct. Bridging the gap would page on a guess. I would rather restart the clock.

**Q. The alert is already open, then the sensor is quiet for two hours.**

A. The **open alert stays open**. The gap clears timers, not the incident. If nothing else arrives, dispatch still sees the open page. That is the behavior I want when the box was already bad and then went dark.

**Q. The truck was only Warming, then it goes dark for an hour.**

A. No temperature alert, and no “no data” alert. This is the worst v1 hole: a failure that never reached 15 minutes, plus a dead radio, is invisible. I would add a separate `DATA_STALE` alert later. I did not overload the temperature rule to mean “missing.”

**Q. Why not interpolate the missing samples?**

A. Interpolated temperatures would show up on the chart as if the sensor said them. That is worse than a gap.

---

## 6. Late, duplicate, and bad data

**Q. A 20°C reading arrives late, with a timestamp older than the last one we processed.**

A. It is **stored** for the chart and **ignored** by the state machine. Late packets must not reopen an old incident or move `last_processed_at` backwards. Status follows the engine plus the newest timestamp, not the late spike.

**Q. Two posts with the same truck and the same timestamp, different temperatures.**

A. The first one wins. The second is a duplicate and is not applied. Replay is built so running the file twice does not double readings or alerts.

Trap: “What if the first one was wrong?” v1 has no correction API. A corrected sample needs a new timestamp or an explicit retract, which we did not build.

**Q. Temperature is null, or 999°C.**

A. Rejected. Valid range is **-50 to 50°C**. Nothing is stored and state does not move. NF-05 includes a 999°C row that the replay counts as rejected.

**Q. The seed file is not in time order.**

A. Replay **sorts by timestamp**, then truck id, and then uses the same ingest path as a live post. File order is not trusted. Out-of-order behavior is still tested directly, because after the sort the seed itself is in order.

**Q. Two trucks report the same timestamp.**

A. Independent. Sort order between them does not matter; each truck has its own state.

---

## 7. Clock and batching

**Q. The server receives an hour of readings in one burst.**

A. Sustain uses **sensor time**, not arrival time. If the timestamps really span an hour and the box was hot the whole way, one alert opens when the 15-minute point is processed. If every timestamp was stamped “now” by a bad gateway, duration collapses and we will not alert. I would ask whether the timestamp is sensor time or ingest time.

**Q. Why not “three hot readings in a row”?**

A. The brief says “every few minutes,” not a fixed period. Three readings could be 6 minutes or 30. A clock window stays 15 minutes even if the sensor slows down.

---

## 8. Product edges they will poke

**Q. Defrost runs hot for 20 minutes every cycle.**

A. With these defaults, that **will alert**. The 15-minute gate only filters short spikes. I would ask how long a normal defrost is before I raised the window or added a defrost flag. I will not pretend the rule knows about compressors.

**Q. A sensor stuck at 25°C. A sensor stuck at 4°C.**

A. Stuck hot: one open alert, then silence from us, which is correct. Stuck at a healthy 4°C: looks **OK**. v1 does not detect a flatlined or dead probe. I would say that out loud before they do.

**Q. Grocery vs pharma on the same thresholds.**

A. v1 is one global policy. `cargo_type` is stored so the next step is per-cargo numbers, not a schema change. I would not pretend they already differ.

**Q. Door sensor, GPS, second probe?**

A. Not in the feed I was given. I would not invent a door-open exception without a signal.

**Q. Who closes the alert if the truck never cools down?**

A. Nobody, in v1. It stays open. There is no ack, snooze, or manual resolve. That is a dispatcher workflow I would add after the rule is trusted.

**Q. You have an LLM in the design. Does it decide the alert?**

A. No. The rule engine decides. If `OPENAI_API_KEY` is set, the model may rewrite the sentence. If the call fails or the key is missing, a fixed sentence is stored. Pull the key and the alerts are identical.

---

## 9. Engineering edges

**Q. Two readings for the same truck arrive at the same time on two servers.**

A. Not safe. Ingest is transactional per request, but there is no truck-level lock and this process is a single demo instance. Parallel posts can race. For production I would serialize per truck. I will not claim it is horizontally safe.

**Q. Replay dies halfway.**

A. Rows already committed stay. Run it again: duplicates are skipped, and only the rest is processed. Alerts already created are not duplicated because those readings are not evaluated twice.

**Q. Why synchronous, no Kafka?**

A. The failure mode that matters here is the rule, not throughput. Five trucks and a seed file do not need a bus. A queue is the right next step when ingest is continuous and the API must not block on it.

**Q. Why MySQL and also H2?**

A. MySQL is the real target (`docker compose`). The `dev` profile is file-backed H2 so the demo still runs if Docker is down. Same schema script, same engine. I would not demo H2 as the production store.

**Q. How do I know the seed matches the story?**

A. `FleetBehaviorTest` replays the CSV and checks each truck: Aurora freezing, Borealis open warm after a resolved one, Cirrus open freeze, Drift warming with no alert, Ember one warm alert. A second replay inserts nothing. Engine unit tests cover the boundaries without the database: 14 vs 15 minutes, exactly 30 vs 31 minute gap, supersede, second episode.

---

## 10. Design tradeoffs

Answer these as a choice, not a fact. Name what you gave up.

**Q. Why 15 minutes of clock time instead of three hot readings?**

A. The sensor interval is “every few minutes,” not fixed. Three readings might be six minutes or thirty. A duration rule still means 15 minutes if the radio slows down.

Gave up: simplicity. A counter is easier to explain. It lies when the sampling rate changes.

**Q. Why warn at 5°C instead of the 8°C spec limit?**

A. 8°C is already out of the usual 2–8°C pharma band. 5°C is an early warning inside the band so dispatch can act before the load is non-compliant.

Gave up: fewer alerts. Trucks that sit at 6°C will page even though a strict reading of the label might still say “in range.” If ops wants the legal ceiling, the threshold is one config value.

**Q. Why two thresholds (5 to open, 4 to close) instead of one?**

A. A single line at 5°C flaps. 5.1 opens, 4.9 closes, 5.1 opens again. Hysteresis makes recovery mean “clearly cooler,” not “touched the line once.”

Gave up: a dead zone. An open alert at 4.5°C stays open until it is at or below 4°C for 10 minutes. I would rather leave a page up than flap.

**Q. Why the same pattern for freeze, with 2°C to open and 3°C to close?**

A. Freeze is the other way the cargo is destroyed, and the flap problem is identical. One policy shape, two directions.

Gave up: scope. The brief only required warm. I added freeze because the domain needs it, and I documented it as a choice. The dashboard OK band (3–5°C) is narrower than 2–8°C because of those two hysteresis gaps. 2.5°C shows Watch, not OK.

**Q. Why one open alert per truck instead of one warm and one freeze at the same time?**

A. The box cannot be above 5 and below 2 at once. Two open rows train people to ignore the inbox. A real swing closes the old alert as `SUPERSEDED` and opens the new type.

Gave up: history of both being “open” together. The superseded row is still in the list, so the swing is visible. A five-minute dip the other way does not close the current page.

**Q. Why reset the warm clock after a gap longer than 30 minutes, instead of bridging or interpolating?**

A. During the hole we do not know the temperature. Bridging would count outage time as “still hot.” Interpolating would draw temperatures the sensor never sent.

Gave up: recall. A failure that started before the outage and never reached 15 minutes is forgotten when the gap hits. An alert that was **already open** is kept. I would add a separate stale-data alert before I bridged the temperature clock.

**Q. Why keep late readings on the chart but ignore them in the state machine?**

A. Dropping them hides what the device sent. Applying them out of order moves the incident clock backwards and can reopen a closed event.

Gave up: a late but true spike never changes state if its timestamp is older than the last processed one. The chart still shows it, so a human can see the conflict. There is no correction API if the first duplicate was the wrong value.

**Q. Why reject 999°C instead of clamping it?**

A. A clamped 50°C would still look like a real sustained failure and page someone. Out of range means the sample is not a temperature.

Gave up: visibility of the bad sample in the chart. It is counted on replay and not stored. I would rather lose the point than alert on garbage.

**Q. Why process inside the API call instead of a queue?**

A. The thing being judged is the rule, and the demo is five trucks. Synchronous ingest makes replay, tests, and the dashboard one path.

Gave up: ingest latency and horizontal scale. A burst that blocks the request thread is the production reason to add a queue. I would not add Kafka to look distributed.

**Q. Why MySQL for the design and H2 for local dev?**

A. The schema and the engine are the product. MySQL is what I would run with Docker. H2 is only so the demo still boots when Docker is not installed. Same SQL script.

Gave up: a guarantee that every MySQL quirk was hit in the demo. I should say which database is actually running.

**Q. Why is cargo type stored but unused in the rule?**

A. Pharma and grocery may need different set points, and I do not have those numbers. Shipping one global policy is honest. The column is there so the next change is config, not a migration.

Gave up: a more impressive per-truck matrix I cannot defend.

**Q. Why a fixed sentence, with an optional model rewrite?**

A. The decision to page must be explainable without an API key. The model only rewrites words. If it fails, the template is what gets stored.

Gave up: the bonus-track polish when there is no key. I will not claim the model detected the incident.

**Q. Why polling every 10 seconds and no login, ack, or webhook?**

A. Those are delivery and workflow. v1 had to prove which readings become an incident. A websocket does not fix a bad rule.

Gave up: a live ops console and any way for a dispatcher to own the page. I would add ack/snooze and a stale-sensor alert before I added another service.

**Q. What would you change first if Priya Shah disagreed?**

A. The numbers in `application.yml`: 5°C, 15 minutes, 4°C, 10 minutes, 30-minute gap. I would not change the shape — duration, hysteresis, one open incident, no bridging — unless she explicitly wanted flapping or guessed-through-outages.

---

## 11. If they push you off the happy path

Say this, then stop:

1. State the rule in one sentence.
2. Say what the truck status and alert row will be.
3. Name the assumption.
4. Name what you would ask Priya Shah before changing it.

Example: “4.5°C with an open warm alert stays open, because recovery is 4°C for 10 minutes, not ‘dipped under 5 once.’ If ops wants it to clear at 5, I would remove hysteresis and accept flapping. I would not silently change that in the room.”

---

## Quick card

| Situation | Result |
| --- | --- |
| 8 min above 5°C | No alert (Warming, then clear) |
| 15 min above 5°C | One HIGH_TEMP, peak tracked |
| Still hot for hours | Same open alert |
| Recovers ≤4°C for 10 min, fails again | Second alert |
| Sits at 4.5°C while open | Stays open |
| Exactly 5.0 or exactly 2.0 | Not a violation |
| 2.5°C all day | Watch, no alert |
| Gap > 30 min while only warming | Timer restarts, no alert yet |
| Gap > 30 min while already open | Alert stays open |
| Late older timestamp | Chart only |
| Duplicate timestamp | First value kept |
| 999°C | Rejected |
| Warm open, then freeze for 15 min | Supersede, then LOW_TEMP |
| Warm open, freeze for 5 min | Warm alert stays |
| One hot sample, sensor dies | No alert, no stale alert |
| Defrost longer than 15 min | Alerts, until they say otherwise |
| LLM down | Template sentence, same decision |

## Tradeoff card

| They suggest | You chose | What you gave up |
| --- | --- | --- |
| 3 hot readings | 15 min of sensor time | A simpler counter |
| Alert at 8°C | Alert above 5°C | Fewer pages |
| One threshold | 5 open / 4 close | A dead zone at 4.5°C |
| Two open alerts | One, else supersede | Both “open” at once |
| Bridge the gap | Reset after >30 min | Recall across an outage |
| Drop late packets | Store, don’t apply | Late spikes don’t move state |
| Clamp 999°C | Reject it | Bad points on the chart |
| Kafka | Sync ingest | Burst headroom |
| Per-cargo rules now | One global policy | A matrix you cannot defend |
| Model decides | Model rewrites text | AI as the core |
