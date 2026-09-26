package net.thaumcraft.mortuorum.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A malha da foice: o {@code scythe.obj} do Necromancy.
 *
 * <p>O original desenha a foice com um modelo de Blender, e não com caixas do jogo — são mil trezentos e trinta e
 * seis triângulos. Eles vêm em dois arquivos de dados ({@code scythe_blade.mesh} e {@code scythe_cloth.mesh}),
 * feitos por {@code scratchpad/am-foice.js} a partir do modelo original, porque trinta mil números não cabem num
 * método de Java. Cada canto são oito números: onde fica, onde está na folha e para onde a face olha; a malha já
 * vem centrada e reduzida ao tamanho de um bloco.
 */
public final class ScytheMesh {
    public static final Identifier BLADE_TEXTURE = Thaumcraft.id("textures/models/scythe_blade.png");
    public static final Identifier CLOTH_TEXTURE = Thaumcraft.id("textures/models/scythe_cloth.png");
    /** O cabo da foice de sangue, e o da de osso, que é o mesmo pano mais claro. */
    public static final Identifier HANDLE_TEXTURE = Thaumcraft.id("textures/models/scythe_handle.png");
    public static final Identifier HANDLE_BONE_TEXTURE = Thaumcraft.id("textures/models/scythe_handle_bone.png");

    /** Quantos números tem cada canto. */
    public static final int STRIDE = 8;

    private static float[] blade;
    private static float[] cloth;
    private static float[] handle;

    private ScytheMesh() {
    }

    public static float[] blade() {
        if (blade == null) blade = read(Thaumcraft.id("models/scythe_blade.mesh"));
        return blade;
    }

    public static float[] cloth() {
        if (cloth == null) cloth = read(Thaumcraft.id("models/scythe_cloth.mesh"));
        return cloth;
    }

    public static float[] handle() {
        if (handle == null) handle = read(Thaumcraft.id("models/scythe_handle.mesh"));
        return handle;
    }

    /** Esquece o que leu, para a próxima leitura pegar o que o pacote de recursos tiver posto. */
    public static void forget() {
        blade = null;
        cloth = null;
        handle = null;
    }

    private static float[] read(Identifier onde) {
        var recursos = Minecraft.getInstance().getResourceManager();
        try (InputStream entrada = recursos.open(onde)) {
            return readAll(entrada);
        } catch (IOException erro) {
            Thaumcraft.LOGGER.error("não consegui ler a malha da foice em {}", onde, erro);
            return new float[0];
        }
    }

    private static float[] readAll(InputStream entrada) throws IOException {
        byte[] bytes = entrada.readAllBytes();
        float[] saida = new float[bytes.length / 4];
        try (DataInputStream dados = new DataInputStream(new java.io.ByteArrayInputStream(bytes))) {
            for (int i = 0; i < saida.length; i++) saida[i] = dados.readFloat();
        }
        return saida;
    }
}
