import { useEffect, useContext } from "react";
import { useMap } from "react-leaflet";
import L from "leaflet";
import FreeDraw, { freeDraw } from "leaflet-freedraw";
import { FreeDrawContext, ZoiContext } from "../../contexts/contexts";

const FreeDrawComponent = ({ setPolygon }) => {
  const map = useMap();
  const freeDrawContext = useContext(FreeDrawContext);
  const { zoi } = useContext(ZoiContext);

  useEffect(() => {
    if (!map) return;

    const freeDraw = new FreeDraw({ mode: freeDrawContext.freeDrawOn ? FreeDraw.CREATE : FreeDraw.NONE });
    map.addLayer(freeDraw);
    if (!zoi.show) {
      freeDraw.clear();

      map.eachLayer((layer) => {
        if (layer instanceof L.Polygon) {
          map.removeLayer(layer);
        }
      });
      setPolygon(null);
      return;
    }

    const handleMarkers = (event) => {
      // create polygon
      const newPolygon = L.polygon(event.latLngs, { color: "rgba(200, 0, 0, 0.5)" });
      newPolygon.addTo(map);

      // send polygon to parent
      setPolygon(newPolygon);
    };

    freeDraw.on("markers", handleMarkers);

    const handleKeyDown = (event) => {
      if (event.key === "Escape") {
        freeDraw.cancel();
      } else if (event.key === "z" && event.ctrlKey === true && freeDrawContext.freeDrawOn) {
        freeDraw.clear();

        map.eachLayer((layer) => {
          if (layer instanceof L.Polygon) {
            map.removeLayer(layer);
          }
        });

        setPolygon(null); // reset polygon in parent
      }
    };

    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      freeDraw.off("markers", handleMarkers);
      map.removeLayer(freeDraw);
    };
  }, [map, setPolygon, zoi.show]);

  return null;
};

export default FreeDrawComponent;