import type { TruckStatus } from "./types";

export function statusLabel(status: TruckStatus): string {
  switch (status) {
    case "OK":
      return "OK";
    case "WARMING":
      return "Warming";
    case "FREEZING":
      return "Freezing";
    case "ALERT_HIGH":
      return "Alert · warm";
    case "ALERT_LOW":
      return "Alert · freeze";
    case "WATCH":
      return "Watch";
    case "NO_DATA":
      return "No data";
  }
}

export function formatTemp(value: number | null): string {
  if (value === null || value === undefined) {
    return "—";
  }
  return `${Number(value).toFixed(1)}°C`;
}

export function formatWhen(value: string | null): string {
  if (!value) {
    return "—";
  }
  return new Date(value).toLocaleString(undefined, {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}
