package pl.pomoku.mcframework.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class PaginatedMenu extends Menu {
    protected int page = 0;
    protected int maxItemsPerPage;
    protected int index = 0;

    public abstract int setMaxItemsPerPage();

    public PaginatedMenu(Player player) {
        super(player);
        this.maxItemsPerPage = setMaxItemsPerPage();
    }

    public void previewsPage(int slotNumber, ItemStack item) {
        if (page != 0) {
            inventory.setItem(slotNumber, item);
        }
    }

    public void nextPage(int slotNumber, int amountOfItems, ItemStack item) {
        if (page != amountOfItems / maxItemsPerPage) {
            inventory.setItem(slotNumber, item);
        }
    }
}
