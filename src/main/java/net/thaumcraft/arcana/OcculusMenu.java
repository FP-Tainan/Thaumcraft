package net.thaumcraft.arcana;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCMenus;

import java.util.List;

/**
 * A árvore de perícias aberta: o caminho para a tela e para o botão de comprar.
 *
 * <p>Ela não tem casa nenhuma — não há nada para pôr nem para tirar. O que ela tem é o
 * {@link #clickMenuButton}, que é como o jogo manda um clique de botão ao servidor sem se inventar pacote
 * nenhum: é o mesmo caminho da mesa de encantamento e do cortador de pedra.
 *
 * <p>Quem sabe o que se sabe é o dado preso à pessoa, que já chega ao cliente por conta própria. Por isso a
 * tela desenha sem perguntar nada, e só a <b>compra</b> precisa de ir ao servidor.
 */
public class OcculusMenu extends AbstractContainerMenu {
    public OcculusMenu(int id, Inventory mochila) {
        super(TCMenus.OCCULUS, id);
    }

    /**
     * Compra a perícia daquele número.
     *
     * <p>O número é o lugar dela em {@link SkillTree#entries()}, que é fixo — o quadro se escreve uma vez e
     * não muda. O servidor prova tudo de novo: que a perícia existe, que o que ela pede já é sabido, e que há
     * ponto daquela cor. Um cliente mentiroso não compra nada.
     */
    @Override
    public boolean clickMenuButton(Player quem, int qual) {
        List<SkillTree.Entry> quadro = SkillTree.entries();
        if (qual < 0 || qual >= quadro.size()) return false;

        SkillTree.Entry perícia = quadro.get(qual);
        SkillData sabe = SkillData.of(quem);
        int nível = Mana.of(quem).level();

        if (!sabe.canLearn(perícia, nível)) return false;
        SkillData agora = sabe.learn(perícia, nível);
        if (agora.equals(sabe)) return false;
        SkillData.set(quem, agora);

        // saber é receber: a peça vai para a mão de quem aprendeu
        var item = ArcanaItems.itemOf(perícia.part());
        if (item != null && !quem.getInventory().add(new ItemStack(item))) {
            quem.drop(new ItemStack(item), false);
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player quem, int qual) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player quem) {
        return true;
    }
}
