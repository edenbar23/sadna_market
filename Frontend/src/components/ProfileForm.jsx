import React, { useState, useEffect } from 'react';
import "../index.css";

const ProfileForm = ({ userInfo, onSave }) => {
    const [formData, setFormData] = useState({
        email: userInfo.email || '',
        firstName: userInfo.firstName || '',
        lastName: userInfo.lastName || '',
        // phoneNumber: userInfo.phoneNumber || '',
        // address: userInfo.address || '',
    });

    // 🔁 Sync when userInfo changes
    useEffect(() => {
        setFormData({
            email: userInfo.email || '',
            firstName: userInfo.firstName || '',
            lastName: userInfo.lastName || '',
        });
    }, [userInfo]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSave(formData);
    };

    return (
        <form onSubmit={handleSubmit} className="profile-form">
            <h2 className="form-title">Edit Profile</h2>

            <label>Username</label>
            <input type="text" value={userInfo.userName} disabled className="disabled-input" />

            <label>Email</label>
            <input type="email" name="email" value={formData.email} onChange={handleChange} />

            <div className="form-row">
                <div className="form-column">
                    <label>First Name</label>
                    <input type="text" name="firstName" value={formData.firstName} onChange={handleChange} />
                </div>
                <div className="form-column">
                    <label>Last Name</label>
                    <input type="text" name="lastName" value={formData.lastName} onChange={handleChange} />
                </div>
            </div>

            {/*<label>Phone Number</label>*/}
            {/*<input type="text" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} />*/}

            {/*<label>Address</label>*/}
            {/*<textarea name="address" rows={3} value={formData.address} onChange={handleChange}></textarea>*/}

            <button type="submit" className="save-button">Save Changes</button>
        </form>
    );
};

export default ProfileForm;
