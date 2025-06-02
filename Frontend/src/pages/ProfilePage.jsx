import React, { useEffect, useState } from 'react';
import ProfileForm from '../components/ProfileForm';
import { returnInfo, changeUserInfo } from '../api/user';
import { useAuthContext } from '../context/AuthContext';
import "../index.css";

const ProfilePage = () => {
    const { user, token } = useAuthContext(); // ✅ use context instead of localStorage
    const [userInfo, setUserInfo] = useState(null);

    const fetchUserInfo = async () => {
        console.log("Calling returnInfo with", user?.username, token);
        try {
            const res = await returnInfo(user.username, token);
            console.log("res.data", res.data);
            const dto = res.data;
            setUserInfo({
                userName: dto.userName,
                email: dto.email,
                firstName: dto.firstName,
                lastName: dto.lastName,
                // phoneNumber: dto.phoneNumber || '',
                // address: dto.address || '',
            });
        } catch (error) {
            console.error("Error fetching profile:", error);
        }
    };


    useEffect(() => {
        if (user?.username && token) {
            fetchUserInfo();
        }
    }, [user, token]);

    const handleSave = async (updatedData) => {
        try {
            const updateRequest = {
                ...updatedData,
                username: user.username,
                //password: 'placeholder-password', // Replace with real password if needed
            };
            const res = await changeUserInfo(user.username, token, updateRequest);
            if (res.data.error) {
                alert("Error updating profile: " + res.data.message);
            } else {
                alert("Profile updated successfully!");
                setUserInfo(res.data);
            }
        } catch (error) {
            console.error("Error updating profile:", error);
            alert("Failed to update profile.");
        }
    };

    return (
        <div className="profile-page">
            <h1 className="profile-title">My Profile</h1>
            {userInfo ? (
                <ProfileForm userInfo={userInfo} onSave={handleSave} />
            ) : (
                <p className="loading-text">Loading profile...</p>
            )}
        </div>
    );
};

export default ProfilePage;
