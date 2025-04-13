import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.time.LocalDate;
// import java.time.format.DateTimeParseException;
// import java.time.DayOfWeek;

import java.sql.*;

class DatabaseConfig {
    public static final String URL = "jdbc:sqlite:C:/Users/ingma/Documents/sqlite3/pianobasedb.db";
}

public class Main{

    public static void loadPianosFromDatabase(PianoTechnician technician) {
        
        String sql = "SELECT merk, model, serie_nummer, status, bouwjaar, klantnaam FROM piano";

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String merk = resultSet.getString("merk");
                String model = resultSet.getString("model");
                String serialNumber = resultSet.getString("serie_nummer");
                String status = resultSet.getString("status");
                int bouwjaar = resultSet.getInt("bouwjaar");
                String klantnaam = resultSet.getString("klantnaam");

                Piano piano = new Piano(merk, model, serialNumber, status, bouwjaar);
                piano.setKlantnaam(klantnaam); // Stel de klantnaam in, indien beschikbaar
                technician.addPiano(piano);
            }
            System.out.println("All pianos loaded from the database and added to the technician.");

        } catch (SQLException e) {
            System.out.println("Error loading pianos from the database:");
            e.printStackTrace();
        }
    }

    public static void loadRepairsFromDatabase(PianoTechnician technician) {
    
    String sql = "SELECT reparatieid, datum, pianoid, tijd_gekost, status, beschrijving, onderdelen_kosten FROM reparatie";
    
    try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
         Statement statement = connection.createStatement();
         ResultSet rs = statement.executeQuery(sql)) {

        while (rs.next()) {
            int id = rs.getInt("reparatieid");
            String date = rs.getString("datum");
            String dbPianoId = rs.getString("pianoid");
            double timeSpent = rs.getDouble("tijd_gekost");
            String status = rs.getString("status");
            String beschrijving = rs.getString("beschrijving");
            double cost = rs.getDouble("onderdelen_kosten");

            Repair repair = new Repair(id, date, dbPianoId, timeSpent, status, beschrijving, cost);

            // Zoek de corresponderende piano op basis van serienummer hashcode.
            // We gaan ervan uit dat bij het toevoegen van een repair het piano-id werd gemaakt als:
            // repairPianoId = piano.getSerialNumber().hashCode()
            for (Piano piano : technician.getPianos()) {
                if (piano.getSerialNumber().equals(dbPianoId)) {
                    technician.addRepairToPiano(piano.getSerialNumber(), repair);
                    break;
                }
            }
        }
        System.out.println("All repairs loaded and linked to their corresponding pianos.");
    } catch (SQLException e) {
        System.out.println("Error loading repairs from the database:");
        e.printStackTrace();
    }  
}

    public static void loadTuningsFromDatabase(PianoTechnician technician) {
        

        String sql = "SELECT stemburtid, datum, pianoid, tijd_gekost, opmerking, stembaarheid, soepelheid FROM stembeurt";
        
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("stemburtid");
                String date = rs.getString("datum");
                String dbPianoId = rs.getString("pianoid");
                double timeSpent = rs.getDouble("tijd_gekost");
                String remark = rs.getString("opmerking");
                int tuningEase = rs.getInt("stembaarheid");
                int tuningQuality = rs.getInt("soepelheid");

                Tuning tuning = new Tuning(id, date, dbPianoId, timeSpent, tuningEase, tuningQuality, remark);

                // Zoek de corresponderende piano op basis van het serialnummer
                for (Piano piano : technician.getPianos()) {
                    if (piano.getSerialNumber().equals(dbPianoId)) {
                        technician.addTuningToPiano(piano.getSerialNumber(), tuning);
                        break;
                    }
                }
            }
            System.out.println("All tunings loaded and linked to their corresponding pianos.");
        } catch (SQLException e) {
            System.out.println("Error loading tunings from the database:");
            e.printStackTrace();
        }
    }

    public static void loadCustomersFromDatabase(PianoTechnician technician) {
        
        String sql = "SELECT naam, adres, email, telefoon FROM  klant";

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String name = resultSet.getString("naam");
                String address = resultSet.getString("adres");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("telefoon");

                Customer customer = new Customer(name, address, email, phone);

                for (Piano piano : technician.getPianos()) {
                    // We gaan ervan uit dat in de database en/of via eerdere koppeling in de piano
                    // het klantveld (klantnaam) al is ingevuld met de naam van de klant
                    if (piano.getKlantnaam() != null && piano.getKlantnaam().equals(name)) {
                        piano.setCustomer(customer);
                    }
                }
            }
            System.out.println("All customers loaded from the database and added to the technician.");

        } catch (SQLException e) {
            System.out.println("Error loading customers from the database:");
            e.printStackTrace();
        }
    }

    public static void loadIndividualPartListsFromDatabase(PianoTechnician technician) {
        String sql = "SELECT datum, pianoid, " +
                "kastwerk, zangbodem, frame, stemblok, stempennen, besnaring, toetsbeleg, invoering_klavier, hamerkoppen, dempers, toonhoogte_A440, stemming, afregeling_mechaniek, werking_pedalen, alternatief_onderdeel, " +
                "kastwerkno, zangbodemno, frameno, stemblokno, stempennenno, besnaringno, toetsbelegno, invoering_klavierno, hamerkoppenno, dempersno, toonhoogte_A440no, stemmingno, afregeling_mechaniekno, werking_pedalenno " +
                "FROM onderhoudslijst";
        
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
             
             while (rs.next()) {
                 String date = rs.getString("datum");
                 String pianoId = rs.getString("pianoid");
                 
                 // Maak de scores-array (15 elementen) op basis van de score-kolommen
                 int[] scores = new int[15];
                 scores[0] = rs.getInt("kastwerk");
                 scores[1] = rs.getInt("zangbodem");
                 scores[2] = rs.getInt("frame");
                 scores[3] = rs.getInt("stemblok");
                 scores[4] = rs.getInt("stempennen");
                 scores[5] = rs.getInt("besnaring");
                 scores[6] = rs.getInt("toetsbeleg");
                 scores[7] = rs.getInt("invoering_klavier");
                 scores[8] = rs.getInt("hamerkoppen");
                 scores[9] = rs.getInt("dempers");
                 scores[10] = rs.getInt("toonhoogte_A440");
                 scores[11] = rs.getInt("stemming");
                 scores[12] = rs.getInt("afregeling_mechaniek");
                 scores[13] = rs.getInt("werking_pedalen");
                 scores[14] = rs.getInt("alternatief_onderdeel");
                 
                 // Maak de notes-array (14 elementen) op basis van de note-kolommen
                 String[] notes = new String[14];
                 notes[0] = rs.getString("kastwerkno");
                 notes[1] = rs.getString("zangbodemno");
                 notes[2] = rs.getString("frameno");
                 notes[3] = rs.getString("stemblokno");
                 notes[4] = rs.getString("stempennenno");
                 notes[5] = rs.getString("besnaringno");
                 notes[6] = rs.getString("toetsbelegno");
                 notes[7] = rs.getString("invoering_klavierno");
                 notes[8] = rs.getString("hamerkoppenno");
                 notes[9] = rs.getString("dempersno");
                 notes[10] = rs.getString("toonhoogte_A440no");
                 notes[11] = rs.getString("stemmingno");
                 notes[12] = rs.getString("afregeling_mechaniekno");
                 notes[13] = rs.getString("werking_pedalenno");
                 
                 // Maak een nieuw IndividualPartList-object en vul de data in
                 IndividualPartList partList = new IndividualPartList(date);
                 partList.setpianoId(pianoId);
                 partList.setScores(scores);
                 partList.setNotes(notes);
                 partList.linkPartListToPiano(technician, partList);
                 
                 // Koppel de partlist aan het juiste piano-object, indien gewenst.
                 // Bijvoorbeeld, als je een methode hebt zoals:
                 // technician.addPartListToPiano(pianoId, partList);
                 // voeg dan hier de koppeling toe.
                 // Voor nu tonen we alleen dat de data geladen is:
                 System.out.println("Loaded individual part list for piano: " + pianoId);
             }
             System.out.println("All individual part lists loaded from the database.");
             
        } catch (SQLException e) {
             System.out.println("Error loading individual part lists from the database:");
             e.printStackTrace();
        }
    }

    public static void main(String[] args) {// Database configuratie
        
        // Probeer verbinding te maken
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL)) {
            System.out.println("Verbinding met de database is succesvol!");
        } catch (SQLException e) {
            System.out.println("Fout bij het verbinden met de database:");
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);
        PianoTechnician technician = new PianoTechnician("Technician Name");
        loadPianosFromDatabase(technician); // Laad pianos uit de database
        loadRepairsFromDatabase(technician); // Laad reparaties uit de database
        loadTuningsFromDatabase(technician); // Laad stembeurten uit de database
        loadCustomersFromDatabase(technician); // Laad klanten uit de database
        loadIndividualPartListsFromDatabase(technician); // Laad onderhoudslijsten uit de database



        while (true) {
            System.out.println("\n--- Piano Technician Program ---");
            System.out.println("1. Add Piano");
            System.out.println("2. Edit Piano");
            System.out.println("3. Remove Piano");
            System.out.println("4. View All Pianos");
            System.out.println("5. Add Customer");
            System.out.println("6. View Customer Info");
            System.out.println("7. Add Individual Part List to Piano");
//            System.out.println("7. View Latest Individual Part List for a Piano");
            System.out.println("8. View Latest Individual Part List for a Piano");
//            System.out.println("9. Assign Customer to Piano");
            System.out.println("11. Add Repair");
            System.out.println("12. Add Tuning");
            System.out.println("13. View All Repairs");
            System.out.println("14. View All Tunings");
            System.out.println("15. Exit Program");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1://add piano
                    System.out.print("Enter piano merk number: ");
                    String merk = scanner.nextLine();

                    System.out.print("Enter piano model: ");
                    String model = scanner.nextLine();

                    System.out.print("Enter serialNumber: ");
                    String serialNumber = scanner.nextLine();

                    System.out.print("Enter piano status: ");
                    String status = scanner.nextLine();

                    System.out.print("Enter piano year of construction (bouwjaar): ");
                    int bouwjaar = scanner.nextInt();
                    scanner.nextLine(); // Consume newline




                    Piano piano = new Piano(merk, model, serialNumber, status, bouwjaar);
                    technician.addPiano(piano);
                    piano.pianoSaveToDatabase();
                    break;
                case 2://edit piano
                    // Bijvoorbeeld in case 2 (Edit Piano)
                    technician.viewAllPianosOnlyNameAndId();
                    System.out.print("Enter piano serial number to edit: ");
                    String serialToEdit = scanner.nextLine();
                    Piano pianoToEdit = technician.getPianoBySerialNumber(serialToEdit);
                    if (pianoToEdit != null) {
                        System.out.print("Enter new merk: ");
                        String newMerk = scanner.nextLine();
                        pianoToEdit.setMerk(newMerk);
                        
                        System.out.print("Enter new model: ");
                        String newModel = scanner.nextLine();
                        pianoToEdit.setModel(newModel);
                        
                        System.out.print("Enter new serial number: ");
                        String newSerial = scanner.nextLine();
                        pianoToEdit.setSerialNumber(newSerial);
                        
                        System.out.print("Enter new status: ");
                        String newStatus = scanner.nextLine();
                        pianoToEdit.setStatus(newStatus);
                        
                        System.out.print("Enter new bouwjaar: ");
                        int newBouwjaar = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        pianoToEdit.setBouwjaar(newBouwjaar);
                        
                        // Optioneel: als er andere velden zijn, zoals theoreticePrijs en gebruikerId, kun je die hier ook vragen.
                        
                        System.out.println("Piano updated successfully.");
                    } else {
                        System.out.println("Piano not found.");
                    }
                case 3://remove piano
                    technician.viewAllPianosOnlyNameAndId();
                    System.out.print("Enter piano serial number to remove: ");
                    String serialToRemove = scanner.nextLine();
                    technician.removePiano(serialToRemove);
                    break;
                case 4://view all pianos
                    technician.viewAllPianos();
                    break;
                case 5:// Add Customer and assign to a piano (optioneel)
                    System.out.print("Enter customer name: ");
                    String customerName = scanner.nextLine();
                    
                    System.out.print("Enter customer address: ");
                    String customerAddress = scanner.nextLine();
                    
                    System.out.print("Enter customer email: ");
                    String customerEmail = scanner.nextLine();
                    
                    System.out.print("Enter customer phone: ");
                    String customerPhone = scanner.nextLine();
                    
                    // Maak een Customer-object. Zorg dat de Customer-klasse overeenkomt met de tabelkolommen
                    Customer customer = new Customer(customerName, customerAddress, customerEmail, customerPhone);
                    customer.addCustomerToDatabase(); // Voeg de klant toe aan de database
                    
                    
                    
                    System.out.print("Do you want to assign this customer to a piano? (yes/no): ");
                    String assign = scanner.nextLine();
                    if(assign.equalsIgnoreCase("yes")) {
                         // Laat de beschikbare piano's zien
                         technician.viewAllPianosOnlyNameAndId();
                         System.out.print("Enter piano serial number to assign customer: ");
                         String pianoSerial = scanner.nextLine();
                         technician.assignCustomerToPiano(pianoSerial, customer);
                         System.out.println("Customer assigned to piano with serial number: " + pianoSerial);
                    } else {
                         System.out.println("Customer added without piano assignment.");
                    }
                    break;
                case 6://view customer info
                    System.out.print("Enter customer name to view details: ");
                    String searchName = scanner.nextLine();
                    boolean found = false;
                    if (searchName.equalsIgnoreCase("@e")) {
                        for (Piano pianox : technician.getPianos()) {
                            Customer customery = pianox.getCustomer();
                            if (customery != null) {
                                System.out.println(customery); // toString() geeft alle gegevens weer
                                found = true;
                            }
                        }
                        if (!found) {
                            System.out.println("No customers available.");
                        }
                    } else {
                        for (Piano pianoz : technician.getPianos()) {
                            Customer customerz = pianoz.getCustomer();
                            if (customerz != null && customerz.getName().equalsIgnoreCase(searchName)) {
                                System.out.println(customerz); // toString() geeft alle gegevens weer
                                found = true;
                            }
                        }
                        if (!found) {
                            System.out.println("Customer not found.");
                        }
                    }
                    break;
                case 7://add individual-part-list to piano
                    String[] onderdelen = {
                        "kastwerk", 
                        "zangbodem", 
                        "frame", 
                        "stemblok", 
                        "stempennen", 
                        "besnaring", 
                        "toetsbeleg", 
                        "invoering_klavier", 
                        "hamerkoppen", 
                        "dempers", 
                        "toonhoogte_A440", 
                        "stemming", 
                        "afregeling_mechaniek", 
                        "werking_pedalen",
                        "alternatief_onderdeel"
                    };
                    int[] scores = new int[onderdelen.length];
                    String[] notes = new String[onderdelen.length];

                    for (int i = 0; i < onderdelen.length; i++) {
                        System.out.print("Enter score (1-10) for " + onderdelen[i] + ": ");
                        scores[i] = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        
                        System.out.print("Do you want to add a note for " + onderdelen[i] + " (yes/no): ");
                        String response = scanner.nextLine();
                        if(response.equalsIgnoreCase("yes")){
                            System.out.print("Enter note for " + onderdelen[i] + ": ");
                            notes[i] = scanner.nextLine();
                        } else {
                            notes[i] = "nun";
                        }
                    }
                    
                    IndividualPartList partList = new IndividualPartList(LocalDate.now().toString()); // Hier moet je de juiste pianoId instellen
                    partList.setScores(scores);
                    partList.setNotes(notes);
                    technician.viewAllPianosOnlyNameAndId();
                    System.out.print("Enter piano serial number to add part list: ");
                    String pianoid = scanner.nextLine();
                    for (Piano pianoToAdd : technician.getPianos()) {
                        if (pianoToAdd.getSerialNumber().equals(pianoid)) {
                            partList.setpianoId(pianoid);
                            partList.addPartlistToDatabase();
                            partList.linkPartListToPiano(technician, partList);
                            System.out.println("Individual Part List added to piano with serial number: " + pianoid);
                            break;
                        } else {
                            System.out.println("Piano not found.");
                        }
                    }
                    break;

                case 8://view latest individual part list for a piano
                    technician.viewAllPianosOnlyNameAndId();
                    System.out.print("Enter piano serial number to view latest part list: ");
                    String pianoSerialToView = scanner.nextLine();
                    String lijst = technician.getPianoBySerialNumber(pianoSerialToView).getlaatsteOnderhoudslijst().toString();
                    System.out.println(lijst);
                    // Dit kan een databasequery zijn of een methode in de PianoTechnician-klasse
                    break;
                
                case 9:
                    /*System.out.print("Enter piano serial number: ");
                    String pianoSerialToAssign = scanner.nextLine();
                    System.out.print("Enter customer name: ");
                    String customerNameToAssign = scanner.nextLine();
                    System.out.print("Enter customer contact: ");
                    String customerContactToAssign = scanner.nextLine();
                    Customer customerToAssign = new Customer(customerNameToAssign, customerContactToAssign);
                    technician.assignCustomerToPiano(pianoSerialToAssign, customerToAssign);
                    break;*/

               
                
                case 11://add repair
                    //maak hier code die laat zien wekle pianos er zijn met weke ids
                    System.out.println("Available Pianos:");
                    technician.viewAllPianosOnlyNameAndId();
                    System.out.print("Enter piano serial number for repair: ");
                    String repairPianoSerial = scanner.nextLine();
                    
                    System.out.print("Enter repair date (YYYY-MM-DD): ");
                    String repairDate = scanner.nextLine();
                    
                    System.out.print("Enter time spent (hours): ");
                    double repairTimeSpent = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline
                    
                    System.out.print("Enter repair status (e.g., 'in progress', 'completed'): ");
                    String repairStatus = scanner.nextLine();
                    
                    System.out.print("Enter repair description: ");
                    String repairDescription = scanner.nextLine();
                    
                    System.out.print("Enter repair cost: ");
                    double repairCost = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline
                
                    // Genereer unieke repair ID en bereken dbPianoId via de hashCode van de serial
                    int repairId = technician.generateRepairId();
                    String dbPianoId = repairPianoSerial;
                
                    // Maak een nieuw Repair-object
                    Repair repair = new Repair(repairId, repairDate, dbPianoId, repairTimeSpent, repairStatus, repairDescription, repairCost);
                    
                    

                    // Koppel de repair aan de bijbehorende piano in de lijst (aan de hand van dbPianoId)
                    for (Piano pianox : technician.getPianos()) {
                        if (pianox.getSerialNumber().equals(dbPianoId)) {
                            technician.addRepairToPiano(pianox.getSerialNumber(), repair);
                            break;
                        }
                    }
                    
                    // Voeg de repair tevens toe aan de database
                    repair.addRepairToDatabase();
                    break;

                    
                case 12: // add tuning (stembeurt)
                    
                    // Laat de beschikbare piano's zien
                    technician.viewAllPianosOnlyNameAndId();
                    System.out.println("Available Pianos:");
                    
                    
                    System.out.print("Enter piano serial number for tuning: ");
                    String tuningPianoSerial = scanner.nextLine();
                    
                    System.out.print("Enter tuning date (YYYY-MM-DD): ");
                    String tuningDate = scanner.nextLine();
                    
                    System.out.print("Enter time spent (hours): ");
                    double tuningTimeSpent = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline
                    
                    System.out.print("Enter tuning remark (opmerking): ");
                    String tuningRemark = scanner.nextLine();
                    
                    System.out.print("Enter stembaarheid (1-10): ");
                    int stembaarheid = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    
                    System.out.print("Enter soepelheid (1-10): ");
                    int soepelheid = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    
                    // Maak een nieuw Tuning-object met de opgegeven waarden
                    Tuning tuning = new Tuning(technician.generateTuningId(), tuningDate, tuningPianoSerial, 
                                                 tuningTimeSpent, stembaarheid, soepelheid, tuningRemark);
                    
                    // Koppel de tuning aan de bijbehorende piano in de lijst (aan de hand van het piano-serial)
                    technician.addTuningToPiano(tuningPianoSerial, tuning);
                    tuning.addStembeurtToDatabase(); // Voeg de tuning toe aan de database
                    break;
                
                case 13://view all repairs
                    technician.viewAllRepairs();
                    break;

                case 14://view all tunings
                    technician.viewAllTunings();
                    break; 
                case 15://exit program
                    System.out.println("Exiting program...");
                    scanner.close();
                    return;

                                    
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}

class PianoTechnician {
    //private String name;
    private List<Piano> pianos;
    private List<Repair> repairs = new ArrayList<>();
    private List<Tuning> tunings = new ArrayList<>();

    private int repairIdCounter = 1;
    private int tuningIdCounter = 1; // Teller voor unieke tuning IDs

    public List<Piano> getPianos() {
        return pianos;
    }
    public int generateTuningId() {
        return tuningIdCounter++;
    }

    public int generateRepairId() {
        return repairIdCounter++;
    }

    public void addRepairToPiano(String pianoSerial, Repair repair) {
        Piano piano = getPianoBySerialNumber(pianoSerial);
        if (piano != null) {
            repairs.add(repair);
            System.out.println("Repair added successfully to piano with serial number: " + pianoSerial);
        } else {
            System.out.println("Piano not found.");
        }
    }

    public void addTuningToPiano(String pianoSerial, Tuning tuning) {
        Piano piano = getPianoBySerialNumber(pianoSerial);
        if (piano != null) {
            tunings.add(tuning);
            System.out.println("Tuning added successfully to piano with serial number: " + pianoSerial);
        } else {
            System.out.println("Piano not found.");
        }
    }

    public void addTuning(Tuning tuning) {
        tunings.add(tuning);
        System.out.println("Tuning added successfully.");
    }

    public void viewAllRepairs() {
        if (repairs.isEmpty()) {
            System.out.println("No repairs available.");
        } else {
            for (Repair repair : repairs) {
                System.out.println(repair.getServiceDetails());
            }
        }
    }

    public void viewAllTunings() {
        if (tunings.isEmpty()) {
            System.out.println("No tunings available.");
        } else {
            for (Tuning tuning : tunings) {
                System.out.println(tuning.getServiceDetails());
            }
        }
    }

    public Piano getPianoBySerialNumber(String serialNumber) {
        for (Piano piano : pianos) {
            if (piano.getSerialNumber().equals(serialNumber)) {
                return piano;
            }
        }
        return null;
    }

    public void assignCustomerToPiano(String serialNumber, Customer customer) {
        Piano piano = getPianoBySerialNumber(serialNumber);
        if (piano != null) {
            piano.setCustomer(customer);
            piano.setCustomerIndatabase();// Voeg de klant toe aan de database
            System.out.println("Customer assigned to piano successfully.");
        } else {
            System.out.println("Piano not found.");
        }
    }

    public PianoTechnician(String name) {
        this.pianos = new ArrayList<>();
    }

    public void addPiano(Piano piano) {
        pianos.add(piano);
        System.out.println("Piano added successfully.");
    }

    public void removePiano(String serialNumber) {
        // 1. Verwijder uit de lijst
        pianos.removeIf(piano -> piano.getSerialNumber().equals(serialNumber));

        // 2. Verwijder uit de database
        
        String sql = "DELETE FROM piano WHERE serie_nummer = ?";

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, serialNumber);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Piano removed from database successfully.");
            } else {
                System.out.println("No piano found with serial number: " + serialNumber);
            }

        } catch (SQLException e) {
            System.out.println("Error removing Piano from database:");
            e.printStackTrace();
        }
    }

    public void viewAllPianos() {
        if (pianos.isEmpty()) {
            System.out.println("No pianos available.");
        } else {
            System.out.println("Pianos:");
            for (Piano piano : pianos) {
                if (piano.getKlantnaam() != null && !piano.getKlantnaam().isEmpty()) {
                    System.out.println("- " + piano + " (Customer: " + piano.getKlantnaam() + ")");
                } else {
                    System.out.println("- " + piano);
                }
            }
        }
    }

    public void viewAllPianosOnlyNameAndId() {
        if (pianos.isEmpty()) {
            System.out.println("No pianos available.");
        } else {
            System.out.println("Pianos:");
            for (Piano piano : pianos) {
                System.out.println("- " + piano.getSerialNumber() + " (" + piano.getModel() + ")");
            }
        }
    }

   
}

class Piano {
    private String merk;
    private String model;
    private String serialNumber;
    private String status;
    private int bouwjaar;
    private int gebruikerId;
    private double teoreticePrijs;
    private Customer customer = new Customer("", "", "", "");; 
    private IndividualPartList partList; // Hier kan je de partlist aan de piano koppelen

    // Constructor
    public Piano(String merk, String model, String serialNumber, String status, int bouwjaar) {
        this.merk = merk;
        this.model = model;
        this.serialNumber = serialNumber;
        this.status = status;
        this.bouwjaar = bouwjaar;



    }

    public void pianoSaveToDatabase() {
        
        String sql = "INSERT INTO piano (merk, model, serie_nummer, status, bouwjaar, gebruikerId) VALUES (?, ?, ?, ?, ?, ?)";


        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, merk);
            statement.setString(2, model);
            statement.setString(3, serialNumber);
            statement.setString(4, status);
            statement.setInt(5, bouwjaar);
            statement.setInt(6, 1);

            statement.executeUpdate();
            System.out.println("Piano saved to database successfully.");

        } catch (SQLException e) {
            System.out.println("Error saving Piano to database:");
            e.printStackTrace();
        }
    }
    
    public void setlaatsteOnderhoudslijst(IndividualPartList partList) {
        this.partList = partList;
    }

    public IndividualPartList getlaatsteOnderhoudslijst() {
        return partList;
    }

    public void setKlantnaam(String klantnaam) {
        customer.setName(klantnaam);
    }

    public String getKlantnaam() {
        return (customer != null) ? customer.getName() : "";
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        this.merk = merk;
    }

    // Getters and setters for all fields
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBouwjaar() {
        return bouwjaar;
    }

    public void setBouwjaar(int bouwjaar) {
        this.bouwjaar = bouwjaar;
    }

    public int getGebruikerId() {
        return gebruikerId;
    }

    public void setGebruikerId(int gebruikerId) {
        this.gebruikerId = gebruikerId;
    }

    public double getTeoreticePrijs() {
        return teoreticePrijs;
    }

    public void setTeoreticePrijs(double teoreticePrijs) {
        this.teoreticePrijs = teoreticePrijs;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        System.out.println("Customer added successfully.");
    }

    public void setCustomerIndatabase() {
        String sql = "UPDATE piano SET klantnaam = ? WHERE serie_nummer = ?";

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getName());
            statement.setString(2, serialNumber);

            statement.executeUpdate();
            System.out.println("Customer updated in database successfully.");

        } catch (SQLException e) {
            System.out.println("Error updating customer in database:");
            e.printStackTrace();
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    @Override
    public String toString() {
        return String.format("Model: %s, Serial Number: %s, Status: %s, Bouwjaar: %d, Gebruiker ID: %d, Theoretical Price: %.2f",
                model, serialNumber, status, bouwjaar, gebruikerId, teoreticePrijs);
    }
}

class Customer {
    private String name;
    private String adres;
    private String email;
    private String telefoon;

    // Constructor met alle velden
    public Customer(String name, String adres, String email, String telefoon) {
        this.name = name;
        this.adres = adres;
        this.email = email;
        this.telefoon = telefoon;
    }

    // Getters en setters (indien nodig)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdres() {
        return adres;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefoon() {
        return telefoon;
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Adres: %s, Email: %s, Telefoon: %s", 
                              name, adres, email, telefoon);
    }

    // Methode om de Customer in de database toe te voegen
    public void addCustomerToDatabase() {
        
        String sql = "INSERT INTO klant (naam, adres, email, telefoon) VALUES (?, ?, ?, ?)";
        
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, adres);
            statement.setString(3, email);
            statement.setString(4, telefoon);

            statement.executeUpdate();
            System.out.println("Customer added to database successfully.");

        } catch (SQLException e) {
            System.out.println("Error adding customer to database:");
            e.printStackTrace();
        }
    }
}

class IndividualPartList {
    private String date;
    private String pianoId = "";
    private int[] scores;
    private String[] notes;
   
    
    public IndividualPartList(String date) {
        this.date = date; 
    }

    public void setScores(int[] scores) {
        this.scores = scores;
    }

    public void setNotes(String[] notes) {
        this.notes = notes;
    }

    public void linkPartListToPiano(PianoTechnician technician, IndividualPartList partList) {
        Piano matchingPiano = technician.getPianoBySerialNumber(pianoId);
        if (matchingPiano != null) {
            matchingPiano.setlaatsteOnderhoudslijst(partList);
            System.out.println("Individual Part List linked to piano with serial number: " + pianoId);
        } else {
            System.out.println("No piano found with serial number: " + pianoId);
        }
    }

    public void setpianoId(String pianoId) {
        this.pianoId = pianoId;
    }

    public void addPartlistToDatabase() {
        String sql = "INSERT INTO onderhoudslijst (datum, pianoid, " +
             "kastwerk, zangbodem, frame, stemblok, stempennen, besnaring, toetsbeleg, invoering_klavier, hamerkoppen, dempers, toonhoogte_A440, stemming, afregeling_mechaniek, werking_pedalen, alternatief_onderdeel, " +
             "kastwerkno, zangbodemno, frameno, stemblokno, stempennenno, besnaringno, toetsbelegno, invoering_klavierno, hamerkoppenno, dempersno, toonhoogte_A440no, stemmingno, afregeling_mechaniekno, werking_pedalenno) " +
             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
            PreparedStatement statement = connection.prepareStatement(sql)) {

            // Stel datum en pianoid in (voorbeeldwaarden)
            statement.setString(1, LocalDate.now().toString());
            statement.setString(2, pianoId); // Pas aan naar de juiste piano identifier

            // Stel de scores (als String of een ander datatype) in
            statement.setString(3, String.valueOf(scores[0])); // kastwerk
            statement.setString(4, String.valueOf(scores[1])); // zangbodem
            statement.setString(5, String.valueOf(scores[2])); // frame
            statement.setString(6, String.valueOf(scores[3])); // stemblok
            statement.setString(7, String.valueOf(scores[4])); // stempennen
            statement.setString(8, String.valueOf(scores[5])); // besnaring
            statement.setString(9, String.valueOf(scores[6])); // toetsbeleg
            statement.setString(10, String.valueOf(scores[7])); // invoering_klavier
            statement.setString(11, String.valueOf(scores[8])); // hamerkoppen
            statement.setString(12, String.valueOf(scores[9])); // dempers
            statement.setString(13, String.valueOf(scores[10])); // toonhoogte_A440
            statement.setString(14, String.valueOf(scores[11])); // stemming
            statement.setString(15, String.valueOf(scores[12])); // afregeling_mechaniek
            statement.setString(16, String.valueOf(scores[13])); // werking_pedalen
            statement.setString(17, String.valueOf(scores[14])); // alternatief_onderdeel

            // Stel de note kolommen in
            statement.setString(18, notes[0]); // kastwerkno
            statement.setString(19, notes[1]); // zangbodemno
            statement.setString(20, notes[2]); // frameno
            statement.setString(21, notes[3]); // stemblokno
            statement.setString(22, notes[4]); // stempennenno
            statement.setString(23, notes[5]); // besnaringno
            statement.setString(24, notes[6]); // toetsbelegno
            statement.setString(25, notes[7]); // invoering_klavierno
            statement.setString(26, notes[8]); // hamerkoppenno
            statement.setString(27, notes[9]); // dempersno
            statement.setString(28, notes[10]); // toonhoogte_A440no
            statement.setString(29, notes[11]); // stemmingno
            statement.setString(30, notes[12]); // afregeling_mechaniekno
            statement.setString(31, notes[13]); // werking_pedalenno

            statement.executeUpdate();
            System.out.println("Onderhoudslijst successfully added to the database.");
            
        } catch (SQLException e) {
            System.out.println("Error adding onderhoudslijst to the database:");
            e.printStackTrace();
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IndividualPartList for Piano ID: ").append(pianoId).append("\n");
        sb.append("Date: ").append(date).append("\n");
        sb.append("Kastwerk: ").append(scores[0]).append(" (Notes: ").append(notes[0]).append(")\n");
        sb.append("Zangbodem: ").append(scores[1]).append(" (Notes: ").append(notes[1]).append(")\n");
        sb.append("Frame: ").append(scores[2]).append(" (Notes: ").append(notes[2]).append(")\n");
        sb.append("Stemblok: ").append(scores[3]).append(" (Notes: ").append(notes[3]).append(")\n");
        sb.append("Stempennen: ").append(scores[4]).append(" (Notes: ").append(notes[4]).append(")\n");
        sb.append("Besnaring: ").append(scores[5]).append(" (Notes: ").append(notes[5]).append(")\n");
        sb.append("Toetsbeleg: ").append(scores[6]).append(" (Notes: ").append(notes[6]).append(")\n");
        sb.append("Invoering Klavier: ").append(scores[7]).append(" (Notes: ").append(notes[7]).append(")\n");
        sb.append("Hamerkoppen: ").append(scores[8]).append(" (Notes: ").append(notes[8]).append(")\n");
        sb.append("Dempers: ").append(scores[9]).append(" (Notes: ").append(notes[9]).append(")\n");
        sb.append("Toonhoogte A440: ").append(scores[10]).append(" (Notes: ").append(notes[10]).append(")\n");
        sb.append("Stemming: ").append(scores[11]).append(" (Notes: ").append(notes[11]).append(")\n");
        sb.append("Afregeling Mechaniek: ").append(scores[12]).append(" (Notes: ").append(notes[12]).append(")\n");
        sb.append("Werking Pedalen: ").append(scores[13]).append(" (Notes: ").append(notes[13]).append(")\n");
        // Indien er een alternatief onderdeel is opgeslagen, bijvoorbeeld op index 14:
        if(scores.length > 14 && notes.length > 14) {
            sb.append("Alternatief Onderdeel: ").append(scores[14])
            .append(" (Notes: ").append(notes[14]).append(")\n");
        }
        return sb.toString();
    }

}

abstract class PianoService {
    protected int id;
    protected String date = LocalDate.now().toString();
    protected String pianoId;
    protected double timeSpent; // tijd in uren

    public PianoService(int id, String date, String pianoId, double timeSpent) {
        this.id = id;
        this.date = LocalDate.now().toString();
        this.pianoId = pianoId;
        this.timeSpent = timeSpent;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getPianoId() {
        return pianoId;
    }

    public double getTimeSpent() {
        return timeSpent;
    }

    // Abstracte methode voor aanvullende details, te implementeren in de subklassen
    public abstract String getServiceDetails();

    @Override
    public String toString() {
        return String.format("ID: %d, Date: %s, Piano ID: %d, Time Spent: %.2f hrs, Details: %s",
                id, date, pianoId, timeSpent, getServiceDetails());
    }
}

class Repair extends PianoService {
    private String status; // Bijvoorbeeld: "in progress", "completed"
    private String repairedParts; // Wat er gerepareerd is
    private double cost; // Onderdeel(en) kosten

    public Repair(int repairId, String date, String pianoId, double timeSpent, String status, String repairedParts, double cost) {
        super(repairId, date, pianoId, timeSpent);
        this.status = status;
        this.repairedParts = repairedParts;
        this.cost = cost;
    }

    public void addRepairToDatabase() {
        
        String sql = "INSERT INTO reparatie (datum, pianoid, tijd_gekost, status, beschrijving, onderdelen_kosten) VALUES (?, ?, ?, ?, ?, ?)";
    
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
         PreparedStatement statement = connection.prepareStatement(sql)) {
         
         // Haal de waarden uit de superclass via getters
         statement.setString(1, getDate());
         statement.setString(2, getPianoId());
         statement.setDouble(3, getTimeSpent());
         
         // status, repairedParts (voor beschrijving) en cost zijn specifiek voor Repair
         statement.setString(4, status);
         statement.setString(5, repairedParts);
         statement.setDouble(6, cost);
         
         statement.executeUpdate();
         System.out.println("Repair added to database successfully.");
         
    } catch (SQLException e) {
         System.out.println("Error adding Repair to database:");
         e.printStackTrace();
    }
    }
    

    public String getStatus() {
        return status;
    }
    
    public String getRepairedParts() {
        return repairedParts;
    }
    
    public double getCost() {
        return cost;
    }
    
    @Override
    public String getServiceDetails() {
        return String.format("Piano ID: %s, Status: %s, Repaired Parts: %s, Cost: €%.2f",getPianoId(),  status, repairedParts, cost);
    }
}

class Tuning extends PianoService {
    private int tuningEase;      // Hoe soepel het ging (1-10)
    private int tuningQuality;   // Stembaarheid (1-10)
    private String remarks;      // Opmerkingen

    public Tuning(int tuningId, String date, String pianoId, double timeSpent, int tuningEase, int tuningQuality, String remarks) {
        super(tuningId, date, pianoId, timeSpent);
        this.tuningEase = tuningEase;
        this.tuningQuality = tuningQuality;
        this.remarks = remarks;
    }

    public void addStembeurtToDatabase() {
        
        String sql = "INSERT INTO stembeurt (datum, pianoid, tijd_gekost, opmerking, stembaarheid, soepelheid) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL);
     PreparedStatement statement = connection.prepareStatement(sql)) {

    // Gebruik de getters om de waarden op te halen:
    statement.setString(1, getDate());
    statement.setString(2, getPianoId());
    statement.setDouble(3, getTimeSpent());
    statement.setString(4, remarks);       // Dit levert de opmerking (tuning remark) op
    statement.setInt(5, tuningEase);         // Dit levert de stembaarheid op
    statement.setInt(6, tuningQuality);      // Dit levert de soepelheid op

    statement.executeUpdate();
    System.out.println("Stembeurt added to database successfully.");

} catch (SQLException e) {
    System.out.println("Error adding Stembeurt to database:");
    e.printStackTrace();
}
    }

    public int getTuningEase() {
        return tuningEase;
    }
    
    public int getTuningQuality() {
        return tuningQuality;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    @Override
    public String getServiceDetails() {
        return String.format("Piano ID: %s, Tuning Ease: %d, Tuning Quality: %d, Remarks: %s",getPianoId(),  tuningEase, tuningQuality, remarks);
    }
}

