import React, { useContext } from 'react';
import { Marker } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import ShipArrow from '../../assets/images/shiparrow.png';
import { SelectedShipContext } from '../../contexts/contexts';  

const MapMarker = ({ position, label, heading = 0 }) => {
  const { setSelectedShipInfo } = useContext(SelectedShipContext);  

  const icon = L.divIcon({
    className: 'ship-icon',
    html: `<div style="
      transform: rotate(${-heading}deg);
      width: 32px;
      height: 32px;
      background-image: url(${ShipArrow});
      background-size: contain;
      background-repeat: no-repeat;
      background-position: center;      
    "></div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 16],
  });

  return (
    <Marker
      position={position}
      icon={icon}
      eventHandlers={{
        click: () => {
          setSelectedShipInfo({
            mmsi: label,
            heading,
            coordinates: position,
          });
        },
      }}
    />
  );
};

export default MapMarker;
