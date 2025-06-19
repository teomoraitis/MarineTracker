## Summary

I've created a WebSocket solution. Here's what I've implemented:

### **Key Features:**

1. **Dual WebSocket Endpoints:**
   - `/ws/guest` - Public endpoint for anonymous users (shows all vessels)
   - `/ws/auth` - Authenticated endpoint for logged-in users (with filtering)

2. **WebSocketService** - Core service that handles:
   - Broadcasting to public guests
   - User session management
   - Personalized filtering based on:
     - User's fleet vessels
     - Vessel type filters
     - Zone of Interest matching
   - Notification generation for zone alerts

3. **Real-time Filtering:**
   - Users see their fleet vessels regardless of filters
   - Optional vessel type filtering
   - Zone of Interest-based filtering
   - Notifications when vessels enter zones

4. **Session Management:**
   - Tracks active authenticated users
   - Manages user preferences and filters
   - Handles connection/disconnection events

### **WebSocket Topics:**

- **Public:** `/topic/guest` - All vessels for anonymous users
- **Authenticated:** `/user/{userId}/queue/vessels` - Filtered vessels for specific users
- **Filter Updates:** `/app/filters` - Clients can update their filters in real-time

### **Client Usage Examples:**

**For Guests (JavaScript):**
```javascript
const socket = new SockJS('/ws/guest');
const stompClient = Stomp.over(socket);
stompClient.connect({}, function() {
    stompClient.subscribe('/topic/guest', function(message) {
        const data = JSON.parse(message.body);
        // Handle vessel updates
    });
});
```

**For Authenticated Users (JavaScript):**
```javascript
const socket = new SockJS('/ws/auth');
const stompClient = Stomp.over(socket);
stompClient.connect({
    'Authorization': 'Bearer ' + jwtToken
}, function() {
    stompClient.subscribe('/user/queue/vessels', function(message) {
        const data = JSON.parse(message.body);
        // Handle filtered vessel updates
    });
    
    // Update filters
    stompClient.send('/app/filters', {}, JSON.stringify({
        vesselTypes: ['CARGO', 'TANKER']
    }));
});
```

### **Message Format:**
```json
{
    "setShips": [...],
    "hideShips": [...],
    "hideAllShips": false,
    "notifications": [...],
    "timestamp": 1234567890
}
```
