package net.thaumcraft.mortuorum.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.thaumcraft.mortuorum.MinionParts;

/** O que o desenhista precisa saber do lacaio: de que peças ele é feito e há quanto tempo bateu. */
public class MinionRenderState extends LivingEntityRenderState {
    public MinionParts parts = MinionParts.EMPTY;
    /** O {@code getAttackTimer} do original: enquanto corre, os braços sobem. */
    public int attackTimer;
    public float partial;
}
