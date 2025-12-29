import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Train {
    private static int trainCount = 1;

    int trainID;
    String trainName;

    String source;
    String destination;


    Map<String, Integer> availableSeats;


    Map<String, Double> coachPrice;


    Map<Integer, String> bookings;

    int nextSeatNumber;
    private static int pnrGenerator = 1000;

    public Train(String name, String src, String dest) {
        this.trainID = trainCount++;
        this.trainName = name;
        this.source = src;
        this.destination = dest;
        this.nextSeatNumber = 1;

        availableSeats = new HashMap<>();
        coachPrice = new HashMap<>();
        bookings = new HashMap<>();


        availableSeats.put("Sleeper", 50);
        availableSeats.put("AC", 30);
        availableSeats.put("General", 100);

        coachPrice.put("Sleeper", 500.0);
        coachPrice.put("AC", 1200.0);
        coachPrice.put("General", 200.0);
    }

    public int bookTickets(String coach, List<String[]> passengerInfos) {
        if (!availableSeats.containsKey(coach)) {
            System.out.println("Invalid coach selection.");
            return -1;
        }

        int seatsLeft = availableSeats.get(coach);
        if (passengerInfos.size() > seatsLeft) {
            System.out.println("Not enough seats in " + coach + " coach.");
            return -1;
        }

        int pnr = pnrGenerator++;
        StringBuilder details = new StringBuilder();

        for (String[] info : passengerInfos) {
            String name = info[0];
            int age = Integer.parseInt(info[1]);
            String gender = info[2];

            details.append("PNR: ").append(pnr)
                    .append(", Name: ").append(name)
                    .append(", Age: ").append(age)
                    .append(", Gender: ").append(gender)
                    .append(", Coach: ").append(coach)
                    .append(", Seat: ").append(nextSeatNumber++)
                    .append(", Fare: ₹").append(coachPrice.get(coach))
                    .append("\n");
        }

        availableSeats.put(coach, seatsLeft - passengerInfos.size());
        bookings.put(pnr, details.toString());

        writeToFile("BOOKED", pnr, details.toString());
        return pnr;
    }

    public void cancelTicket(int pnr) {
        if (!bookings.containsKey(pnr)) {
            System.out.println("PNR " + pnr + " not found.");
            return;
        }

        String details = bookings.get(pnr);
        int cancelledCount = (int) details.lines().count();


        if (details.contains("Sleeper")) {
            availableSeats.put("Sleeper", availableSeats.get("Sleeper") + cancelledCount);
        } else if (details.contains("AC")) {
            availableSeats.put("AC", availableSeats.get("AC") + cancelledCount);
        } else if (details.contains("General")) {
            availableSeats.put("General", availableSeats.get("General") + cancelledCount);
        }

        bookings.remove(pnr);
        System.out.println("PNR " + pnr + " cancelled successfully.");
        writeToFile("CANCELLED", pnr, details);
    }

    public void trainSummary() {
        System.out.println("\nTrain ID: " + trainID + " | " + trainName + " (" + source + " → " + destination + ")");
        for (String coach : availableSeats.keySet()) {
            System.out.println(coach + " - Available Seats: " + availableSeats.get(coach) + " | Fare: ₹" + coachPrice.get(coach));
        }
    }

    public void printBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings for " + trainName);
            return;
        }
        System.out.println("\n--- Passenger Details for Train " + trainID + " ---");
        for (String details : bookings.values()) {
            System.out.print(details);
        }
    }

    private void writeToFile(String action, int pnr, String content) {
        try (FileWriter writer = new FileWriter("railway_bookings.txt", true)) {
            writer.write("[" + action + "] Train ID: " + trainID + ", PNR: " + pnr + "\n");
            writer.write(content);
            writer.write("********************************************************");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}

public class RailwayReservationSys {
    private static final Scanner sc = new Scanner(System.in);
    private static final ArrayList<Train> trains = new ArrayList<>();

    public static void main(String[] args) {
        initializeTrains();

        while (true) {
            System.out.println("\n--- Railway Reservation System ---");
            System.out.println("1. Book Ticket");
            System.out.println("2. Cancel Ticket");
            System.out.println("3. Train Summary");
            System.out.println("4. Print Passenger Details");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int ch = readInt();

            switch (ch) {
                case 1 -> handleBooking();
                case 2 -> handleCancellation();
                case 3 -> printAllTrains();
                case 4 -> printAllBookings();
                case 5 -> {
                    System.out.println("Exiting... Thank you for using Indian Railways!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void initializeTrains() {
        trains.add(new Train("Krishna Exp", "Venkatagiri", "Nellore"));
        trains.add(new Train("Narsapur Exp", "Venkatagiri", "Rajamundry"));
    }

    private static void handleBooking() {
        System.out.print("Enter Train ID: ");
        int tid = readInt();

        Train train = getTrainById(tid);
        if (train == null) return;

        System.out.print("Enter Coach (Sleeper/AC/General): ");
        sc.nextLine();
        String coach = sc.nextLine();

        System.out.print("Enter number of tickets: ");
        int ticketCount = readInt();

        List<String[]> passengerInfos = new ArrayList<>();
        for (int i = 1; i <= ticketCount; i++) {
            System.out.println("\nEnter details for Passenger " + i + ":");
            System.out.print("Name: ");
            sc.nextLine();
            String name = sc.nextLine();
            System.out.print("Age: ");
            int age = readInt();
            System.out.print("Gender: ");
            String gender = sc.next();

            passengerInfos.add(new String[]{name, String.valueOf(age), gender});
        }

        int pnr = train.bookTickets(coach, passengerInfos);
        if (pnr != -1) {
            System.out.println("Booking Successful! PNR: " + pnr);
            train.trainSummary();
        }
    }

    private static void handleCancellation() {
        System.out.print("Enter Train ID: ");
        int tid = readInt();

        Train train = getTrainById(tid);
        if (train == null) return;

        System.out.print("Enter PNR to cancel: ");
        int pnr = readInt();

        train.cancelTicket(pnr);
        train.trainSummary();
    }

    private static void printAllTrains() {
        for (Train t : trains) {
            t.trainSummary();
        }
    }

    private static void printAllBookings() {
        for (Train t : trains) {
            t.printBookings();
        }
    }

    private static Train getTrainById(int tid) {
        for (Train t : trains) {
            if (t.trainID == tid) return t;
        }
        System.out.println("Invalid Train ID.");
        return null;
    }

    private static int readInt() {
        while (!sc.hasNextInt()) {
            System.out.print("Enter a valid number: ");
            sc.next();
        }
        return sc.nextInt();
    }
}
