package co.edu.uniquindio.sga.servicios;

import co.edu.uniquindio.sga.Fixtures;
import co.edu.uniquindio.sga.alojamiento.Apartamento;
import co.edu.uniquindio.sga.alojamiento.Temporada;
import co.edu.uniquindio.sga.alojamiento.TipoTemporada;
import co.edu.uniquindio.sga.personas.Ocupante;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServicioTarifasTest {

    private ServicioTarifas servicio;
    private Apartamento apto;

    @BeforeEach
    void preparar() {
        servicio = new ServicioTarifas();
        apto = Fixtures.apto();
    }

    private static LocalDate dic(int dia) { return LocalDate.of(2026, 12, dia); }

    @Test
    void sinTemporadasDefinidasTodoEsBaja() {
        assertEquals(TipoTemporada.BAJA, servicio.temporadaDe(dic(10)));
    }

    @Test
    void identificaLaTemporadaIncluyendoLosLimites() {
        servicio.agregarTemporada(new Temporada(TipoTemporada.MEDIA, dic(1), dic(5)));
        assertEquals(TipoTemporada.BAJA, servicio.temporadaDe(LocalDate.of(2026, 11, 30)));
        assertEquals(TipoTemporada.MEDIA, servicio.temporadaDe(dic(1)));
        assertEquals(TipoTemporada.MEDIA, servicio.temporadaDe(dic(5)));
        assertEquals(TipoTemporada.BAJA, servicio.temporadaDe(dic(6)));
    }

    @Test
    void altaTienePrioridadSobreMediaCuandoSeSolapan() {
        servicio.agregarTemporada(new Temporada(TipoTemporada.MEDIA, dic(1), dic(5)));
        servicio.agregarTemporada(new Temporada(TipoTemporada.ALTA, dic(3), dic(10)));
        assertEquals(TipoTemporada.MEDIA, servicio.temporadaDe(dic(2)));
        assertEquals(TipoTemporada.ALTA, servicio.temporadaDe(dic(4)));
        assertEquals(TipoTemporada.ALTA, servicio.temporadaDe(dic(10)));
        assertEquals(TipoTemporada.BAJA, servicio.temporadaDe(dic(11)));
    }

    @Test
    void liquidaNocheXNocheCuandoLaEstanciaCruzaTemporadas() {
        servicio.agregarTemporada(new Temporada(TipoTemporada.MEDIA, dic(3), dic(4)));
        servicio.agregarTemporada(new Temporada(TipoTemporada.ALTA, dic(5), dic(31)));
        Map<LocalDate, Long> detalle = servicio.desglose(apto, dic(2), dic(6), Fixtures.adultos(2));
        assertEquals(4, detalle.size());
        assertEquals(200_000L, detalle.get(dic(2)));
        assertEquals(300_000L, detalle.get(dic(3)));
        assertEquals(300_000L, detalle.get(dic(4)));
        assertEquals(400_000L, detalle.get(dic(5)));
        assertEquals(1_200_000, servicio.calcularValor(apto, dic(2), dic(6), Fixtures.adultos(2)));
    }

    @Test
    void losMenoresDeSeisOcupanCupoPeroNoSuman() {
        List<Ocupante> grupo = new ArrayList<>(Fixtures.adultos(2));
        grupo.add(Fixtures.nino("Sofía"));
        assertEquals(600_000, servicio.calcularValor(apto, Fixtures.ENTRADA, Fixtures.SALIDA, grupo));
    }

    @Test
    void laEdadSeCalculaALaFechaDeEntrada() {
        LocalDate entrada = dic(2);
        Ocupante cumpleSeisEseDia = new Ocupante("A", LocalDate.of(2020, 12, 2));
        Ocupante cumpleSeisAlDiaSiguiente = new Ocupante("B", LocalDate.of(2020, 12, 3));
        long valor = servicio.calcularValor(apto, entrada, dic(3), List.of(cumpleSeisEseDia, cumpleSeisAlDiaSiguiente));
        assertEquals(100_000, valor); // solo paga A (ya tiene 6 años a la fecha de entrada)
    }

    @Test
    void exigeSalidaPosteriorALaEntrada() {
        assertThrows(IllegalArgumentException.class, () -> servicio.calcularValor(apto, dic(5), dic(5), Fixtures.adultos(1)));
        assertThrows(IllegalArgumentException.class, () -> servicio.desglose(apto, dic(6), dic(5), Fixtures.adultos(1)));
    }

    @Test
    void sinTarifaParaLaTemporadaNoSeLiquida() {
        Apartamento sinAlta = new Apartamento("A-7", "Sin alta", 1, 2, List.of());
        sinAlta.definirTarifa(TipoTemporada.BAJA, 100_000);
        servicio.agregarTemporada(new Temporada(TipoTemporada.ALTA, dic(1), dic(31)));
        assertThrows(IllegalStateException.class,
                () -> servicio.calcularValor(sinAlta, dic(2), dic(4), Fixtures.adultos(1)));
    }

    @Test
    void guardaLasTemporadasAgregadas() {
        servicio.agregarTemporada(new Temporada(TipoTemporada.ALTA, dic(1), dic(31)));
        assertEquals(1, servicio.getTemporadas().size());
        assertThrows(UnsupportedOperationException.class, () -> servicio.getTemporadas().clear());
    }
}
