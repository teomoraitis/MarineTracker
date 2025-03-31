import React, { useRef } from 'react';
import { MapContainer, TileLayer, useMapEvents } from 'react-leaflet';
import Marker from '../MapMarker/MapMarker.jsx';
import FreeDrawComponent from '../Freedraw/Freedraw.jsx';


const Map = ({ center }) => {
  const polygonRef = useRef(null); // store polygon reference

  const handlePolygonChange = (newPolygon) => {
    console.log("Polygon Updated:", newPolygon);

    if (polygonRef.current) {
      polygonRef.current.remove(); // remove the old polygon from the map
    }

    polygonRef.current = newPolygon; // store the new polygon reference
  };

  useMapEvents({
    click: () => {}, // add event handlers like so
  });

  return (
    <>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <Marker position={center} label={center?.label}/>
      <FreeDrawComponent setPolygon={handlePolygonChange} />
    </>
  );
};

const MapWrapper = ({}) => {

  // test point to show MapMarker functionality
  const center = {
    lat: 37.97539455379486,
    lng: 23.736047744750977,
    label: 'Syntagma'
  };

  return (
    <MapContainer
      center={center}
      zoom={11}
      scrollWheelZoom={true}
      style={{height: "500px", width: "1000px"}}
    >
      <Map center={center}/>
    </MapContainer>
  );
}
 
export default MapWrapper;