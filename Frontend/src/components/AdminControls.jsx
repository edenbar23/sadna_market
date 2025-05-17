import React from "react";
import { Link } from "react-router-dom";

export default function AdminControls() {
  return (
    <div className="admin-buttons">
      <Link to="/market/toggle-market">
        <button className="button admin-button">Close Market</button>
      </Link>
      <Link to="/market/reports">
        <button className="button admin-button">Reports</button>
      </Link>
      <Link to="/market/users">
        <button className="button admin-button">Users</button>
      </Link>
      <Link to="/market/insights">
        <button className="button admin-button">Insights</button>
      </Link>
    </div>
  );
}
