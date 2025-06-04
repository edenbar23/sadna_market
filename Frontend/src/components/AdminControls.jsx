import React from "react";
import { Link } from "react-router-dom";

export default function AdminControls() {
    return (
        <div className="admin-buttons">
            <Link to="/admin">
                <button className="button admin-button">📊 Dashboard</button>
            </Link>
            <Link to="/admin/users">
                <button className="button admin-button">👥 Users</button>
            </Link>
            <Link to="/admin/stores">
                <button className="button admin-button">🏪 Stores</button>
            </Link>
            <Link to="/admin/reports">
                <button className="button admin-button">📋 Reports</button>
            </Link>
            <Link to="/admin/insights">
                <button className="button admin-button">📈 Insights</button>
            </Link>
        </div>
    );
}