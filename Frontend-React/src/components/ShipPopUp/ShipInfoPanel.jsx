import React, { useContext, useState } from 'react';
import { SelectedShipContext, MapContext } from '../../contexts/contexts';
import Toggle from '../Toggle/Toggle.jsx';

const ShipInfoPanel = () => {
  const {
    ship,
    toggleShowPath,
    showPath,
    hideShipInfo,
    setHideShipInfo
  } = useContext(SelectedShipContext);
  const { ships } = useContext(MapContext);
  const [inFleet, setInFleet] = useState(false);

  if (!ship?.mmsi || hideShipInfo) return null;

  const fullShip = ships[ship.mmsi];

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
      <div><strong>MMSI:</strong> {fullShip?.mmsi}</div>
      <div><strong>Speed:</strong> {fullShip?.speed ?? 'N/A'} knots</div>
      <div><strong>Course:</strong> {fullShip?.course ?? 'N/A'}°</div>
      <div><strong>Heading:</strong> {fullShip?.heading ?? 'N/A'}°</div>
      <div><strong>Coordinates:</strong> {fullShip?.lat}, {fullShip?.lon}</div>

      <div className="mt-4 space-y-3">
        <div className="flex items-center justify-between">
          <span className="text-sm">Path history (12 hrs)</span>
          <Toggle
            value={showPath}
            onChange={toggleShowPath}
          />
        </div>

        <div className="flex items-center justify-between">
          <span className="text-sm">In my fleet</span>
          <input
            type="checkbox"
            checked={inFleet}
            onChange={() => setInFleet(prev => !prev)}
            className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500"
          />
        </div>
      </div>
    </div>
  );
};

export default ShipInfoPanel;
