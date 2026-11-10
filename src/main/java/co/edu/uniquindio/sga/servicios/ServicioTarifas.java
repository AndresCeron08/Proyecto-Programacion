package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.alojamiento.Temporada;
import co.edu.uniquindio.sga.alojamiento.TipoTemporada;
import co.edu.uniquindio.sga.personas.Ocupante;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServicioTarifas {

    private final List<Temporada> temporadas = new ArrayList<>();

    public void agregarTemporada(Temporada temporada) { temporadas.add(temporada); }

    public List<Temporada> getTemporadas() { return Collections.unmodifiableList(temporadas); }

    public TipoTemporada temporadaDe(LocalDate noche) {
        if (esDeTipo(TipoTemporada.ALTA, noche)) return TipoTemporada.ALTA;
        if (esDeTipo(TipoTemporada.MEDIA, noche)) return TipoTemporada.MEDIA;
        return TipoTemporada.BAJA;
    }

    private boolean esDeTipo(TipoTemporada tipo, LocalDate noche) {
        return temporadas.stream().anyMatch(t -> t.getTipo() == tipo && t.contiene(noche));
    }

    public Map<LocalDate, Long> desglose(Apartamento apto, LocalDate entrada, LocalDate salida,
                                         List<Ocupante> ocupantes) {
        if (!salida.isAfter(entrada)) {
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada");
        }
        long pagantes = ocupantes.stream().filter(o -> o.paga(entrada)).count();
        Map<LocalDate, Long> detalle = new LinkedHashMap<>();
        for (LocalDate noche = entrada; noche.isBefore(salida); noche = noche.plusDays(1)) {
            detalle.put(noche, apto.valorPersonaNoche(temporadaDe(noche)) * pagantes);
        }
        return detalle;
    }

    public long calcularValor(Apartamento apto, LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes) {
        return desglose(apto, entrada, salida, ocupantes).values().stream().mapToLong(Long::longValue).sum();
    }
}
