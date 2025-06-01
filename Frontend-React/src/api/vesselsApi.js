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

export const getVessel = (mmsi) => {
  return api.get(`vessels/${mmsi}`).then(res => res.data);
};

export const getVesselPath = (mmsi) => {
  return api.get(`vessels/${mmsi}/path`).then(res => res.data);
};
