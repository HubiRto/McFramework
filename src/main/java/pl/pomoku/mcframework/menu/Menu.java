package pl.pomoku.mcframework.menu;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@RequiredArgsConstructor
public abstract class Menu implements InventoryHolder {
    protected Inventory inventory;
    protected final Player player;

    public abstract Component getMenuName();

    public abstract int getSlots();

    public abstract void handleClick(InventoryClickEvent e);

    public abstract void setMenuItems();

    public void open() {
        this.inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        this.setMenuItems();
        this.player.openInventory(this.inventory);
    }

    @Override
    public @NonNull Inventory getInventory() {
        return inventory;
    }
}