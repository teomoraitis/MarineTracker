import React, { useContext } from 'react';
import { Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import ShipArrow from '../../assets/images/shiparrow.png';
import ShipArrowRed from '../../assets/images/redshiparrow.png';
import { SelectedShipContext, MapContext, AuthContext, ZoiContext } from '../../contexts/contexts';
import ShipDot from '../../assets/images/stationaryship.png';
import { getVessel } from '../../api/vesselsApi';

const MapMarker = ({ label, isInZoi=false }) => {
  const { ship, setSelectedShipInfo } = useContext(SelectedShipContext);
  const { ships } = useContext(MapContext);
  const { user } = useContext(AuthContext);

  const liveShip = ships[label];
  if (!liveShip) return null;

  const isSelected = ship?.mmsi == label;

  const speed = liveShip.speed ?? 0;
  const isStationary = speed < 0.3;
  const image = isStationary ? ShipDot : (isSelected || isInZoi ? ShipArrowRed : ShipArrow);

  const course = liveShip.course ?? 0;
  const heading = liveShip.heading ?? 511;
  const position = [liveShip.lat, liveShip.lon];
  const calculatedHeading = heading !== 511 ? heading : course;

  const icon = L.divIcon({
    className: 'ship-icon',
    html: `<div style="
      transform: rotate(${calculatedHeading - 90}deg);
      width: 32px;
      height: 32px;
      background-image: url(${image});
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
        click: async () => {
          if (user) {
            const vessel = await getVessel(label);
            setSelectedShipInfo(vessel);
          } else {
            setSelectedShipInfo({
              mmsi: label,
              heading: liveShip.heading,
              course: liveShip.course,
              speed: liveShip.speed,
              lat: liveShip.lat,
              lon: liveShip.lon,
              inFleet: liveShip.inFleet,
            });
          }
        },
      }}
    >
    </Marker>
  );
};

export default MapMarker;
