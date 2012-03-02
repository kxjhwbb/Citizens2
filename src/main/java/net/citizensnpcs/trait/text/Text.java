package net.citizensnpcs.trait.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.SaveId;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.trait.Toggleable;
import net.citizensnpcs.trait.text.prompt.StartPrompt;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Paginator;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLiving;

@SaveId("text")
public class Text extends Trait implements Runnable, Toggleable {
    private final Citizens plugin;
    private final NPC npc;
    private final List<String> text = new ArrayList<String>();
    private boolean talkClose;

    public Text(NPC npc) {
        this.npc = npc;
        plugin = (Citizens) Bukkit.getPluginManager().getPlugin("Citizens");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        for (DataKey sub : key.getIntegerSubKeys())
            text.add(sub.getString(""));

        if (key.keyExists("talk-close"))
            talkClose = key.getBoolean("talk-close");
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("talk-close", talkClose);
        for (int i = 0; i < text.size(); i++)
            key.setString(String.valueOf(i), text.get(i));
    }

    @Override
    public void toggle() {
        talkClose = !talkClose;
    }

    @Override
    public void run() {
        EntityHuman search = null;
        EntityLiving handle = ((CitizensNPC) npc).getHandle();
        if ((search = handle.world.findNearbyPlayer(handle, 5)) != null && talkClose)
            sendRandomText((Player) search.getBukkitEntity());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Text{talk-close=" + talkClose + ",text=");
        for (String line : text)
            builder.append(line + ",");
        builder.append("}");
        return builder.toString();
    }

    public Editor getEditor(final Player player) {
        final StartPrompt startPrompt = new StartPrompt(this);
        return new Editor() {

            @Override
            public void begin() {
                Messaging.send(player, "<b>Entered the text editor!");

                new ConversationFactory(plugin).withFirstPrompt(startPrompt).withEscapeSequence("/npc text")
                        .buildConversation(player).begin();
            }

            @Override
            public void end() {
                Messaging.send(player, "<a>Exited the text editor.");
            }
        };
    }

    public void add(String string) {
        text.add(string);
    }

    public void remove(int index) {
        text.remove(index);
    }

    public void edit(int index, String newText) {
        text.set(index, newText);
    }

    public boolean sendPage(Player player, int page) {
        Paginator paginator = new Paginator();
        paginator.setHeaderText(npc.getName() + "'s Text Entries");
        for (String line : text)
            paginator.addLine(line);

        return paginator.sendPage(player, page);
    }

    private void sendRandomText(Player player) {
        npc.chat(player, text.get(new Random().nextInt(text.size())));
    }
}