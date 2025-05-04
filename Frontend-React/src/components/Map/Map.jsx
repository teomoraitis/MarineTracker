import React, { useRef, useState, useEffect, useContext, createContext } from 'react';
import { MapContainer, TileLayer, useMapEvents } from 'react-leaflet';
import Marker from '../MapMarker/MapMarker.jsx';
import FreeDrawComponent from './Freedraw.jsx'

export const MapContext = createContext({
  error: undefined,
  setError: () => {},
});


const Map = ({ center }) => {
  const polygonRef = useRef(null); // store polygon reference
  const mapContext = useContext(MapContext);

  const handlePolygonChange = (newPolygon) => {
    console.log("Polygon Updated:", newPolygon);

    if (polygonRef.current) {
      polygonRef.current.remove(); // remove the old polygon from the map
    }

    polygonRef.current = newPolygon; // store the new polygon reference
    if (newPolygon?.getLatLngs()?.length > 1) {
      mapContext.setError('Μπορείτε να έχετε μόνο μια και συνεχόμενη ζώνη ενδιαφέροντος.');
    } else {
      mapContext.setError(undefined);
    }
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
  const [error, setError] = useState(undefined);
  // test point to show MapMarker functionality
  const center = {
    lat: 37.97539455379486,
    lng: 23.736047744750977,
    label: 'Syntagma'
  };

  return (
    <MapContext.Provider value={{
      error: error,
      setError: setError
    }}>
      <MapContainer
        center={center}
        zoom={11}
        scrollWheelZoom={true}
      style={{height: "500px", width: "1000px"}}
      >
        <Map center={center}/>
      </MapContainer>
    </MapContext.Provider>
  );
}
 
export default MapWrapper;