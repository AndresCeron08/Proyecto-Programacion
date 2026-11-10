package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.reservas.EstadoReserva;
import co.edu.uniquindio.sga.reservas.Reserva;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServicioReportes {

    private static final List<String> CANALES = List.of("PORTAL", "DIRECTA", "EXTERNA");

    private final ServicioReservas reservas;
    private final ServicioApartamentos apartamentos;

    public ServicioReportes(ServicioReservas reservas, ServicioApartamentos apartamentos) {
        this.reservas = reservas;
        this.apartamentos = apartamentos;
    }

    private static boolean generaOcupacion(Reserva r) {
        EstadoReserva e = r.getEstado();
        return e == EstadoReserva.CONFIRMADA || e == EstadoReserva.EN_CURSO || e == EstadoReserva.FINALIZADA;
    }

    public long nochesOcupadas(Apartamento apto, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        long total = 0;
        for (Reserva r : reservas.getReservas()) {
            if (r.getApartamento() == apto && generaOcupacion(r)) {
                LocalDate ini = r.getEntrada().isAfter(desde) ? r.getEntrada() : desde;
                LocalDate fin = r.getSalida().isBefore(hasta) ? r.getSalida() : hasta;
                if (fin.isAfter(ini)) total += ChronoUnit.DAYS.between(ini, fin);
            }
        }
        return total;
    }

    public Map<Apartamento, Double> ocupacionPorApartamento(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        long noches = ChronoUnit.DAYS.between(desde, hasta);
        Map<Apartamento, Double> resultado = new LinkedHashMap<>();
        for (Apartamento a : apartamentos.listarActivos()) {
            resultado.put(a, nochesOcupadas(a, desde, hasta) * 100.0 / noches);
        }
        return resultado;
    }

    public long ingresosPorPeriodo(LocalDate desde, LocalDate hasta) {
        return reservasConEntradaEn(desde, hasta).stream().mapToLong(Reserva::getValorCongelado).sum();
    }

    public long comisionesPorPeriodo(LocalDate desde, LocalDate hasta) {
        return reservasConEntradaEn(desde, hasta).stream().mapToLong(Reserva::calcularComision).sum();
    }

    public long ingresosNetosPorPeriodo(LocalDate desde, LocalDate hasta) {
        return ingresosPorPeriodo(desde, hasta) - comisionesPorPeriodo(desde, hasta);
    }

    private List<Reserva> reservasConEntradaEn(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) throw new IllegalArgumentException("Periodo inválido");
        return reservas.getReservas().stream().filter(ServicioReportes::generaOcupacion).filter(r -> !r.getEntrada().isBefore(desde) && !r.getEntrada().isAfter(hasta)).toList();
    }

    public Map<String, Map<EstadoReserva, Long>> reservasPorCanal() {
        Map<String, Map<EstadoReserva, Long>> resultado = new LinkedHashMap<>();
        for (String canal : CANALES) {
            resultado.put(canal, new EnumMap<>(EstadoReserva.class));
            for (EstadoReserva e : EstadoReserva.values()) resultado.get(canal).put(e, 0L);
        }
        for (Reserva r : reservas.getReservas()) {
            resultado.get(r.getCanal()).merge(r.getEstado(), 1L, Long::sum);
        }
        return resultado;
    }

    public List<Reserva> saldosPendientes() {
        return reservas.getReservas().stream().filter(r -> r.getEstado() != EstadoReserva.CANCELADA && r.getEstado() != EstadoReserva.NO_SHOW).filter(r -> r.getFolio().calcularSaldo() > 0).sorted(Comparator.comparingLong((Reserva r) -> r.getFolio().calcularSaldo()).reversed()).toList();
    }

    public List<Apartamento> apartamentosConMasNovedades(int limite) {
        return apartamentos.listar().stream().filter(a -> !a.getNovedades().isEmpty()).sorted(Comparator.comparingInt((Apartamento a) -> a.getNovedades().size()).reversed()).limit(limite).toList();
    }

    private static void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (!hasta.isAfter(desde)) throw new IllegalArgumentException("Periodo inválido");
    }
}
