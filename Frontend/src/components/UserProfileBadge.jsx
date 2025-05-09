import React from "react";

function UserProfileBadge({ user }) {
  return (
    <div className="flex items-center space-x-2">
      <img
        src="https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"
        alt="Profile"
        className="profile-pic"
      />
      <span>{user ? user.name : "Guest"}</span>
    </div>
  );
}

export default UserProfileBadge;
