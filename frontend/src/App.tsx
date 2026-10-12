import { BrowserRouter, Route, Routes } from "react-router-dom";
import { Dashboard } from "./Dashboard";
import { TruckDetail } from "./TruckDetail";

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/trucks/:id" element={<TruckDetail />} />
      </Routes>
    </BrowserRouter>
  );
}
