import { createContext } from "react";

export const MapContext = createContext({
  error: undefined,
  setError: () => {},
});

export const FreeDrawContext = createContext({
  freeDrawOn: false,
  setFreeDraw: () => {},
});

export const AuthContext = createContext({
  user: null,
  login: () => {},
  logout: () => {},
});
