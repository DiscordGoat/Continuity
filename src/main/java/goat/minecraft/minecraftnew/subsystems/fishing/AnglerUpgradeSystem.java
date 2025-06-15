package goat.minecraft.minecraftnew.subsystems.fishing;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AnglerUpgradeSystem implements Listener {
    private final MinecraftNew plugin;

    public enum UpgradeType {
        FISH_YIELD("Fish Yield", "Chance to catch extra fish", Material.COD, 5, 11),
        TREASURE("Treasure Luck", "Increased treasure chance", Material.CHEST, 5, 13),
        SEA_CREATURE("Sea Creature Lure", "Increased sea creature chance", Material.TRIDENT, 5, 15);

        private final String name; private final String desc; private final Material icon; private final int max; private final int slot;
        UpgradeType(String n, String d, Material i, int m, int s){this.name=n;this.desc=d;this.icon=i;this.max=m;this.slot=s;}
        public String getName(){return name;} public String getDesc(){return desc;} public Material getIcon(){return icon;} public int getMax(){return max;} public int getSlot(){return slot;}
    }

    public AnglerUpgradeSystem(MinecraftNew plugin){this.plugin=plugin;}

    public void openUpgradeGUI(Player player, ItemStack rod){
        int total = getTotalEnergy(rod);
        if(total==0){player.sendMessage(ChatColor.RED+"This rod has no angler energy!");return;}
        Inventory gui = Bukkit.createInventory(new AnglerUpgradeHolder(),27,ChatColor.AQUA+"Fishing Upgrades");
        for(int i=0;i<27;i++) gui.setItem(i,createPane());
        int avail = calcAvailable(rod);
        int cost =8;
        gui.setItem(10,createUpgradeItem(UpgradeType.FISH_YIELD,rod,cost,avail));
        gui.setItem(12,createUpgradeItem(UpgradeType.TREASURE,rod,cost,avail));
        gui.setItem(14,createUpgradeItem(UpgradeType.SEA_CREATURE,rod,cost,avail));
        gui.setItem(26,createPowerDisplay(total,getPowerCap(rod),avail));
        player.openInventory(gui);
    }

    private ItemStack createUpgradeItem(UpgradeType up, ItemStack rod, int cost, int avail){
        ItemStack item=new ItemStack(up.getIcon());
        ItemMeta m=item.getItemMeta();
        int lvl=getUpgradeLevel(rod,up);
        boolean max=lvl>=up.getMax();
        boolean can=avail>=cost;
        String name=max?ChatColor.GOLD+up.getName()+" (MAX)":can?ChatColor.GREEN+up.getName()+" ("+lvl+"/"+up.getMax()+")":ChatColor.RED+up.getName()+" ("+lvl+"/"+up.getMax()+")";
        m.setDisplayName(name);
        List<String> lore=new ArrayList<>();
        lore.add(ChatColor.GRAY+up.getDesc());
        lore.add(ChatColor.GRAY+"Current: "+ChatColor.WHITE+lvl+"/"+up.getMax());
        if(!max){
            lore.add(ChatColor.GRAY+"Cost: "+ChatColor.WHITE+cost+"% energy");
            lore.add(can?ChatColor.GREEN+"Click to upgrade!":ChatColor.RED+"Not enough energy!");
        } else lore.add(ChatColor.GOLD+"Maximum level reached!");
        m.setLore(lore);
        item.setItemMeta(m);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(!(e.getInventory().getHolder() instanceof AnglerUpgradeHolder)) return;
        e.setCancelled(true);
        if(!(e.getWhoClicked() instanceof Player)) return;
        Player player=(Player)e.getWhoClicked();
        ItemStack rod=player.getInventory().getItemInMainHand();
        if(rod==null||rod.getType()!=Material.FISHING_ROD) {player.sendMessage(ChatColor.RED+"Hold a fishing rod!");return;}
        UpgradeType clicked=null;
        for(UpgradeType u:UpgradeType.values()) if(u.getSlot()==e.getSlot()) clicked=u;
        if(clicked==null) return;
        int avail=calcAvailable(rod); int cost=8; int lvl=getUpgradeLevel(rod,clicked);
        if(lvl>=clicked.getMax()){player.sendMessage(ChatColor.RED+"Upgrade maxed!");return;}
        if(avail<cost){player.sendMessage(ChatColor.RED+"Not enough angler energy!");return;}
        setUpgradeLevel(rod,clicked,lvl+1);
        player.sendMessage(ChatColor.GREEN+"Upgraded "+clicked.getName()+" to level "+(lvl+1));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1f,1f);
        player.closeInventory();
        openUpgradeGUI(player,rod);
    }

    private int getTotalEnergy(ItemStack rod){return BaitApplicationSystem.getRodAnglerEnergyStatic(rod);}
    private int calcAvailable(ItemStack rod){int total=getTotalEnergy(rod);int spent=0;for(UpgradeType u:UpgradeType.values()){spent+=getUpgradeLevel(rod,u)*8;}return total-spent;}
    private int getPowerCap(ItemStack rod){if(!rod.hasItemMeta()||!rod.getItemMeta().hasLore()) return 100;for(String line:rod.getItemMeta().getLore()){String s=ChatColor.stripColor(line);if(s.startsWith("Power Cap: ")){String c=s.substring(10).replace("%","");try{return Integer.parseInt(c);}catch(Exception ignored){return 100;}}}return 100;}

    private int getUpgradeLevel(ItemStack rod, UpgradeType up){if(!rod.hasItemMeta()||!rod.getItemMeta().hasLore()) return 0;for(String line:rod.getItemMeta().getLore()){String st=ChatColor.stripColor(line);if(st.startsWith("Fishing Upgrades:")){return parseFromLine(line,up);} }return 0;}

    private int parseFromLine(String line, UpgradeType up){String sym=getPlainSymbol(up);String stripped=ChatColor.stripColor(line);int idx=stripped.indexOf(sym);if(idx==-1) return 0;String after=stripped.substring(idx+sym.length());if(after.startsWith("ⱽᴵ")) return 6; if(after.startsWith("ᴵᴵᴵ")) return 3; if(after.startsWith("ᴵᴵ")) return 2; if(after.startsWith("ᴵⱽ")) return 4; if(after.startsWith("ⱽ")) return 5; if(after.startsWith("ᴵ")) return 1; return 0;}

    private void setUpgradeLevel(ItemStack rod, UpgradeType up, int level){ItemMeta meta=rod.getItemMeta();List<String> lore=meta.hasLore()?new ArrayList<>(meta.getLore()):new ArrayList<>();lore.removeIf(l->ChatColor.stripColor(l).startsWith("UPGRADE_"));if(level>0) updateLore(lore,up,level);meta.setLore(lore);rod.setItemMeta(meta);}

    private void updateLore(List<String> lore, UpgradeType up,int level){int idx=-1;for(int i=0;i<lore.size();i++){if(ChatColor.stripColor(lore.get(i)).startsWith("Fishing Upgrades:")){idx=i;break;}}Map<UpgradeType,Integer> map=getAll(lore);if(level>0) map.put(up,level); else map.remove(up);if(!map.isEmpty()){StringBuilder sb=new StringBuilder();sb.append(ChatColor.GRAY).append("Fishing Upgrades: ");boolean first=true;for(Map.Entry<UpgradeType,Integer>e:map.entrySet()){if(!first) sb.append(" ");sb.append(getSymbol(e.getKey(),e.getValue()));first=false;}String line=sb.toString();if(idx>=0) lore.set(idx,line);else lore.add(findInsert(lore),line);} else if(idx>=0) lore.remove(idx);}

    private Map<UpgradeType,Integer> getAll(List<String> lore){Map<UpgradeType,Integer> m=new LinkedHashMap<>();for(String line:lore){String stripped=ChatColor.stripColor(line);if(stripped.startsWith("Fishing Upgrades:")){for(UpgradeType u:UpgradeType.values()){int lv=parseFromLine(line,u);if(lv>0)m.put(u,lv);}break;}}return m;}

    private int findInsert(List<String> lore){return lore.size();}

    private String getSymbol(UpgradeType up,int level){String s=getPlainSymbol(up);ChatColor c=getColor(level);return c+s+getNumeral(level);} private String getPlainSymbol(UpgradeType up){return switch(up){case FISH_YIELD->"🐟";case TREASURE->"💰";case SEA_CREATURE->"🐠";};}
    private ChatColor getColor(int level){return switch(level){case 1->ChatColor.WHITE;case 2->ChatColor.GREEN;case 3->ChatColor.BLUE;case 4->ChatColor.LIGHT_PURPLE;case 5->ChatColor.GOLD;default->ChatColor.GRAY;};}
    private String getNumeral(int level){return switch(level){case 1->"ᴵ";case 2->"ᴵᴵ";case 3->"ᴵᴵᴵ";case 4->"ᴵⱽ";case 5->"ⱽ";default->"";};}

    private ItemStack createPowerDisplay(int total,int cap,int avail){ItemStack it=new ItemStack(Material.PRISMARINE_CRYSTALS);ItemMeta m=it.getItemMeta();m.setDisplayName(ChatColor.AQUA+"Angler Energy Status");List<String> lore=new ArrayList<>();lore.add(ChatColor.GRAY+"Total: "+ChatColor.WHITE+total+"%"+ChatColor.GRAY+" / "+ChatColor.YELLOW+cap+"%");lore.add(ChatColor.GRAY+"Available: "+ChatColor.GREEN+avail+"%"+ChatColor.GRAY+" Spent: "+ChatColor.RED+(total-avail)+"%");lore.add("");lore.add(createBar(total,cap,avail));lore.add("");lore.add(ChatColor.GRAY+"Apply bait to increase energy");lore.add(ChatColor.GRAY+"Use Pearls to raise cap");m.setLore(lore);it.setItemMeta(m);return it;}

    private String createBar(int total,int cap,int avail){int len=20+(cap-100)/100*5;int filled=(int)((double)total/cap*len);int spent=(int)((double)(total-avail)/cap*len);StringBuilder sb=new StringBuilder();sb.append(ChatColor.DARK_GRAY+"[");for(int i=0;i<spent;i++) sb.append(ChatColor.RED+"|");for(int i=spent;i<filled;i++) sb.append(ChatColor.GREEN+"|");for(int i=filled;i<len;i++) sb.append(ChatColor.GRAY+"|");sb.append(ChatColor.DARK_GRAY+"]");return sb.toString();}

    private ItemStack createPane(){ItemStack it=new ItemStack(Material.GRAY_STAINED_GLASS_PANE);ItemMeta m=it.getItemMeta();m.setDisplayName(ChatColor.BLACK+"");it.setItemMeta(m);return it;}

    private static class AnglerUpgradeHolder implements InventoryHolder {public Inventory getInventory(){return null;}}
}
