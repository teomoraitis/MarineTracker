import React, { useContext, useState } from 'react';
import { SelectedShipContext, MapContext, AuthContext } from '../../contexts/contexts';
import Toggle from '../Toggle/Toggle.jsx';
import { addToFleet, removeFromFleet } from '../../api/fleetApi.js';

const ShipInfoPanel = () => {
  const {
    ship,
    toggleShowPath,
    showPath,
    hideShipInfo,
    setHideShipInfo,
    setSelectedShipInfo
  } = useContext(SelectedShipContext);
  const { user } = useContext(AuthContext);
  const { ships } = useContext(MapContext);

  // Admin editing state
  const [isEditing, setIsEditing] = useState(false);
  const [editName, setEditName] = useState('');
  const [editType, setEditType] = useState('');

  if (!ship?.mmsi || hideShipInfo) return null;

  const fullShip = ships[ship.mmsi];
  // Check if the user is an admin
  const isAdmin = user && user.username === 'admin';
  console.log('User object:', user);
  console.log('Is admin?', isAdmin);

  const handleFleetChange = async () => {
    console.log(ship.mmsi)
    if (ship.inFleet) {
      try {
        await removeFromFleet({ mmsi: ship.mmsi });
        setSelectedShipInfo({
          ...ship,
          inFleet: !ship.inFleet,
        });
      } catch {
        console.error("Error removing vessel from fleet");
      }
    } else {
      try {
        await addToFleet({ mmsi: ship.mmsi });
        setSelectedShipInfo({
          ...ship,
          inFleet: !ship.inFleet,
        });
      } catch {
        console.error("Error adding vessel to fleet");
      }
    }
  };

  const handleEditStart = () => {
    setEditName(ship.name || '');
    setEditType(ship.type || '');
    setIsEditing(true);
  };

  const handleEditCancel = () => {
    setIsEditing(false);
    setEditName('');
    setEditType('');
  };

  // Admin edit save handler
  const handleEditSave = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/vessels/${ship.mmsi}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          name: editName,
          type: editType
        })
      });

      if (!response.ok) {
        throw new Error('Failed to update vessel');
      }

      // Update the ship info in context
      setSelectedShipInfo({
        ...ship,
        name: editName,
        type: editType
      });

      setIsEditing(false);
      alert('Ship updated successfully!');
    } catch (error) {
      console.error('Error updating vessel:', error);
      alert('Failed to update ship');
    }
  };

  return (
    <div className="fixed top-25 right-4 bg-white shadow-lg p-4 rounded-md w-64 z-50">
      <div className="flex justify-between items-center mb-2">
        <h2 className="text-lg font-semibold">Ship Info</h2>
        <button
          onClick={() => setHideShipInfo(true)}
          className="text-gray-500 hover:text-gray-800 text-sm font-bold"
        >
          ✕
        </button>
      </div>
      <div><strong>MMSI:</strong> {ship.mmsi}</div>

      {/* Editable fields for admin */}
      {isEditing ? (
          <>
            <div className="mb-2">
              <strong>Name:</strong>
              <input
                  type="text"
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                  className="w-full border border-gray-300 rounded px-2 py-1 mt-1"
              />
            </div>
            <div className="mb-2">
              <strong>Type:</strong>
              <input
                  type="text"
                  value={editType}
                  onChange={(e) => setEditType(e.target.value)}
                  className="w-full border border-gray-300 rounded px-2 py-1 mt-1"
              />
            </div>
          </>
      ) : (
          <>
            <div><strong>Name:</strong> {ship.name || 'N/A'}</div>
            <div><strong>Type:</strong> {ship.type || 'N/A'}</div>
          </>
      )}

      <div><strong>Speed:</strong> {fullShip?.speed ?? 'N/A'} knots</div>
      <div><strong>Course:</strong> {fullShip?.course ?? 'N/A'}°</div>
      <div><strong>Heading:</strong> {fullShip?.heading ?? 'N/A'}°</div>
      <div><strong>Coordinates:</strong> {fullShip?.lat}, {fullShip?.lon}</div>

      {/* Admin edit buttons */}
      {isAdmin && (
          <div className="mt-3 space-y-2">
            {isEditing ? (
                <div className="flex gap-2">
                  <button
                      onClick={handleEditSave}
                      className="px-3 py-1 bg-green-600 text-white rounded text-sm hover:bg-green-700"
                  >
                    Save
                  </button>
                  <button
                      onClick={handleEditCancel}
                      className="px-3 py-1 bg-gray-500 text-white rounded text-sm hover:bg-gray-600"
                  >
                    Cancel
                  </button>
                </div>
            ) : (
                <button
                    onClick={handleEditStart}
                    className="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700"
                >
                  Edit Ship
                </button>
            )}
          </div>
      )}

      {
        user != null && (
          <div className="mt-4 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm">Path history (12 hrs)</span>
              <Toggle
                value={showPath}
                onChange={() => toggleShowPath(ship.mmsi)}
              />
            </div>

            <div className="flex items-center justify-between">
              <span className="text-sm">In my fleet</span>
              <input
                type="checkbox"
                checked={!!ship.inFleet}
                onChange={handleFleetChange}
                className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500"
              />
            </div>
          </div>
        )
      }
    </div>
  );
};

export default ShipInfoPanel;
