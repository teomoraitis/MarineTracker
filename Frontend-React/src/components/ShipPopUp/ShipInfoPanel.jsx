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

  if (!ship?.mmsi || hideShipInfo) return null;

  const fullShip = ships[ship.mmsi];

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
      { ship?.name && (<div><strong>Name:</strong> {ship.name}</div>) }
      { ship?.type && (<div><strong>Type:</strong> {ship.type}</div>) }
      <div><strong>Speed:</strong> {fullShip?.speed ?? 'N/A'} knots</div>
      <div><strong>Course:</strong> {fullShip?.course ?? 'N/A'}°</div>
      <div><strong>Heading:</strong> {fullShip?.heading ?? 'N/A'}°</div>
      <div><strong>Coordinates:</strong> {fullShip?.lat}, {fullShip?.lon}</div>

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
