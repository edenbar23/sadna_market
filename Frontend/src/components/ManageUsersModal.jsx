import React, { useState, useEffect, useMemo } from 'react';
import ReactDOM from 'react-dom';
import { useAuthContext } from '../context/AuthContext';
import { useStorePersonnel } from '../hooks/useStorePersonnel';

const modalRoot = document.getElementById('root');

const ALL_PERMISSIONS = [
  'VIEW_STORE_INFO', 'VIEW_PRODUCT_INFO', 'MANAGE_INVENTORY', 'ADD_PRODUCT', 'REMOVE_PRODUCT', 'UPDATE_PRODUCT',
  'MANAGE_PURCHASE_POLICY', 'MANAGE_DISCOUNT_POLICY',
  'APPOINT_STORE_OWNER', 'REMOVE_STORE_OWNER', 'APPOINT_STORE_MANAGER', 'REMOVE_STORE_MANAGER', 'UPDATE_MANAGER_PERMISSIONS',
  'CLOSE_STORE', 'REOPEN_STORE',
  'VIEW_STORE_PURCHASE_HISTORY', 'RESPOND_TO_USER_INQUIRIES', 'RESPOND_TO_BID', 'MANAGE_AUCTIONS', 'MANAGE_LOTTERIES'
];
const VIEW_PERMS = ['VIEW_STORE_INFO', 'VIEW_PRODUCT_INFO'];

async function fetchPermissions(storeId, targetUsername, token, requester) {
  const url = `http://localhost:8081/api/stores/${storeId}/permissions/${targetUsername}?byUser=${requester}`;
  try {
    const res = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    if (!res.ok) {
      throw new Error(`Server error: ${res.status} - ${(await res.text())}`);
    }
    const contentType = res.headers.get("content-type");
    if (!contentType?.includes("application/json")) throw new Error("Invalid JSON response");
    const json = await res.json();
    return Array.isArray(json) ? json : [];
  } catch {
    return [];
  }
}

export default function ManageUsersModal({ storeId, personnel, isOpen, onClose }) {
  const { user } = useAuthContext();
  const { updateOwnerPermissions, updateManagerPermissions } = useStorePersonnel(storeId, user);

  const [permMap, setPermMap] = useState({});
  const [saving, setSaving] = useState({});
  const [error, setError] = useState({});
  const [loading, setLoading] = useState(false);
  const [editableManagers, setEditableManagers] = useState({});

  const filteredPersonnel = useMemo(
    () => personnel.filter(p => p.username !== user.username),
    [personnel, user.username]
  );

  useEffect(() => {
    if (!isOpen) return;
    const loadPermissionsAndEditability = async () => {
      setLoading(true);
      const token = localStorage.getItem('token');
      const requester = user.username;
      const permInit = {};
      const editStatus = {};

      // Fetch all permissions first
      for (const p of filteredPersonnel) {
        permInit[p.username] = await fetchPermissions(storeId, p.username, token, requester);
      }
      setPermMap(permInit);

      // Dynamically test if current user can update manager perms
      for (const p of filteredPersonnel) {
        if (p.role === 'Manager') {
          try {
            // Attempt a dummy save with no changes—if forbidden, lock it
            await updateManagerPermissions(p.username, permInit[p.username] || []);
            editStatus[p.username] = true;
          } catch {
            editStatus[p.username] = false;
          }
        }
      }
      setEditableManagers(editStatus);

      setLoading(false);
    };
    loadPermissionsAndEditability();
  }, [isOpen, storeId, filteredPersonnel, user.username]);

  const toggle = (username, code) => {
    setPermMap(prev => {
      const current = new Set(prev[username] || []);
      current.has(code) ? current.delete(code) : current.add(code);
      return { ...prev, [username]: [...current] };
    });
  };

  const saveForUser = async (person) => {
    setSaving(prev => ({ ...prev, [person.username]: true }));
    setError(prev => ({ ...prev, [person.username]: null }));
    try {
      const codes = permMap[person.username] || [];
      if (person.role === 'Owner') {
        await updateOwnerPermissions(person.username, codes);
      } else {
        await updateManagerPermissions(person.username, codes);
      }
    } catch (err) {
      setError(prev => ({
        ...prev,
        [person.username]: err.message || 'Failed to update permissions'
      }));
    }
    setSaving(prev => ({ ...prev, [person.username]: false }));
  };

  if (!isOpen) return null;

  return ReactDOM.createPortal(
    <div
      onClick={onClose}
      style={{
        position: 'fixed',
        top: 0, left: 0, right: 0, bottom: 0,
        background: 'rgba(0,0,0,0.4)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          background: '#fff',
          borderRadius: 20,
          padding: 36,
          width: '85%',
          maxWidth: 980,
          maxHeight: '90%',
          overflowY: 'auto',
          boxShadow: '0 10px 36px rgba(0,0,0,0.16)',
          fontFamily: 'Segoe UI, Arial, sans-serif'
        }}
      >
        <h2 style={{ marginTop: 0, marginBottom: 28, fontWeight: 600, fontSize: 26, letterSpacing: 0.5 }}>
          Manage Store Personnel
        </h2>

        {loading ? <p>Loading permissions...</p> : filteredPersonnel.map(p => {
          const isOwner = p.role === 'Owner';
          const isManager = p.role === 'Manager';
          const canEdit = isManager ? editableManagers[p.username] : false;
          const perms = permMap[p.username] || [];

          // Should entire row of checkboxes be locked
          const rowLocked = isOwner || (isManager && !canEdit);

          return (
            <div
              key={p.username}
              style={{
                marginBottom: 36,
                border: '1px solid #f2f2f2',
                padding: 24,
                borderRadius: 18,
                background: rowLocked ? '#fafafc' : '#fcfcfc',
                boxShadow: rowLocked ? '0 0px 0px' : '0 2px 8px rgba(0,0,0,0.05)',
                position: 'relative'
              }}
            >
              <div style={{ marginBottom: 8, display: 'flex', alignItems: 'center' }}>
                <strong style={{ fontSize: 17, color: '#111' }}>@{p.username}</strong>
                <span style={{ marginLeft: 12, fontSize: 15, color: '#666' }}>({p.role})</span>
                {isOwner && (
                  <span style={{
                    marginLeft: 18, background: '#f1fae9', color: '#45942c', fontWeight: 500,
                    padding: '2px 14px', borderRadius: 15, fontSize: 13, letterSpacing: 0.2
                  }}>
                    All permissions granted
                  </span>
                )}
                {isManager && !canEdit && (
                  <span style={{
                    marginLeft: 18, background: '#faedea', color: '#a13b1d', fontWeight: 500,
                    padding: '2px 14px', borderRadius: 15, fontSize: 13, letterSpacing: 0.2
                  }}>
                    Only their appointer can change permissions
                  </span>
                )}
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                gap: 14,
                marginTop: 16,
                opacity: rowLocked ? 0.68 : 1
              }}>
                {ALL_PERMISSIONS.map(code => {
                  const label = code.replace(/_/g, ' ').toLowerCase();
                  const checked =
                    isOwner ||
                    (isManager && VIEW_PERMS.includes(code)) ||
                    perms.includes(code);

                  const disabled =
                    rowLocked ||
                    (isManager && VIEW_PERMS.includes(code));
                  return (
                    <label key={code} style={{ fontSize: 14, color: disabled ? '#bbb' : '#222', display: 'flex', alignItems: 'center' }}>
                      <input
                        type="checkbox"
                        disabled={disabled}
                        checked={checked}
                        onChange={() => !disabled && toggle(p.username, code)}
                        style={{
                          marginRight: 8,
                          accentColor: checked ? '#48a868' : '#bbb'
                        }}
                      />
                      {label}
                    </label>
                  );
                })}
              </div>

              {(!rowLocked) && (
                <div style={{ marginTop: 18, textAlign: 'right' }}>
                  <button
                    onClick={() => saveForUser(p)}
                    disabled={saving[p.username]}
                    style={{
                      padding: '9px 26px',
                      background: saving[p.username] ? '#a3cfa7' : '#48a868',
                      color: '#fff',
                      border: 'none',
                      borderRadius: 7,
                      fontSize: 15,
                      fontWeight: 500,
                      cursor: saving[p.username] ? 'not-allowed' : 'pointer',
                      boxShadow: '0 2px 8px rgba(40,168,104,0.08)'
                    }}
                  >
                    {saving[p.username] ? 'Saving...' : 'Save'}
                  </button>
                  {error[p.username] && (
                    <span style={{ color: '#c00', marginLeft: 13 }}>{error[p.username]}</span>
                  )}
                </div>
              )}
            </div>
          );
        })}

        <div style={{ textAlign: 'right', marginTop: 12 }}>
          <button
            onClick={onClose}
            style={{
              padding: '9px 20px',
              background: '#ddd',
              border: 'none',
              borderRadius: 6,
              fontSize: 15,
              fontWeight: 500,
              cursor: 'pointer'
            }}
          >
            Cancel
          </button>
        </div>
      </div>
    </div>,
    modalRoot
  );
}
