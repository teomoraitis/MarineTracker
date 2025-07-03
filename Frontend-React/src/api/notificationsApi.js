import api from "./api";

export const getNotifications = () => {
  const mergedConfig = {
    params: {
      unreadOnly: true,
    },
  };
  return api.get('notifications').then(res => res.data);
};

export const getUnreadNotificationsCount = () => {
  return api.get('notifications/count/unread').then(res => res.data);
}

export const dismissNotification = (notificationId) => {
  return api.put(`notifications/${notificationId}/read`).then(res => res.data);
};

export const dismissAllNotifications = () => {
  return api.put('notifications/read/all').then(res => res.data);
};
