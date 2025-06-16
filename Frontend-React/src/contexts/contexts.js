import { createContext } from "react";

export const MapContext = createContext({
  ships: {},
  setShips: () => {},
});

export const FreeDrawContext = createContext({
  freeDrawOn: false,
  setFreeDraw: () => {},
});

export const AuthContext = createContext({
  user: null,
  handleLogin: () => {},
  handleLogout: () => {},
});

export const FilterContext = createContext({
  filters: {
    types: [],
    fleetOnly: false,
    zoi: {
      show: false,
      area: [],
      restrictions: {
        speed: 0,
        types: [],
      },
    },
    bounds: null,
  },
  onFilterChange: () => {},
});

export const SelectedShipContext = createContext({
  ship: {
    mmsi: null,
    inFleet: false,
    coordinates: {
      lat: null,
      lng: null,
    },
    path: [],
    // ...
  },
  showPath: false,
  setShowPath: () => {},
  toggleShowPath: () => {},
  setSelectedShipInfo: () => {},
  setPath: () => {},
});