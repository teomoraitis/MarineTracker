import React, { useContext } from 'react';
import { SelectedShipContext, MapContext } from '../../contexts/contexts';

const ShipInfoPanel = () => {
  const { ship } = useContext(SelectedShipContext);
  const { ships } = useContext(MapContext);

  if (!ship?.mmsi) return null;

  const fullShip = ships[ship.mmsi];

  return (
    <div className="fixed top-25 right-4 bg-white shadow-lg p-4 rounded-md w-64 z-50">
      <h2 className="text-lg font-semibold mb-2">Ship Info</h2>
      <div><strong>MMSI:</strong> {fullShip?.mmsi}</div>
      <div><strong>Speed:</strong> {fullShip?.speed ?? 'N/A'} knots</div>
      <div><strong>Course:</strong> {fullShip?.course ?? 'N/A'}°</div>
      <div><strong>Heading:</strong> {fullShip?.heading ?? 'N/A'}°</div>
      <div><strong>Coordinates:</strong> {fullShip?.lat}, {fullShip?.lon}</div>
    </div>
  );
};

export default ShipInfoPanel;
