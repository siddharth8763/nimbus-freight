# Nimbus Freight — solution design

## Problem

Nimbus Freight hauls temperature-sensitive pharma and grocery loads in refrigerated trucks. Each truck reports a temperature every few minutes. Today, operations is paged on **every** reading outside a limit. Door openings, defrost cycles, and sensor noise look the same as a reefer that is actually failing, so dispatch has started ignoring the feed and can miss a real spoilage or freeze event.

We need one alert per **sustained** problem on a specific truck, not a page per reading, and not a repeat page for the same ongoing incident.

## Assumptions

These are product choices, not facts from the brief. They live in `backend/src/main/resources/application.yml` as `nimbus.alert.*`.

1. **Safe band.** Pharma reefers are commonly held at 2–8°C. We warn **early**, inside that band, rather than waiting until cargo is already out of spec at 8°C.
2. **Too warm.** A warm episode starts when temperature is **strictly above 5.0°C**. A `HIGH_TEMP` alert opens only after the episode has lasted **at least 15 minutes** of sensor time (not “N readings”).
3. **Too cold.** A freeze episode starts when temperature is **strictly below 2.0°C**. A `LOW_TEMP` alert uses the same 15-minute sustain rule. `2.0°C` itself is not a violation.
4. **Hysteresis.** A warm alert resolves only after the truck is **at or below 4.0°C for 10 minutes**. A freeze alert resolves only after it is **at or above 3.0°C for 10 minutes**. That stops a truck hovering on the line from opening and closing alerts.
5. **One open incident.** A truck has at most one open alert. If a sustained warm problem turns into a sustained freeze (or the reverse) before the first alert resolves normally, the open alert is closed as `SUPERSEDED` and the new type is opened. A short blip into the opposite band does **not** close the current alert.
6. **Repeat failures are new incidents.** After an alert resolves, a later sustained violation creates a new alert. While an alert is open we only update its peak (warm) or minimum (freeze).
7. **Gaps.** If the gap between two processed readings is **longer than 30 minutes**, warm and freeze **timers reset**. We do not pretend the cargo stayed hot across an outage. An alert that is already open stays open until later readings resolve it.
8. **Out of order and duplicates.** Every accepted reading is stored for the chart. The state machine moves only when `recordedAt` is **strictly after** the last processed time for that truck. A repeated `(truck, timestamp)` is ignored. Readings outside **-50°C to 50°C** are rejected and do not move state.
9. **Silence is not a temperature alert.** Hours with no telemetry do not by themselves page dispatch. A future `DATA_STALE` signal is the right place for that.
10. **One policy for every cargo type in v1.** Trucks are tagged `PHARMA` or `GROCERY`, but thresholds are global.

Dashboard **OK** means no open alert, no in-progress episode, and the latest reading sits between the freeze-resolve and warm thresholds (3.0–5.0°C with the defaults). **Warming** and **Freezing** mean an episode has started but has not lasted 15 minutes yet.

## Questions we would ask operations

- Is 2–8°C always right, or does each shipment carry its own set point?
- How many minutes above the limit are acceptable before dispatch calls the driver?
- Should freeze pages be as loud as warm pages for grocery as well as pharma?
- If a truck goes quiet for 45 minutes, do you want a “no data” page, or only a temperature page once readings resume?

## Alternatives we did not ship

| Option | Why we passed |
| --- | --- |
| Alert on 3 consecutive hot readings | Interval is “every few minutes,” not fixed. A time window still means 15 minutes if the sensor slows down. |
| Single threshold, no hysteresis | Trucks that sit at 5.1 / 4.9 would flap open and resolved. |
| Bridge a warm episode across a gap | A 40-minute hole is unknown state. Bridging it would page on a guess. |
| Interpolate missing samples | Same problem, with fake data in the chart. |
| Page on every out-of-order spike | Late packets would reopen old incidents. We keep them for the chart and ignore them for state. |

## How a reading becomes an alert

```text
sensor reading
  -> reject if missing, duplicate, or outside -50..50°C
  -> store the row
  -> if timestamp is not newer than the last processed reading, stop
  -> if the gap is over 30 minutes, clear episode timers
  -> update the warm track and the freeze track
  -> open at most one alert, or resolve / supersede the open one
```

Processing is synchronous in the API process. There is no queue. `POST /api/admin/replay-seed` loads `data/seed/readings.csv`, sorts by time, and runs the same ingest path. That is enough to demo five trucks and to re-run the file without duplicating alerts.

## If we had more time

Per-cargo set points, a stale-telemetry alert, defrost-aware patterns, operator acknowledge / snooze, webhooks to dispatch, and more than one sensor per trailer. The optional plain-English summary (`OPENAI_API_KEY`) only rewrites the alert sentence; the decision to alert is entirely the rule engine.
