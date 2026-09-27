package net.thaumcraft.occulta.rite;

import java.util.List;

/**
 * O que um rito faz: o {@code Rite} do Witchery.
 *
 * <p>Um rito é só uma fila de passos. Quem manda no que acontece são eles — e é por isso que um rito que chama
 * chuva e um que abre um portal são a mesma coisa vista de fora: uma fila.
 *
 * <p>O tamanho do coven entra aqui porque há ritos que fazem mais com mais bruxas em volta.
 */
public interface Rite {
    List<RiteStep> steps(int coven);
}
