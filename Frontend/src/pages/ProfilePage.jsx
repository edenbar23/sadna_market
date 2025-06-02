import React, { useEffect, useState } from 'react';
import ProfileForm from '../components/ProfileForm';
import { returnInfo, changeUserInfo } from '../api/user';
import "../index.css";

const ProfilePage = () => {
    const [userInfo, setUserInfo] = useState(null);
    const [token, setToken] = useState('');
    const [username, setUsername] = useState('');

    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        const storedUsername = localStorage.getItem('username');
        setToken(storedToken);
        setUsername(storedUsername);

        const fetchUserInfo = async () => {
            try {
                const res = await returnInfo(storedUsername, storedToken);
                const dto = res.data.data;
                setUserInfo({
                    userName: dto.userName,
                    email: dto.email,
                    firstName: dto.firstName,
                    lastName: dto.lastName,
                    phoneNumber: dto.phoneNumber || '',
                    address: dto.address || '',
                });
            } catch (error) {
                console.error("Error fetching profile:", error);
            }
        };

        if (storedToken && storedUsername) {
            fetchUserInfo();
        }
    }, []);

    const handleSave = async (updatedData) => {
        try {
            const updateRequest = {
                ...updatedData,
                username,
                password: 'placeholder-password', // Replace with real password if needed
            };
            const res = await changeUserInfo(username, token, updateRequest);
            if (res.data.error) {
                alert("Error updating profile: " + res.data.message);
            } else {
                alert("Profile updated successfully!");
                setUserInfo(res.data.data);
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
