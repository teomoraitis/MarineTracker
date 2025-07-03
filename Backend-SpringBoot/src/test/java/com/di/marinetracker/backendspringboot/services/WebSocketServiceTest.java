package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.controllers.VesselCacheController;
import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import com.di.marinetracker.backendspringboot.repositories.ZoneOfInterestRepository;
import com.di.marinetracker.backendspringboot.utils.JwtPrincipal;
import com.di.marinetracker.backendspringboot.utils.JwtUtils;
import org.checkerframework.checker.optional.qual.Present;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private ZoneOfInterestRepository zoneRepository;
    @Mock
    private NotificationService notificationService;

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
        // added this to fix errors: Setup JWT validation to pass
        when(jwtUtils.validateJwtToken(anyString())).thenReturn(true);
        when(zoneRepository.findByUserIdWithVesselTypes(anyString())).thenReturn(null);

        createUser("a");
        broadcastSomething();
        verify(messagingTemplate).convertAndSendToUser(eq("a"), eq("/queue/vessels"), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/guest"), anyString());
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void testTwoUsers_bothGetBroadcast() {
        // added this to fix errors: Setup JWT validation to pass
        when(jwtUtils.validateJwtToken(anyString())).thenReturn(true);
        when(zoneRepository.findByUserIdWithVesselTypes(anyString())).thenReturn(null);

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
        // added this to fix errors: Setup JWT validation to pass
        when(jwtUtils.validateJwtToken(anyString())).thenReturn(true);
        when(zoneRepository.findByUserIdWithVesselTypes(anyString())).thenReturn(null);

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
//    void testDisablingFilters() {
//        createUser("a");
//        createUser("b");
//        ArrayList<String> types = new ArrayList<>();
//        types.add("type-that-doesnt-exist");
//        webSocketService.updateUserFilters("session-of-a", types);
//        webSocketService.updateFleetFilter("session-of-a", false);
//
//        broadcastSomething();
//        verify(messagingTemplate).convertAndSendToUser(eq("a"), eq("/queue/vessels"), anyString());
//        verify(messagingTemplate).convertAndSendToUser(eq("b"), eq("/queue/vessels"), anyString());
//        verify(messagingTemplate).convertAndSend(eq("/topic/guest"), anyString());
//        verifyNoMoreInteractions(messagingTemplate);
//    }

    @Test
    void testReenablingFilters() {
        // added this to fix errors: Setup JWT validation to pass
        when(jwtUtils.validateJwtToken(anyString())).thenReturn(true);
        when(zoneRepository.findByUserIdWithVesselTypes(anyString())).thenReturn(null);

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
//    void testTwoFiltersWhereOneMatches_sendsMessage() {
//        createUser("a");
//        createUser("b");
//        ArrayList<String> types = new ArrayList<>();
//        types.add("type-that-doesnt-exist");
//        types.add("Cargo");
//        webSocketService.updateUserFilters("session-of-a", types);
//
//        broadcastSomething();
//        verify(messagingTemplate).convertAndSendToUser(eq("a"), eq("/queue/vessels"), anyString());
//        verify(messagingTemplate).convertAndSendToUser(eq("b"), eq("/queue/vessels"), anyString());
//        verify(messagingTemplate).convertAndSend(eq("/topic/guest"), anyString());
//        verifyNoMoreInteractions(messagingTemplate);
//    }


    private void createUser(String name) {
        // modified this to fix errors:
        JwtPrincipal principal = mock(JwtPrincipal.class);
        when(principal.getName()).thenReturn(name);
        when(principal.getJwt()).thenReturn("valid-jwt-token");

        User user = new User(name, "a@a", "password", Set.of());
        Optional<User> optionalUser = Optional.of(user);
        when(userRepository.findByUserNameWithFleet(name)).thenReturn(optionalUser);
        webSocketService.registerUserSession("session-of-" + name, principal);//, false);
    }

    private void broadcastSomething() {
        Vessel vessel = mock(Vessel.class);

        String mmsi = "12345";
//        String type = "Cargo";

        when(vessel.getMmsi()).thenReturn(mmsi);

        // edited this to fix errors: Remove conditional stubbing of getType() unless explicitly required
        String vesselMessage = "{\"mmsi\":\"" + mmsi + "\",\"type\":\"Cargo\"}";

        VesselPosition sameVesselAsPosition = new VesselPosition(vessel, 0.0, 0.0, 1.0, 0.0, 0, 0.0, 0, Instant.ofEpochSecond(0));
        webSocketService.broadcastVesselPosition(vesselMessage, vessel, sameVesselAsPosition);
    }

}
