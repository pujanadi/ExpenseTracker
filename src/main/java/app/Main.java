package app;

//import app.ExpenseTracker.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {

        ExpenseTracker tracker = new ExpenseTracker();

        tracker.addTransaction(
                LocalDate.now().minusDays(1),
                "Food",
                new BigDecimal("23000.00"),
                "Rokok di warung madura");
        tracker.addTransaction(
                LocalDate.now(),
                "Home",
                new BigDecimal("45000.00"),
                "Mur washtafel skyhouse");
        tracker.addTransaction(
                LocalDate.now(),
                "Tech",
                new BigDecimal("25000.00"),
                "IP publik myrep");


        tracker.printHeader();
        tracker.printRow();

        // Day 4
        UUID id = tracker.getTransactions().get(2).id();
        System.out.println("Find transaction id: " + id);
        Optional<ExpenseTracker.Transaction> t = tracker.findTransactionById(id);

        if (t.isPresent())
            System.out.println("id '" + t.get().id() +"' found");

        // TODO Day 5: add updateTransactionNote(UUID id, String newNote) returning boolean
        System.out.println("Sum Amount:" + tracker.summaryTotalAmount());

        // TODO Day 6
        System.out.println("summaryTotalByCategory:" + tracker.summaryTotalByCategory());

        // Mock live-interview

        // TODO Day 8: summaryTotalByCategoryUsingStreams()

        // TODO Day 10: listTransactionsPaged(int page, int size) with validation + tests

        // TODO Day 12: listTransactionsBetweenDates(LocalDate from, LocalDate to) + tests


    }

}