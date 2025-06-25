package at.fhv.wallet;

import at.fhv.wallet.model.*;
import at.fhv.wallet.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BrawlerRepository brawlerRepository;
    private final GadgetRepository gadgetRepository;
    private final WeaponRepository weaponRepository;
    private final LevelRepository levelRepository;
    private final PlayerRepository playerRepository;
    private final SelectedRepository selectedRepository;

    public DataInitializer(BrawlerRepository brawlerRepository,
                           GadgetRepository gadgetRepository,
                           WeaponRepository weaponRepository, LevelRepository levelRepository, PlayerRepository playerRepository, SelectedRepository selectedRepository) {
        this.brawlerRepository = brawlerRepository;
        this.gadgetRepository = gadgetRepository;
        this.weaponRepository = weaponRepository;
        this.levelRepository = levelRepository;
        this.playerRepository = playerRepository;
        this.selectedRepository = selectedRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (brawlerRepository.count() == 0) {
            List<Brawler> defaultBrawlers = Arrays.asList(
                new Brawler(1L,
                            "Sniper",   
                            0,         
                            100
                ),
                new Brawler(2L,
                            "Tank",   
                            100,         
                            100
                ),
                new Brawler(3L,
                            "Mage",   
                            200,         
                            100
                ),
                new Brawler(4L,
                            "Healer",   
                            300,         
                            100
                )
            );
            brawlerRepository.saveAll(defaultBrawlers);
            System.out.println("Initialized default brawlers: " + defaultBrawlers.size());
        } else {
            System.out.println("Brawlers already initialized (count = " + brawlerRepository.count() + ")");
        }

        if (gadgetRepository.count() == 0) {
            List<Gadget> defaultGadgets = Arrays.asList(
                new Gadget(1L,
                           "Damage Boost",
                           0,
                           "Gives a temporary damage buff to take down opponents faster."
                ),
                new Gadget(2L,
                           "Speed Boost",
                           150,
                           "Gives a temporary speed buff to outrun opponents."
                ),
                new Gadget(3L,
                           "HP Boost",
                           200,
                           "Gives a temporary health buff to outtank your opponents."
                )
            );
            gadgetRepository.saveAll(defaultGadgets);
            System.out.println("Initialized default gadgets: " + defaultGadgets.size());
        } else {
            System.out.println("Gadgets already initialized (count = " + gadgetRepository.count() + ")");
        }

        if (weaponRepository.count() == 0) {
            List<Weapon> defaultWeapons = Arrays.asList(
                    new Weapon(
                            1L,
                            "Rifle",    
                            2,        
                            1000,        
                            700,          
                            "100 ms",     
                            15          
                    ),
                    new Weapon(
                            2L,
                            "Sniper",    
                            30,        
                            1400,        
                            2500,          
                            "2000 ms",     
                            1          
                    ),
                    new Weapon(
                            3L,
                            "Shotgun",    
                            5,        
                            800,        
                            700,          
                            "2000 ms",     
                            3          
                    ),
                    new Weapon(
                            4L,
                            "Mine",    
                            40,        
                            750,        
                            700,          
                            "2000 ms",     
                            1          
                    )
            );
            weaponRepository.saveAll(defaultWeapons);
            System.out.println("Initialized default weapons: " + defaultWeapons.size());
        } else {
            System.out.println("Weapons already initialized (count = " + weaponRepository.count() + ")");
        }

        if (levelRepository.count() == 0) {
            List<Level> defaultLevels = Arrays.asList(
                    new Level(1L, "Level 1", 0),
                    new Level(2L, "Level 2", 200),
                    new Level(3L, "Level 3", 300)
            );
            levelRepository.saveAll(defaultLevels);
            System.out.println("Initialized default levels: " + defaultLevels.size());
        } else {
            System.out.println("Levels already initialized (count = " + levelRepository.count() + ")");
        }

        if (playerRepository.count() == 0) {
            Player defaultPlayer = new Player();
            defaultPlayer.setId(UUID.randomUUID());
            defaultPlayer.setCoins(0);
            defaultPlayer.getLevels().add(new Level(1L, "Level 1", 0));
            defaultPlayer.getBrawlers().add(new Brawler(1L, "Sniper",0,100));
            defaultPlayer.getGadgets().add(new Gadget(1L,"Damage Boost",0,"Gives a temporary damage buff to take down opponents faster."));

            playerRepository.save(defaultPlayer);

            Selected s = new Selected();
            s.setSelectedId(defaultPlayer.getId());
            selectedRepository.save(s);
        }


    }
}
