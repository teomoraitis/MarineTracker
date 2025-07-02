import api from "./api";

export const listVessels = (config) => {
  const mergedConfig = {
    ...config,
    params: {
      ...config?.params,
      limit: 3,
    },
  };
  return api.get('vessels', mergedConfig).then(res => res.data);
};

export const getVesselsStaticData = (config) => {
  return api.get('vessels/static-info').then(res => res.data);
};

export const getVessel = (mmsi) => {
  return api.get(`vessels/${mmsi}`).then(res => res.data);
};

export const getVesselPath = (mmsi) => {
  return api.get(`vessels/${mmsi}/path`).then(res => res.data);
};

export const updateVesselData = (mmsi, name, type) => {
  return api.put(
    `vessels/${mmsi}`,
    { name, type }
  ).then(res => res.data);
};
