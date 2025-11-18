package com.eventos.backend.service;

import com.eventos.backend.domain.Usuario;
import com.eventos.backend.dto.ChangePasswordRequestDTO;
import com.eventos.backend.dto.UpdateUsuarioRequestDTO;
import com.eventos.backend.dto.UsuarioDTO;
import com.eventos.backend.exception.BadRequestException;
import com.eventos.backend.exception.ConflictException;
import com.eventos.backend.exception.ForbiddenException;
import com.eventos.backend.exception.ResourceNotFoundException;
import com.eventos.backend.mapper.UsuarioMapper;
import com.eventos.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private UsuarioDTO usuarioDTO;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .username("testuser")
                .password("$2a$10$hashedpassword")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        usuarioDTO = UsuarioDTO.builder()
                .id(1L)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testFindAll() {
        // Given
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioRepository.findAll()).thenReturn(usuarios);
        when(usuarioMapper.toDTO(any(Usuario.class))).thenReturn(usuarioDTO);

        // When
        List<UsuarioDTO> result = usuarioService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Success() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDTO(usuario)).thenReturn(usuarioDTO);

        // When
        UsuarioDTO result = usuarioService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(usuarioDTO.getId(), result.getId());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> usuarioService.findById(1L));
    }

    @Test
    void testFindByUsername_Success() {
        // Given
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDTO(usuario)).thenReturn(usuarioDTO);

        // When
        UsuarioDTO result = usuarioService.findByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals(usuarioDTO.getUsername(), result.getUsername());
    }

    @Test
    void testUpdate_Success() {
        // Given
        UpdateUsuarioRequestDTO updateRequest = UpdateUsuarioRequestDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .email("updated@example.com")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioMapper.toDTO(usuario)).thenReturn(usuarioDTO);

        // When
        UsuarioDTO result = usuarioService.update(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void testUpdate_EmailConflict() {
        // Given
        UpdateUsuarioRequestDTO updateRequest = UpdateUsuarioRequestDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .email("existing@example.com")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThrows(ConflictException.class, () -> usuarioService.update(1L, updateRequest));
    }

    @Test
    void testChangePassword_Success() {
        // Given
        ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
                .currentPassword("oldpassword")
                .newPassword("newpassword")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("oldpassword", usuario.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newpassword", usuario.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newpassword")).thenReturn("$2a$10$newhashedpassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.changePassword(1L, changePasswordRequest, "testuser");

        // Then
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void testChangePassword_WrongCurrentPassword() {
        // Given
        ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
                .currentPassword("wrongpassword")
                .newPassword("newpassword")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongpassword", usuario.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(BadRequestException.class, 
                () -> usuarioService.changePassword(1L, changePasswordRequest, "testuser"));
    }

    @Test
    void testChangePassword_Forbidden() {
        // Given
        ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
                .currentPassword("oldpassword")
                .newPassword("newpassword")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // When & Then
        assertThrows(ForbiddenException.class, 
                () -> usuarioService.changePassword(1L, changePasswordRequest, "otheruser"));
    }

    @Test
    void testDisable_Success() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.disable(1L);

        // Then
        assertFalse(usuario.getEnabled());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void testEnable_Success() {
        // Given
        usuario.setEnabled(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.enable(1L);

        // Then
        assertTrue(usuario.getEnabled());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void testExistsByUsername() {
        // Given
        when(usuarioRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = usuarioService.existsByUsername("testuser");

        // Then
        assertTrue(result);
        verify(usuarioRepository, times(1)).existsByUsername("testuser");
    }

    @Test
    void testExistsByEmail() {
        // Given
        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = usuarioService.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
        verify(usuarioRepository, times(1)).existsByEmail("test@example.com");
    }
}

