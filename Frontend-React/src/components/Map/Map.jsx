import React, { useRef, useState, useContext, useEffect } from 'react';
import { MapContainer, Polyline, TileLayer, ZoomControl, useMap, useMapEvents } from 'react-leaflet';
import Marker from '../MapMarker/MapMarker.jsx';
import FreeDrawComponent from './Freedraw.jsx'
import { FilterContext, MapContext, SelectedShipContext } from '../../contexts/contexts.js';


const Map = ({}) => {
  const polygonRef = useRef(null); // store polygon reference
  const { ships } = useContext(MapContext);
  const selectedShipContext = useContext(SelectedShipContext);
  const { filters, onFilterChange } = useContext(FilterContext);
  const map = useMap();

  const handlePolygonChange = (newPolygon) => {

    if (polygonRef.current) {
      polygonRef.current.remove(); // remove the old polygon from the map
    }

    polygonRef.current = newPolygon; // store the new polygon reference
    if (newPolygon?.getLatLngs()?.length <= 1) {
      onFilterChange({
        ...filters,
        zoi: {
          ...filters.zoi,
          area: polygonRef.current?.getLatLngs() ?? [],
        },
      });
    }
  };

  useMapEvents({
    click: () => {}, // add event handlers like so
    moveend: () => {
      const bounds = map.getBounds();
      onFilterChange({
        ...filters,
        bounds: [
          bounds.getNorthEast(),
          bounds.getSouthEast(),
          bounds.getSouthWest(),
          bounds.getNorthWest(),
        ],
      });
    },
  });

  useEffect(() => {
    const bounds = map.getBounds();
    onFilterChange({
      ...filters,
      bounds: [
        bounds.getNorthEast(),
        bounds.getSouthEast(),
        bounds.getSouthWest(),
        bounds.getNorthWest(),
      ],
    });
  }, []);

  return (
    <>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {
        selectedShipContext.showPath && selectedShipContext.path.length > 0 && (
          <Polyline
            pathOptions={{ color: '#FF0000', weight: 2 }}
            positions={selectedShipContext.path.map(position => ({ lat: position.latitude, lng: position.longitude }))}
          />
        )
      }
      {
        Object.values(ships).map(ship => {
          return (
            <Marker
              key={ship.mmsi}
              position={{
                lat: ship.lat,
                lng: ship.lon
              }}
              label={ship.mmsi}
              heading={ship.heading || 0}
            />
          )
        })
      }
      <FreeDrawComponent setPolygon={handlePolygonChange} />
      <ZoomControl position='bottomright'/>
    </>
  );
};

const MapWrapper = ({ ships }) => {
  const selectedShipContext = useContext(SelectedShipContext);
  const [center, setCenter] = useState(selectedShipContext.ship?.coordinates ?? { lat: 38, lng: 24 });

  useEffect(() => {
    setCenter(selectedShipContext.ship?.coordinates ?? center);
  }, [selectedShipContext.ship?.mmsi]);

  return (
    <MapContainer
      center={center}
      zoom={9}
      scrollWheelZoom={true}
      zoomControl={false}
      className='w-full h-[85vh] z-10'
    >
      <Map ships={ships} />
    </MapContainer>
  );
};
 
export default MapWrapper;