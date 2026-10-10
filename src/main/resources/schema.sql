CREATE TABLE IF NOT EXISTS trucks (
  id VARCHAR(32) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  cargo_type VARCHAR(16) NOT NULL
);

CREATE TABLE IF NOT EXISTS sensor_readings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  truck_id VARCHAR(32) NOT NULL,
  temperature_c DECIMAL(6,2) NOT NULL,
  recorded_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_reading_truck FOREIGN KEY (truck_id) REFERENCES trucks(id),
  CONSTRAINT uq_truck_time UNIQUE (truck_id, recorded_at)
);

CREATE TABLE IF NOT EXISTS alerts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  truck_id VARCHAR(32) NOT NULL,
  type VARCHAR(16) NOT NULL,
  status VARCHAR(16) NOT NULL,
  started_at TIMESTAMP NOT NULL,
  resolved_at TIMESTAMP NULL,
  peak_temp_c DECIMAL(6,2) NULL,
  min_temp_c DECIMAL(6,2) NULL,
  resolution_reason VARCHAR(16) NULL,
  message VARCHAR(1000) NOT NULL,
  CONSTRAINT fk_alert_truck FOREIGN KEY (truck_id) REFERENCES trucks(id)
);

CREATE TABLE IF NOT EXISTS truck_alert_state (
  truck_id VARCHAR(32) PRIMARY KEY,
  last_processed_at TIMESTAMP NULL,
  warm_since TIMESTAMP NULL,
  warm_resolve_since TIMESTAMP NULL,
  warm_peak_c DECIMAL(6,2) NULL,
  freeze_since TIMESTAMP NULL,
  freeze_resolve_since TIMESTAMP NULL,
  freeze_min_c DECIMAL(6,2) NULL,
  open_high_alert_id BIGINT NULL,
  open_low_alert_id BIGINT NULL,
  CONSTRAINT fk_state_truck FOREIGN KEY (truck_id) REFERENCES trucks(id)
);
