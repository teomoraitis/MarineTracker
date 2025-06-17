import api from "./api";

export const login = ({ username, password }) => {
  return api.post(
    'auth/login',
    { username, password }
  ).then(res => res.data);
};

export const logout = () => {
  return api.post(
    'auth/logout',
    {}
  ).then(res => res.data);
};

export const signup = ({ email, username, password }) => {
  return api.post(
    'auth/signup',
    { email, username, password }
  ).then(res => res.data);
};
 