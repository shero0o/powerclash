package at.fhv.wallet;

import at.fhv.wallet.model.Brawler;
import at.fhv.wallet.model.Gadget;
import at.fhv.wallet.model.Weapon;
import at.fhv.wallet.repository.BrawlerRepository;
import at.fhv.wallet.repository.GadgetRepository;
import at.fhv.wallet.repository.LevelRepository;
import at.fhv.wallet.model.Level;
import at.fhv.wallet.repository.WeaponRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BrawlerRepository brawlerRepository;
    private final GadgetRepository gadgetRepository;
    private final WeaponRepository weaponRepository;
    private final LevelRepository levelRepository;

    public DataInitializer(BrawlerRepository brawlerRepository,
                           GadgetRepository gadgetRepository,
                           WeaponRepository weaponRepository, LevelRepository levelRepository) {
        this.brawlerRepository = brawlerRepository;
        this.gadgetRepository = gadgetRepository;
        this.weaponRepository = weaponRepository;
        this.levelRepository = levelRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (brawlerRepository.count() == 0) {
            List<Brawler> defaultBrawlers = Arrays.asList(
                new Brawler(null,   
                            "Sniper",   
                            0,         
                            100
                ),
                new Brawler(null,   
                            "Tank",   
                            100,         
                            100
                ),
                new Brawler(null,   
                            "Mage",   
                            200,         
                            100
                ),
                new Brawler(null,   
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
                new Gadget(null,
                           "Damage Boost",
                           100, 
                           "Gives a temporary damage buff to take down opponents faster."
                ),
                new Gadget(null,
                           "Speed Boost",
                           150,
                           "Gives a temporary speed buff to outrun opponents."
                ),
                new Gadget(null,
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
                            null,
                            "Rifle",    
                            2,        
                            1000,        
                            700,          
                            "100 ms",     
                            15          
                    ),
                    new Weapon(
                            null,
                            "Sniper",    
                            30,        
                            1400,        
                            2500,          
                            "2000 ms",     
                            1          
                    ),
                    new Weapon(
                            null,
                            "Shotgun",    
                            5,        
                            800,        
                            700,          
                            "2000 ms",     
                            3          
                    ),
                    new Weapon(
                            null,
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
                    new Level(null, "Level 1", 100),
                    new Level(null, "Level 2", 200),
                    new Level(null, "Level 3", 300)
            );
            levelRepository.saveAll(defaultLevels);
            System.out.println("Initialized default levels: " + defaultLevels.size());
        } else {
            System.out.println("Levels already initialized (count = " + levelRepository.count() + ")");
        }
    }
}
