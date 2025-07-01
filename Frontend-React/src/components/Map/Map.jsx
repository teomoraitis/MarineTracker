import React, { useRef, useState, useContext, useEffect } from 'react';
import { MapContainer, Polyline, TileLayer, ZoomControl, useMap, useMapEvents } from 'react-leaflet';
import Marker from '../MapMarker/MapMarker.jsx';
import FreeDrawComponent from './Freedraw.jsx'
import { FilterContext, MapContext, SelectedShipContext, ZoiContext } from '../../contexts/contexts.js';
import { getZoneOfInterest } from '../../api/zoneApi.js';
import L from "leaflet";


const Map = ({}) => {
  const polygonRef = useRef(null); // store polygon reference
  const { ships } = useContext(MapContext);
  const selectedShipContext = useContext(SelectedShipContext);
  const { filters, onFilterChange } = useContext(FilterContext);
  const map = useMap();
  const { zoi, setZoi } = useContext(ZoiContext);

  useEffect(() => {
    const fetchZoi = async () => {
      if(zoi.show) {
        const savedZoi = await getZoneOfInterest();

        const zoiPolygon = L.polygon(savedZoi.area.map((point) => {return [point.lat, point.lng]}), { color: "rgba(200, 0, 0, 0.5)" });

        if (polygonRef.current) {
          polygonRef.current.remove(); // remove the old polygon from the map
        }
        polygonRef.current = zoiPolygon; // store the new polygon reference
        zoiPolygon.addTo(map);
        setZoi({
          ...zoi,
          restrictions: savedZoi.restrictions,
          area: zoiPolygon?.getLatLngs()[0],
        });

      }
    }
    fetchZoi();
  }, [zoi.show]);

  const handlePolygonChange = (newPolygon) => {

    if (polygonRef.current) {
      polygonRef.current.remove(); // remove the old polygon from the map
    }

    polygonRef.current = newPolygon; // store the new polygon reference
    if (newPolygon?.getLatLngs()?.length <= 1) {
      setZoi({
        ...zoi,
        area: newPolygon?.getLatLngs()[0],
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
              heading={ship.heading ?? 511}
              course={(ship.courseoverground) ?? undefined}
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
  const [center, setCenter] = useState(selectedShipContext.ship?.coordinates ?? { lat: 48, lng: -5 });

  useEffect(() => {
    const newCenter = {
      lat: selectedShipContext.ship?.vesselPosition?.latitude ?? center.lat,
      lng: selectedShipContext.ship?.vesselPosition?.longitude ?? center.lng
    };
    console.log(newCenter)
    setCenter(newCenter);
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