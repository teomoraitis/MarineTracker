//package com.di.marinetracker.backendspringboot.services;
//
//import com.di.marinetracker.backendspringboot.entities.User;
//import com.di.marinetracker.backendspringboot.entities.Vessel;
//import com.di.marinetracker.backendspringboot.repositories.UserRepository;
//import com.di.marinetracker.backendspringboot.utils.JwtUtils;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//
//import java.util.*;
//
//import static org.mockito.Mockito.*;
//
//@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
//class WebSocketServiceTest {
//
//    @Mock
//    private SimpMessagingTemplate messagingTemplate;
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private JwtUtils jwtUtils;
//
//    @InjectMocks
//    private WebSocketService webSocketService;
//
//    @Test
//    void testBroadcastVesselPosition_sendsToGuestAndAuthenticated() {
//        Vessel vessel = mock(Vessel.class);
//        when(vessel.getMmsi()).thenReturn("123456789");
//        when(vessel.getType()).thenReturn("Cargo");
//
//        String vesselMessage = "{\"mmsi\":\"123456789\",\"type\":\"Cargo\"}";
//
//        webSocketService.broadcastVesselPosition(vesselMessage, vessel);
//
//        verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(), anyString());
//    }
//
//    @Test
//    void testRegisterUserSession_validToken_registersSessionAndSendsInitialData() {
//        String sessionId = "session1";
//        String jwtToken = "valid.jwt.token";
//        String userId = "user1";
//        User user = mock(User.class);
//        when(jwtUtils.validateJwtToken(jwtToken)).thenReturn(true);
//        when(jwtUtils.getUserNameFromJwtToken(jwtToken)).thenReturn(userId);
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(user.getFleet()).thenReturn(Collections.emptyList());
//        when(user.getZoneOfInterest()).thenReturn(null);
//
//        webSocketService.registerUserSession(sessionId, jwtToken);
//
//        verify(userRepository).findById(userId);
//    }
//
//    @Test
//    void testRemoveUserSession_removesSession() {
//        String sessionId = "session2";
//        webSocketService.removeUserSession(sessionId);
//    }
//
//    @Test
//    void testUpdateUserFilters_updatesFiltersAndSendsFilteredData() {
//        String sessionId = "session3";
//        webSocketService.updateUserFilters(sessionId, Set.of("Cargo"));
//    }
//}