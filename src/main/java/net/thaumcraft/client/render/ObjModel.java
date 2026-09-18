package net.thaumcraft.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Um {@code .obj} do Thaumcraft original lido como o {@code AdvancedModelLoader} do Forge lia: por grupo
 * ({@code g}), com o V da textura virado, e cada triângulo como um quadrilátero de canto repetido — no formato
 * do {@link ObjMesh}.
 *
 * <p>Os arquivos ficam em {@code assets/thaumcraft/models/obj}, copiados do jar sem mudança nenhuma. Os
 * modelos maiores não cabem numa tabela no código (o Java limita o tamanho de um método), por isso são lidos
 * na primeira vez em que alguém os desenha.
 */
public final class ObjModel {
    private static final Map<String, Map<String, float[]>> CACHE = new HashMap<>();

    private ObjModel() {
    }

    /** Um grupo do modelo, ou todos juntos com {@code null}. */
    public static float[] part(String model, String group) {
        Map<String, float[]> groups = CACHE.computeIfAbsent(model, ObjModel::load);
        if (group != null) return groups.getOrDefault(group, new float[0]);
        return groups.getOrDefault("", new float[0]);
    }

    private static Map<String, float[]> load(String model) {
        Identifier id = Thaumcraft.id("models/obj/" + model + ".obj");
        List<float[]> v = new ArrayList<>(), vt = new ArrayList<>(), vn = new ArrayList<>();
        Map<String, List<Float>> groups = new LinkedHashMap<>();
        List<Float> all = new ArrayList<>();
        List<Float> current = null;
        try (BufferedReader reader = Minecraft.getInstance().getResourceManager().openAsReader(id)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.trim().split("\\s+");
                switch (p[0]) {
                    case "v" -> v.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                    case "vt" -> vt.add(new float[]{Float.parseFloat(p[1]), 1.0f - Float.parseFloat(p[2])});
                    case "vn" -> vn.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                    case "g", "o" -> current = groups.computeIfAbsent(p.length > 1 ? p[1] : "", k -> new ArrayList<>());
                    case "f" -> {
                        int corners = p.length - 1;
                        int[] order = corners == 3 ? new int[]{1, 2, 3, 3} : new int[]{1, 2, 3, 4};
                        for (int c : order) {
                            String[] idx = p[c].split("/");
                            float[] pos = v.get(Integer.parseInt(idx[0]) - 1);
                            float[] uv = idx.length > 1 && !idx[1].isEmpty() ? vt.get(Integer.parseInt(idx[1]) - 1) : new float[]{0, 0};
                            float[] n = idx.length > 2 ? vn.get(Integer.parseInt(idx[2]) - 1) : new float[]{0, 1, 0};
                            for (float f : new float[]{pos[0], pos[1], pos[2], uv[0], uv[1], n[0], n[1], n[2]}) {
                                all.add(f);
                                if (current != null) current.add(f);
                            }
                        }
                    }
                    default -> {
                    }
                }
            }
        } catch (IOException e) {
            Thaumcraft.LOGGER.error("não consegui ler o modelo {}", id, e);
        }
        Map<String, float[]> out = new HashMap<>();
        out.put("", toArray(all));
        groups.forEach((name, list) -> out.put(name, toArray(list)));
        return out;
    }

    private static float[] toArray(List<Float> list) {
        float[] out = new float[list.size()];
        for (int i = 0; i < out.length; i++) out[i] = list.get(i);
        return out;
    }
}
