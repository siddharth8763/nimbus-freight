import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchAlerts, fetchTrucks, replaySeed } from "./api";
import { formatTemp, formatWhen, statusLabel } from "./format";
import type { Alert, ReplayResult, TruckSummary } from "./types";

export function Dashboard() {
  const [trucks, setTrucks] = useState<TruckSummary[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [replay, setReplay] = useState<ReplayResult | null>(null);

  async function load() {
    const [nextTrucks, nextAlerts] = await Promise.all([fetchTrucks(), fetchAlerts()]);
    setTrucks(nextTrucks);
    setAlerts(nextAlerts);
  }

  useEffect(() => {
    let cancelled = false;
    async function refresh() {
      try {
        await load();
        if (!cancelled) {
          setError(null);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Could not reach the API");
        }
      }
    }
    void refresh();
    const timer = window.setInterval(() => void refresh(), 10000);
    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, []);

  async function onReplay() {
    setBusy(true);
    setError(null);
    try {
      const result = await replaySeed();
      setReplay(result);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Replay failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Nimbus Freight</p>
          <h1>Reefer watch</h1>
          <p className="sub">Sustained warm and freeze incidents only. Brief blips stay quiet.</p>
        </div>
        <button type="button" onClick={() => void onReplay()} disabled={busy}>
          {busy ? "Replaying seed…" : "Replay seed data"}
        </button>
      </header>

      {error && <p className="error">{error}</p>}
      {replay && (
        <p className="note">
          Last replay: {replay.processed} processed, {replay.duplicates} duplicates, {replay.rejectedInvalid} rejected.
        </p>
      )}

      <div className="layout">
        <section className="panel">
          <h2>Fleet</h2>
          <div className="truck-grid">
            {trucks.map((truck) => (
              <Link key={truck.id} to={`/trucks/${truck.id}`} className="truck-card">
                <div>
                  <div className="truck-name">{truck.name}</div>
                  <div className="meta">
                    {truck.id} · {truck.cargoType.toLowerCase()} · {formatWhen(truck.latestRecordedAt)}
                  </div>
                </div>
                <div className="truck-side">
                  <div className="temp">{formatTemp(truck.latestTempC)}</div>
                  <span className={`badge ${truck.status}`}>{statusLabel(truck.status)}</span>
                </div>
              </Link>
            ))}
          </div>
        </section>

        <section className="panel">
          <h2>Alerts</h2>
          {alerts.length === 0 ? (
            <p className="note">No incidents yet. Replay the seed to walk through the five trucks.</p>
          ) : (
            <ul className="alert-list">
              {alerts.map((alert) => (
                <li key={alert.id}>
                  <div className="alert-title">
                    <strong>
                      {alert.truckName} · {alert.type === "HIGH_TEMP" ? "Too warm" : "Freezing"}
                    </strong>
                    <span className="meta">{alert.status}</span>
                  </div>
                  <p className="note">{alert.message}</p>
                  <p className="meta">
                    From {formatWhen(alert.startedAt)}
                    {alert.resolvedAt ? ` · resolved ${formatWhen(alert.resolvedAt)} (${alert.resolutionReason})` : ""}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
