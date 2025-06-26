package at.fhv.shop_catalogue;

import at.fhv.shop_catalogue.model.Brawler;
import at.fhv.shop_catalogue.model.Gadget;
import at.fhv.shop_catalogue.model.Level;
import at.fhv.shop_catalogue.repository.BrawlerRepository;
import at.fhv.shop_catalogue.repository.GadgetRepository;
import at.fhv.shop_catalogue.repository.LevelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BrawlerRepository brawlerRepository;
    private final GadgetRepository gadgetRepository;
    private final LevelRepository levelRepository;


    public DataInitializer(BrawlerRepository brawlerRepository,
                           GadgetRepository gadgetRepository,
                           LevelRepository levelRepository) {
        this.brawlerRepository = brawlerRepository;
        this.gadgetRepository = gadgetRepository;
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
                            "Armor",
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
                           0,
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

        if (levelRepository.count() == 0) {
            levelRepository.saveAll(Arrays.asList(
                    new Level("Level 1", 0),      // Kostenlos
                    new Level("Level 2", 500),    // Kostenpflichtig
                    new Level("Level 3", 1000)    // Kostenpflichtig
            ));
            System.out.println("Initialized default levels: 3");
        } else {
            System.out.println("Levels already initialized (count = " + levelRepository.count() + ")");
        }

    }
}
