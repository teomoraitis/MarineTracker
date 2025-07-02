import api from "./api";

export const login = ({ username, password }) => {
  return api.post(
    'auth/login',
    { username, password }
  ).then(res => res.data);
};

export const getUser = async () => {
  try {
    const res = await api.get('auth/login-status');
    return res.data;
  } catch (e) {
    console.error("User is not logged in.", e);
    return null;
  }
}

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
 