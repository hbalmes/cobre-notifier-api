package com.cobre.notifier.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para Fase 1: Setup Inicial
 * Verifica que la estructura básica del proyecto está correcta.
 * 
 * Nota: Para tests de integración completos que requieren contexto de Spring,
 * ver los tests de las fases siguientes (Fase 4, Fase 5).
 */
@DisplayName("Fase 1: Setup Inicial - Basic Structure Tests")
class CobreNotifierApiApplicationTest {

    @Test
    @DisplayName("Should have main application class")
    void shouldHaveMainApplicationClass() {
        // Verificar que la clase principal existe y es accesible
        Class<?> mainClass = CobreNotifierApiApplication.class;
        assertThat(mainClass).isNotNull();
        assertThat(mainClass.getSimpleName()).isEqualTo("CobreNotifierApiApplication");
        assertThat(mainClass.getPackage().getName()).isEqualTo("com.cobre.notifier.api");
    }

    @Test
    @DisplayName("Should have main method")
    void shouldHaveMainMethod() throws NoSuchMethodException {
        // Verificar que la clase tiene el método main
        var mainMethod = CobreNotifierApiApplication.class.getMethod("main", String[].class);
        assertThat(mainMethod).isNotNull();
        assertThat(mainMethod.getName()).isEqualTo("main");
    }

    @Test
    @DisplayName("Should have correct package structure")
    void shouldHaveCorrectPackageStructure() {
        // Verificar que las clases están en el paquete correcto
        assertThat(CobreNotifierApiApplication.class.getPackage())
                .isNotNull();
        assertThat(CobreNotifierApiApplication.class.getPackage().getName())
                .startsWith("com.cobre.notifier.api");
    }
}

