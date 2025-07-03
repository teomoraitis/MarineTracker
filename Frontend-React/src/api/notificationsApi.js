import api from "./api";

export const getNotifications = async () => {
  const res = await api.get('notifications');
  const notifications = res.data;

  notifications.sort((a, b) => {
    if (a.is_read !== b.is_read) return a.is_read ? -1 : 1;

    const dateA = new Date(a.created_at);
    const dateB = new Date(b.created_at);
    if (dateA > dateB) return -1;
    if (dateA < dateB) return 1;
    return 0;
  });
  return notifications;
};

export const getUnreadNotificationsCount = async () => {
  const res = await api.get('notifications/count/unread');
  const count = res.data.unreadCount;
  return count;
}

export const dismissNotification = (notificationId) => {
  return api.put(`notifications/${notificationId}/read`).then(res => res.data);
};

export const dismissAllNotifications = () => {
  return api.put('notifications/read/all').then(res => res.data);
};
