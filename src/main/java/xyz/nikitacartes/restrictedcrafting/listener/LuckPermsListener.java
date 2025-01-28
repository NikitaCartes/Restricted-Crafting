package xyz.nikitacartes.restrictedcrafting.listener;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.group.Group;

import static xyz.nikitacartes.restrictedcrafting.RestrictedCrafting.updateCrafterRestriction;

public class LuckPermsListener {
    private final LuckPerms luckPerms;

    public LuckPermsListener(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    public void registerListeners() {
        EventBus eventBus = this.luckPerms.getEventBus();
        eventBus.subscribe(NodeMutateEvent.class, this::onNodeMutate);
    }

    private void onNodeMutate(NodeMutateEvent e) {
        if (e.isUser()) {
            return;
        }

        if (((Group) e.getTarget()).getName().equals("default")) {
            updateCrafterRestriction();
        }
    }
}
