import React from "react";
import { useAuthContext } from "../context/AuthContext";
import { Link } from "react-router-dom";

function UserProfileBadge() {
    const { user, isAuthenticated } = useAuthContext();

    return (
        <div className="flex items-center space-x-2">
            <img
                src="https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"
                alt="Profile"
                className="profile-pic"
            />
            <span>{isAuthenticated ? (
                <Link
                    to="/my-profile"
                    title="Click for user info"
                    className="username-link"
                >
                    {user.username}
                </Link>
            ):
                (
                    <span className="guest-label">Guest</span>
                )}</span>
        </div>
    );
}

export default UserProfileBadge;