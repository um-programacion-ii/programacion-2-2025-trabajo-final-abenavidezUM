package com.eventos.backend.mapper;

import com.eventos.backend.domain.Venta;
import com.eventos.backend.dto.VentaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VentaMapper {

    private final AsientoVentaMapper asientoVentaMapper;

    public VentaDTO toDTO(Venta venta) {
        if (venta == null) {
            return null;
        }

        return VentaDTO.builder()
                .id(venta.getId())
                .idExterno(venta.getIdExterno())
                .eventoId(venta.getEvento() != null ? venta.getEvento().getId() : null)
                .eventoTitulo(venta.getEvento() != null ? venta.getEvento().getTitulo() : null)
                .fechaVenta(venta.getFechaVenta())
                .precioTotal(venta.getPrecioTotal())
                .resultado(venta.getResultado())
                .descripcion(venta.getDescripcion())
                .confirmadaCatedra(venta.getConfirmadaCatedra())
                .asientos(asientoVentaMapper.toDTOList(venta.getAsientos()))
                .createdAt(venta.getCreatedAt())
                .build();
    }

    public List<VentaDTO> toDTOList(List<Venta> ventas) {
        if (ventas == null) {
            return null;
        }

        return ventas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

