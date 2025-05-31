import axios from "axios";

const axiosParams = {
  baseURL: process.env.REACT_APP_BACKEND_URL,
};

const axiosInstance = axios.create(axiosParams);

const api = axios => {
  return {
    get: (url, config={}) => axios.get(url, config),
    post: (url, body, config={}) => axios.post(url, body, config),
    put: (url, body, config={}) => axios.put(url, body, config),
    patch: (url, body, config={}) => axios.put(url, body, config),
    delete: (url, config={}) => axios.delete(url, config),
  };
};

export default api(axiosInstance);