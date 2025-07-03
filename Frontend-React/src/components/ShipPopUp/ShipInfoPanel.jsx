import React, { useContext, useState, useEffect } from 'react';
import { SelectedShipContext, MapContext, AuthContext } from '../../contexts/contexts';
import Toggle from '../Toggle/Toggle.jsx';
import { addToFleet, removeFromFleet } from '../../api/fleetApi.js';
import { getVessel, updateVesselData } from '../../api/vesselsApi.js';

// Define shipTypes array
const shipTypes = [
  "anti-pollution", "cargo", "cargo-hazarda(major)", "cargo-hazardb",
  "cargo-hazardc(minor)", "cargo-hazardd(recognizable)", "divevessel", "dredger",
  "fishing", "high-speedcraft", "lawenforce", "localvessel", "militaryops",
  "other", "passenger", "pilotvessel", "pleasurecraft", "sailingvessel",
  "sar", "specialcraft", "tanker", "tanker-hazarda(major)", "tanker-hazardb",
  "tanker-hazardc(minor)", "tanker-hazardd(recognizable)", "tug", "unknown", "wingingrnd"
];

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

  if (!ship?.mmsi || hideShipInfo) return null;
  
  const [note, setNote] = useState(''); 
  const [saved, setSaved] = useState(false);

  const [editMode, setEditMode] = useState(false);
  const [editName, setEditName] = useState(null);
  const [editType, setEditType] = useState(null);

  const fullShip = ships[ship.mmsi];
  
  useEffect(() => {
    if (!ship?.mmsi) return;
    const savedNote = localStorage.getItem(`ship-note-${ship.mmsi}`);
    setNote(savedNote || '');
    setEditMode(false);
    setEditName(ship?.name);
    setEditType(ship?.type);
  }, [ship?.mmsi]);

  const handleSaveNote = () => {
    if (!ship?.mmsi) return;
    localStorage.setItem(`ship-note-${ship.mmsi}`, note);
    setSaved(true);
    setTimeout(() => setSaved(false), 1500);
  };

  const handleFleetChange = async () => {
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

  const handleSaveEdit = async () => {
    const result = await updateVesselData(ship?.mmsi, editName, editType);
    if (result) {
      const updatedVessel = await getVessel(ship?.mmsi);
      setEditMode(false);
      setSelectedShipInfo(updatedVessel);
    }
  };

  const handleCancelEdit = () => {
    setEditMode(false);
    setEditName(ship?.name);
    setEditType(ship?.type);
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

      {editMode ? (
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
              <select
                  value={editType}
                  onChange={(e) => setEditType(e.target.value)}
                  className="w-full border border-gray-300 rounded px-2 py-1 mt-1"
              >
                {shipTypes.map((type, index) => (
                    <option key={index} value={type}>
                      {type}
                    </option>
                ))}
              </select>
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

      {user && user.username == 'admin' && (
          <div className="mt-3 space-y-2">
            {editMode ? (
                <div className="flex gap-2">
                  <button
                      onClick={handleSaveEdit}
                      className="px-3 py-1 bg-green-600 text-white rounded text-sm hover:bg-green-700"
                  >
                    Save
                  </button>
                  <button
                      onClick={handleCancelEdit}
                      className="px-3 py-1 bg-gray-500 text-white rounded text-sm hover:bg-gray-600"
                  >
                    Cancel
                  </button>
                </div>
            ) : (
                <button
                    onClick={() => setEditMode(true)}
                    className="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700"
                >
                  Edit Ship
                </button>
            )}
          </div>
      )}
      <div className="mt-4 space-y-3"></div>
      <div className="flex items-center justify-between">
        <span className="text-sm">Path history (12 hrs)</span>
        <Toggle
          value={showPath}
          onChange={toggleShowPath}
        />
      </div>
      {
        user != null && (
          <div className="mt-4 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm">In my fleet</span>
              <input
                type="checkbox"
                checked={!!ship.inFleet}
                onChange={handleFleetChange}
                className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500"
              />
            </div>

            <div className="mt-2">
              <label htmlFor="ship-note" className="block text-sm font-medium text-gray-700">Notes</label>
              <textarea
                id="ship-note"
                className="mt-1 block w-full border border-gray-300 rounded-md p-1 text-sm"
                rows={3}
                value={note}
                onChange={(e) => setNote(e.target.value)}
              />
              <button
                onClick={handleSaveNote}
                className={`mt-1 px-2 py-1 text-sm rounded transition-colors duration-500
                  ${saved ? 'bg-green-500' : 'bg-blue-500'}
                  ${saved ? 'hover:bg-green-600' : 'hover:bg-blue-600'}
                  text-white`}
              >
                {saved ? 'Saved!' : 'Save Note'}
              </button>
            </div>
          </div>
        )
      }
    </div>
  );
};

export default ShipInfoPanel;
