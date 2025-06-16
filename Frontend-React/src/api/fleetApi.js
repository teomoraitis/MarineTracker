import api from "./api";

export const addToFleet = ({ mmsi }) => {
  return api.post(
    `fleet/${mmsi}`,
    {}
  ).then(res => res.data);
};

export const removeFromFleet = ({ mmsi }) => {
  return api.delete(
    `fleet/${mmsi}`,
  ).then(res => res.data);
};
