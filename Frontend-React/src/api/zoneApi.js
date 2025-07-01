import api from "./api";

export const getZoneOfInterest = async () => {
  try {
    const res = await api.get('zone');
    const polygonWKT = res.data.polygonWKT;
    const trimmed = polygonWKT
      .replace(/^POLYGON\s*\(\(/i, "")
      .replace(/\)\)$/i, "");
    const pairs = trimmed.split(",");
    const area = pairs.map(pair => {
      const [lngStr, latStr] = pair.trim().split(/\s+/);
      return {
        lat: parseFloat(latStr),
        lng: parseFloat(lngStr),
      };
    });

    return {
      area: area,
      restrictions: {
        speed: res.data.maxVesselSpeed,
        types: res.data.vesselTypes,
      }
    };
  } catch (e) {
    console.error("No zone of interest found", e);
    return {
      area: [],
      restrictions: {
        speed: 0,
        types: [],
      }
    };
  }
};

export const saveZoneOfInterest = async (polygon, speed, types) => {
  console.log(polygon);
  if (
    polygon[0].lat !== polygon[polygon.length - 1].lat ||
    polygon[0].lng !== polygon[polygon.length - 1].lng
  ) {
    polygon.push(polygon[0]);
  }
  const requestBody = {
    polygonWKT: `POLYGON((` + polygon.map(point => `${point.lng} ${point.lat}`).join(", ") + `))`,
    maxVesselSpeed: speed,
    vesselTypes: types,
  };
  return api.post(
    'zone',
    requestBody
  );
};

