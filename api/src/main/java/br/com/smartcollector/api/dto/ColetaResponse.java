package br.com.smartcollector.api.dto;

import br.com.smartcollector.api.model.Coleta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ColetaResponse(
        Long id,
        LocalDateTime dataColeta,
        Long idCatador,
        Long idDescartador,
        Long idCentro,
        boolean finalizada,
        BigDecimal volumeTotal,
        List<Long> idsItens
) {
    public static ColetaResponse de(Coleta coleta, BigDecimal volume, List<Long> itens) {
        return new ColetaResponse(
                coleta.getId(),
                coleta.getDataColeta(),
                coleta.getCatador() == null ? null : coleta.getCatador().getId(),
                coleta.getDescartador() == null ? null : coleta.getDescartador().getId(),
                coleta.getCentro() == null ? null : coleta.getCentro().getId(),
                Boolean.TRUE.equals(coleta.getFinalizada()),
                volume,
                itens);
    }
}
