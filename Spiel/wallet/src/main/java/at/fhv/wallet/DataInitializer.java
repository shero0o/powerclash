package at.fhv.wallet;

import at.fhv.wallet.model.Brawler;
import at.fhv.wallet.model.Gadget;
import at.fhv.wallet.model.Weapon;
import at.fhv.wallet.repository.BrawlerRepository;
import at.fhv.wallet.repository.GadgetRepository;
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

    public DataInitializer(BrawlerRepository brawlerRepository,
                           GadgetRepository gadgetRepository,
                           WeaponRepository weaponRepository) {
        this.brawlerRepository = brawlerRepository;
        this.gadgetRepository = gadgetRepository;
        this.weaponRepository = weaponRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (brawlerRepository.count() == 0) {
            List<Brawler> defaultBrawlers = Arrays.asList(
                new Brawler(null,   // id
                            "Shelly",   // name
                            20,         // cost
                            100,        // healthPoints
                            "A powerful shotgun that deals heavy damage at close range."
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
                           "Healing Syringe",
                           100, 
                           "Deploys a syringe that heals you over time."
                ),
                new Gadget(null,
                           "Speed Boost",
                           150,
                           "Gives a temporary speed buff to outrun opponents."
                ),
                new Gadget(null,
                           "Shield Generator",
                           200,
                           "Places a shield that blocks incoming projectiles."
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
                            "Rifle",    // weapon type
                            200,        // damage
                            200,        // projectileSpeed
                            7,          // range
                            "1.2s",     // weaponCooldown
                            6,          // magazineSize
                            "A powerful shotgun that deals heavy damage at close range."
                    )
            );
            weaponRepository.saveAll(defaultWeapons);
            System.out.println("Initialized default weapons: " + defaultWeapons.size());
        } else {
            System.out.println("Weapons already initialized (count = " + weaponRepository.count() + ")");
        }
    }
}
