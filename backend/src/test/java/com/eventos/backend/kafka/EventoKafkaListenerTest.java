package com.eventos.backend.infrastructure.adapter.input.kafka;

import com.eventos.backend.dto.kafka.EventoKafkaMessageDTO;
import com.eventos.backend.infrastructure.adapter.output.external.service.EventoSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoKafkaListenerTest {

    @Mock
    private EventoSyncService eventoSyncService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private EventoKafkaListener eventoKafkaListener;

    private EventoKafkaMessageDTO message;

    @BeforeEach
    void setUp() {
        message = EventoKafkaMessageDTO.builder()
                .operacion("UPDATE")
                .eventoId(1L)
                .timestamp(LocalDateTime.now())
                .usuario("admin")
                .descripcion("Evento actualizado")
                .build();
    }

    @Test
    void testOnEventoChange_Create_Success() {
        // Given
        message.setOperacion("CREATE");

        // When
        eventoKafkaListener.onEventoChange(message, 0, 100L, acknowledgment);

        // Then
        verify(eventoSyncService, times(1)).sincronizarEvento(1L);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void testOnEventoChange_Update_Success() {
        // Given
        message.setOperacion("UPDATE");

        // When
        eventoKafkaListener.onEventoChange(message, 0, 100L, acknowledgment);

        // Then
        verify(eventoSyncService, times(1)).sincronizarEvento(1L);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void testOnEventoChange_Delete_Success() {
        // Given
        message.setOperacion("DELETE");

        // When
        eventoKafkaListener.onEventoChange(message, 0, 100L, acknowledgment);

        // Then
        verify(eventoSyncService, times(1)).sincronizarEvento(1L);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void testOnEventoChange_UnknownOperation_StillSyncs() {
        // Given
        message.setOperacion("UNKNOWN");

        // When
        eventoKafkaListener.onEventoChange(message, 0, 100L, acknowledgment);

        // Then
        verify(eventoSyncService, times(1)).sincronizarEvento(1L);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void testOnEventoChange_NullEventoId_NoSync() {
        // Given
        message.setEventoId(null);

        // When
        eventoKafkaListener.onEventoChange(message, 0, 100L, acknowledgment);

        // Then
        verify(eventoSyncService, never()).sincronizarEvento(anyLong());
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void testOnEventoChange_SyncError_NoAcknowledgment() {
        // Given
        doThrow(new RuntimeException("Error de sincronización"))
                .when(eventoSyncService).sincronizarEvento(anyLong());

        // When
        eventoKafkaListener.onEventoChange(message, 0, 100L, acknowledgment);

        // Then
        verify(eventoSyncService, times(1)).sincronizarEvento(1L);
        verify(acknowledgment, never()).acknowledge(); // NO se confirma si hay error
    }
}

