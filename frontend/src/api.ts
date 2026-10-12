import type { Alert, Policy, Reading, ReplayResult, TruckSummary } from "./types";

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(path);
  if (!response.ok) {
    throw new Error(`${response.status} ${response.statusText}`);
  }
  return response.json() as Promise<T>;
}

export function fetchTrucks(): Promise<TruckSummary[]> {
  return getJson("/api/trucks");
}

export function fetchTruck(id: string): Promise<TruckSummary> {
  return getJson(`/api/trucks/${id}`);
}

export function fetchReadings(id: string): Promise<Reading[]> {
  return getJson(`/api/trucks/${id}/readings`);
}

export function fetchAlerts(truckId?: string): Promise<Alert[]> {
  const query = truckId ? `?truckId=${encodeURIComponent(truckId)}` : "";
  return getJson(`/api/alerts${query}`);
}

export function fetchPolicy(): Promise<Policy> {
  return getJson("/api/policy");
}

export async function replaySeed(): Promise<ReplayResult> {
  const response = await fetch("/api/admin/replay-seed", { method: "POST" });
  if (!response.ok) {
    throw new Error(`${response.status} ${response.statusText}`);
  }
  return response.json() as Promise<ReplayResult>;
}
