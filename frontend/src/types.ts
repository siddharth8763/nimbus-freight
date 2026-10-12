export type CargoType = "PHARMA" | "GROCERY";
export type AlertType = "HIGH_TEMP" | "LOW_TEMP";
export type AlertStatus = "OPEN" | "RESOLVED";
export type ResolutionReason = "NORMAL" | "SUPERSEDED";
export type TruckStatus =
  | "OK"
  | "WARMING"
  | "FREEZING"
  | "ALERT_HIGH"
  | "ALERT_LOW"
  | "WATCH"
  | "NO_DATA";

export interface Alert {
  id: number;
  truckId: string;
  truckName: string;
  type: AlertType;
  status: AlertStatus;
  startedAt: string;
  resolvedAt: string | null;
  peakTempC: number | null;
  minTempC: number | null;
  resolutionReason: ResolutionReason | null;
  message: string;
}

export interface TruckSummary {
  id: string;
  name: string;
  cargoType: CargoType;
  latestTempC: number | null;
  latestRecordedAt: string | null;
  status: TruckStatus;
  openAlert: Alert | null;
}

export interface Reading {
  id: number;
  truckId: string;
  temperatureC: number;
  recordedAt: string;
}

export interface Policy {
  thresholdHighC: number;
  thresholdHighResolveC: number;
  thresholdFreezeC: number;
  thresholdFreezeResolveC: number;
  sustainDurationMinutes: number;
  resolveDurationMinutes: number;
  gapResetMinutes: number;
}

export interface ReplayResult {
  rowsRead: number;
  processed: number;
  duplicates: number;
  storedOutOfOrder: number;
  rejectedInvalid: number;
}
