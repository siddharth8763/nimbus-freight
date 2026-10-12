import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  CartesianGrid,
  Line,
  LineChart,
  ReferenceLine,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { fetchAlerts, fetchPolicy, fetchReadings, fetchTruck } from "./api";
import { formatTemp, formatWhen, statusLabel } from "./format";
import type { Alert, Policy, Reading, TruckSummary } from "./types";

export function TruckDetail() {
  const { id = "" } = useParams();
  const [truck, setTruck] = useState<TruckSummary | null>(null);
  const [readings, setReadings] = useState<Reading[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [policy, setPolicy] = useState<Policy | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        const [nextTruck, nextReadings, nextAlerts, nextPolicy] = await Promise.all([
          fetchTruck(id),
          fetchReadings(id),
          fetchAlerts(id),
          fetchPolicy(),
        ]);
        if (!cancelled) {
          setTruck(nextTruck);
          setReadings(nextReadings);
          setAlerts(nextAlerts);
          setPolicy(nextPolicy);
          setError(null);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Could not load truck");
        }
      }
    }
    void load();
    const timer = window.setInterval(() => void load(), 10000);
    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, [id]);

  const points = readings.map((reading) => ({
    time: formatWhen(reading.recordedAt),
    temp: Number(reading.temperatureC),
  }));

  return (
    <div className="app-shell">
      <Link className="back" to="/">← Fleet</Link>
      {error && <p className="error">{error}</p>}
      {truck && (
        <>
          <header className="topbar">
            <div>
              <p className="eyebrow">{truck.id} · {truck.cargoType.toLowerCase()}</p>
              <h1>{truck.name}</h1>
              <p className="sub">Latest {formatTemp(truck.latestTempC)} · {formatWhen(truck.latestRecordedAt)}</p>
            </div>
            <span className={`badge ${truck.status}`}>{statusLabel(truck.status)}</span>
          </header>

          <section className="panel">
            <h2>Temperature</h2>
            {points.length === 0 ? (
              <p className="note">No readings stored for this truck yet.</p>
            ) : (
              <div className="chart-wrap">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={points} margin={{ top: 12, right: 12, left: 0, bottom: 0 }}>
                    <CartesianGrid stroke="#2c3b4a" strokeDasharray="3 3" />
                    <XAxis dataKey="time" tick={{ fill: "#93a4b5", fontSize: 12 }} minTickGap={24} />
                    <YAxis tick={{ fill: "#93a4b5", fontSize: 12 }} unit="°" domain={["auto", "auto"]} />
                    <Tooltip />
                    {policy && (
                      <>
                        <ReferenceLine y={policy.thresholdFreezeC} stroke="#79d0ea" strokeDasharray="4 4" label="Freeze" />
                        <ReferenceLine y={policy.thresholdFreezeResolveC} stroke="#7aa2ff" strokeDasharray="2 4" />
                        <ReferenceLine y={policy.thresholdHighResolveC} stroke="#e2b340" strokeDasharray="2 4" />
                        <ReferenceLine y={policy.thresholdHighC} stroke="#ef6b6b" strokeDasharray="4 4" label="Warm" />
                      </>
                    )}
                    <Line type="monotone" dataKey="temp" stroke="#e8eef4" strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            )}
          </section>

          <section className="panel" style={{ marginTop: "1rem" }}>
            <h2>Incidents</h2>
            {alerts.length === 0 ? (
              <p className="note">No sustained incident on this truck.</p>
            ) : (
              <ul className="alert-list">
                {alerts.map((alert) => (
                  <li key={alert.id}>
                    <div className="alert-title">
                      <strong>{alert.type === "HIGH_TEMP" ? "Too warm" : "Freezing"} · {alert.status}</strong>
                      <span className="meta">
                        {alert.peakTempC !== null ? `peak ${formatTemp(alert.peakTempC)}` : `low ${formatTemp(alert.minTempC)}`}
                      </span>
                    </div>
                    <p className="note">{alert.message}</p>
                    <p className="meta">
                      {formatWhen(alert.startedAt)}
                      {alert.resolvedAt ? ` → ${formatWhen(alert.resolvedAt)} (${alert.resolutionReason})` : ""}
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </div>
  );
}
