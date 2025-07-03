package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.controllers.VesselCacheController;
import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import com.di.marinetracker.backendspringboot.utils.JwtPrincipal;
import com.di.marinetracker.backendspringboot.utils.JwtUtils;
import org.checkerframework.checker.optional.qual.Present;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class WebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtils jwtUtils;

    private boolean revalidateJwtWhenUsersRegister = false;

    @InjectMocks
    private WebSocketService webSocketService;
    @Mock
    private VesselCacheController vesselDataCache;

    @Test
    void testRemoveUserThatDoesntExists_doesntThrow() {
        webSocketService.removeUserSession("session that doesn't exist");
    }

    @Test
    void testBroadcastToNobody_doesNothing() {
        broadcastSomething();
        verify(messagingTemplate).convertAndSend(eq("/topic/guest"), anyString());
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void testRegisterUserAndBroadcastVessel_sendsIt() {
        createUser("a");
        broadcastSomething();
        verify(messagingTemplate).convertAndSendToUser(eq("a"), eq("/queue/vessels"), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/guest"), anyString());
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void testTwoUsers_bothGetBroadcast() {
        createUser("a");
        createUser("b");
        broadcastSomething();
        verify(messagingTemplate).convertAndSendToUser(eq("b"), eq("/queue/vessels"), anyString());
        verify(messagingTemplate).convertAndSendToUser(eq("a"), eq("/queue/vessels"), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/guest"), anyString());
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void testFilterByNonExistingFleet_noMessage() {
        createUser("a");
        createUser("b");
        ArrayList<String> types = new ArrayList<>();
        types.add("type-that-doesnt-exist");
        webSocketService.updateUserFilters("session-of-a", types);

        broadcastSomething();
        verify(messagingTemplate).convertAndSendToUser(eq("b"), eq("/queue/vessels"), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/guest"), anyString());
        verifyNoMoreInteractions(messagingTemplate);
    }

    // TODO the filters are under construction
    // @Test
    void testDisablingFilters() {
        createUser("a");
        createUser("b");
        ArrayList<String> types = new ArrayList<>();
        types.add("type-that-doesnt-exist");
        webSocketService.updateUserFilters("session-of-a", types);
        webSocketService.updateFleetFilter("session-of-a", false);

        broadcastSomething();
        verify(messagingTemplate).convertAndSendToUser(eq("a"), eq("/queue/vessels"), anyString());
        verify(messagingTemplate).convertAndSendToUser(eq("b"), eq("/queue/vessels"), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/guest"), anyString());
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void testReenablingFilters() {
        createUser("a");
        createUser("b");
        ArrayList<String> types = new ArrayList<>();
        types.add("type-that-doesnt-exist");
        webSocketService.updateUserFilters("session-of-a", types);
        webSocketService.updateFleetFilter("session-of-a", false);
        webSocketService.updateFleetFilter("session-of-a", true);

        broadcastSomething();
        verify(messagingTemplate).convertAndSendToUser(eq("b"), eq("/queue/vessels"), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/guest"), anyString());
        verifyNoMoreInteractions(messagingTemplate);
    }

    // TODO the filters are under construction
    //@Test
    void testTwoFiltersWhereOneMatches_sendsMessage() {
        createUser("a");
        createUser("b");
        ArrayList<String> types = new ArrayList<>();
        types.add("type-that-doesnt-exist");
        types.add("Cargo");
        webSocketService.updateUserFilters("session-of-a", types);

        broadcastSomething();
        verify(messagingTemplate).convertAndSendToUser(eq("a"), eq("/queue/vessels"), anyString());
        verify(messagingTemplate).convertAndSendToUser(eq("b"), eq("/queue/vessels"), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/guest"), anyString());
        verifyNoMoreInteractions(messagingTemplate);
    }

    private void createUser(String name) {
        Principal principal = Mockito.mock(JwtPrincipal.class);
        Mockito.when(principal.getName()).thenReturn(name);
        User user = new User(name, "a@a", "password", Set.of());
        Optional<User> optionalUser = Optional.of(user);
        when(userRepository.findByUserNameWithFleet(name)).thenReturn(optionalUser);
        webSocketService.registerUserSession("session-of-" + name, principal);
    }

    private void broadcastSomething() {
        Vessel vessel = mock(Vessel.class);

        String mmsi = "12345";
        String type = "Cargo";

        when(vessel.getMmsi()).thenReturn(mmsi);

        String vesselMessage = "{\"mmsi\":\"" + mmsi + "\",\"type\":\"" + type + "\"}";
        // There would be all the fields in the real JSON. Maybe the code can be written differently.

        VesselPosition sameVesselAsPosition = new VesselPosition(vessel, 0.0, 0.0, 1.0, 0.0, 0, 0.0, 0, Instant.ofEpochSecond(0));
        webSocketService.broadcastVesselPosition(vesselMessage, vessel, sameVesselAsPosition);
    }

}
